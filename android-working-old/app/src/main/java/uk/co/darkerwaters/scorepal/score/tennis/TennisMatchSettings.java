package uk.co.darkerwaters.scorepal.score.tennis;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;

import uk.co.darkerwaters.scorepal.score.MatchSettings;
import uk.co.darkerwaters.scorepal.score.base.Sport;

public class TennisMatchSettings extends MatchSettings<TennisMatch> {

    private int finalSetTieTarget;
    private int gamesInSet;
    private boolean isDecidingPointOnDeuce;

    public TennisMatchSettings(Context context) {
        super(Sport.TENNIS);
    }

    @Override
    public void resetSettings() {
        // let the base
        super.resetSettings();
        // and set our settings
        this.finalSetTieTarget = -1;
        this.gamesInSet = 6;
        this.isDecidingPointOnDeuce = false;
        setScoreGoal(TennisSets.K_DEFAULT.val);
    }

    public TennisMatch createMatch() {
        return new TennisMatch(this);
    }

    @Override
    public void serialiseToJson(Context context, JSONArray dataArray) throws JSONException {
        // let the base do it's thing
        super.serialiseToJson(context, dataArray);
        // add our data
        dataArray.put(this.finalSetTieTarget);
        dataArray.put(this.gamesInSet);
        dataArray.put(this.isDecidingPointOnDeuce);
    }

    @Override
    public MatchSettings deserialiseFromJson(Context context, int version, JSONArray dataArray) throws JSONException {
        // let the base do it's thing now
        MatchSettings toReturn = super.deserialiseFromJson(context, version, dataArray);
        // pull our data off the top
        switch (version) {
            case 1:
                this.finalSetTieTarget = dataArray.getInt(0);
                dataArray.remove(0);

                this.gamesInSet = dataArray.getInt(0);
                dataArray.remove(0);

                this.isDecidingPointOnDeuce = dataArray.getBoolean(0);
                dataArray.remove(0);

                break;
        }
        return toReturn;
    }

    public int getFinalSetTieTarget() {
        return this.finalSetTieTarget;
    }

    public int getGamesInSet() {
        return this.gamesInSet;
    }

    public boolean isDecidingPointOnDeuce() {
        return this.isDecidingPointOnDeuce;
    }

    public void setFinalSetTieTarget(int target) {
        this.finalSetTieTarget = target;
    }

    public void setGamesInSet(int games) { this.gamesInSet = games; }

    public void setIsDecidingPointOnDeuce(boolean isDecidingPoint) { this.isDecidingPointOnDeuce = isDecidingPoint; }
}
