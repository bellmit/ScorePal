package uk.co.darkerwaters.scorepal.score;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.co.darkerwaters.scorepal.application.Log;
import uk.co.darkerwaters.scorepal.players.Player;
import uk.co.darkerwaters.scorepal.players.Team;
import uk.co.darkerwaters.scorepal.score.base.Point;
import uk.co.darkerwaters.scorepal.score.base.SimplePoint;

public class Score<S extends MatchSettings> {

    static final int INVALID_POINT = -1;
    static final int CLEAR_POINT = 0;

    private final Team[] teams;
    private final int[][] points;
    private final List<int[]>[] pointsHistory;
    private final Player[] players;

    private int scoreGoal = -1;
    private boolean isDoubles;

    private boolean isInformActive = true;

    interface ScoreListener {
        void onScoreChanged(ScoreChange type);
        void onScoreChanged(Team team, int level, int newPoint);
    }

    public enum ScoreChange {
        RESET, INCREMENT, POINTS_SET, SERVER, ENDS, GOAL, DECIDING_POINT, TIE_BREAK, BREAK_POINT_CONVERTED, BREAK_POINT
    }

    private final Set<ScoreListener> listeners;

    // default access, make the users go through the match class to access the score
    public Score(Team[] startingTeams, S startingSettings, int pointsLevels) {
        // initialise final empty arrays of teams and players
        this.teams = new Team[startingTeams.length];
        int playerCount = 0;
        for (Team startingTeam : startingTeams) {
            playerCount += startingTeam.getPlayers().length;
        }
        this.players = new Player[playerCount];
        // setup our members lists
        this.points = new int[pointsLevels][startingTeams.length];
        this.pointsHistory = new List[pointsLevels];
        this.listeners = new HashSet<>();
        // make sure everything starts off the same each time
        resetScore(startingTeams, startingSettings);
    }

    public void serialiseToJson(Context context, JSONArray dataArray) throws JSONException {
        // add our data to this array
        dataArray.put(this.scoreGoal);
    }

    public void deserialiseFromJson(Context context, int version, JSONArray dataArray) throws JSONException {
        switch (version) {
            case 1:
                // load version 1 data
                this.scoreGoal = dataArray.getInt(0); dataArray.remove(0);
                break;
        }
    }

    boolean addListener(ScoreListener listener) {
        synchronized (this.listeners) {
            return this.listeners.add(listener);
        }
    }

    boolean removeListener(ScoreListener listener) {
        synchronized (this.listeners) {
            return this.listeners.remove(listener);
        }
    }

    protected void silenceInformers(boolean isSilence) {
        this.isInformActive = !isSilence;
    }

    protected boolean isInformActive() {
        return this.isInformActive;
    }

    protected void informListeners(ScoreChange type) {
        if (isInformActive()) {
            synchronized (this.listeners) {
                for (ScoreListener listener : this.listeners) {
                    listener.onScoreChanged(type);
                }
            }
        }
    }

    protected void informListenersOfScoreChange(Team team, int level, int newPoint) {
        if (isInformActive()) {
            synchronized (this.listeners) {
                for (ScoreListener listener : this.listeners) {
                    listener.onScoreChanged(team, level, newPoint);
                }
            }
        }
    }

    protected void resetScore(Team[] startingTeams, S startingSettings) {
        // we need to know if we are a doubles match or not
        this.isDoubles = startingSettings.getIsDoubles();
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
        // reset all the player data (server etc) to the new starting teams
        int playerIndex = 0;
        for (int i = 0; i < this.teams.length; ++i) {
            // just use the team, is already a copy in Match
            this.teams[i] = startingTeams[i];
            // and do the players
            for (Player player : this.teams[i].getPlayers()) {
                // set this player
                this.players[playerIndex++] = player;
            }
        }
        // inform listeners of this
        informListeners(ScoreChange.RESET);
    }

    public int getLevels() {
        // return the number of levels we store the points at
        return this.points.length;
    }

    public int incrementPoint(Team team) {
        // just add a point to the base level
        int point = getPoint(0, team) + 1;
        setPoint(0, team, point);
        // inform listeners of this
        informListeners(ScoreChange.INCREMENT);
        return point;
    }

    public void setPoint(int level, Team team, int point) {
        int teamIndex = getTeamIndex(team);
        this.points[level][teamIndex] = point;
        // inform listeners of this
        informListeners(ScoreChange.POINTS_SET);
        // and send the special message
        informListenersOfScoreChange(team, level, point);
    }

    public Player[] getPlayers() {
        return Arrays.copyOf(this.players, this.players.length);
    }

    public Team[] getTeams() {
        return Arrays.copyOf(this.teams, this.teams.length);
    }

    public void changeServer(Player server) {
        for (Player player : this.players) {
            // set the server correctly for all players
            player.setIsServing(player.equals(server));
        }
        // inform listeners of this
        informListeners(ScoreChange.SERVER);
    }

    public Team getServingTeam() {
        Team servingTeam = null;
        for (Team team : this.teams) {
            // check the players
            for (Player player : team.getPlayers()) {
                // if this player is serving, we found the serving team
                if (player.getIsServing()) {
                    servingTeam = team;
                    break;
                }
            }
            if (null != servingTeam) {
                break;
            }
        }
        return servingTeam;
    }

    public Team getReceivingTeam() {
        return getOtherTeam(getServingTeam());
    }

    public Player getServer() {
        Player server = null;
        for (Player player : this.players) {
            // set the server correctly for all players
            if (player.getIsServing()) {
                server = player;
            }
        }
        return server;
    }

    protected void clearLevel(int level) {
        // we just set the points for a level that is not the bottom, we want to store
        // the points that were the level below in the history and clear them here
        storeHistory(level, this.points[level]);
        // clear this data
        for (int i = 0; i < this.teams.length; ++i) {
            this.points[level][i] = CLEAR_POINT;
        }
    }

    public int getPoint(int level, Team team) {
        return this.points[level][getTeamIndex(team)];
    }

    public Point getDisplayPoint(int level, Team team) {
        // just return the point as a string
        return new SimplePoint(getPoint(level, team));
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

    public List<int[]> getPointHistory(int level) {
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

    protected void changeEnds() {
        // cycle each team's court position
        for (Team team : this.teams) {
            // set the court position to the next position in the list
            team.setCourtPosition(team.getCourtPosition().getNext());
        }
        // inform listeners of this
        informListeners(ScoreChange.ENDS);
    }

    public int getScoreGoal() {
        return this.scoreGoal;
    }

    public void setScoreGoal(int goal) {
        this.scoreGoal = goal;
        // inform listeners of this
        informListeners(ScoreChange.GOAL);
    }

    public boolean isMatchOver() { return false; }

    protected int getTeamIndex(Team team) {
        for (int i = 0; i < this.teams.length; ++i) {
            if (this.teams[i].equals(team)) {
                // this is the team
                return i;
            }
        }
        Log.error("Searching for a team in score that is not in the list");
        return 0;
    }

    protected Team getOtherTeam(Team team) {
        for (Team other : getTeams()) {
            if (!other.equals(team)) {
                // this is the one
                return other;
            }
        }
        // quite serious this (only one team?)
        Log.error("There are not enough teams when trying to find the other...");
        return team;
    }

    public Team getWinner(int level) {
        int topTeam = 0;
        int[] finalScore = null;
        while (level >= 0) {
            // check the final score at the this level
            List<int[]> pointHistory = getPointHistory(level);
            if (null != pointHistory) {
                finalScore = pointHistory.get(pointHistory.size() - 1);
                if (null != finalScore && finalScore[0] != finalScore[1]) {
                    // there is a difference here, this can be used to determine the winner
                    break;
                }
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
                    // this is the top team
                    topPoints = finalScore[i];
                    topTeam = i;
                }
            }
        }
        return this.teams[topTeam];
    }

    protected Player getNextPlayer() {
        Team servingTeam = getServingTeam();
        if (null != servingTeam) {
            // we have a serving team, the next player will be the other team
            Team otherTeam = getOtherTeam(servingTeam);
            if (this.isDoubles) {
                // we are playing doubles, cycle the server in the team
                return otherTeam.getNextPlayer(otherTeam.getServingPlayer());
            }
            else {
                // just return the server of the other team
                return otherTeam.getServingPlayer();
            }
        }
        else {
            // no one is serving, return the server
            return getServer();
        }
    }

    protected void changeServer() {
        // the current server must yield now to the new one
        // find the team that is serving at the moment
        Team servingTeam = getServingTeam();
        if (null != servingTeam) {
            // we have a serving team, be sure that the serving player is set and not because
            // there isn't one specified
            servingTeam.setServingPlayer(servingTeam.getServingPlayer());
            // change team, and not the player that was last serving
            Team otherTeam = getOtherTeam(servingTeam);
            Player newServer;
            if (this.isDoubles) {
                // we are playing doubles, cycle the server in the team
                newServer = otherTeam.getNextServer();
            }
            else {
                // just use the server of the other team
                newServer = otherTeam.getServingPlayer();
            }
            // change the server to this new player
            changeServer(newServer);
        }
    }

    public int getPointsTotal(int level, Team team) {
        // add all the points for this team
        int iTeam = getTeamIndex(team);
        int total = getPoint(level, team);
        List<int[]> history = getPointHistory(level);
        if (null != history) {
            // add all this history to the total
            for (int[] points : history) {
                total += points[iTeam];
            }
        }
        return total;
    }
}
