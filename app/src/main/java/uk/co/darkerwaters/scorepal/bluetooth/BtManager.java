package uk.co.darkerwaters.scorepal.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.MainActivity;
import uk.co.darkerwaters.scorepal.storage.ScoreData;
import uk.co.darkerwaters.scorepal.storage.StorageManager;

/**
 * Created by douglasbrain on 26/05/2017.
 */

public class BtManager implements BtConnectionThread.IBtDataListener {
    // make this a singleton
    private static BtManager INSTANCE = null;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_LOCATION = 9003;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_BLUETOOTH = 9004;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_BLUETOOTH_ADMIN = 9005;

    private BluetoothSocket btSocket = null;
    private BluetoothAdapter adapter = null;
    private FragmentActivity container = null;

    private String connectedDeviceAddress = null;
    private String connectedDeviceName = null;

    private ArrayList<BroadcastReceiver> registeredReceivers = new ArrayList<BroadcastReceiver>();

    private BtConnectionThread mConnectedThread = null;

    private ScoreData latestScoreData = null;

    private Thread connectingThread = null;

    public interface IBtManagerListener {
        void onBtStatusChanged();
        void onBtConnectionStatusChanged();
        void onBtDataChanged(ScoreData scoreData);
    }

    public interface  IBtManagerScanningListener {
        void onBtScanStarted();
        void onBtScanEnded();
        void onBtDeviceFound(BluetoothDevice device);
    }
    private ArrayList<IBtManagerListener> listeners = new ArrayList<IBtManagerListener>();
    private IBtManagerScanningListener scanningListener = null;

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

        // request the BT permissions required
        if (ContextCompat.checkSelfPermission(containerActivity,
                Manifest.permission.BLUETOOTH)
                != PackageManager.PERMISSION_GRANTED) {

            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(containerActivity,
                    new String[]{Manifest.permission.BLUETOOTH},
                    MY_PERMISSIONS_REQUEST_ACCESS_BLUETOOTH);
            //TODO Handle when the app has no bluetooth permissions (intercept callback ID)

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        }

        // request the BT permissions required
        if (ContextCompat.checkSelfPermission(containerActivity,
                Manifest.permission.BLUETOOTH_ADMIN)
                != PackageManager.PERMISSION_GRANTED) {

            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(containerActivity,
                    new String[]{Manifest.permission.BLUETOOTH_ADMIN},
                    MY_PERMISSIONS_REQUEST_ACCESS_BLUETOOTH_ADMIN);
            //TODO Handle when the app has no bluetooth admin permissions (intercept callback ID)
        }

        //TODO periodically check for BT connectivity in case it is dropped - poll and assume the device sends us data updates?
    }

    public void registerGlobalListeners() {
        // register the broadcast receiver for messages when we discover some device
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        // register this listener on the main context
        registerActionReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    INSTANCE.btStateChanged();
                }
                else if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
                    // to call the function
                    INSTANCE.btConnectionStateChanged();
                }
                else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                    // discovery started
                    synchronized (BtManager.this) {
                        if (null != BtManager.this.scanningListener) {
                            BtManager.this.scanningListener.onBtScanStarted();
                        }
                    }
                }
                else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    // discovery ended
                    synchronized (BtManager.this) {
                        if (null != BtManager.this.scanningListener) {
                            BtManager.this.scanningListener.onBtScanEnded();
                        }
                    }
                }
                else if (BluetoothDevice.ACTION_FOUND.equals(action)) {// When discovery finds a device
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // and call the function
                    synchronized (BtManager.this) {
                        if (null != BtManager.this.scanningListener) {
                            BtManager.this.scanningListener.onBtDeviceFound(device);
                        }
                    }
                }
            }
        }, filter);
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

    public void connectToDevice(final BluetoothDevice device) {
        synchronized (this) {
            if (null == connectingThread &&
                    (false == isSocketConnected() || false == device.getAddress().equals(connectedDeviceAddress))) {
                // create the new thread to connect to the device without blocking for this new device
                connectingThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // disconnect the old one as the new one is different
                        if (null != btSocket && btSocket.isConnected()) {
                            try {
                                btSocket.close();
                            } catch (IOException e) {
                                Log.e(MainActivity.TAG, e.getMessage());
                            }
                        }
                        // now disconnected, connect to a new one
                        try {
                            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                        } catch (IOException e) {
                            Log.e(MainActivity.TAG, e.getMessage());
                        }
                        try {
                            btSocket.connect();
                        } catch (IOException e) {
                            try {
                                btSocket.close();
                                Log.e(MainActivity.TAG, e.getMessage());
                            } catch (IOException e1) {
                                Log.e(MainActivity.TAG, e1.getMessage());
                            }
                        }
                        if (btSocket.isConnected()) {
                            // this worked ok
                            storeConnectedDevice(device);
                        }
                        // create the connection thread to manage communications to this device
                        createConnectionThread();

                        // finished, set the member to null so next time they try they will
                        // try rather than letting the existing thread end
                        synchronized (BtManager.this) {
                            BtManager.this.connectingThread = null;
                        }
                    }
                }, "BT Connecting");
                connectingThread.start();
            }
        }
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
        // this changes our connection state
        btConnectionStateChanged();
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

    public void connectToDevice(String wantedDeviceAddress) {
        // connect to this device
        Set<BluetoothDevice> devices = getBondedDevices();
        for (BluetoothDevice device : devices) {
            if (null != device && device.getAddress().equals(wantedDeviceAddress)) {
                // this is the device, the address matches the one we want
                connectToDevice(device);
                // stop trying
                break;
            }
        }
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

    public void scanForDevices(IBtManagerScanningListener listener) {
        synchronized (this) {
            // set the new listener, protected as functions can be called from listening callback
            this.scanningListener = listener;
        }
        if (null != adapter) {
            // request the location permission because we need it to scan for BT devices
            if (ContextCompat.checkSelfPermission(this.container,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this.container,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_LOCATION);
                //TODO Handle when the app has no location admin permissions (intercept callback ID)

                // MY_PERMISSIONS_REQUEST_ACCESS_LOCATION is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }

            if (adapter.isDiscovering()) {
                adapter.cancelDiscovery();
            }
            adapter.startDiscovery();
        }
    }

    public void cancelScanning() {
        if (null != adapter) {
            adapter.cancelDiscovery();
        }
        synchronized (this) {
            // clear the listener, protected as functions can be called from listening callback
            this.scanningListener = null;
        }
    }

    public String getConnectedDevice() {
        String connectedDevice = null;
        if (isSocketConnected()) {
            connectedDevice = this.connectedDeviceName;
        }
        return connectedDevice;
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
                StorageManager.getManager().resetMatchStartedDate(-scoreData.secondsGameDuration);
            }
        }
        this.latestScoreData = scoreData;
        for (IBtManagerListener listener : this.listeners) {
            listener.onBtDataChanged(scoreData);
        }
    }
}
