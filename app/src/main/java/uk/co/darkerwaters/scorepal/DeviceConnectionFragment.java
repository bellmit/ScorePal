package uk.co.darkerwaters.scorepal;


import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import uk.co.darkerwaters.scorepal.activities.ScoreActivity;
import uk.co.darkerwaters.scorepal.bluetooth.BtConnectActivity;
import uk.co.darkerwaters.scorepal.bluetooth.BtManager;
import uk.co.darkerwaters.scorepal.storage.ScoreData;

public class DeviceConnectionFragment extends Fragment implements BtManager.IBtManagerListener {

    private TextView connectionText;
    private ImageView connectionImage;
    private ImageButton connectionButton;
    private Activity parentContext;
    private IDeviceConnectionListener listener;

    public interface IDeviceConnectionListener {
        public void onDeviceConnectionChanged(boolean isManagerEnabled, String deviceConnected);
    }

    public DeviceConnectionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            // we want it to be an activity to call RunOnUI
            this.parentContext = (Activity)context;
            // also it has to be an listener
            this.listener = (IDeviceConnectionListener)context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement IDeviceConnectionListener and Activity");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_device_connection, container, false);

        connectionText = (TextView) view.findViewById(R.id.bt_connected_text);
        // setup the button click
        connectionButton = (ImageButton) view.findViewById(R.id.bt_connect_button);
        connectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onConnectionClicked();
            }
        });
        // and the view
        connectionImage = (ImageView) view.findViewById(R.id.bt_connected_image);

        // return the view that is the fragment expanded from the xml
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // each time we start - try to connect to the last device
        BtManager manager = BtManager.getManager();
        // listen for updates from the manager
        manager.registerListener(this);
        // try the connection here, can take a little while
        manager.connectToLastDevice();
        // update our current connectivity
        updateConnectionStatus();
    }

    private void onConnectionClicked() {
        Intent intent = new Intent(parentContext.getApplicationContext(), BtConnectActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBtDeviceFound(BluetoothDevice device) {
        // not very interesting, update the display anyway
        updateConnectionStatus();
    }

    @Override
    public void onBtStatusChanged() {
        // not very interesting, update the display anyway
        updateConnectionStatus();
    }

    @Override
    public void onBtConnectionStatusChanged() {
        // very interesting
        parentContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // update our display of connectivity
                updateConnectionStatus();
            }
        });
    }

    @Override
    public void onBtDataChanged(ScoreData scoreData) {
        // not very interesting
    }

    public void updateConnectionStatus() {
        BtManager manager = BtManager.getManager();
        String connectedDevice = manager.getConnectedDevice();
        // show the data if we are enabled and connected, else show we are not connected
        if (manager.isEnabled() && null != connectedDevice) {
            connectionText.setText(connectedDevice);
            connectionImage.setImageResource(R.drawable.scorepal);
        }
        else {
            connectionText.setText(R.string.bt_not_connected);
            connectionImage.setImageResource(R.drawable.bluetooth);
        }
        // send this new data to the listener
        listener.onDeviceConnectionChanged(manager.isEnabled() && null != connectedDevice, connectedDevice);
    }
}
