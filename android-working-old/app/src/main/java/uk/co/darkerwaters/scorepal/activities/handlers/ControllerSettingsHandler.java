package uk.co.darkerwaters.scorepal.activities.handlers;

import android.content.Context;
import android.support.v4.view.ActionProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;

import uk.co.darkerwaters.scorepal.R;

public class ControllerSettingsHandler extends ActionProvider {

    private final Context parent;

    public ControllerSettingsHandler(Context context) {
        super(context);
        this.parent = context;
    }

    @Override
    public View onCreateActionView() {
        // Inflate the action provider to be shown on the action bar.
        LayoutInflater layoutInflater = LayoutInflater.from(parent);
        return layoutInflater.inflate(R.layout.layout_controller_settings, null);
    }

    public void setOnClickListener(View parentView, View.OnClickListener listener) {
        // get the button
        ImageButton button = parentView.findViewById(R.id.imageButton);
        button.setOnClickListener(listener);
    }
}