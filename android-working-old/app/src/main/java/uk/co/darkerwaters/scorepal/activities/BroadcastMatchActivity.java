package uk.co.darkerwaters.scorepal.activities;

import android.bluetooth.BluetoothAdapter;
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
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Locale;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.handlers.BluetoothMatchRecyclerAdapter;
import uk.co.darkerwaters.scorepal.application.Application;
import uk.co.darkerwaters.scorepal.application.GamePlayBroadcaster;

public class BroadcastMatchActivity extends BaseBluetoothActivity implements
        GamePlayBroadcaster.GamePlayBroadcastListener,
        BluetoothMatchRecyclerAdapter.BluetoothDeviceRecyclerListener {

    private static final int REQUEST_BLUETOOTH_DISCOVERABLE = 153;
    private static final int K_BT_DISCOVERY_PERIOD_SEC = 60;

    private Button discoverableButton;
    private ImageView statusIcon;
    private TextView statusText;
    private TextView countdownText;
    private TextView helpTextView;
    private Switch allowClientMessagesSwitch;

    private RecyclerView connectedDevicesList;
    private BluetoothMatchRecyclerAdapter listAdapter;

    private BroadcastReceiver broadcastReceiver;
    private volatile long discoveryEnd = 0L;

    private Runnable timerRunnable;
    private Handler timerHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcast_match);

        // set the title of this
        setupActivity(R.string.menu_broadcastMatchSetup);

        this.statusIcon = findViewById(R.id.btStatusIcon);
        this.statusText = findViewById(R.id.btStatusTextView);
        this.countdownText = findViewById(R.id.btStatusCountdown);
        this.helpTextView = findViewById(R.id.btHelpTextView);
        this.allowClientMessagesSwitch = findViewById(R.id.allowClientMessagesSwitch);
        this.connectedDevicesList = findViewById(R.id.btConnectedDevices);

        this.allowClientMessagesSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                application.getSettings().setIsAllowClientsToChangeScore(b);
            }
        });
        this.allowClientMessagesSwitch.setChecked(application.getSettings().getIsAllowClientsToChangeScore());

        this.discoverableButton = findViewById(R.id.discoverableButton);
        this.discoverableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeDeviceDiscoverable();
            }
        });

        this.listAdapter = new BluetoothMatchRecyclerAdapter(GamePlayBroadcaster.ActivateBroadcaster(this), this);
        this.connectedDevicesList.setAdapter(this.listAdapter);

        float displaySize = Application.getDisplaySize(this).getWidth();
        int noColumns = 1 + (int)(displaySize / 550f);
        this.connectedDevicesList.setLayoutManager(new GridLayoutManager(this, Math.min(2, noColumns)));

        // create our update timer task
        this.timerHandler = new Handler(getMainLooper());
        this.timerRunnable = new Runnable() {
            @Override
            public void run() {
                // update the display
                if (updateDiscoveryPeriod()) {
                    // and again in a second
                    timerHandler.postDelayed(this, 1000);
                }
            }
        };
        // and turn on bluetooth
        turnOnBluetooth();
    }

    private boolean updateDiscoveryPeriod() {
        // set the text to the time remaining
        long timeRemaining = (this.discoveryEnd - System.currentTimeMillis()) / 1000L;
        if (timeRemaining > 0L) {
            this.countdownText.setText(String.format(Locale.getDefault(), "%d", timeRemaining));
            setStatusDisplay(true, true);
        }
        else {
            this.countdownText.setText("");
            setStatusDisplay(true, false);
        }
        // return if we want to update again
        return timeRemaining > 0L;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // and listen for changes in our BT state to show
        this.broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (null != action) {
                    switch (action) {
                        case BluetoothAdapter.ACTION_STATE_CHANGED:
                            // the state changed
                            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                            switch (state) {
                                case BluetoothAdapter.STATE_OFF:
                                    setStatusDisplay(false, false);
                                    break;
                                case BluetoothAdapter.STATE_TURNING_OFF:
                                    break;
                                case BluetoothAdapter.STATE_ON:
                                    setStatusDisplay(true, false);
                                    break;
                                case BluetoothAdapter.STATE_TURNING_ON:
                                    break;
                            }
                            break;
                        case BluetoothAdapter.ACTION_SCAN_MODE_CHANGED:
                            // the scan mode changed
                            int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);
                            switch (mode) {
                                case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                                    // the device is in discoverable mode.
                                    setStatusDisplay(true, true);
                                    break;
                                case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                                    // The device isn't in discoverable mode but can still receive connections.
                                    setStatusDisplay(true, false);
                                    break;
                                case BluetoothAdapter.SCAN_MODE_NONE:
                                    // The device isn't in discoverable mode and cannot receive connections.
                                    setStatusDisplay(true, false);
                                    break;
                            }
                            break;
                    }
                }
            }
        };
        // create the filter and register the receiver
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(this.broadcastReceiver, filter);
    }

    @Override
    protected boolean turnOnBluetooth() {
        // let the base handle this
        boolean result = super.turnOnBluetooth();
        // but show our status
        setStatusDisplay(result, false);
        // and return the result
        return result;
    }

    @Override
    protected void onBluetoothEnabled() {
        super.onBluetoothEnabled();
        // BT is now enabled, we should create our BT server now so clients can connect to us
        GamePlayBroadcaster broadcaster = GamePlayBroadcaster.ActivateBroadcaster(this);
        broadcaster.addListener(this);

        // start broadcasting what we can.
        broadcaster.startBroadcasting();

        for (BluetoothDevice device : broadcaster.getConnectedSocketDevices()) {
            this.listAdapter.add(device);
        }
    }

    private void setStatusDisplay(final boolean isEnabled, final boolean isDiscoverable) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isDiscoverable) {
                    BaseActivity.setupButtonIcon(statusIcon, R.drawable.ic_baseline_bluetooth_searching);
                    statusText.setText(R.string.bt_status_discoverable);
                    helpTextView.setText(R.string.bt_help_discoverable);
                }
                else if (isEnabled) {
                    BaseActivity.setupButtonIcon(statusIcon, R.drawable.ic_baseline_bluetooth);
                    statusText.setText(R.string.bt_status_on);
                    helpTextView.setText(R.string.bt_help_enabled);
                }
                else {
                    BaseActivity.setupButtonIcon(statusIcon, R.drawable.ic_baseline_bluetooth_disabled);
                    statusText.setText(R.string.bt_status_off);
                    helpTextView.setText(R.string.bt_help_disabled);
                }
            }
        });
    }

    @Override
    protected void onPause() {
        // unregister our broadcast receiver for the state of this
        unregisterReceiver(this.broadcastReceiver);
        // stop listening to the broadcaster
        GamePlayBroadcaster broadcaster = GamePlayBroadcaster.ActivateBroadcaster(this);
        broadcaster.removeListener(this);
        // cancel any timer
        this.discoveryEnd = 0L;
        this.timerHandler.removeCallbacks(this.timerRunnable);
        // and pause the activity
        super.onPause();
    }

    private void makeDeviceDiscoverable() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, K_BT_DISCOVERY_PERIOD_SEC);

        // want to listen to changes here
        startActivityForResult(discoverableIntent, REQUEST_BLUETOOTH_DISCOVERABLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_BLUETOOTH_DISCOVERABLE:
                // the result code is the amount of time we will now be discoverable for
                if (resultCode == RESULT_CANCELED) {
                    // cancelled, no end time
                    this.discoveryEnd = 0L;
                }
                else {
                    // update the time accordingly
                    this.discoveryEnd = System.currentTimeMillis() + (resultCode * 1000L);
                }
                // schedule the timer to update the display of this
                this.timerHandler.post(this.timerRunnable);
                break;
        }
    }

    @Override
    public void onDeviceSocketConnected(final BluetoothDevice connectedDevice) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listAdapter.add(connectedDevice);
            }
        });
    }

    @Override
    public void onDeviceSocketDisconnected(final BluetoothDevice disconnectedDevice) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listAdapter.remove(disconnectedDevice);
            }
        });
    }

    @Override
    public void onDeviceClicked(BluetoothDevice device) {
        // device in the list was clicked, disconnect it (boot it)
        GamePlayBroadcaster broadcaster = GamePlayBroadcaster.ActivateBroadcaster(this);
        //TODO this is just a test - better to have a 'boot' button so the user knows what it does
        broadcaster.disconnectConnectedSocketDeviceFromBroadcaster(device);
    }
}
