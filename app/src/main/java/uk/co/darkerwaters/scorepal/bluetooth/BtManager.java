package uk.co.darkerwaters.scorepal.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.ScoreData;
import uk.co.darkerwaters.scorepal.history.HistoryManager;

/**
 * Created by douglasbrain on 26/05/2017.
 */

public class BtManager implements BtConnectionThread.IBtDataListener {
    // make this a singleton
    private static BtManager INSTANCE = null;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothSocket btSocket = null;
    private BluetoothAdapter adapter = null;
    private FragmentActivity container = null;

    private String connectedDeviceAddress = null;
    private String connectedDeviceName = null;

    private ArrayList<BroadcastReceiver> registeredReceivers = new ArrayList<BroadcastReceiver>();

    private BtConnectionThread mConnectedThread = null;

    private ScoreData latestScoreData = null;

    public interface IBtManagerListener {
        void onBtDeviceFound(BluetoothDevice device);
        void onBtStatusChanged();
        void onBtConnectionStatusChanged();
        void onBtDataChanged(ScoreData scoreData);
    }
    private ArrayList<IBtManagerListener> listeners = new ArrayList<IBtManagerListener>();

    public static BtManager getManager() {
        return INSTANCE;
    }

    private BtManager() {
    }

    public static void initialise(FragmentActivity containerActivity) {
        // initialise this class
        if (null == INSTANCE) {
            // create the instance (not bothering with thread safety as safely done in create of
            // the container activity
            INSTANCE = new BtManager();
        }
        // enable the intent to let us use bluetooth
        Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        containerActivity.startActivityForResult(turnOn, 0);
        // remember the activity - useful
        INSTANCE.container = containerActivity;
        // get the adapter
        INSTANCE.adapter = BluetoothAdapter.getDefaultAdapter();
        INSTANCE.registerGlobalListeners();
    }

    public void registerGlobalListeners() {
        // register the broadcast receiver for messages when we discover some device
        registerActionReceiver(new BroadcastReceiver() {
               @Override
               public void onReceive(Context context, Intent intent) {
                   // to call the function
                   BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                   INSTANCE.btDeviceFound(device);
               }
           }, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        // and for state changes on the adapter
        registerActionReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // to call the function
                INSTANCE.btStateChanged();
            }
        }, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        // and for any change in the connnection status
        registerActionReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // to call the function
                INSTANCE.btConnectionStateChanged();
            }
        }, new IntentFilter(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED));
    }

    private void registerActionReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        // create the receiver that we will later unregister
        this.container.registerReceiver(receiver, filter);
        this.registeredReceivers.add(receiver);
    }

    public void unregisterGlobalListeners() {
        for (BroadcastReceiver receiver : this.registeredReceivers) {
            this.container.unregisterReceiver(receiver);
        }
        this.registeredReceivers.clear();
    }

    public synchronized boolean registerListener(IBtManagerListener listener) {
        boolean result = false;
        if (false == this.listeners.contains(listener)) {
            result = this.listeners.add(listener);
        }
        return result;
    }

    public synchronized boolean unregisterListener(IBtManagerListener listener) {
        return this.listeners.remove(listener);
    }

    public Set<BluetoothDevice> getBondedDevices() {
        // return the list of devices we are paired with
        if (null == adapter) {
            return new HashSet<BluetoothDevice>(0);
        }
        else {
            return adapter.getBondedDevices();
        }
    }

    public void connectToLastDevice() {
        SharedPreferences sharedPref = this.container.getPreferences(Context.MODE_PRIVATE);
        String defaultValue = this.container.getResources().getString(R.string.stored_device);
        String wantedDeviceAddress = sharedPref.getString(this.container.getString(R.string.stored_device), defaultValue);
        if (wantedDeviceAddress.equals(defaultValue)) {
            // there is none saved, don't bother
        }
        else {
            // we want to connect to this
            if (null != adapter && false == adapter.isEnabled()) {
                // not enabled, turn on
                adapter.enable();
            }
            // connect to the device we were connected to last
            connectToDevice(wantedDeviceAddress);
        }
    }

    public boolean connectToDevice(BluetoothDevice device) {
        boolean result = false;
        if (false == isSocketConnected() || false == device.getAddress().equals(connectedDeviceAddress)) {
            // disconnect the old one as the new one is different
            if (null != btSocket && btSocket.isConnected()) {
                try {
                    btSocket.close();
                } catch (IOException e1) {
                    //Log.d(TAG,"Socket not closed");
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
            // now disconnected, connect to a new one
            try {
                btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e1) {
                //Log.d(this.container.TAG,"socket not created");
            }
            try{
                btSocket.connect();
            }
            catch(IOException e){
                try {
                    btSocket.close();
                    e.printStackTrace();
                    //Log.d(TAG,"Cannot connect");
                } catch (IOException e1) {
                    //Log.d(TAG,"Socket not closed");
                }
            }
            if (btSocket.isConnected()) {
                // this worked ok
                result = true;
                storeConnectedDevice(device);
            }
            // create the connection thread to manage communications to this device
            createConnectionThread();

        }
        else {
            // there is no change in the device, just leave it alone
            result = true;
        }
        return result;
    }

    private boolean isSocketConnected() {
        // check everything is up and working well
        return null != btSocket &&
                btSocket.isConnected() &&
                null != mConnectedThread &&
                mConnectedThread.isConnected();
    }

    private void createConnectionThread() {
        if (null != mConnectedThread) {
            mConnectedThread.cancel();
        }
        // start the new handler thread for this
        mConnectedThread = new BtConnectionThread(btSocket);
        mConnectedThread.registerListener(this);
        mConnectedThread.start();
    }

    private void storeConnectedDevice(BluetoothDevice device) {
        if (null != device) {
            connectedDeviceAddress = device.getAddress();
            connectedDeviceName = device.getName();
            // store that we connected to this so we do again next time we start up
            SharedPreferences sharedPref = this.container.getPreferences(Context.MODE_PRIVATE);
            if (null != sharedPref) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(this.container.getString(R.string.stored_device), connectedDeviceAddress);
                editor.apply();
            }
        }
    }

    public boolean connectToDevice(String wantedDeviceAddress) {
        // connect to this device
        boolean result = false;
        Set<BluetoothDevice> devices = getBondedDevices();
        for (BluetoothDevice device : devices) {
            if (null != device && device.getAddress().equals(wantedDeviceAddress)) {
                // this is the device, the address matches the one we want
                if (connectToDevice(device)) {
                    // connected, stop trying
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    public boolean sendMessage(String message) {
        boolean result = false;
        if (null != mConnectedThread) {
            result = mConnectedThread.write(message);
        }
        return result;
    }

    public boolean isEnabled() {
        // set the status of the adapter
        return null != adapter && adapter.isEnabled();
    }

    public void enable(boolean isChecked) {
        if (null != adapter) {
            if (adapter.isEnabled() && false == isChecked) {
                // BT is on but the user just turned it off
                adapter.disable();
            } else if (false == adapter.isEnabled() && isChecked) {
                // BT is off but the user just turned it on
                adapter.enable();
            }
        }
    }

    public boolean isDeviceConnected(BluetoothDevice device) {
        // are we connected to this specified device
        boolean result = false;
        if (isSocketConnected() && null != device) {
            // we are connected, but to what?
            result = device.getAddress().equals(connectedDeviceAddress);
        }
        return result;
    }

    public void scanForDevices() {
        if (null != adapter) {
            adapter.startDiscovery();
        }
    }

    public void cancelScanning() {
        if (null != adapter) {
            adapter.cancelDiscovery();
        }
    }

    public String getConnectedDevice() {
        String connectedDevice = null;
        if (isSocketConnected()) {
            connectedDevice = this.connectedDeviceName;
        }
        return connectedDevice;
    }

    private synchronized void btDeviceFound(BluetoothDevice device) {
        for (IBtManagerListener listener : this.listeners) {
            listener.onBtDeviceFound(device);
        }
    }

    private synchronized void btConnectionStateChanged() {
        for (IBtManagerListener listener : this.listeners) {
            listener.onBtConnectionStatusChanged();
        }
    }

    private synchronized void btStateChanged() {
        for (IBtManagerListener listener : this.listeners) {
            listener.onBtStatusChanged();
        }
    }

    public ScoreData getLatestScoreData() {
        return this.latestScoreData;
    }

    @Override
    public synchronized void onBtDataReceived(ScoreData scoreData) {
        // received some data, process this to show the current score now
        if (null != this.latestScoreData && null != scoreData) {
            // this is replacing some existing score data, this happens all the time
            // but quickly we want to check to see if this is a new match (user reset on the device)
            if (this.latestScoreData.secondsStartTime != scoreData.secondsStartTime) {
                // this is a different game, has a different start time, reset this
                HistoryManager.getManager().resetMatchStartedDate(-scoreData.secondsGameDuration);
            }
        }
        this.latestScoreData = scoreData;
        for (IBtManagerListener listener : this.listeners) {
            listener.onBtDataChanged(scoreData);
        }
    }
}
