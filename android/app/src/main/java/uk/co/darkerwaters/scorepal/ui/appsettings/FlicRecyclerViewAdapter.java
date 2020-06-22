package uk.co.darkerwaters.scorepal.ui.appsettings;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import io.flic.flic2libandroid.Flic2Button;
import io.flic.flic2libandroid.Flic2ButtonListener;
import io.flic.flic2libandroid.Flic2Manager;
import uk.co.darkerwaters.scorepal.R;

public class FlicRecyclerViewAdapter extends RecyclerView.Adapter<FlicRecyclerViewAdapter.FlicViewHolder> {


    static class ButtonData {
        Flic2Button button;
        FlicViewHolder holder;
        boolean isDown;
        Flic2ButtonListener listener;

        public ButtonData(Flic2Button button) {
            this.button = button;
        }

        int getShapeColor(Context context) {
            switch (button.getConnectionState()) {
                case Flic2Button.CONNECTION_STATE_CONNECTING:
                    return context.getColor(R.color.secondaryLightColor);
                case Flic2Button.CONNECTION_STATE_CONNECTED_STARTING:
                    return context.getColor(R.color.secondaryColor);
                case Flic2Button.CONNECTION_STATE_CONNECTED_READY:
                    return isDown ? context.getColor(R.color.primaryDarkColor) : context.getColor(R.color.primaryColor);
                default:
                    return Color.BLACK;
            }
        }
    }

    private ArrayList<ButtonData> dataSet = new ArrayList<>();
    private Context context = null;

    static class FlicViewHolder extends RecyclerView.ViewHolder {
        ButtonData buttonData;
        public View viewParent;
        public TextView bdaddrTxt;
        public Button connectBtn;
        public Button removeBtn;
        public LinearLayout circle;
        public FlicViewHolder(View itemView) {
            super(itemView);
            viewParent = itemView;
            bdaddrTxt = itemView.findViewById(R.id.bdaddr);
            connectBtn = itemView.findViewById(R.id.button_connect);
            removeBtn = itemView.findViewById(R.id.button_remove);
            circle = itemView.findViewById(R.id.circle);
        }
    }

    @Override
    public FlicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_flic2, parent, false);
        context = parent.getContext();
        return new FlicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final FlicViewHolder holder, int position) {
        ButtonData buttonData = dataSet.get(position);
        holder.buttonData = buttonData;
        holder.buttonData.holder = holder;
        String name = buttonData.button.getName();
        if (null != name && !name.isEmpty()) {
            holder.bdaddrTxt.setText(name);
        }
        else {
            holder.bdaddrTxt.setText(buttonData.button.getBdAddr());
        }
        holder.connectBtn.setText(buttonData.button.getConnectionState() == Flic2Button.CONNECTION_STATE_DISCONNECTED ? "Connect" : "Disconnect");
        holder.circle.getBackground().setColorFilter(new PorterDuffColorFilter(holder.buttonData.getShapeColor(context), PorterDuff.Mode.SRC_ATOP));
        holder.connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.buttonData.button.getConnectionState() == Flic2Button.CONNECTION_STATE_DISCONNECTED) {
                    holder.buttonData.button.connect();
                    holder.connectBtn.setText("Disconnect");
                } else {
                    holder.buttonData.button.disconnectOrAbortPendingConnection();
                    holder.connectBtn.setText("Connect");
                }
                holder.circle.getBackground().setColorFilter(new PorterDuffColorFilter(holder.buttonData.getShapeColor(context), PorterDuff.Mode.SRC_ATOP));
            }
        });
        holder.removeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Flic2Manager.getInstance().forgetButton(holder.buttonData.button);
            }
        });
    }

    public void clearAllButtons() {
        this.dataSet.clear();
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public void onDestroy() {
        for (ButtonData data : dataSet) {
            data.button.removeListener(data.listener);
        }
    }

    public void addButton(Flic2Button button) {
        final ButtonData buttonData = new ButtonData(button);

        buttonData.listener = new Flic2ButtonListener() {
            FlicViewHolder getHolder() {
                if (buttonData.holder != null && buttonData.holder.buttonData == buttonData) {
                    return buttonData.holder;
                }
                return null;
            }

            private void updateColor() {
                FlicViewHolder holder = getHolder();
                if (holder != null) {
                    holder.circle.getBackground().setColorFilter(new PorterDuffColorFilter(holder.buttonData.getShapeColor(context), PorterDuff.Mode.SRC_ATOP));
                }
            }

            @Override
            public void onButtonUpOrDown(Flic2Button button, boolean wasQueued, boolean lastQueued, long timestamp, boolean isUp, boolean isDown) {
                buttonData.isDown = isDown;
                updateColor();
            }

            @Override
            public void onConnect(Flic2Button button) {
                updateColor();
            }

            @Override
            public void onReady(Flic2Button button, long timestamp) {
                updateColor();
            }

            @Override
            public void onDisconnect(Flic2Button button) {
                updateColor();
            }

            @Override
            public void onUnpaired(Flic2Button button) {
                int index = -1;
                for (int i = 0; i < dataSet.size(); i++) {
                    if (dataSet.get(i).button == button) {
                        index = i;
                        break;
                    }
                }
                if (index != -1) {
                    dataSet.remove(index);
                    notifyItemRemoved(index);
                }
            }
        };
        button.addListener(buttonData.listener);

        dataSet.add(buttonData);
        notifyItemInserted(dataSet.size() - 1);
    }
}
