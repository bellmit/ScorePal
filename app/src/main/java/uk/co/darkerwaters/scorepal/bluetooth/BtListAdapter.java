package uk.co.darkerwaters.scorepal.bluetooth;

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

/**
 * Created by douglasbrain on 26/05/2017.
 */

public class BtListAdapter extends BaseAdapter {

    private Context mContext;

    private LayoutInflater mLayoutInflater;

    private ArrayList<BluetoothDevice> mEntries = new ArrayList<BluetoothDevice>();

    public BtListAdapter(Context context) {
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
            itemView = (RelativeLayout) mLayoutInflater.inflate(R.layout.bt_list_view_item, parent, false);

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
                titleText.setText(title);
                // check this - might it be a scorepal device?
                if (title.toLowerCase().contains(mContext.getString(R.string.bt_scorepal_device_title))) {
                    // set the nice image
                    imageView.setImageResource(R.drawable.scorepal);
                }
                String description = device.getAddress();
                if (description.trim().length() == 0) {
                    description = "Sorry, no address...";
                }
                descriptionText.setText(description);
                // is this device connected?
                if (BtManager.getManager().isDeviceConnected(device)) {
                    itemView.setBackgroundColor(Common.getThemeAccentColor(mContext));
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
        mEntries.add(device);
        notifyDataSetChanged();
    }
}