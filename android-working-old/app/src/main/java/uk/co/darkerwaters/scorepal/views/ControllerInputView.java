package uk.co.darkerwaters.scorepal.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.controllers.Controller;

public class ControllerInputView extends View implements Controller.ControllerListener {

    private static final long K_CLEAR_INPUT_DELAY = 1000;

    private final List<Controller.KeyPress> keyPressesToDraw;
    private Paint dotPaint;
    private Paint textPaint;
    private String actionText;

    public ControllerInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // initialise to draw nothing
        this.keyPressesToDraw = new ArrayList<>();
        this.dotPaint = null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // paint the key presses to draw here
        float height = getHeight() - getPaddingTop() - getPaddingBottom();
        float dotRadius = height * 0.5f;
        float gap = (dotRadius * 1.2f) + getPaddingLeft();
        float dashRadius = dotRadius * 2.2f;

        if (null == dotPaint) {
            this.dotPaint = new Paint();
            this.dotPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            this.dotPaint.setColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
            this.dotPaint.setAntiAlias(true);
            this.dotPaint.setAlpha(150);
        }
        if (null == textPaint) {
            this.textPaint = new Paint();
            this.textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            this.textPaint.setTextSize(height * 0.8f);
            this.textPaint.setStrokeWidth(2f);
            this.textPaint.setTextAlign(Paint.Align.LEFT);
            this.textPaint.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
            this.textPaint.setAntiAlias(true);
            this.textPaint.setAlpha(150);
        }

        float x = gap;
        synchronized (this.keyPressesToDraw) {
            for (Controller.KeyPress keyPress : this.keyPressesToDraw) {
                switch(keyPress) {
                    case Short:
                        canvas.drawCircle(x, dotRadius, dotRadius, this.dotPaint);
                        x += dotRadius;
                        break;
                    case Long:
                        canvas.drawOval(x - dotRadius, 0, x + dashRadius + dotRadius, height, this.dotPaint);
                        x += dashRadius + dotRadius;
                        break;

                }
                // move the gap on
                x += gap;
            }
            if (null != this.actionText && !actionText.isEmpty()) {
                canvas.drawText(this.actionText, gap, height * 0.9f, this.textPaint);
            }
        }
    }

    public void addController(Controller controller) {
        // add us as a listener to this controller
        controller.addListener(this);
    }

    public void removeController(Controller controller) {
        // remove us as a listener to this controller
        controller.removeListener(this);
    }

    @Override
    public void onControllerInput(Controller.ControllerAction action) {
        // set the action text to show on this view
        setActionText(action.toString(getContext()));
    }

    private void setActionText(String text) {
        // not really interested in the action, other things are
        synchronized (this.keyPressesToDraw) {
            this.actionText = text;
        }
        if (null != text && !text.isEmpty()) {
            // there is data to show now, we will invalidate to show it
            // but after a second we want to delete it
            new Handler(getContext().getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    setActionText("");
                    invalidate();
                }
            }, K_CLEAR_INPUT_DELAY);
        }
        else {
            // draw this new data on this view
            new Handler(getContext().getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    invalidate();
                }
            });
        }
    }

    @Override
    public void onControllerKeyPress(Controller.KeyPress[] keyPresses) {
        // more interested in the raw data really
        synchronized (this.keyPressesToDraw) {
            if (keyPresses == null) {
                this.keyPressesToDraw.clear();
            }
            else {
                // add them all to the list to draw
                this.keyPressesToDraw.addAll(Arrays.asList(keyPresses));
            }
        }
        // draw this new data on this view
        new Handler(getContext().getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        });
    }
}
