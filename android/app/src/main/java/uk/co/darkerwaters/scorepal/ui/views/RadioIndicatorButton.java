package uk.co.darkerwaters.scorepal.ui.views;

import android.content.Context;
import android.util.AttributeSet;

public class RadioIndicatorButton extends CheckableIndicatorButton {

    public RadioIndicatorButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RadioIndicatorButton(Context context) {
        this(context, (AttributeSet)null, 0);
    }

    public RadioIndicatorButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public boolean performClick() {
        return this.isChecked() ? false : super.performClick();
    }
}

