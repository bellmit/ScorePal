package uk.co.darkerwaters.scorepal.score.squash;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;

import uk.co.darkerwaters.scorepal.score.MatchSettings;
import uk.co.darkerwaters.scorepal.score.base.Sport;

public class SquashMatchSettings extends MatchSettings<SquashMatch> {

    public final static int K_MAX_GAMES = 5;
    public final static int K_MIN_GAMES = 1;

    public final static int K_DEFAULT_POINTS = 11;
    public final static int K_DEFAULT_GAMES = 5;

    private int pointsInGame;

    public SquashMatchSettings(Context context) {
        super(Sport.SQUASH);
    }

    @Override
    public void resetSettings() {
        // let the base
        super.resetSettings();
        // and set ours
        this.pointsInGame = K_DEFAULT_POINTS;
        // the games are the score goal
        setScoreGoal(K_DEFAULT_GAMES);
    }

    public SquashMatch createMatch() {
        return new SquashMatch(this);
    }

    @Override
    public void serialiseToJson(Context context, JSONArray dataArray) throws JSONException {
        // let the base do it's thing
        super.serialiseToJson(context, dataArray);
        // add our data
        dataArray.put(this.pointsInGame);
    }

    @Override
    public MatchSettings deserialiseFromJson(Context context, int version, JSONArray dataArray) throws JSONException {
        // let the base do it's thing now
        MatchSettings toReturn = super.deserialiseFromJson(context, version, dataArray);
        // pull our data off the top
        switch (version) {
            case 1:
                this.pointsInGame = dataArray.getInt(0);
                dataArray.remove(0);
                break;
        }
        return toReturn;
    }

    public int getPointsInGame() {
        return this.pointsInGame;
    }

    public int getGamesInMatch() {
        return getScoreGoal();
    }

    public void setPointsInGame(int points) {
        this.pointsInGame = points;
    }

    public void setGamesInMatch(int games) { setScoreGoal(games); }
}
