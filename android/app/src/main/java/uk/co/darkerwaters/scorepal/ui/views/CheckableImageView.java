package uk.co.darkerwaters.scorepal.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.ImageView;

public final class CheckableImageView extends androidx.appcompat.widget.AppCompatImageView implements Checkable {
    private boolean mChecked;
    private static final int[] CHECKED_STATE_SET = new int[]{android.R.attr.state_checked};

    public CheckableImageView(Context context) {
        super(context);
    }

    public CheckableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean isChecked() {
        return this.mChecked;
    }

    public void setChecked(boolean b) {
        if (b != this.mChecked) {
            this.mChecked = b;
            this.refreshDrawableState();
        }

    }

    public void toggle() {
        this.setChecked(!this.mChecked);
    }

    public int[] onCreateDrawableState(int extraSpace) {
        int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (this.isChecked()) {
            ImageView.mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }

    protected void drawableStateChanged() {
        super.drawableStateChanged();
        this.invalidate();
    }
}
