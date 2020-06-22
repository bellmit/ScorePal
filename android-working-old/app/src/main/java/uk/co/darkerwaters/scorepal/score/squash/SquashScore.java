package uk.co.darkerwaters.scorepal.score.squash;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;

import uk.co.darkerwaters.scorepal.players.Player;
import uk.co.darkerwaters.scorepal.players.Team;
import uk.co.darkerwaters.scorepal.score.Score;
import uk.co.darkerwaters.scorepal.score.base.Point;
import uk.co.darkerwaters.scorepal.score.base.SimplePoint;

public class SquashScore extends Score<SquashMatchSettings> {

    public final static int LEVEL_POINT = 0;
    public final static int LEVEL_GAME = 1;

    private final int POINTS_TO_WIN_GAME = SquashMatchSettings.K_DEFAULT_POINTS;
    private final int POINTS_AHEAD_IN_GAME = 2;
    
    private final static int K_LEVELS = 2;

    private int pointsInGame = POINTS_TO_WIN_GAME;

    SquashScore(Team[] startingTeams, SquashMatchSettings startingSettings) {
        super(startingTeams, startingSettings, K_LEVELS);
        // the score goal is the number of games to play
        setScoreGoal(startingSettings.getGamesInMatch());
    }

    @Override
    protected void resetScore(Team[] startingTeams, SquashMatchSettings startingSettings) {
        // let the base reset
        super.resetScore(startingTeams, startingSettings);
        // setup this score class from the settings we have
        this.pointsInGame = startingSettings.getPointsInGame();
    }

    @Override
    public void serialiseToJson(Context context, JSONArray dataArray) throws JSONException {
        // do the base
        super.serialiseToJson(context, dataArray);
        // add our data to this array
        dataArray.put(this.pointsInGame);
    }

    @Override
    public void deserialiseFromJson(Context context, int version, JSONArray dataArray) throws JSONException {
        // do the base
        super.deserialiseFromJson(context, version, dataArray);
        // and ours now
        switch (version) {
            case 1:
                // load version 1 data
                this.pointsInGame = dataArray.getInt(0); dataArray.remove(0);
                break;
        }
    }

    @Override
    public boolean isMatchOver() {
        boolean isMatchOver = false;
        int targetGames = (int)((getScoreGoal() + 1f) / 2f);
        // return if a player has reached the number of games required (this is just over half)
        for (Team team : getTeams()) {
            if (getGames(team) >= targetGames) {
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

    public int getGames(Team team) {
        return super.getPoint(LEVEL_GAME, team);
    }

    public Point getDisplayGame(Team team) {
        // just return the point as a string
        return new SimplePoint(getPoint(LEVEL_GAME, team));
    }

    @Override
    public int incrementPoint(Team team) {
        // add one to the point already stored
        int point = super.incrementPoint(team);
        Team otherTeam = getOtherTeam(team);
        int otherPoint = getPoints(otherTeam);
        int pointsAhead = point - otherPoint;
        // has this team won the game with this new point addition (can't be the other)
        if (point >= this.pointsInGame && pointsAhead >= POINTS_AHEAD_IN_GAME) {
            // we have enough points to win
            incrementGame(team);
        }
        if (team.equals(getServingTeam())) {
            // this is a win by the server, change ends
            changeEnds();
        }
        else {
            // the server just lost, change the server
            changeServer();
        }
        return point;
    }

    private void incrementGame(Team team) {
        // add one to the game already stored
        int point = super.getPoint(LEVEL_GAME, team) + 1;
        // set this back on the score
        super.setPoint(LEVEL_GAME, team, point);
        // also clear the points
        super.clearLevel(LEVEL_POINT);
    }
}
