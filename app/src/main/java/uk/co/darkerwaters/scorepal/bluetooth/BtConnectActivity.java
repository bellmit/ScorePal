package uk.co.darkerwaters.scorepal.bluetooth;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.ScoreData;

public class BtConnectActivity extends AppCompatActivity implements BtManager.IBtManagerListener {

    private ListView devicesPairedListView;
    private ListView devicesScanListView;
    private TextView btStatusText;
    private ToggleButton btStatusToggle;
    private boolean isScanInitiated = false;
    private BtListAdapter pairedListAdapter = null;
    private BtListAdapter scanListAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_connect);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        devicesPairedListView = (ListView) findViewById(R.id.bt_devices_list);
        devicesScanListView = (ListView) findViewById(R.id.bt_scan_devices_list);
        btStatusText = (TextView) findViewById(R.id.bt_status_text);
        btStatusToggle = (ToggleButton) findViewById(R.id.bt_toggle);

        // create the list view adapters
        scanListAdapter = new BtListAdapter(this);
        devicesScanListView.setAdapter(scanListAdapter);
        pairedListAdapter = new BtListAdapter(this);
        devicesPairedListView.setAdapter(pairedListAdapter);

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
        devicesScanListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // handle the user clicked the item in the scanned device view
                btScanedDeviceSelected(position);
            }
        });
        // and fill the list if we can
        populateList();
    }

    @Override
    protected void onPause() {
        BtManager.getManager().unregisterListener(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        BtManager.getManager().registerListener(this);
    }

    private void btScanedDeviceSelected(int position) {

    }

    private void btPairedDeviceSelected(int position) {
        // this is the paired device to connect to
        final BtManager manager = BtManager.getManager();
        // find the BT device at this position in the list
        final Object clickedItem = pairedListAdapter.getItem(position);
        if (null != clickedItem && clickedItem instanceof BluetoothDevice) {
            final ProgressDialog progress = ProgressDialog.show(this,
                    getResources().getString(R.string.connecting),
                    getResources().getString(R.string.please_wait_connecting), false);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // try the connection here, can take a little while
                    manager.connectToDevice((BluetoothDevice)clickedItem);
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
        //TODO scanning or BT devices is not working for some reason
        // scan for new devices to connect to
        BtManager.getManager().scanForDevices();
    }

    @Override
    public void onBtDeviceFound(BluetoothDevice device) {
        // Discovery has found a device
        //String deviceName = device.getName();
        //String deviceHardwareAddress = device.getAddress(); // MAC address
        if (null != scanListAdapter) {
            // add to the list of scanned devices to appear in the list
            scanListAdapter.add(device);
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
        pairedListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBtDataChanged(ScoreData scoreData) {

    }
}
