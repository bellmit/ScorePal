package uk.co.darkerwaters.scorepal.controllers;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.scorepal.application.Application;
import uk.co.darkerwaters.scorepal.application.Log;

public class BleDeviceListener {

    public static final int K_BT_SCAN_PERIOD = 10000;

    private static final String BT_OVER_LE_UUID = "03B80E5A-EDE8-4B33-A751-6CE34EC4C700";
    private final Application application;

    private BluetoothManager bluetoothManager = null;
    private BluetoothAdapter adapter = null;
    private ScanCallback btScanCallback = null;
    private volatile boolean isBtScanning = false;
    private BluetoothLeScanner btLeScanner = null;

    private String activeConnectionId = "";

    private static final BluetoothDeviceList btDevices = new BluetoothDeviceList();
    private BluetoothGatt bluetoothGatt = null;

    public interface BluetoothListener {
        void btScanStatusChange(boolean isScanning);
        void btDeviceDiscovered(BluetoothDevice device);
    }

    private final List<BluetoothListener> bluetoothListeners = new ArrayList<>();

    public BleDeviceListener(Application application, Context context) {
        this.application = application;
        // constructor for the input type, set everything up here
        Log.debug("input type bluetooth initialised");
        initialiseConnection(context);
    }

    public void initialiseConnection(Context context) {
        // start up the BT stuff
        if (initialiseBluetooth(context)) {
            // we are initialised, scan too
            scanForBluetoothDevices(context, true);
        }
    }

    public boolean addListener(BluetoothListener listener) {
        synchronized (bluetoothListeners) {
            return bluetoothListeners.add(listener);
        }
    }

    public boolean removeListener(BluetoothListener listener) {
        synchronized (bluetoothListeners) {
            return bluetoothListeners.remove(listener);
        }
    }

    public boolean isBtAvailable(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    public boolean isBtActive() {
        return this.adapter != null && false == adapter.isEnabled();
    }

    private boolean initialiseBluetooth(final Context context) {
        if (null == context) {
            // no context, cannot do this here then
            Log.error("Failed to initialise BT as no activity when requested to do so");
            return false;
        }
        // check we have the permissions to perform a search here
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // no permission
            return false;
        }
        // create the manager and get the adapter to check it
        if (isBtAvailable(context)) {
            this.bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            this.adapter = this.bluetoothManager.getAdapter();
        }
        if (null != this.bluetoothManager) {
            // there is a manager
            if (null == this.adapter) {
                this.adapter = this.bluetoothManager.getAdapter();
            }
        }
        return isBtActive();
    }

    public boolean stopBluetoothScanning() {
        // stop scanning
        if (null != this.btLeScanner && null != btScanCallback) {
            this.btLeScanner.stopScan(btScanCallback);
            this.btScanCallback = null;
        }
        this.isBtScanning = false;
        // inform the listeners of this change
        synchronized (bluetoothListeners) {
            for (BluetoothListener listener : bluetoothListeners) {
                listener.btScanStatusChange(this.isBtScanning);
            }
        }
        return isBtScanning == false;
    }

    public boolean scanForBluetoothDevices(final Context context, boolean isIncludePrevious) {
        // initialise it all
        if (!initialiseBluetooth(context)) {
            return false;
        }
        if (null != this.bluetoothManager) {
            // there is a manager, get the adapter and do the scan
            BluetoothAdapter adapter = this.bluetoothManager.getAdapter();
            if (null != adapter && adapter.isEnabled()) {
                // finally we can start the scan on this adapter, just looking for BLE MIDI devices
                // if we are already scanning, stop already
                if (this.isBtScanning()) {
                    // stop the old scanning
                    stopBluetoothScanning();
                }
                // get the active scanner we want to use
                this.btLeScanner = adapter.getBluetoothLeScanner();
                // create the callback we need for being informed of devices found
                btScanCallback = new ScanCallback() {
                    @Override
                    public void onScanResult(int callbackType, ScanResult result) {
                        super.onScanResult(callbackType, result);
                        processDevice(result.getDevice());
                    }
                    private void processDevice(BluetoothDevice device) {
                        // add to the list
                        btDevices.add(device);
                        // inform the listeners of this new device available
                        onBtDeviceDiscovered(device);
                    }
                    @Override
                    public void onBatchScanResults(List<ScanResult> results) {
                        super.onBatchScanResults(results);
                        for (ScanResult result : results) {
                            processDevice(result.getDevice());
                        }
                    }
                    @Override
                    public void onScanFailed(int errorCode) {
                        super.onScanFailed(errorCode);
                        Log.error("BTLE Scanning error code encountered \"" + errorCode + "\".");
                    }
                };
                // start scanning and stop after a pre-defined scan period.
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // stop without passing the adapter, will get it again - more current
                        stopBluetoothScanning();
                    }
                }, K_BT_SCAN_PERIOD);
                // do we want to inform of existing found devices
                if (isIncludePrevious) {
                    // send a message for each device we already have
                    for (BluetoothDevice device : btDevices.getAll()) {
                        // inform the listeners of this previously available device
                        onBtDeviceDiscovered(device);
                    }
                }
                // and start scanning
                ScanSettings settings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build();
                ArrayList<ScanFilter> filters = new ArrayList<>();
                filters.add(new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(BT_OVER_LE_UUID)).build());
                // scan with these generic settings
                this.btLeScanner.startScan(filters, settings, btScanCallback);
                this.isBtScanning = true;
            }
        }
        // inform the listeners of this change in status
        synchronized (bluetoothListeners) {
            for (BluetoothListener listener : bluetoothListeners) {
                listener.btScanStatusChange(this.isBtScanning);
            }
        }
        // return if we are scanning now
        return this.isBtScanning;
    }

    private void onBtDeviceDiscovered(BluetoothDevice device) {
        // are we connected to any device yet?
        if (this.activeConnectionId == null || this.activeConnectionId.isEmpty()) {
            // there is no active connection, connect to this then
            connectToDevice(device);
        }
        else {
            // so there is an active connection, is this a new discovery of the same device?
            // if it is then connect to this instead as newer is better
            connectToDevice(device);
        }
        // inform the listeners we discovered a BT device
        synchronized (bluetoothListeners) {
            for (BluetoothListener listener : bluetoothListeners) {
                listener.btDeviceDiscovered(device);
            }
        }
    }

    public boolean isBtScanning() {
        return this.isBtScanning;
    }

    public boolean isConnectionActive() {
        return this.bluetoothGatt != null;
    }

    public boolean connectToDevice(final BluetoothDevice item) {
        boolean isConnected = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && null != item) {
            // check our existing connection
            if (isConnectionActive() && this.activeConnectionId.equals(GetDeviceId(item))) {
                // this is connected already
                isConnected = true;
            }
            else {
                // things can go badly wrong in the depths of BT so try/catch it
                try {
                    // store what we are trying here
                    this.activeConnectionId = GetDeviceId(item);
                    // and open the device
                    this.bluetoothGatt = item.connectGatt(this.application.getActiveActivity(), true, new BluetoothGattCallback() {
                        @Override
                        public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
                            super.onPhyUpdate(gatt, txPhy, rxPhy, status);
                            Log.info("BT GATT UPDATED: " + gatt);
                        }
                    });
                    // if here then it didn't throw - we are connected
                    isConnected = true;
                } catch (Exception e) {
                    // inform the dev but just carry on and return out false
                    Log.error("Failed to open BT GATT", e);
                }
            }
        }
        return isConnected;
    }

    public void shutdown() {
        // shutdown the stuff
        if (null != this.bluetoothGatt) {
            try {
                this.bluetoothGatt.disconnect();
                this.bluetoothGatt.close();
            }
            catch (Exception e) {
                Log.error("failed to disconnect or close the GATT", e);
            }
            this.bluetoothGatt = null;
        }
        Log.debug("bluetooth LE shutdown");
        // stop any BT scanning that might be running
        stopBluetoothScanning();
    }

    public String getActiveConnection() {
        return this.activeConnectionId;
    }

    /*
    need to do this special as scanning twice can miss previously discovered devices
    so we will have to keep and refresh a nice static list of BT devices
     */
    private static class BluetoothDeviceList {
        private final List<BluetoothDevice> devices;
        private BluetoothDevice defaultDevice;
        BluetoothDeviceList() {
            devices = new ArrayList<>();
            defaultDevice = null;
        }
        void add(BluetoothDevice device) {
            synchronized (this.devices) {
                // remove any that match this device
                String deviceId = GetDeviceId(device);
                for (BluetoothDevice old : this.devices) {
                    if (GetDeviceId(old).equals(deviceId)) {
                        // this is a match - remove
                        this.devices.remove(old);
                        break;
                    }
                }
                // add the new one
                this.devices.add(device);
                if (null == defaultDevice || GetDeviceId(defaultDevice).equals(deviceId)) {
                    // there is no default, or this replaces is
                    defaultDevice = device;
                }
            }
        }
        BluetoothDevice getDefaultDevice() {
            return this.defaultDevice;
        }
        int size() {
            synchronized (this.devices) {
                return this.devices.size();
            }
        }
        BluetoothDevice[] getAll() {
            synchronized (this.devices) {
                return this.devices.toArray(new BluetoothDevice[0]);
            }
        }
    }

    public static String GetDeviceId(BluetoothDevice device) {
        String deviceId = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && null != device) {
            deviceId = device.getName();
        }
        return deviceId == null ? "" : deviceId;
    }
}
