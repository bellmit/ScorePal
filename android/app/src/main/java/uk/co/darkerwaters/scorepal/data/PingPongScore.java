package uk.co.darkerwaters.scorepal.data;

import org.json.JSONException;
import org.json.JSONObject;

public class PingPongScore extends Score<PingPongSetup> {

    public final static int LEVEL_POINT = 0;
    public final static int LEVEL_ROUND = 1;

    private final int POINTS_AHEAD_IN_ROUND = 2;
    
    private final static int K_LEVELS = 2;

    boolean isExpediteSystemInEffect = false;
    private boolean isTeamServerChangeAllowed;
    private MatchSetup.Player nextRoundServer;

    PingPongScore(PingPongSetup setup) {
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
    protected int getScoreGoal() {
        return setup.getRoundsInMatch().num;
    }

    @Override
    protected void resetScore() {
        // let the base reset
        super.resetScore();
        // and reset our data
        this.isExpediteSystemInEffect = false;
        this.isTeamServerChangeAllowed = false;
        this.nextRoundServer = getNextServer();
    }

    public void setExpediteSystemInEffect(boolean isInEffect) {
        this.isExpediteSystemInEffect = isInEffect;
    }

    public boolean isExpediteSystemInEffect() {
        return this.isExpediteSystemInEffect;
    }

    @Override
    public boolean isMatchOver() {
        boolean isMatchOver = false;
        int targetRounds = (int)((getScoreGoal() + 1f) / 2f);
        // return if a player has reached the number of rounds required (this is just over half)
        for (int i = 0; i < NO_TEAMS; ++i) {
            if (getRounds(MatchSetup.Team.values()[i]) >= targetRounds) {
                // this team has reached the limit, match is over
                isMatchOver = true;
            }
        }
        return isMatchOver;
    }

    public int getPoints(MatchSetup.Team team) {
        return super.getPoint(LEVEL_POINT, team);
    }

    public int getRounds(MatchSetup.Team team) {
        return super.getPoint(LEVEL_ROUND, team);
    }

    public boolean isTeamServerChangeAllowed() { return this.isTeamServerChangeAllowed; }

    @Override
    public int incrementPoint(MatchSetup.Team team, int level) {
        // do the work
        int point = super.incrementPoint(team, level);
        // but we have to handle things specially here
        switch (level) {
            case LEVEL_POINT:
                onPointIncremented(team, point);
                break;
            case LEVEL_ROUND:
                onRoundIncremented(team, point);
                break;
        }
        return point;
    }

    /**
     * only call this privately as a game is won by winning points to prevent it going in the history
     * as some user entry that they won the set
     * @param team is the team that has won the game
     */
    private void incrementRound(MatchSetup.Team team) {
        // add one to the rounds already stored
        int rounds = super.getPoint(LEVEL_ROUND, team) + 1;
        // set this back on the score
        super.setPoint(LEVEL_ROUND, team, rounds);
        // and handle this
        onRoundIncremented(team, rounds);
    }

    private int onPointIncremented(MatchSetup.Team team, int point) {
        MatchSetup.Team otherTeam = setup.getOtherTeam(team);
        int otherPoint = getPoints(otherTeam);
        int pointsAhead = point - otherPoint;
        // as soon as a point is played, you cannot change the server in the team
        isTeamServerChangeAllowed = false;
        // started playing, remember who should start the next round
        if (null == this.nextRoundServer) {
            this.nextRoundServer = getNextServer();
        }
        // has this team won the round with this new point addition (can't be the other)
        if (point >= setup.getPointsInRound().num && pointsAhead >= POINTS_AHEAD_IN_ROUND) {
            // we have enough points to win
            incrementRound(team);
        }
        else {
            int roundsPlayed = getRounds(getServingTeam()) + getRounds(setup.getOtherTeam(getServingTeam()));
            int totalPoints = point + otherPoint;
            if (roundsPlayed == getScoreGoal() - 1           // last game
                    && point == (setup.getPointsInRound().num - 1) / 2 // reached 5 points
                    && otherPoint < point) {                 // the other player didn't already
                // this is the last round, we want to change ends when someone makes 5 points
                changeEnds();
            }
            int decidingPoint = setup.getDecidingPoint();
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

    private void onRoundIncremented(MatchSetup.Team team, int rounds) {
        // clear the points
        super.clearLevel(LEVEL_POINT);
        // no longer expediting, new round
        this.isExpediteSystemInEffect = false;

        if (false == isMatchOver()) {
            // every game we change ends
            changeEnds();
        }
        if (!this.nextRoundServer.equals(getServingPlayer())) {
            // the sever is not correct, change the server
            setServer(this.nextRoundServer);
        }
        // remember the next one from here now we have changed
        this.nextRoundServer = getNextServer();
    }
}
