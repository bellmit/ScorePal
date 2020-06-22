package uk.co.darkerwaters.scorepal.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Score<S extends MatchSetup> {

    static final int INVALID_POINT = -1;
    static final int CLEAR_POINT = 0;

    protected static final int NO_TEAMS = MatchSetup.Team.values().length;

    private final int[][] points;
    private final List<int[]>[] pointsHistory;

    protected final boolean[] playingEnd = new boolean[NO_TEAMS];

    protected final ScoreState state;
    protected final S setup;
    protected MatchSetup.Team servingTeam;
    protected final MatchSetup.Player[] servingPlayers = new MatchSetup.Player[NO_TEAMS];

    private final ScoreHistory history;

    // default access, make the users go through the match class to access the score
    public Score(S setup, int pointsLevels) {
        this.state = new ScoreState();
        this.setup = setup;
        this.history = new ScoreHistory();
        // setup our members lists
        this.points = new int[pointsLevels][NO_TEAMS];
        this.pointsHistory = new List[pointsLevels];
        // make sure everything starts off the same each time
        resetScore();
    }

    protected void resetState() {
        // as we are about to affect a change, reset the change state
        this.state.reset();
    }

    protected void storeJSONData(JSONObject data) throws JSONException {
        // we only need to store the data that cannot be re-established from the settings
        // so basically just the actual score data
        data.put("pts", this.history.getPointHistoryAsString(this.setup.getStraightPointsToWin()));

        // we don't need to store the points at all (they are re-created from the points) but
        // other apps (a website maybe) might want to skip that and just have the resuts. So, to
        // be nice, we can put them in the JSON
        JSONArray levelsArray = new JSONArray();
        for (List<int[]> level : this.pointsHistory) {
            if (level == null) {
                levelsArray.put(new JSONArray());
            }
            else {
                // do the level by putting all the scores into an array (alternating each team)
                JSONArray scoreArray = new JSONArray();
                for (int[] score : level) {
                    for (int point : score) {
                        scoreArray.put(point);
                    }
                }
                // and put the array of this
                levelsArray.put(scoreArray);
            }
        }
        // and put this in the JSON
        data.put("scr", levelsArray);
    }

    protected void restoreFromJSON(JSONObject data, int version, RedoListener actionListener) throws JSONException {
        // all we did was store the raw score points, restore from this data
        StringBuilder pointsString = new StringBuilder(data.getString("pts"));
        this.history.clear();
        this.history.restorePointHistoryFromString(pointsString);
        // now we have the history restored, we can restore the score from it
        restorePointHistory(actionListener);
    }

    protected void resetScore() {
        this.servingTeam = setup.getFirstServingTeam();
        for (int i = 0; i < NO_TEAMS; ++i) {
            this.servingPlayers[i] = null;
        }
        // set the starting server though
        this.servingPlayers[servingTeam.index] = setup.getFirstServingPlayer(this.servingTeam);
        // and the ends we start at (1 and 0)
        this.playingEnd[0] = true;
        this.playingEnd[1] = false;
        // set all the points to zero
        for (int[] teamPoints : this.points) {
            for (int i = 0; i < teamPoints.length; ++i) {
                teamPoints[i] = 0;
            }
        }
        // clear the history lists
        for (int i = 0; i < this.pointsHistory.length; ++i) {
            this.pointsHistory[i] = null;
        }
        state.reset();
    }

    public int getLevels() {
        return this.points.length;
    }

    public MatchSetup.Team getServingTeam() {
        return servingTeam;
    }

    public MatchSetup.Player getServingPlayer() {
        MatchSetup.Player server = servingPlayers[servingTeam.index];
        if (null == server) {
            // there is no server recorded, the first server of the team is serving
            server = setup.getFirstServingPlayer(servingTeam);
        }
        // return the server, correcting any errors
        return correctServerErrors(server);
    }

    public MatchSetup.Team undoLastPoint(RedoListener listener) {
        // we want to remove the last point, this can be tricky as can effect an awful lot of things
        // like are we in a tie-break, serving end, number of sets games, etc. This is hard to undo
        // so instead we are being lazy and using the power of the device you are on. ie, reset
        // the score and re-populate based on the history
        ScoreHistory.HistoryValue historyValue = null;
        if (false == this.history.isEmpty()) {
            // pop the last point from the history
            historyValue = history.pop();
            // and restore the history that remains
            restorePointHistory(listener);
            // inform listeners of this
            state.addChange(ScoreState.ScoreChange.DECREMENT);
        }
        // return the team who's point was popped
        return historyValue != null ? historyValue.team : null;
    }

    interface RedoListener {
        void pointIncremented();
    }

    protected void restorePointHistory(RedoListener actionListener) {
        // reset the score
        resetScore();
        // and restore the rest, adding points will create a new history - so let's clear it
        ScoreHistory.HistoryValue[] historyValues = getWinnersHistory();
        history.clear();
        int lastState = 0;
        int lastLevel = 0;
        MatchSetup.Team lastTeam = null;
        ScoreHistory.HistoryValue value;
        for (int i = 0; i < historyValues.length; ++i) {
            // for every item (save the last one) increment the point
            value = historyValues[i];
            // increment the point - not adding to this history as we will be here a while with that
            incrementPoint(value.team, value.level);
            // store this last state
            lastState = state.getState();
            lastLevel = state.getLevelChanged();
            lastTeam = state.getTeamChanged();
            // this is actually a 'redo' so be sure to add this
            state.addChange(ScoreState.ScoreChange.INCREMENT_REDO);
            // inform listener of this action
            if (null != actionListener) {
                actionListener.pointIncremented();
            }
            // and reset this state
            resetState();
        }
        // restore the very last state of what we just changed
        state.setState(lastState, lastLevel, lastTeam);
    }

    protected ScoreHistory.HistoryValue[] getWinnersHistory() {
        // return the history as an array of which team won each point and the score at the time
        ScoreHistory.HistoryValue[] toReturn;
        toReturn = new ScoreHistory.HistoryValue[history.getSize()];
        for (int i = 0; i < history.getSize(); ++i) {
            toReturn[i] = history.get(i).copy();
        }
        return toReturn;
    }

    protected int incrementPoint(MatchSetup.Team team, int level) {
        // just add a point to the base level
        int point = setPoint(level, team, getPoint(level, team) + 1);
        // push this to our history stack with the latest state
        history.push(team, level, state.getState());
        // remember this change in state
        state.addChange(ScoreState.ScoreChange.INCREMENT, team, level);
        // and return the point
        return point;
    }

    protected void describeLastPoint(int newState, String historyDescription) {
        history.describe(newState, historyDescription);
    }

    protected int setPoint(int level, MatchSetup.Team team, int point) {
        this.points[level][team.index] = point;
        // remember this change in state
        state.addChange(ScoreState.ScoreChange.INCREMENT, team, level);
        // also remember the top level that each change performd
        history.measureLevel(level);
        return point;
    }

    protected void setServer(MatchSetup.Player server) {
        // store the serving team
        servingTeam = setup.getPlayerTeam(server);
        // and the player serving for that team
        servingPlayers[servingTeam.index] = server;
        // inform listeners of this
        state.addChange(ScoreState.ScoreChange.SERVER);
    }

    protected void clearLevel(int level) {
        // we just set the points for a level that is not the bottom, we want to store
        // the points that were the level below in the history and clear them here
        storeHistory(level, this.points[level]);
        // clear this data
        for (int i = 0; i < NO_TEAMS; ++i) {
            this.points[level][i] = CLEAR_POINT;
        }
    }

    protected int getPoint(int level, MatchSetup.Team team) {
        return this.points[level][team.index];
    }

    private void storeHistory(int level, int[] toStore) {
        List<int[]> points = this.pointsHistory[level];
        if (points == null) {
            points = new ArrayList<>();
            this.pointsHistory[level] = points;
        }
        // create the array of points we currently have and add to the list
        points.add(Arrays.copyOf(toStore, toStore.length));
    }

    protected List<int[]> getPointHistory(int level) {
        List<int[]> history = this.pointsHistory[level];
        if (null != history) {
            List<int[]> toReturn = new ArrayList<>();
            for (int[] points : history) {
                if (null != points) {
                    toReturn.add(Arrays.copyOf(points, points.length));
                }
            }
            return toReturn;
        }
        else {
            // no history for this
            return null;
        }
    }

    protected void changeServer() {
        // just set the next server to be the new server
        setServer(getNextServer());
    }

    protected void changeEnds() {
        // cycle each MatchSetup.Team's court position
        for (int i = 0; i < NO_TEAMS; ++i) {
            playingEnd[i] = !playingEnd[i];
        }
        // inform listeners of this
        state.addChange(ScoreState.ScoreChange.ENDS);
    }

    protected abstract int getScoreGoal();

    public abstract boolean isMatchOver();

    public abstract boolean isTeamServerChangeAllowed();

    protected MatchSetup.Team getWinner(int level) {
        int topTeam = 0;
        int[] finalScore = null;
        while (level >= 0) {
            // check the final score at the this level
            List<int[]> pointHistory = getPointHistory(level);
            if (null != pointHistory) {
                // the final score is the last point in the list of historic values
                finalScore = pointHistory.get(pointHistory.size() - 1);
            } else {
                // there is no history at this level, use what is current instead
                finalScore = new int[] {
                        getPoint(level, MatchSetup.Team.T_ONE),
                        getPoint(level, MatchSetup.Team.T_TWO)
                };
            }
            if (null != finalScore && finalScore[0] != finalScore[1]) {
                // there is a difference here, this can be used to determine the winner
                break;
            }
            // if here then at this level we are drawing, or there is no score, check lower down.
            --level;
        }
        if (null == finalScore) {
            // need to just use the points at the lowest level as there is no history
            finalScore = this.points[0];
        }
        if (null != finalScore) {
            int topPoints = INVALID_POINT;
            for (int i = 0; i < finalScore.length; ++i) {
                if (finalScore[i] > topPoints) {
                    // this is the top MatchSetup.Team
                    topPoints = finalScore[i];
                    topTeam = i;
                }
            }
        }
        return MatchSetup.Team.values()[topTeam];
    }

    protected MatchSetup.Player getNextServer() {
        // the current server must yield now to the new one
        // find the team that is serving at the moment
        MatchSetup.Player newServer = getServingPlayer();
        if (null != servingTeam) {
            // change team, and not the player that was last serving
            MatchSetup.Team otherTeam = setup.getOtherTeam(servingTeam);
            if (setup.getType() == MatchSetup.MatchType.DOUBLES) {
                // we are playing doubles, cycle the server in the team
                newServer = servingPlayers[otherTeam.index];
                if (null != newServer) {
                    // this new server is the current server for the team, cycle this
                    newServer = setup.getOtherPlayer(newServer);
                }
                else {
                    // there is no server who has served yet - use the first one
                    newServer = setup.getFirstServingPlayer(otherTeam);
                }
            }
            else {
                // just use the server of the other team
                newServer = servingPlayers[otherTeam.index];
                if (null == newServer) {
                    // there isn't one served yet - just use the first
                    newServer = setup.getFirstServingPlayer(otherTeam);
                }
            }
        }
        // return the server - correcting any errors
        return correctServerErrors(newServer);
    }

    private MatchSetup.Player correctServerErrors(MatchSetup.Player server) {
        if (setup.getType() == MatchSetup.MatchType.SINGLES) {
            // we are playing a singles match, fiddling with the servers can see us letting
            // the partner server, correct this here
            switch (server) {
                case P_ONE:
                case PT_ONE:
                    server = MatchSetup.Player.P_ONE;
                    break;
                case P_TWO:
                case PT_TWO:
                    server = MatchSetup.Player.P_TWO;
                    break;
            }
        }
        // return this corrected server
        return server;
    }

    protected int getPointsTotal(int level, MatchSetup.Team team) {
        // add all the points for this team
        int total = getPoint(level, team);
        List<int[]> history = getPointHistory(level);
        if (null != history) {
            // add all this history to the total
            for (int[] points : history) {
                total += points[team.index];
            }
        }
        return total;
    }
}
