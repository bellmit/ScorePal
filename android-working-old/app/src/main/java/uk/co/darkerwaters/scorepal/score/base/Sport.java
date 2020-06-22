package uk.co.darkerwaters.scorepal.score.base;

import android.app.Activity;
import android.content.Context;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.BadmintonPlayActivity;
import uk.co.darkerwaters.scorepal.activities.BadmintonSetupActivity;
import uk.co.darkerwaters.scorepal.activities.BadmintonSummaryActivity;
import uk.co.darkerwaters.scorepal.activities.BluetoothMatchPlayActivity;
import uk.co.darkerwaters.scorepal.activities.BluetoothMatchSetupActivity;
import uk.co.darkerwaters.scorepal.activities.PingPongPlayActivity;
import uk.co.darkerwaters.scorepal.activities.PingPongSetupActivity;
import uk.co.darkerwaters.scorepal.activities.PingPongSummaryActivity;
import uk.co.darkerwaters.scorepal.activities.PointsPlayActivity;
import uk.co.darkerwaters.scorepal.activities.PointsSetupActivity;
import uk.co.darkerwaters.scorepal.activities.SquashPlayActivity;
import uk.co.darkerwaters.scorepal.activities.SquashSetupActivity;
import uk.co.darkerwaters.scorepal.activities.SquashSummaryActivity;
import uk.co.darkerwaters.scorepal.activities.TennisPlayActivity;
import uk.co.darkerwaters.scorepal.activities.TennisSetupActivity;
import uk.co.darkerwaters.scorepal.activities.PointsSummaryActivity;
import uk.co.darkerwaters.scorepal.activities.TennisSummaryActivity;
import uk.co.darkerwaters.scorepal.score.MatchSettings;
import uk.co.darkerwaters.scorepal.score.badminton.BadmintonMatchSettings;
import uk.co.darkerwaters.scorepal.score.bluetooth.BluetoothMatchSettings;
import uk.co.darkerwaters.scorepal.score.pingpong.PingPongMatchSettings;
import uk.co.darkerwaters.scorepal.score.points.PointsMatchSettings;
import uk.co.darkerwaters.scorepal.score.squash.SquashMatchSettings;
import uk.co.darkerwaters.scorepal.score.tennis.TennisMatchSettings;

public enum Sport implements MatchMessage.Param {
    POINTS(0
            , "images/points.jpg"
            , R.string.points_sport
            , R.string.pointsSubtitle
            , PointsSetupActivity.class
            , PointsPlayActivity.class
            , PointsSummaryActivity.class),
    TENNIS(1
            , "images/tennis.jpg"
            , R.string.tennis_sport
            , R.string.tennisSubtitle
            , TennisSetupActivity.class
            , TennisPlayActivity.class
            , TennisSummaryActivity.class),
    BLUETOOTH(2
            , "images/bluetooth_phone.jpg"
            , R.string.bluetooth_sport
            , R.string.bluetoothSubtitle
            , BluetoothMatchSetupActivity.class
            , BluetoothMatchPlayActivity.class
            , null),
    PING_PONG(3
            , "images/ping_pong.jpg"
            , R.string.ping_pong_sport
            , R.string.pingPongSubtitle
            , PingPongSetupActivity.class
            , PingPongPlayActivity.class
            , PingPongSummaryActivity.class),
    SQUASH(4
            , "images/squash.jpg"
            , R.string.squash_sport
            , R.string.squashSubtitle
            , SquashSetupActivity.class
            , SquashPlayActivity.class
            , SquashSummaryActivity.class),
    BADMINTON(5
            , "images/badminton.jpg"
            , R.string.badminton_sport
            , R.string.badmintonSubtitle
            , BadmintonSetupActivity.class
            , BadmintonPlayActivity.class
            , BadmintonSummaryActivity.class),
    ;

    public static final Sport DEFAULT = TENNIS;

    public final int value;
    public final String imageFilename;
    public final int titleResId;
    public final int subtitleResId;
    public final Class<? extends Activity> setupActivityClass;
    public final Class<? extends Activity> playActivityClass;
    public final Class<? extends Activity> summariseActivityClass;

    private String resolvedTitle = null;
    private String resolvedSubtitle = null;

    Sport(int value, String imageFilename,
          int titleResId, int subtitleResId,
          Class<? extends Activity> setupActivityClass,
          Class<? extends Activity> playActivityClass,
          Class<? extends Activity> summariseActivityClass) {
        this.value = value;
        this.imageFilename = imageFilename;
        this.titleResId = titleResId;
        this.subtitleResId = subtitleResId;
        this.setupActivityClass = setupActivityClass;
        this.playActivityClass = playActivityClass;
        this.summariseActivityClass = summariseActivityClass;
    }

    public MatchSettings createMatchSettings(Context context) throws ClassNotFoundException {
        MatchSettings settings;
        switch (this) {
            case TENNIS:
                // create the tennis settings
                settings = new TennisMatchSettings(context);
                break;
            case POINTS:
                // create the points settings
                settings = new PointsMatchSettings(context);
                break;
            case PING_PONG:
                // create the ping-pong settings
                settings = new PingPongMatchSettings(context);
                break;
            case SQUASH:
                // create the squash settings
                settings = new SquashMatchSettings(context);
                break;
            case BADMINTON:
                // create the badminton settings
                settings = new BadmintonMatchSettings(context);
                break;
            case BLUETOOTH:
                // create the bluetooth settings
                settings = new BluetoothMatchSettings(context);
                break;
            default:
                throw new ClassNotFoundException("Not implemented settings for sport " + toString());
        }
        return settings;
    }

    public static Sport[] GetSports() {
        return values();
    }

    public static void ResolveSportTitles(Context context) {
        // set the string members on all the sports from the context strings
        for (Sport sport : values()) {
            sport.getTitle(context);
            sport.getSubtitle(context);
        }
    }

    public static Sport from(int value) {
        for (Sport sport : values()) {
            if (sport.value == value) {
                return sport;
            }
        }
        // return the default
        return Sport.DEFAULT;
    }

    public String getTitle(Context context) {
        if (null == this.resolvedTitle) {
            this.resolvedTitle = context.getString(this.titleResId);
        }
        return this.resolvedTitle;
    }

    public String getSubtitle(Context context) {
        if (null == this.resolvedSubtitle) {
            this.resolvedSubtitle = context.getString(this.subtitleResId);
        }
        return this.resolvedSubtitle;
    }

    public static Sport from(String string, Context context) {
        for (Sport sport : values()) {
            if (sport.getTitle(context).equals(string)) {
                return sport;
            }
        }
        // return the default
        return Sport.DEFAULT;
    }

    @Override
    public String toString() {
        if (null != this.resolvedTitle) {
            return this.resolvedTitle;
        }
        else {
            return super.toString();
        }
    }

    @Override
    public String serialiseToString(Context context) throws Exception {
        // easy for this,  just use the title
        return getTitle(context);
    }

    @Override
    public MatchMessage.Param deserialiseFromString(Context context, int version, String string) throws Exception {
        // and return the sport that was the title
        return from(string, context);
    }
}
