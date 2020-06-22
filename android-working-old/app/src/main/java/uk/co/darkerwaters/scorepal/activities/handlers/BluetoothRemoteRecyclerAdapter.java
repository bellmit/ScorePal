package uk.co.darkerwaters.scorepal.activities.handlers;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.fragments.CardHolderBluetoothRemote;

public class BluetoothRemoteRecyclerAdapter extends RecyclerView.Adapter<CardHolderBluetoothRemote> {

    // create the cards here
    private final List<BluetoothDevice> devices;
    private final List<BluetoothDevice> connectedDevices;
    private final BluetoothRemoteRecyclerListener listener;

    public interface BluetoothRemoteRecyclerListener {
        void onDeviceClicked(BluetoothDevice device);
        boolean isDeviceConnected(BluetoothDevice device);
    }

    public BluetoothRemoteRecyclerAdapter(BluetoothRemoteRecyclerListener listener) {
        // create the list of cards to show here
        this.devices = new ArrayList<>();
        this.connectedDevices = new ArrayList<>();
        this.listener = listener;
    }

    public void remove(Context context, BluetoothDevice device) {
        final int removedIndex;
        synchronized (this.devices) {
            removedIndex = this.devices.indexOf(device);
            if (-1 != removedIndex) {
                this.devices.remove(removedIndex);
            }
        }
        if (removedIndex != -1) {
            new Handler(context.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    notifyItemRemoved(removedIndex);
                    notifyItemRangeChanged(removedIndex, devices.size());
                }
            });

        }
    }

    public void addToTop(BluetoothDevice device, boolean isDeviceConnected) {
        int addedIndex = -1;
        setDeviceConnected(device, isDeviceConnected);
        synchronized (this.devices) {
            if (false == this.devices.contains(device)) {
                this.devices.add(0, device);
                addedIndex = 0;
            }
        }
        if (addedIndex != -1) {
            notifyDataSetChanged();
        }
    }

    public void add(BluetoothDevice device, boolean isDeviceConnected) {
        int addedIndex = -1;
        setDeviceConnected(device, isDeviceConnected);
        synchronized (this.devices) {
            if (false == this.devices.contains(device)) {
                this.devices.add(device);
                addedIndex = this.devices.size() - 1;
            }
        }
        if (addedIndex != -1) {
            notifyDataSetChanged();
        }
    }

    private void setDeviceConnected(BluetoothDevice device, boolean isDeviceConnected) {
        synchronized (this.connectedDevices) {
            if (isDeviceConnected) {
                if (false == this.connectedDevices.contains(device)) {
                    this.connectedDevices.add(device);
                }
            }
            else {
                this.connectedDevices.remove(device);
            }
        }
    }

    public boolean isDeviceConnected(BluetoothDevice device) {
        synchronized (this.connectedDevices) {
            return this.connectedDevices.contains(device);
        }
    }

    public void update(Context context, BluetoothDevice device, boolean isDeviceConnected) {
        final int index;
        setDeviceConnected(device, isDeviceConnected);
        // and update the item in the list
        synchronized (this.devices) {
            if (!this.devices.contains(device)) {
                addToTop(device, isDeviceConnected);
            }
            index = this.devices.indexOf(device);
        }
        if (index != -1) {
            new Handler(context.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    notifyItemChanged(index);
                }
            });
        }
    }

    @Override
    public CardHolderBluetoothRemote onCreateViewHolder(ViewGroup viewGroup, int i) {
        //GRR - have to use our own counter for getting the class to create as i == 0 regardless...
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_bluetooth_device, viewGroup, false);
        // create the holder and return it
        return new CardHolderBluetoothRemote(v, this.listener);
    }

    @Override
    public void onBindViewHolder(CardHolderBluetoothRemote viewHolder, int i) {
        // initialise the card holder here
        BluetoothDevice bluetoothDevice = this.devices.get(i);
        viewHolder.initialiseCard(bluetoothDevice);
    }

    @Override
    public int getItemCount() {
        return this.devices.size();
    }
}
