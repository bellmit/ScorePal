package uk.co.darkerwaters.scorepal.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import uk.co.darkerwaters.scorepal.activities.MainActivity;
import uk.co.darkerwaters.scorepal.storage.ScoreData;

/**
 * Created by douglasbrain on 26/05/2017.
 */

public class BtConnectionThread extends Thread {
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;

    private volatile boolean isRunning = true;

    private StringBuilder recDataString = new StringBuilder();

    //creation of the connect thread
    public BtConnectionThread(BluetoothSocket socket) {
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            //Create I/O streams for connection
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public boolean isConnected() {
        boolean result = false;
        try {
            // the only way we can be sure is to send a little something, send a fake command
            result = write("{x0}");
        }
        catch (Exception e) {
            Log.e(MainActivity.TAG, e.getMessage());
        }
        return result;
    }

    public interface IBtDataListener {
        void onBtDataReceived(ScoreData scoreData);
    }
    private ArrayList<IBtDataListener> listeners = new ArrayList<IBtDataListener>();

    public synchronized boolean registerListener(IBtDataListener listener) {
        return this.listeners.add(listener);
    }

    public synchronized boolean unregisterListener(IBtDataListener listener) {
        return this.listeners.remove(listener);
    }

    public synchronized void onDataReceived(ScoreData data) {
        for (IBtDataListener listener : this.listeners) {
            listener.onBtDataReceived(data);
        }
    }

    public void cancel() {
        // set the volatile flag to end this
        isRunning = false;
    }

    public void run() {
        byte[] buffer = new byte[256];
        int bytes;

        // as we run send a little poke to the device to get the latest score...
        write("{r-1}");

        // Keep looping to listen for received messages
        while (isRunning) {
            try {
                bytes = mmInStream.read(buffer);            //read bytes from input buffer
                String readMessage = new String(buffer, 0, bytes);
                // handle this received data
                handleReceivedData(readMessage);
            } catch (IOException e) {
                break;
            }
        }
    }

    public boolean write(String input) {
        boolean result = false;
        byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
        try {
            mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            result = true;
        } catch (IOException e) {
            //if you cannot write, close the application
            Log.e(MainActivity.TAG, e.getMessage());
        }
        return result;
    }

    public void handleReceivedData(String readMessage) {
        // append the new string to the running total of data
        recDataString.append(readMessage);
        int startOfLineIndex = recDataString.lastIndexOf("{");
        int endOfLineIndex = recDataString.indexOf("}", startOfLineIndex);
        if (endOfLineIndex > 0 && startOfLineIndex > -1 && startOfLineIndex < endOfLineIndex) {
            // we have our data, extract it now
            try {
                // get the data we want
                recDataString.delete(endOfLineIndex, recDataString.length() + 1);
                recDataString.delete(0, startOfLineIndex + 1);
                ScoreData data = new ScoreData(recDataString);
                if (data.dataCommand.equals("u")) {
                    // inform listeners we have data received
                    onDataReceived(data);
                    // this was all good and working, respond
                    write("{r" + data.dataCode + "}");
                }
            }
            catch (Exception e) {
                Log.e(MainActivity.TAG, e.getMessage());
            }
            // clear the string
            recDataString.delete(0, recDataString.length());
        }
    }
}
