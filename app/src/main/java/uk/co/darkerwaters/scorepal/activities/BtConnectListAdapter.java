package uk.co.darkerwaters.scorepal.activities;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Set;

import uk.co.darkerwaters.scorepal.Common;
import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.bluetooth.BtManager;

/**
 * Created by douglasbrain on 26/05/2017.
 */

public class BtConnectListAdapter extends BaseAdapter {

    private Context mContext;

    private LayoutInflater mLayoutInflater;

    private ArrayList<BluetoothDevice> mEntries = new ArrayList<BluetoothDevice>();

    public BtConnectListAdapter(Context context) {
        mContext = context;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        // return the size of th elist
        return mEntries.size();
    }

    @Override
    public Object getItem(int position) {
        if (position < mEntries.size()) {
            return mEntries.get(position);
        }
        else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RelativeLayout itemView;
        if (convertView == null) {
            itemView = (RelativeLayout) mLayoutInflater.inflate(R.layout.list_item_bt, parent, false);

        } else {
            itemView = (RelativeLayout) convertView;
        }

        ImageView imageView = (ImageView) itemView.findViewById(R.id.listImage);
        TextView titleText = (TextView) itemView.findViewById(R.id.listTitle);
        TextView descriptionText = (TextView) itemView.findViewById(R.id.listDescription);

        if (position < mEntries.size()) {
            BluetoothDevice device = mEntries.get(position);
            if (null != device) {
                String title = device.getName();
                if (title != null) {
                    titleText.setText(title);
                    // check this - might it be a scorepal device?
                    if (title.toLowerCase().contains(mContext.getString(R.string.bt_scorepal_device_title))) {
                        // set the nice image
                        imageView.setImageResource(R.drawable.scorepal);
                    }
                    else {
                        // set the nice image
                        imageView.setImageResource(R.drawable.bluetooth);
                    }
                }
                String description = device.getAddress();
                if (null != description) {
                    if (description.trim().length() == 0) {
                        description = "Sorry, no address...";
                    }
                    descriptionText.setText(description);
                    // is this device connected?
                    if (BtManager.getManager().isDeviceConnected(device)) {
                        itemView.setBackgroundColor(Common.getThemeAccentColor(mContext));
                    }
                    else {
                        itemView.setBackgroundColor(Common.getThemeBackgroundColor(mContext));
                    }
                }
            }
        }
        // return the expanded view
        return itemView;
    }

    public void upDateEntries(Set<BluetoothDevice> entries) {
        mEntries.clear();
        if (null != entries) {
            for (BluetoothDevice device : entries) {
                mEntries.add(device);
            }
        }
        notifyDataSetChanged();
    }

    public void add(BluetoothDevice device) {
        mEntries.add(0, device);
        notifyDataSetChanged();
    }
}