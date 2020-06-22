package uk.co.darkerwaters.scorepal.points;

import android.content.Context;

import uk.co.darkerwaters.scorepal.R;

public enum TennisPoint implements Point {
    LOVE(0, R.string.display_love, R.string.speak_love, R.string.speak_love, R.string.speak_love_all),
    FIFTEEN(1, R.string.display_15, R.string.speak_15, R.string.speak_15, R.string.speak_15_all),
    THIRTY(2, R.string.display_30, R.string.speak_30, R.string.speak_30, R.string.speak_30_all),
    FORTY(3, R.string.display_40, R.string.speak_40, R.string.speak_40, R.string.speak_deuce),
    DEUCE(4, R.string.display_deuce, R.string.speak_deuce, R.string.speak_deuce, R.string.speak_deuce),
    ADVANTAGE(5, R.string.display_advantage, R.string.speak_advantage, R.string.speak_advantage, R.string.speak_advantage),
    GAME(6, R.string.display_game, R.string.speak_game, R.string.speak_games, 0),
    SET(7, R.string.display_set, R.string.speak_set, R.string.speak_sets, 0),
    MATCH(8, R.string.display_match, R.string.speak_match, R.string.speak_match, 0),
    POINT(6, R.string.points, R.string.speak_point, R.string.speak_points, 0);

    private final int value;
    private final int displayStrId;
    private final int speakStrId;
    private final int speakStrPluralId;
    private final int speakAllStrId;

    TennisPoint(int value, int displayStrId, int speakStrId, int speakStrPluralId, int speakAllStrId) {
        this.value = value;
        this.displayStrId = displayStrId;
        this.speakStrId = speakStrId;
        this.speakStrPluralId = speakStrPluralId;
        this.speakAllStrId = speakAllStrId;
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
        if (0 != this.speakAllStrId) {
            return context.getString(this.speakAllStrId);
        } else {
            // just say the number then 'all'
            return speakString(context) + " " + context.getString(R.string.speak_all);
        }
    }

    public static Point fromVal(int points) {
        for (TennisPoint point : TennisPoint.values()) {
            if (point.val() == points) {
                return point;
            }
        }
        // if here then we don't have a tennis point, return a simple number one
        return new SimplePoint(points);
    }
}
