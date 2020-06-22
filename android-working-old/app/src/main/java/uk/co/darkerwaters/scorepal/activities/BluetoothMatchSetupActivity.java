package uk.co.darkerwaters.scorepal.activities;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Set;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.handlers.BluetoothMatchRecyclerAdapter;
import uk.co.darkerwaters.scorepal.application.Application;
import uk.co.darkerwaters.scorepal.application.GamePlayBroadcaster;
import uk.co.darkerwaters.scorepal.score.base.Sport;

public class BluetoothMatchSetupActivity extends BaseBluetoothActivity implements
        BluetoothMatchRecyclerAdapter.BluetoothDeviceRecyclerListener,
        GamePlayBroadcaster.GamePlayBroadcastListener {

    private Button discoverButton;
    private ImageView statusIcon;
    private TextView statusText;

    private RecyclerView discoveredDevicesList;
    private BluetoothMatchRecyclerAdapter listAdapter;
    private GamePlayBroadcaster broadcaster;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_match_setup);

        // set the title of this
        setupActivity(Sport.BLUETOOTH.titleResId);

        // activate the broadcaster
        this.broadcaster = GamePlayBroadcaster.ActivateBroadcaster(this);

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

        this.listAdapter = new BluetoothMatchRecyclerAdapter(this.broadcaster, this);
        this.discoveredDevicesList.setAdapter(this.listAdapter);

        float displaySize = Application.getDisplaySize(this).getWidth();
        int noColumns = 1 + (int)(displaySize / 550f);
        this.discoveredDevicesList.setLayoutManager(new GridLayoutManager(this, Math.min(2, noColumns)));
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                onDeviceDiscovered(device);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        // activate our broadcaster each time we are shown
        this.broadcaster = GamePlayBroadcaster.ActivateBroadcaster(this);
        this.broadcaster.addListener(this);

        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

        // and turn on bluetooth
        requestBluetoothPermission();

        // and update our list
        updateListAdapter();
    }

    @Override
    protected void onPause() {
        // unregister our receiver
        unregisterReceiver(receiver);
        // stop listening to the broadcaster
        this.broadcaster.removeListener(this);
        // and pause
        super.onPause();
    }

    @Override
    protected void onBluetoothEnabled() {
        super.onBluetoothEnabled();
        // populate the list with the pre-connected devices
        Set<BluetoothDevice> pairedDevices = this.bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                onDeviceDiscovered(device);
            }
        }
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

    private void onDeviceDiscovered(BluetoothDevice device) {
        // add to the list of devices to show the user
        if (broadcaster.isSocketDeviceConnected(device)) {
            this.listAdapter.addToTop(device);
        }
        else {
            this.listAdapter.add(device);
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
                if (isDiscovering()) {
                    BaseActivity.setupButtonIcon(statusIcon, R.drawable.ic_baseline_bluetooth_searching);
                    statusText.setText(R.string.bt_status_discovering);
                    statusText.setVisibility(View.VISIBLE);
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
    public void onDeviceClicked(BluetoothDevice device) {
        // as a client we want to connect to the server now
        // first, cancel discovery because it otherwise slows down the connection.
        this.bluetoothAdapter.cancelDiscovery();
        // remember the device we want to be connecting to
        this.broadcaster.setDeviceToConnect(device);
        // show the connection started
        updateListAdapter();
        // and show the activity that will connect to this
        Intent myIntent = new Intent(this, BluetoothMatchPlayActivity.class);
        myIntent.putExtra(PlayActivity.K_ISFROMSETTINGS, true);
        startActivity(myIntent);
    }

    private void updateListAdapter() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onDeviceSocketConnected(final BluetoothDevice connectedDevice) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listAdapter.moveToTop(connectedDevice);
                discoveredDevicesList.scrollToPosition(0);
            }
        });
    }

    @Override
    public void onDeviceSocketDisconnected(final BluetoothDevice disconnectedDevice) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listAdapter.moveToTop(disconnectedDevice);
                discoveredDevicesList.scrollToPosition(0);
            }
        });
    }
}
