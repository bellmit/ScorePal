package uk.co.darkerwaters.scorepal.activities;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.bluetooth.BtManager;
import uk.co.darkerwaters.scorepal.storage.ScoreData;

public class BtConnectActivity extends AppCompatActivity implements BtManager.IBtManagerListener, BtManager.IBtManagerScanningListener {

    private ListView devicesPairedListView;
    private TextView btStatusText;
    private ProgressBar scanProgress;
    private ToggleButton btStatusToggle;
    private boolean isScanInitiated = false;
    private BtConnectListAdapter pairedListAdapter = null;
    private BtConnectListAdapter scanListAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_connect);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        devicesPairedListView = (ListView) findViewById(R.id.bt_devices_list);
        scanProgress = (ProgressBar) findViewById(R.id.bt_scanning_progress);
        btStatusText = (TextView) findViewById(R.id.bt_status_text);
        btStatusToggle = (ToggleButton) findViewById(R.id.bt_toggle);

        // create the list view adapter
        pairedListAdapter = new BtConnectListAdapter(this);
        devicesPairedListView.setAdapter(pairedListAdapter);

        // hide the progress spinner
        scanProgress.setVisibility(View.INVISIBLE);
        findViewById(R.id.bt_scan_button).setEnabled(true);

        // setup the BT status display things
        BtManager manager = BtManager.getManager();
        boolean isBluetoothOn = manager.isEnabled();
        // and setup the status listener
        btStatusToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onBtStatusCheck(isChecked);
            }
        });
        // and turn it on / off on the view
        onBtStatusCheck(isBluetoothOn);
        // listen for clicks on the list
        devicesPairedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // handle the user clicking the item in the view
                btPairedDeviceSelected(position);
            }
        });
        // and fill the list if we can
        populateList();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // device can be shown from score and main, go back instead of up for
                // consistency of behaviour here then
                onBackPressed();
                return true;
        }
        return false;
    }

    @Override
    protected void onPause() {
        // cancel any BT scanning
        BtManager.getManager().cancelScanning();
        // and unregister us as a listener
        BtManager.getManager().unregisterListener(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        BtManager.getManager().registerListener(this);
    }

    private void btScanedDeviceSelected(int position) {
        // we want to pair this device and then connect to it, add it to the paired list
        final Object clickedItem = scanListAdapter.getItem(position);
        // add this to the paired list
        if (null != clickedItem && clickedItem instanceof BluetoothDevice) {
            connectDevice((BluetoothDevice)clickedItem);

        }
    }

    private void btPairedDeviceSelected(int position) {
        // this is the paired device to connect to
        final BtManager manager = BtManager.getManager();
        // find the BT device at this position in the list
        final Object clickedItem = pairedListAdapter.getItem(position);
        if (null != clickedItem && clickedItem instanceof BluetoothDevice) {
            connectDevice((BluetoothDevice)clickedItem);
        }
    }

    private void connectDevice(final BluetoothDevice device) {
        final BtManager manager = BtManager.getManager();
        final ProgressDialog progress = ProgressDialog.show(this,
                getResources().getString(R.string.connecting),
                getResources().getString(R.string.please_wait_connecting), false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                // try the connection here, can take a little while
                manager.connectToDevice(device);
                // dismiss the progress dialog
                progress.dismiss();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (manager.getConnectedDevice() == null) {
                            Toast.makeText(BtConnectActivity.this, R.string.failed_to_connect, Toast.LENGTH_SHORT).show();
                        }
                        else {
                            //highlight the connected device
                            populateList();
                        }
                    }
                });
            }
        }).start();
    }


    public void onBtStatusCheck(boolean isChecked) {
        BtManager manager = BtManager.getManager();
        if (isChecked) {
            // set the status on the text
            btStatusText.setText(R.string.bt_status_on_text);
        }
        else {
            // show the user that bluetooth is not enabled
            btStatusText.setText(R.string.bt_status_text);
        }
        // set the check button on / off
        btStatusToggle.setChecked(isChecked);
        // this might be a change, change the settings on the manager
        manager.enable(isChecked);
        // and update the list
        populateList();
    }

    private void populateList() {
        BtManager manager = BtManager.getManager();
        if (manager.isEnabled()) {
            // fill the list with the bluetooth devices we are connected to
            pairedListAdapter.upDateEntries(manager.getBondedDevices());
        }
        else {
            // clear the list
            pairedListAdapter.upDateEntries(null);
        }
        devicesPairedListView.setAdapter(pairedListAdapter);
    }

    public void scanForDevices(View view) {
        // scan for new devices to connect to
        BtManager.getManager().scanForDevices(this);
    }

    @Override
    public void onBtScanStarted() {
        // scanning has started, show the progress indicator for this
        findViewById(R.id.bt_scan_button).setEnabled(false);
        scanProgress.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBtScanEnded() {
        findViewById(R.id.bt_scan_button).setEnabled(true);
        scanProgress.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onBtDeviceFound(BluetoothDevice device) {
        // Discovery has found a device
        //String deviceName = device.getName();
        //String deviceHardwareAddress = device.getAddress(); // MAC address
        if (null != pairedListAdapter) {
            // add to the list of devices to appear in the list
            pairedListAdapter.add(device);
        }
    }

    @Override
    public void onBtStatusChanged() {
        // the state of the bluetooth just changed, refresh the lists
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                populateList();
            }
        });
        // if we just turned on then try to connect to the remembered device
        BtManager manager = BtManager.getManager();
        if (null != manager && manager.isEnabled()) {
            manager.connectToLastDevice();
        }
    }

    @Override
    public void onBtConnectionStatusChanged() {
        //TODO update the highlight status of the connected / not connected item
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pairedListAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onBtDataChanged(ScoreData scoreData) {

    }
}
