package uk.co.darkerwaters.scorepal.score.pingpong;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;

import uk.co.darkerwaters.scorepal.players.Player;
import uk.co.darkerwaters.scorepal.players.Team;
import uk.co.darkerwaters.scorepal.score.base.Point;
import uk.co.darkerwaters.scorepal.score.Score;
import uk.co.darkerwaters.scorepal.score.base.SimplePoint;

public class PingPongScore extends Score<PingPongMatchSettings> {

    public final static int LEVEL_POINT = 0;
    public final static int LEVEL_ROUND = 1;

    private final int POINTS_TO_WIN_ROUND = PingPongMatchSettings.K_DEFAULT_POINTS;
    private final int POINTS_AHEAD_IN_ROUND = 2;
    
    private final static int K_LEVELS = 2;

    private int pointsInRound = POINTS_TO_WIN_ROUND;
    private Player nextRoundServer;
    private boolean isExpediteSystemInEffect = false;

    PingPongScore(Team[] startingTeams, PingPongMatchSettings startingSettings) {
        super(startingTeams, startingSettings, K_LEVELS);
        // the score goal is the number of rounds to play
        setScoreGoal(startingSettings.getRoundsInMatch());
    }

    @Override
    protected void resetScore(Team[] startingTeams, PingPongMatchSettings startingSettings) {
        // let the base reset
        super.resetScore(startingTeams, startingSettings);
        // and reset our data
        this.nextRoundServer = getNextPlayer();
        // setup this score class from the settings we have
        this.pointsInRound = startingSettings.getPointsInRound();
    }

    @Override
    public void serialiseToJson(Context context, JSONArray dataArray) throws JSONException {
        // do the base
        super.serialiseToJson(context, dataArray);
        // add our data to this array
        dataArray.put(this.pointsInRound);
    }

    public void setExpediteSystemInEffect(boolean isInEffect) {
        this.isExpediteSystemInEffect = isInEffect;
    }

    public boolean isExpediteSystemInEffect() {
        return this.isExpediteSystemInEffect;
    }

    @Override
    public void deserialiseFromJson(Context context, int version, JSONArray dataArray) throws JSONException {
        // do the base
        super.deserialiseFromJson(context, version, dataArray);
        // and ours now
        switch (version) {
            case 1:
                // load version 1 data
                this.pointsInRound = dataArray.getInt(0); dataArray.remove(0);
                break;
        }
    }

    @Override
    public boolean isMatchOver() {
        boolean isMatchOver = false;
        int targetRounds = (int)((getScoreGoal() + 1f) / 2f);
        // return if a player has reached the number of rounds required (this is just over half)
        for (Team team : getTeams()) {
            if (getRounds(team) >= targetRounds) {
                // this team has reached the limit, match is over
                isMatchOver = true;
            }
        }
        return isMatchOver;
    }

    public int getPoints(Team team) {
        return super.getPoint(LEVEL_POINT, team);
    }

    public Point getDisplayPoint(Team team) {
        // just return the point as a string
        return new SimplePoint(getPoint(LEVEL_POINT, team));
    }

    public int getRounds(Team team) {
        return super.getPoint(LEVEL_ROUND, team);
    }

    public Point getDisplayRound(Team team) {
        // just return the point as a string
        return new SimplePoint(getPoint(LEVEL_ROUND, team));
    }

    int getPointsInRound() {
        return this.pointsInRound;
    }

    int getDecidingPoint() {
        return this.pointsInRound - 1;
    }

    @Override
    public int incrementPoint(Team team) {
        // add one to the point already stored
        int point = super.incrementPoint(team);
        Team otherTeam = getOtherTeam(team);
        int otherPoint = getPoints(otherTeam);
        int pointsAhead = point - otherPoint;
        // started playing, remember who should start the next round
        if (null == this.nextRoundServer) {
            this.nextRoundServer = getNextPlayer();
        }
        // has this team won the round with this new point addition (can't be the other)
        if (point >= this.pointsInRound && pointsAhead >= POINTS_AHEAD_IN_ROUND) {
            // we have enough points to win
            incrementRound(team);
        }
        else {
            int roundsPlayed = getRounds(getServingTeam()) + getRounds(getReceivingTeam());
            int totalPoints = point + otherPoint;
            if (roundsPlayed == getScoreGoal() - 1           // last game
                    && point == (this.pointsInRound - 1) / 2 // reached 5 points
                    && otherPoint < point) {                 // the other player didn't already
                // this is the last round, we want to change ends when someone makes 5 points
                changeEnds();
            }
            int decidingPoint = getDecidingPoint();
            // every two points we change server, or every point if expediting
            if (this.isExpediteSystemInEffect                                   // expedite system
                    || (point >= decidingPoint && otherPoint >= decidingPoint)  // both have at least 10 points
                    || totalPoints % 2 == 0) {                                  // served 2 already
                // change server
                changeServer();
            }
        }
        return point;
    }

    private void incrementRound(Team team) {
        // add one to the round already stored
        int point = super.getPoint(LEVEL_ROUND, team) + 1;
        // set this back on the score
        super.setPoint(LEVEL_ROUND, team, point);
        // also clear the points
        super.clearLevel(LEVEL_POINT);
        // no longer expediting, new round
        this.isExpediteSystemInEffect = false;

        if (false == isMatchOver()) {
            // every game we change ends
            changeEnds();
        }
        if (!this.nextRoundServer.equals(getServer())) {
            // the sever is not correct, change the server
            changeServer();
        }
        // remember the next one from here now we have changed
        this.nextRoundServer = getNextPlayer();
    }
}
