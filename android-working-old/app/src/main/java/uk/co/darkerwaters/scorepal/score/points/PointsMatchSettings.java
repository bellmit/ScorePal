package uk.co.darkerwaters.scorepal.score.points;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;

import uk.co.darkerwaters.scorepal.score.MatchSettings;
import uk.co.darkerwaters.scorepal.score.base.Sport;

public class PointsMatchSettings extends MatchSettings<PointsMatch> {

    public static final int K_DEFAULT_POINTS = 21;
    public static final int K_MIN_POINTS = 7;
    public static final int K_MAX_POINTS = 99;

    private boolean isTwoPointsRequired;
    private int pointsToChangeEnds;
    private int pointsToChangeServer;

    public PointsMatchSettings(Context context) {
        super(Sport.POINTS);
    }

    @Override
    public void resetSettings() {
        // let the base
        super.resetSettings();
        // and set our defaults here
        this.isTwoPointsRequired = true;
        this.pointsToChangeEnds = 6;
        this.pointsToChangeServer = 2;
        setScoreGoal(K_DEFAULT_POINTS);
    }

    public PointsMatch createMatch() {
        return new PointsMatch(this);
    }

    @Override
    public void serialiseToJson(Context context, JSONArray dataArray) throws JSONException {
        // and let the base do it's thing
        super.serialiseToJson(context, dataArray);
        // and add our data
        dataArray.put(this.isTwoPointsRequired);
        dataArray.put(this.pointsToChangeEnds);
        dataArray.put(this.pointsToChangeServer);
    }

    @Override
    public MatchSettings deserialiseFromJson(Context context, int version, JSONArray dataArray) throws JSONException {
        // let the base do it's thing now
        MatchSettings toReturn = super.deserialiseFromJson(context, version, dataArray);
        // and now pull our data off the top
        switch (version) {
            case 1:
                this.isTwoPointsRequired = dataArray.getBoolean(0);
                dataArray.remove(0);

                this.pointsToChangeEnds = dataArray.getInt(0);
                dataArray.remove(0);

                this.pointsToChangeServer = dataArray.getInt(0);
                dataArray.remove(0);

                break;
        }
        return toReturn;
    }

    public boolean isTwoPointsRequired() {
        return this.isTwoPointsRequired;
    }

    public void setIsTwoPointsAheadRequired(boolean isTwoAhead) {
        this.isTwoPointsRequired = isTwoAhead;
    }
    
    public int getPointsToChangeEnds() { return this.pointsToChangeEnds; }
    
    public void setPointsToChangeEnds(int points) { this.pointsToChangeEnds = points; }

    public int getPointsToChangeServer() { return this.pointsToChangeServer; }

    public void setPointsToChangeServer(int points) { this.pointsToChangeServer = points; }
}
