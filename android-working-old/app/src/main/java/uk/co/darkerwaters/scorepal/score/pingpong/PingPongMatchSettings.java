package uk.co.darkerwaters.scorepal.score.pingpong;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;

import uk.co.darkerwaters.scorepal.score.MatchSettings;
import uk.co.darkerwaters.scorepal.score.base.Sport;

public class PingPongMatchSettings extends MatchSettings<PingPongMatch> {

    public final static int K_MAX_ROUNDS = 9;
    public final static int K_MIN_ROUNDS = 1;

    public final static int K_DEFAULT_POINTS = 11;
    public final static int K_DEFAULT_ROUNDS = 3;

    public final static int K_DEFAULT_EXP_POINTS = 18;
    public final static int K_DEFAULT_EXP_MINUTES = 10;
    public final static boolean K_DEFAULT_EXP_ENABLED = true;

    private int pointsInRound;
    private int expediteSystemPoints;
    private int expediteSystemMinutes;
    private boolean isExpediteSystemEnabled;

    public PingPongMatchSettings(Context context) {
        super(Sport.PING_PONG);
    }

    @Override
    public void resetSettings() {
        // let the base
        super.resetSettings();
        // and set ours
        this.pointsInRound = K_DEFAULT_POINTS;
        this.expediteSystemMinutes = K_DEFAULT_EXP_MINUTES;
        this.expediteSystemPoints = K_DEFAULT_EXP_POINTS;
        this.isExpediteSystemEnabled = K_DEFAULT_EXP_ENABLED;
        // the rounds are the score goal
        setScoreGoal(K_DEFAULT_ROUNDS);
    }

    public PingPongMatch createMatch() {
        return new PingPongMatch(this);
    }

    @Override
    public void serialiseToJson(Context context, JSONArray dataArray) throws JSONException {
        // let the base do it's thing
        super.serialiseToJson(context, dataArray);
        // add our data
        dataArray.put(this.pointsInRound);
        dataArray.put(this.expediteSystemPoints);
        dataArray.put(this.expediteSystemMinutes);
        dataArray.put(this.isExpediteSystemEnabled);
    }

    @Override
    public MatchSettings deserialiseFromJson(Context context, int version, JSONArray dataArray) throws JSONException {
        // let the base do it's thing now
        MatchSettings toReturn = super.deserialiseFromJson(context, version, dataArray);
        // pull our data off the top
        switch (version) {
            case 1:
                this.pointsInRound = dataArray.getInt(0);
                dataArray.remove(0);
                this.expediteSystemPoints = dataArray.getInt(0);
                dataArray.remove(0);
                this.expediteSystemMinutes = dataArray.getInt(0);
                dataArray.remove(0);
                this.isExpediteSystemEnabled = dataArray.getBoolean(0);
                dataArray.remove(0);
                break;
        }
        return toReturn;
    }

    public int getPointsInRound() {
        return this.pointsInRound;
    }

    public int getDecidingPoint() {
        return this.pointsInRound - 1;
    }

    public int getRoundsInMatch() {
        return getScoreGoal();
    }

    public void setPointsInRound(int points) {
        this.pointsInRound = points;
    }

    public void setRoundsInMatch(int rounds) { setScoreGoal(rounds); }
    
    public boolean isExpediteSystemEnabled() {
        return this.isExpediteSystemEnabled;
    }

    public void setIsExpediteSystemEnabled(boolean isEnabled) {
        this.isExpediteSystemEnabled = isEnabled;
    }
    
    public int getExpediteSystemPoints() {
        return this.expediteSystemPoints;
    }
    
    public void setExpediteSystemPoints(int points) {
        this.expediteSystemPoints = points;
    }

    public int getExpediteSystemMinutes() {
        return this.expediteSystemMinutes;
    }

    public void setExpediteSystemMinutes(int minutes) {
        this.expediteSystemMinutes = minutes;
    }
}
