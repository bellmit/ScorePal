package uk.co.darkerwaters.scorepal.activities.handlers;

import android.content.Context;
import android.support.v4.view.ActionProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;

import uk.co.darkerwaters.scorepal.R;

public class ControllerSliderHandler extends ActionProvider {

    private final Context parent;

    public ControllerSliderHandler(Context context) {
        super(context);
        this.parent = context;
    }

    @Override
    public View onCreateActionView() {
        // Inflate the action provider to be shown on the action bar.
        LayoutInflater layoutInflater = LayoutInflater.from(parent);
        return layoutInflater.inflate(R.layout.layout_controller_slider, null);
    }

    public void setOnClickListener(View parentView, SeekBar.OnSeekBarChangeListener listener) {
        // get the control
        SeekBar control = parentView.findViewById(R.id.seekBar);
        control.setOnSeekBarChangeListener(listener);
    }

    public void setSliderPosition(View parentView, int max, int position) {
        SeekBar control = parentView.findViewById(R.id.seekBar);
        //control.setMaxWidth(Integer.MAX_VALUE);
        control.setMax(max);
        control.setProgress(position == -1 ? max : position);
    }
}