package uk.co.darkerwaters.scorepal.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.CheckedTextView;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.scorepal.R;

public class CheckableIndicatorButton extends IndicatorButton implements Checkable {

    public interface OnCheckedChangeListener {
        void onCheckedChanged(View view, boolean isChecked);
    }

    private final List<OnCheckedChangeListener> listeners;
    private boolean checked;
    private static final int[] CHECKED_STATE_SET = new int[]{android.R.attr.state_checked};

    public CheckableIndicatorButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CheckableIndicatorButton( Context context) {
        this(context, (AttributeSet)null, 0);
    }

    public CheckableIndicatorButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.listeners = new ArrayList<>();

        TypedArray themeAttributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.IndicatorButton, 0, 0);
        this.checked = themeAttributes.getBoolean(R.styleable.CheckableIndicatorButton_android_checked, isChecked());

        this.setBackground(context.getDrawable(R.drawable.checkable_background_selector));
        CheckableImageView icon = this.findViewById(R.id.clickable_icon);
        icon.setImageTintList(ContextCompat.getColorStateList(context, R.color.checkable_icon_tint_selector));

        CheckableImageView indicator = this.findViewById(R.id.clickable_indicator);
        indicator.setImageTintList(ContextCompat.getColorStateList(context, R.color.checkable_icon_tint_selector));

        CheckedTextView textView = this.findViewById(R.id.clickable_label);
        textView.setTextColor(ContextCompat.getColorStateList(context, R.color.checkable_text_color_selector));
    }

    public void addOnCheckChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
        synchronized (this.listeners) {
            this.listeners.add(onCheckedChangeListener);
        }
    }

    public void removeOnCheckChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
        synchronized (this.listeners) {
            this.listeners.remove(onCheckedChangeListener);
        }
    }

    public boolean isChecked() {
        return this.checked;
    }

    public void toggle() {
        this.setChecked(!this.checked);
    }

    public void setChecked(boolean checked) {
        boolean isChanged = this.checked != checked;
        this.checked = checked;
        // we need to be sure all the children are set as we are set
        for (int i = 0; i < getChildCount(); ++i) {
            View child = getChildAt(i);
            if (child instanceof Checkable) {
                ((Checkable) child).setChecked(checked);
            }
        }
        // but only inform listeners if there is a change
        if (isChanged) {
            synchronized (this.listeners) {
                for (OnCheckedChangeListener listener : this.listeners) {
                    listener.onCheckedChanged(this, this.checked);
                }
            }
        }
        // and be sure we are always drawn correctly
        this.refreshDrawableState();
    }

    public boolean performClick() {
        this.toggle();
        return super.performClick();
    }

    protected int[] onCreateDrawableState(int extraSpace) {
        int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (this.isChecked()) {
            View.mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }
}
