package uk.co.darkerwaters.scorepal.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.CheckedTextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import uk.co.darkerwaters.scorepal.R;

public class IndicatorButton extends ConstraintLayout {

    public IndicatorButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IndicatorButton(Context context) {
        this(context, (AttributeSet)null, 0);
    }

    public IndicatorButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.layout_indicator_button, this, true);

        TypedArray themeAttrs = context.getTheme().obtainStyledAttributes(attrs, R.styleable.IndicatorButton, 0, 0);
        // set if enabled or not
        setEnabled(themeAttrs.getBoolean(R.styleable.IndicatorButton_android_enabled, isEnabled()));
        // set the icon
        CheckableImageView icon = findViewById(R.id.clickable_icon);
        CheckableImageView indicator = findViewById(R.id.clickable_indicator);
        icon.setImageDrawable(themeAttrs.getDrawable(R.styleable.IndicatorButton_icon));
        // set the text view
        CheckedTextView textView = findViewById(R.id.clickable_label);
        textView.setText(themeAttrs.getText(R.styleable.IndicatorButton_android_text));

        this.setBackground(context.getDrawable(R.drawable.background_selector));
        icon.setImageTintList(ContextCompat.getColorStateList(context, R.color.icon_tint_selector));
        indicator.setImageTintList(ContextCompat.getColorStateList(context, R.color.icon_tint_selector));
        textView.setTextColor(ContextCompat.getColorStateList(context, R.color.text_color_selector));

        this.setClickable(true);
        this.setFocusable(true);
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        for (int i = 0; i < getChildCount(); ++i) {
            getChildAt(i).setEnabled(enabled);
        }
    }
}

