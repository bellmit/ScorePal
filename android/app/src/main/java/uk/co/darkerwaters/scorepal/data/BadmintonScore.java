package uk.co.darkerwaters.scorepal.data;

import org.json.JSONException;
import org.json.JSONObject;

public class BadmintonScore extends Score<BadmintonSetup> {

    public final static int LEVEL_POINT = 0;
    public final static int LEVEL_GAME = 1;

    private final int POINTS_AHEAD_IN_GAME = 2;

    private final static int K_LEVELS = 2;
    private boolean isTeamServerChangeAllowed;
    
    BadmintonScore(BadmintonSetup setup) {
        super(setup, K_LEVELS);
    }

    @Override
    protected void storeJSONData(JSONObject data) throws JSONException {
        super.storeJSONData(data);
        // store our data in this object too - only things that will not be recreated when
        // the score is replayed in this match - ie, very little
    }

    protected void restoreFromJSON(JSONObject data, int version, RedoListener listener) throws JSONException {
        super.restoreFromJSON(data, version, listener);
        // and get our data from this object that we stored here
    }

    @Override
    protected void resetScore() {
        // let the base reset
        super.resetScore();
        // and reset any stats we have here
        this.isTeamServerChangeAllowed = false;
    }

    @Override
    protected int getScoreGoal() {
        return setup.getGamesInMatch().num;
    }

    @Override
    public boolean isMatchOver() {
        boolean isMatchOver = false;
        int targetGames = (int)((getScoreGoal() + 1f) / 2f);
        // return if a player has reached the number of games required (this is just over half)
        for (int i = 0; i < NO_TEAMS; ++i) {
            if (getGames(MatchSetup.Team.values()[i]) >= targetGames) {
                // this team has reached the limit, match is over
                isMatchOver = true;
            }
        }
        return isMatchOver;
    }

    public int getPoints(MatchSetup.Team team) {
        return super.getPoint(LEVEL_POINT, team);
    }

    public int getGames(MatchSetup.Team team) {
        return super.getPoint(LEVEL_GAME, team);
    }

    public boolean isTeamServerChangeAllowed() { return this.isTeamServerChangeAllowed; }

    @Override
    public int incrementPoint(MatchSetup.Team team, int level) {
        // let the base do it's thing
        int point = super.incrementPoint(team, level);
        // but we have to handle things specially here
        switch (level) {
            case LEVEL_POINT:
                onPointIncremented(team, point);
                break;
            case LEVEL_GAME:
                onGameIncremented(team, point);
                break;
        }
        return point;
    }

    private int incrementGame(MatchSetup.Team team) {
        // add one to the game already stored
        int games = super.getPoint(LEVEL_GAME, team) + 1;
        // set this back on the score
        super.setPoint(LEVEL_GAME, team, games);
        // and perform actions on this
        onGameIncremented(team, games);
        // and return the games
        return games;
    }

    private void onPointIncremented(MatchSetup.Team team, int point) {
        MatchSetup.Team otherTeam = setup.getOtherTeam(team);
        int otherPoint = getPoints(otherTeam);
        int pointsAhead = point - otherPoint;
        int totalGames = getGames(team) + getGames(setup.getOtherTeam(team));
        // as soon as a point is played, you cannot change the server in the team
        isTeamServerChangeAllowed = false;

        int gameDecider = setup.getDecidingPoint().num;
        if (gameDecider > 0
                && point == gameDecider
                && otherPoint == gameDecider) {
            // we are at the deciding point
            state.addChange(ScoreState.ScoreChange.DECIDING_POINT);
        }
        if (!team.equals(getServingTeam())) {
            // the server just lost their point, change the server away from them
            changeServer();
        }
        // has this team won the game with this new point addition (can't be the other)
        if ((gameDecider > 0 && point > gameDecider)
                || (point >= setup.getPointsInGame().num && pointsAhead >= POINTS_AHEAD_IN_GAME)) {
            // we have enough points to win
            incrementGame(team);
            // do we change ends?
            if (totalGames != getScoreGoal() - 1) {
                // we are not in the final game, change ends when we win a game
                changeEnds();
            }
        }
        else if (totalGames == getScoreGoal() - 1) {
            // game not won, but we are in the last game
            // in the last game we change ends at half way
            if (otherPoint < point && point == (setup.getPointsInGame().num + 1) / 2) {
                // if 11 points to win then change when either player hits 6 ((11 + 1) / 2 == 6)
                changeEnds();
            }
        }
    }

    private void onGameIncremented(MatchSetup.Team team, int games) {
        // clear the points
        super.clearLevel(LEVEL_POINT);
    }
}
