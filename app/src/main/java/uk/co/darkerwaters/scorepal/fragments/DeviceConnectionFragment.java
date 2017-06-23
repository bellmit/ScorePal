package uk.co.darkerwaters.scorepal.fragments;


import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.ViewAnimator;
import uk.co.darkerwaters.scorepal.activities.BtConnectActivity;
import uk.co.darkerwaters.scorepal.bluetooth.BtManager;
import uk.co.darkerwaters.scorepal.storage.ScoreData;

public class DeviceConnectionFragment extends Fragment implements BtManager.IBtManagerListener {

    private TextView connectionText;
    private ImageView connectionImage;
    private ImageButton connectionButton;
    private Activity parentContext;

    boolean isAutoHide = true;
    boolean isConnectivityControlsShown = true;

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
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement Activity");
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

        // initially hide the connection toolbar fragment so slides in if disconnected
        setIsAutoHide(isAutoHide, view);

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

    public void setIsAutoHide(boolean isAutoHide, View topView) {
        this.isAutoHide = isAutoHide;
        if (null == topView) {
            topView = getView();
        }
        if (isAutoHide) {
            // are auto hiding - intially hide everything so they slide in when needed
            // instead of always sliding out when created / orientation changes etc
            isConnectivityControlsShown = false;
            if (null != topView) {
                FragmentManager fm = getFragmentManager();
                fm.beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .hide(this)
                        .commit();
            }
        }
        else {
            // else are never hiding, make sure they are shown
            isConnectivityControlsShown = true;
            if (null != topView) {
                FragmentManager fm = getFragmentManager();
                fm.beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .show(this)
                        .commit();
            }
        }
    }

    private void onConnectionClicked() {
        Intent intent = new Intent(parentContext.getApplicationContext(), BtConnectActivity.class);
        startActivity(intent);
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
            // just hide this because showing what is connected isn't required
            if (isConnectivityControlsShown && isAutoHide) {
                // slide the views away and make them gone
                ViewAnimator.slideControlsUpAndAway(parentContext, null, getView());
                // remember that we hid them
                isConnectivityControlsShown = false;
            }
        }
        else {
            connectionText.setText(R.string.bt_not_connected);
            connectionImage.setImageResource(R.drawable.bluetooth);
            // show that no device is connected
            if (false == isConnectivityControlsShown) {
                // slide the views in and make them gone
                ViewAnimator.slideControlsDownAndIn(parentContext, null, getView());
                // remember that we showed them
                isConnectivityControlsShown = true;
            }
        }
    }
}
