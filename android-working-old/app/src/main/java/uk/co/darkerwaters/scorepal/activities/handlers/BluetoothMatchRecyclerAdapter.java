package uk.co.darkerwaters.scorepal.activities.handlers;

import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.fragments.CardHolderBluetoothMatch;
import uk.co.darkerwaters.scorepal.application.GamePlayBroadcaster;

public class BluetoothMatchRecyclerAdapter extends RecyclerView.Adapter<CardHolderBluetoothMatch> {

    // create the cards here
    private final List<BluetoothDevice> devices;
    private final BluetoothDeviceRecyclerListener listener;
    private final GamePlayBroadcaster broadcaster;

    public interface BluetoothDeviceRecyclerListener {
        void onDeviceClicked(BluetoothDevice device);
    }

    public BluetoothMatchRecyclerAdapter(GamePlayBroadcaster broadcaster, BluetoothDeviceRecyclerListener listener) {
        // create the list of cards to show here
        this.devices = new ArrayList<>();
        this.broadcaster = broadcaster;
        this.listener = listener;
    }

    public void remove(BluetoothDevice device) {
        int removedIndex;
        synchronized (this.devices) {
            removedIndex = this.devices.indexOf(device);
            if (-1 != removedIndex) {
                this.devices.remove(removedIndex);
            }
        }
        if (removedIndex != -1) {
            notifyItemRemoved(removedIndex);
            notifyItemRangeChanged(removedIndex, this.devices.size());
        }
    }

    public void addToTop(BluetoothDevice device) {
        int addedIndex = -1;
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

    public void add(BluetoothDevice device) {
        int addedIndex = -1;
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

    public void moveToTop(BluetoothDevice device) {
        remove(device);
        addToTop(device);
    }

    public void update(BluetoothDevice device) {
        int index;
        synchronized (this.devices) {
            index = this.devices.indexOf(device);
        }
        if (index != -1) {
            notifyItemChanged(index);
        }
    }

    @Override
    public CardHolderBluetoothMatch onCreateViewHolder(ViewGroup viewGroup, int i) {
        //GRR - have to use our own counter for getting the class to create as i == 0 regardless...
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_bluetooth_device, viewGroup, false);
        // create the holder and return it
        return new CardHolderBluetoothMatch(v, this.listener);
    }

    @Override
    public void onBindViewHolder(CardHolderBluetoothMatch viewHolder, int i) {
        // initialise the card holder here
        viewHolder.initialiseCard(this.broadcaster, this.devices.get(i));
    }

    @Override
    public int getItemCount() {
        return this.devices.size();
    }
}
