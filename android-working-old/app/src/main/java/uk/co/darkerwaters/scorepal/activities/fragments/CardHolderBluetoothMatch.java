package uk.co.darkerwaters.scorepal.activities.fragments;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.BaseActivity;
import uk.co.darkerwaters.scorepal.activities.handlers.BluetoothMatchRecyclerAdapter;
import uk.co.darkerwaters.scorepal.application.GamePlayBroadcaster;

public class CardHolderBluetoothMatch extends RecyclerView.ViewHolder {

    private final View parent;

    private final TextView itemTitle;
    private final ImageView itemImage;
    private final TextView itemDetail;

    private final ProgressBar connectingProgressBar;
    private final BluetoothMatchRecyclerAdapter.BluetoothDeviceRecyclerListener listener;

    public CardHolderBluetoothMatch(@NonNull View itemView, BluetoothMatchRecyclerAdapter.BluetoothDeviceRecyclerListener listener) {
        super(itemView);
        this.parent = itemView;
        this.listener = listener;
        // card is created, find all our children views and stuff here
        this.itemImage = this.parent.findViewById(R.id.item_image);
        this.itemTitle = this.parent.findViewById(R.id.item_title);
        this.itemDetail = this.parent.findViewById(R.id.item_detail);
        this.connectingProgressBar = this.parent.findViewById(R.id.connectingProgressBar);
    }

    public void initialiseCard(GamePlayBroadcaster broadcaster, final BluetoothDevice device) {
        this.itemTitle.setText(device.getName());
        this.itemDetail.setText(device.getAddress());

        this.connectingProgressBar.setVisibility(broadcaster.isSocketDeviceConnecting(device) ? View.VISIBLE : View.GONE);

        if (broadcaster.isSocketDeviceConnected(device)) {
            BaseActivity.setupButtonIcon(itemImage, R.drawable.ic_baseline_bluetooth_connected);
        }
        else {
            BaseActivity.setupButtonIcon(itemImage, R.drawable.ic_baseline_bluetooth);
        }

        // also handle the click here, connect to this as our match broadcast server
        this.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // connect to the broadcaster clicked on
                listener.onDeviceClicked(device);
            }
        });
    }




}
