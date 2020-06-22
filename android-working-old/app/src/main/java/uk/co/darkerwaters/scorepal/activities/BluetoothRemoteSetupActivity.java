package uk.co.darkerwaters.scorepal.activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.handlers.BluetoothRemoteRecyclerAdapter;
import uk.co.darkerwaters.scorepal.application.Application;
import uk.co.darkerwaters.scorepal.application.Log;

public class BluetoothRemoteSetupActivity extends BaseBluetoothActivity implements
        BluetoothRemoteRecyclerAdapter.BluetoothRemoteRecyclerListener {

    private Button discoverButton;
    private ImageView statusIcon;
    private TextView statusText;

    private RecyclerView discoveredDevicesList;
    private BluetoothRemoteRecyclerAdapter listAdapter;

    private boolean isBleScanning = false;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_remote_setup);

        // set the title of this
        setupActivity(R.string.bluetoothRemoteSetup);

        this.handler = new Handler();
        this.isBleScanning = false;

        this.statusIcon = findViewById(R.id.btStatusIcon);
        this.statusText = findViewById(R.id.btStatusTextView);
        this.discoveredDevicesList = findViewById(R.id.btDiscoveredDevices);

        this.discoverButton = findViewById(R.id.discoverButton);
        this.discoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                discoverDevices();
            }
        });

        this.listAdapter = new BluetoothRemoteRecyclerAdapter(this);
        this.discoveredDevicesList.setAdapter(this.listAdapter);

        float displaySize = Application.getDisplaySize(this).getWidth();
        int noColumns = 1 + (int)(displaySize / 550f);
        this.discoveredDevicesList.setLayoutManager(new GridLayoutManager(this, Math.min(2, noColumns)));
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_LONG).show();
        }

        // turn on bluetooth
        requestBluetoothPermission();

        // and update our list
        updateListAdapter();
    }

    @Override
    protected void onPause() {
        // and pause
        super.onPause();
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback leScanCallback =
        new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onDeviceDiscovered(device, false);
                    }
                });
            }
        };

    private void scanForBleDevices(boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isBleScanning = false;
                    bluetoothAdapter.stopLeScan(leScanCallback);
                }
            }, 10000);

            isBleScanning = true;
            bluetoothAdapter.startLeScan(leScanCallback);
        } else {
            isBleScanning = false;
            bluetoothAdapter.stopLeScan(leScanCallback);
        }

    }

    @Override
    protected void onBluetoothEnabled() {
        super.onBluetoothEnabled();
        // populate the list with the pre-connected devices
        Set<BluetoothDevice> pairedDevices = this.bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                onDeviceDiscovered(device, false);
            }
        }
        // start scanning
        scanForBleDevices(true);

        // and show this on the status
        setStatusDisplay();
    }

    @Override
    protected boolean turnOnBluetooth() {
        // let the base handle this
        boolean result = super.turnOnBluetooth();
        // but show our status
        setStatusDisplay();
        // and return the result
        return result;
    }

    private void updateDevice(BluetoothDevice device, boolean isConnected) {
        this.listAdapter.update(this, device, isConnected);
    }

    private void onDeviceDiscovered(BluetoothDevice device, boolean isConnected) {
        // add to the list of devices to show the user
        String deviceName = device.getName();
        if (null != deviceName && deviceName.toLowerCase().contains("score")) {
            this.listAdapter.addToTop(device, isConnected);
        }
        else {
            this.listAdapter.add(device, isConnected);
        }
    }

    private void discoverDevices() {
        // get all the pre-bonded devices
        if (isEnabled()) {
            if (false == isDiscovering()) {
                // start discovering
                this.bluetoothAdapter.startDiscovery();
            }
            else {
                // stop discovering
                this.bluetoothAdapter.cancelDiscovery();
            }
            // and update the display
            setStatusDisplay();
        }
        else {
            // not on, turn on
            turnOnBluetooth();
        }
    }

    private void setStatusDisplay() {
        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isBleScanning || isDiscovering()) {
                    BaseActivity.setupButtonIcon(statusIcon, R.drawable.ic_baseline_bluetooth_searching);
                    //statusText.setText(R.string.bt_status_discovering);
                    //statusText.setVisibility(View.VISIBLE);
                    statusText.setVisibility(View.INVISIBLE);
                }
                else if (isEnabled()) {
                    BaseActivity.setupButtonIcon(statusIcon, R.drawable.ic_baseline_bluetooth);
                    statusText.setText(R.string.bt_status_on);
                    statusText.setVisibility(View.INVISIBLE);
                }
                else {
                    BaseActivity.setupButtonIcon(statusIcon, R.drawable.ic_baseline_bluetooth_disabled);
                    statusText.setText(R.string.bt_status_off);
                    statusText.setVisibility(View.INVISIBLE);
                }
            }
        }, 500);
    }

    @Override
    public boolean isDeviceConnected(BluetoothDevice device) {
        return this.listAdapter.isDeviceConnected(device);
    }

    @Override
    public void onDeviceClicked(final BluetoothDevice device) {
        // as a client we want to connect to the server now
        // first, cancel discovery because it otherwise slows down the connection.
        this.bluetoothAdapter.cancelDiscovery();
        // connect to this device by creating the bond
        try {
            if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                // create the bond
                device.createBond();
            }
            // start the connection in a sec to give bonding a chance
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // and connect to this bonded device - just use the auto connect
                    device.connectGatt(BluetoothRemoteSetupActivity.this, true, new BluetoothGattCallback() {
                        @Override
                        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                            // connection state changed
                            if (newState == BluetoothProfile.STATE_CONNECTED) {
                                updateDevice(device, true);
                            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                                updateDevice(device, false);
                            }
                        }
                        @Override
                        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                            // New services discovered
                        }
                        @Override
                        public void onCharacteristicRead(BluetoothGatt gatt,
                                                         BluetoothGattCharacteristic characteristic,
                                                         int status) {
                            // Result of a characteristic read operation
                        }
                    });
                }}, 1000);
        } catch (Exception e) {
            Log.error("Failed to bond to device", e);
        }
        // show the connection changed
        updateListAdapter();
        // and update our display
        setStatusDisplay();
        // inform the user that this can be flakey
        Toast.makeText(this, R.string.ble_might_need_help, Toast.LENGTH_LONG).show();
    }

    private void updateListAdapter() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listAdapter.notifyDataSetChanged();
            }
        });
    }
}
