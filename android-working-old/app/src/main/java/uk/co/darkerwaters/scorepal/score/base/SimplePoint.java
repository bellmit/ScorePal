package uk.co.darkerwaters.scorepal.score.base;

import android.content.Context;

import uk.co.darkerwaters.scorepal.R;

public class SimplePoint implements Point {
    private final int value;

    public SimplePoint(int value) {
        this.value = value;
    }

    @Override
    public int val() {
        return this.value;
    }

    @Override
    public String displayString(Context context) {
        return Integer.toString(this.value);
    }

    @Override
    public String speakString(Context context) {
        if (null != context && this.value == 0) {
            return context.getString(R.string.speak_zero);
        } else {
            return Integer.toString(this.value);
        }
    }

    @Override
    public String speakString(Context context, int number) {
        if (null != context && this.value == 0 && number != 1) {
            return context.getString(R.string.speak_zeros);
        }
        else {
            return speakString(context);
        }
    }

    @Override
    public String speakAllString(Context context) {
        return this.value + " " + context.getString(R.string.speak_all);
    }
}
