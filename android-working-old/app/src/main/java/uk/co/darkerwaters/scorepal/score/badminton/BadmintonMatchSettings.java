package uk.co.darkerwaters.scorepal.score.badminton;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;

import uk.co.darkerwaters.scorepal.score.MatchSettings;
import uk.co.darkerwaters.scorepal.score.base.Sport;

public class BadmintonMatchSettings extends MatchSettings<BadmintonMatch> {

    public final static int K_MAX_GAMES = 5;
    public final static int K_MIN_GAMES = 1;

    public final static int[] K_POINTS_OPTONS= new int[] {11, 15, 21};
    public final static int[] K_DECIDERS_OPTIONS = new int[] {19, 25, 29};
    public final static int[] K_DECIDERS_MIN = new int[] {11, 15, 21};
    public final static int[] K_DECIDERS_MAX = new int[] {25, 31, 39};

    public final static int K_DEFAULT_GAMES = 3;
    public final static int K_DEFAULT_POINTS = K_POINTS_OPTONS[2];
    public final static int K_DEFAULT_DECIDER = K_DECIDERS_OPTIONS[2];

    private int pointsInGame;
    private int decidingPoint;

    public BadmintonMatchSettings(Context context) {
        super(Sport.BADMINTON);
    }

    @Override
    public void resetSettings() {
        // let the base
        super.resetSettings();
        // and set ours
        this.pointsInGame = K_DEFAULT_POINTS;
        this.decidingPoint = K_DEFAULT_DECIDER;
        // the games are the score goal
        setScoreGoal(K_DEFAULT_GAMES);
    }

    public BadmintonMatch createMatch() {
        return new BadmintonMatch(this);
    }

    @Override
    public void serialiseToJson(Context context, JSONArray dataArray) throws JSONException {
        // let the base do it's thing
        super.serialiseToJson(context, dataArray);
        // add our data
        dataArray.put(this.pointsInGame);
        dataArray.put(this.decidingPoint);
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
                this.decidingPoint = dataArray.getInt(0);
                dataArray.remove(0);
                break;
        }
        return toReturn;
    }

    public int getPointsInGame() {
        return this.pointsInGame;
    }

    public int getPointsInGameIndex() {
        for (int i = 0; i < K_POINTS_OPTONS.length; ++i) {
            if (K_POINTS_OPTONS[i] == this.pointsInGame) {
                return i;
            }
        }
        // not in the list
        return -1;
    }

    public int getDefaultDecidingPoint() {
        int index = getPointsInGameIndex();
        if (index >= 0 && index < K_DECIDERS_OPTIONS.length) {
            return K_DECIDERS_OPTIONS[index];
        }
        // if here then we didn't find the games
        return this.pointsInGame;
    }

    public int getMinDecidingPoint() {
        int index = getPointsInGameIndex();
        if (index >= 0 && index < K_DECIDERS_MIN.length) {
            return K_DECIDERS_MIN[index];
        }
        // if here then we didn't find the games
        return this.pointsInGame;
    }

    public int getMaxDecidingPoint() {
        int index = getPointsInGameIndex();
        if (index >= 0 && index < K_DECIDERS_MAX.length) {
            return K_DECIDERS_MAX[index];
        }
        // if here then we didn't find the games
        return this.pointsInGame;
    }

    public int getDecidingPoint() {
        return this.decidingPoint;
    }

    public int getGamesInMatch() {
        return getScoreGoal();
    }

    public void setPointsInGame(int points) {
        this.pointsInGame = points;
    }

    public void setDecidingPoint(int decidingPoint) { this.decidingPoint = decidingPoint; }

    public void setGamesInMatch(int games) { setScoreGoal(games); }
}
