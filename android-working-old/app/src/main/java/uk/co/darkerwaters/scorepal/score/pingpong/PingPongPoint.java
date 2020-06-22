package uk.co.darkerwaters.scorepal.score.pingpong;

import android.content.Context;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.score.base.Point;

public enum PingPongPoint implements Point {
    ZERO(0, R.string.display_zero, R.string.speak_zero, R.string.speak_zeros),
    ROUND(-1, R.string.display_round, R.string.speak_round, R.string.speak_rounds),
    POINT(-2, R.string.points, R.string.speak_point, R.string.speak_points),
    MATCH(-3, R.string.display_match, R.string.speak_match, R.string.speak_match),
    DEUCE(-4, R.string.display_deuce, R.string.speak_deuce, R.string.speak_deuce);

    private final int value;
    private final int displayStrId;
    private final int speakStrId;
    private final int speakStrPluralId;

    PingPongPoint(int value, int displayStrId, int speakStrId, int speakStrPluralId) {
        this.value = value;
        this.displayStrId = displayStrId;
        this.speakStrId = speakStrId;
        this.speakStrPluralId = speakStrPluralId;
    }
    @Override
    public int val() {
        return this.value;
    }
    @Override
    public String displayString(Context context) {
        if (null != context && 0 != this.displayStrId) {
            return context.getString(this.displayStrId);
        }
        else {
            return Integer.toString(this.value);
        }
    }
    @Override
    public String speakString(Context context) {
        if (null != context && 0 != this.speakStrId) {
            return context.getString(this.speakStrId);
        }
        else {
            return Integer.toString(this.value);
        }
    }
    @Override
    public String speakString(Context context, int number) {
        if (null != context) {
            if (number == 1 && 0 != this.speakStrId) {
                // in the singular
                return context.getString(this.speakStrId);
            }
            else if (0 != this.speakStrPluralId) {
                return context.getString(this.speakStrPluralId);
            }
            else {
                return Integer.toString(this.value);
            }
        }
        else {
            return Integer.toString(this.value);
        }
    }
    @Override
    public String speakAllString(Context context) {
        // just say the number then 'all'
        return speakString(context) + " " + context.getString(R.string.speak_all);
    }
}
