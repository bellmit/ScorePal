package uk.co.darkerwaters.scorepal.score.points;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;

import uk.co.darkerwaters.scorepal.players.Team;
import uk.co.darkerwaters.scorepal.score.base.Point;
import uk.co.darkerwaters.scorepal.score.Score;

public class PointsScore extends Score<PointsMatchSettings> {

    private static final int K_POINTS_LEVEL = 1;

    private boolean isTwoPointsAheadRequired;
    private int pointsToChangeEnds;
    private int pointsToChangeServer;

    PointsScore(Team[] startingTeams, PointsMatchSettings startingSettings) {
        super(startingTeams, startingSettings, K_POINTS_LEVEL);
        // remember our goal here from the settings
        setScoreGoal(startingSettings.getScoreGoal());
    }

    @Override
    protected void resetScore(Team[] startingTeams, PointsMatchSettings startingSettings) {
        // let the base reset
        super.resetScore(startingTeams, startingSettings);
        // and do ours
        this.isTwoPointsAheadRequired = startingSettings.isTwoPointsRequired();
        this.pointsToChangeEnds = startingSettings.getPointsToChangeEnds();
        this.pointsToChangeServer = startingSettings.getPointsToChangeServer();
    }

    @Override
    public void serialiseToJson(Context context, JSONArray dataArray) throws JSONException {
        // do the base
        super.serialiseToJson(context, dataArray);
        // add our data to this array
        dataArray.put(this.isTwoPointsAheadRequired);
        dataArray.put(this.pointsToChangeEnds);
        dataArray.put(this.pointsToChangeServer);
    }

    @Override
    public void deserialiseFromJson(Context context, int version, JSONArray dataArray) throws JSONException {
        // do the base
        super.deserialiseFromJson(context, version, dataArray);
        // get our data from this array
        switch (version) {
            case 1:
                // load version 1 data
                this.isTwoPointsAheadRequired = dataArray.getBoolean(0);
                dataArray.remove(0);

                this.pointsToChangeEnds = dataArray.getInt(0);
                dataArray.remove(0);

                this.pointsToChangeServer = dataArray.getInt(0);
                dataArray.remove(0);
                break;
        }
    }

    private int getPlayedPoints() {
        int playedPoints = 0;
        for (Team team : getTeams()) {
            playedPoints += getPoint(0, team);
        }
        return playedPoints;
    }

    public Point getDisplayPoint(Team team) {
        return super.getDisplayPoint(0, team);
    }

    public int getPoints(Team team) {
        return super.getPoint(0, team);
    }

    @Override
    public int incrementPoint(Team team) {
        // add one to the point already stored
        int point = super.incrementPoint(team);

        int pointsToPlayTo = getScoreGoal();
        if (pointsToPlayTo <= 0 || point < pointsToPlayTo) {
            // they have not finished, could we swap ends / servers?
            // play a bit like a tie-break, after the first, and subsequent two points,
            // we have to change servers
            int playedPoints = getPlayedPoints();
            if (isNewServer(playedPoints)) {
                // we are at point 1, 3, 5, 7 etc - change server
                changeServer();
            }
            // also change ends every 6 points
            if (isNewEnd(playedPoints)) {
                // the set ended with
                changeEnds();
            }
        }
        return point;
    }

    public boolean isNewEnd() {
        return this.isNewEnd(getPlayedPoints());
    }

    private boolean isNewEnd(int playedPoints) {
        return playedPoints % this.pointsToChangeEnds == 0;
    }

    public boolean isNewServer() {
        return this.isNewServer(getPlayedPoints());
    }

    private boolean isNewServer(int playedPoints) {
        return (playedPoints - 1) % this.pointsToChangeServer == 0;
    }

    @Override
    public boolean isMatchOver() {
        boolean isMatchOver = false;
        int pointsToPlayTo = getScoreGoal();
        if (pointsToPlayTo > 0) {
            // return if a player has reached the points to play to
            for (Team team : getTeams()) {
                // get the point for each team playing
                int teamPoint = getPoint(0, team);
                if (teamPoint >= pointsToPlayTo) {
                    // someone has achieved the score required, is this enough?
                    int difference = teamPoint - getPoint(0, getOtherTeam(team));
                    if (!this.isTwoPointsAheadRequired || difference >= 2) {
                        // either we don't require 2 ahead, or we are 2 ahead
                        isMatchOver = true;
                    }
                }
            }
        }
        return isMatchOver;
    }
}
