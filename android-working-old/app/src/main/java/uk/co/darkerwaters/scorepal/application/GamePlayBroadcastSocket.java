package uk.co.darkerwaters.scorepal.application;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class GamePlayBroadcastSocket extends Thread{

    private volatile boolean isProcessSocket;
    private final BluetoothSocket btSocket;
    private final InputStream inStream;
    private final OutputStream outStream;
    private final BroadcastSocketListener listener;

    private final StringBuilder readData = new StringBuilder();

    interface BroadcastSocketListener {
        void onDataRead(String dataString);
        void onDataWriteError(String message);
        void onSocketDisconnected(GamePlayBroadcastSocket disconnectedSocket, BluetoothSocket disconnectedBtSocket);
    }

    GamePlayBroadcastSocket(BluetoothSocket socket, BroadcastSocketListener listener) {
        this.btSocket = socket;
        this.listener = listener;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams; using temp objects because
        // member streams are final.
        try {
            tmpIn = socket.getInputStream();
        } catch (IOException e) {
            Log.error("Error occurred when creating input stream", e);
        }
        try {
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.error("Error occurred when creating output stream", e);
        }

        this.inStream = tmpIn;
        this.outStream = tmpOut;

        // we are always started
        this.isProcessSocket = true;
        this.start();
    }

    boolean isConnected() {
        return null != this.btSocket && this.btSocket.isConnected();
    }

    BluetoothDevice getConnectedDevice() {
        if (!isConnected()) {
            return null;
        }
        else {
            return this.btSocket.getRemoteDevice();
        }
    }

    void close() {
        // cancel our running thread
        this.isProcessSocket = false;
        try {
            btSocket.close();
        } catch (IOException e) {
            Log.error("Could not close the connect socket", e);
        }
    }

    void write(String dataString) {
        // the dataString cannot contain our special end char (\n) so be sure to remove
        // them all right at this last second - then add one at the end to signal the end
        // of the data that was sent
        dataString = dataString.replaceAll("\\n", "") + "\n";
        // and write these bytes to the socket listening the other end
        write(dataString.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void run() {
        byte[] dataBuffer = new byte[1024];
        int numBytes; // bytes returned from read()
        // Keep listening to the InputStream until an exception occurs.
        while (isProcessSocket && null != inStream) {
            try {
                // Read from the InputStream until there is no more
                while (-1 != (numBytes = inStream.read(dataBuffer))) {
                    // while there is data, read and process it
                    processDataRead(new String(dataBuffer, 0, numBytes, StandardCharsets.UTF_8));
                }
                // and inform the listener of any residual remaining data
                processDataRead("");
            } catch (IOException e) {
                this.listener.onSocketDisconnected(this, this.btSocket);
                Log.debug("Input stream was disconnected", e);
                break;
            } catch (Throwable e) {
                this.listener.onSocketDisconnected(this, this.btSocket);
                Log.error("Input stream read failed " + e.getMessage());
                break;
            }
        }
    }

    private void processDataRead(String dataString) {
        // append the data string to our stack
        int iDataEnd = dataString.indexOf('\n');
        if (iDataEnd != -1) {
            // this data string has the end of some data, append it to the remaining in our writer
            this.readData.append(dataString.substring(0, iDataEnd));
            // and send this data
            String dataStringSending = this.readData.toString();
            try {
                this.listener.onDataRead(dataStringSending);
            }
            catch (Throwable e) {
                Log.error("failed to process received data [" + dataStringSending + "]" + e.getMessage());
            }
            this.readData.setLength(0);
            if (dataString.length() > iDataEnd) {
                // there is some remaining, put this in the read data we are collecting
                processDataRead(dataString.substring(iDataEnd + 1));
            }
        }
        else {
            // just store it for when we do get a full message
            this.readData.append(dataString);
        }
    }

    // Call this from the main activity to send data to the remote device.
    private void write(byte[] bytes) {
        try {
            // just send this
            if (null != outStream) {
                outStream.write(bytes);
            }
            else {
                Log.error("trying to write to a null outStream");
            }
        } catch (IOException e) {
            Log.error("Error occurred when sending data", e);
            this.listener.onDataWriteError(e.getMessage());
        } catch (Throwable e) {
            Log.error("Output stream write failed: " + e.getMessage());
            this.listener.onDataWriteError(e.getMessage());
        }
    }
}

