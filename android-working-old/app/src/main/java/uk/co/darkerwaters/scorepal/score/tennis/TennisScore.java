package uk.co.darkerwaters.scorepal.score.tennis;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.co.darkerwaters.scorepal.players.Player;
import uk.co.darkerwaters.scorepal.players.Team;
import uk.co.darkerwaters.scorepal.score.base.Point;
import uk.co.darkerwaters.scorepal.score.Score;

public class TennisScore extends Score<TennisMatchSettings> {

    public final static int LEVEL_POINT = 0;
    public final static int LEVEL_GAME = 1;
    public final static int LEVEL_SET = 2;

    private final int POINTS_TO_WIN_GAME = 4;
    private final int POINTS_AHEAD_IN_GAME = 2;

    private final int POINTS_TO_WIN_TIE = 7;
    private final int POINTS_AHEAD_IN_TIE = 2;

    private final int GAMES_AHEAD_IN_SET = 2;

    private final static int K_LEVELS = 3;

    private boolean isInTieBreak;
    private Player tieBreakServer;

    private int finalSetTieTarget;
    private int gamesInSet;
    private boolean isDecidingPointOnDeuce;

    private List<Integer> tieBreakSets;
    private int[] breakPoints;
    private int[] breakPointsConverted;

    TennisScore(Team[] startingTeams, TennisSets setsToPlay, TennisMatchSettings startingSettings) {
        super(startingTeams, startingSettings, K_LEVELS);
        // the score goal is the number of sets to play
        setScoreGoal(setsToPlay.val);
    }

    @Override
    protected void resetScore(Team[] startingTeams, TennisMatchSettings startingSettings) {
        // let the base reset
        super.resetScore(startingTeams, startingSettings);
        // initialise all the data we are gathering
        if (null != this.tieBreakSets) {
            this.tieBreakSets.clear();
            // clear our count of breaks and breaks converted
            Arrays.fill(this.breakPoints, 0);
            Arrays.fill(this.breakPointsConverted, 0);
        }
        else {
            // create the lists
            this.tieBreakSets = new ArrayList<>();
            this.breakPoints = new int[startingTeams.length];
            this.breakPointsConverted = new int[startingTeams.length];
        }
        // and reset our data
        this.isInTieBreak = false;
        this.tieBreakServer = null;
        // setup this score class from the settings we have
        this.finalSetTieTarget = startingSettings.getFinalSetTieTarget();
        this.gamesInSet = startingSettings.getGamesInSet();
        this.isDecidingPointOnDeuce = startingSettings.isDecidingPointOnDeuce();
    }

    @Override
    public void serialiseToJson(Context context, JSONArray dataArray) throws JSONException {
        // do the base
        super.serialiseToJson(context, dataArray);
        // add our data to this array
        dataArray.put(this.gamesInSet);
        dataArray.put(this.finalSetTieTarget);
        dataArray.put(this.isDecidingPointOnDeuce);
    }

    @Override
    public void deserialiseFromJson(Context context, int version, JSONArray dataArray) throws JSONException {
        // do the base
        super.deserialiseFromJson(context, version, dataArray);
        // and ours now
        switch (version) {
            case 1:
                // load version 1 data
                this.gamesInSet = dataArray.getInt(0); dataArray.remove(0);
                this.finalSetTieTarget = dataArray.getInt(0); dataArray.remove(0);
                this.isDecidingPointOnDeuce = dataArray.getBoolean(0); dataArray.remove(0);
                break;
        }
    }

    @Override
    public boolean isMatchOver() {
        boolean isMatchOver = false;
        TennisSets setsToPlay = getSetsToPlay();
        // return if a player has reached the number of sets required (this is just over half)
        for (Team team : getTeams()) {
            if (getSets(team) >= setsToPlay.target) {
                // this team has reached the limit, match is over
                isMatchOver = true;
            }
        }
        return isMatchOver;
    }

    TennisSets getSetsToPlay() {
        // the sets to play are set from the score goal
        return TennisSets.fromValue(getScoreGoal());
    }

    public int getPoints(Team team) {
        return super.getPoint(LEVEL_POINT, team);
    }

    @Override
    public Point getDisplayPoint(int level, Team team) {
        Point displayPoint;
        if (this.isInTieBreak) {
            // we are in a tie break, just use the numbers of the points
            displayPoint = super.getDisplayPoint(level, team);
        }
        else if (level == LEVEL_POINT) {
            // return the point string
            displayPoint = getDisplayPoint(team);
        }
        else {
            // use the base
            displayPoint = super.getDisplayPoint(level, team);
        }
        return displayPoint;
    }

    public Point getDisplayPoint(Team team) {
        Point displayPoint;
        if (this.isInTieBreak) {
            displayPoint = super.getDisplayPoint(LEVEL_POINT, team);
        }
        else {
            // not in a tie, show the points string correctly
            int points = getPoints(team);
            Team opposition = getOtherTeam(team);
            int otherPoints = getPoints(opposition);
            switch (points) {
                case 0: // love
                case 1: // 15
                case 2: // 30
                    // we are less than 40, just return the string from the array
                    displayPoint = TennisPoint.fromVal(points);
                    break;
                case 3:
                    // we have 40, if the other player has 40 too, we are at deuce
                    if (otherPoints == 3) {
                        // this is 40-40
                        displayPoint = TennisPoint.DEUCE;
                    } else {
                        // they have fewer, or advantage, we just have 40
                        displayPoint = TennisPoint.FORTY;
                    }
                    break;
                default:
                    // if we are one ahead we have advantage
                    int delta = points - otherPoints;
                    switch(delta) {
                        case 0 :
                            //this is deuce
                            displayPoint = TennisPoint.DEUCE;
                            break;
                        case 1:
                            // we have ad
                            displayPoint = TennisPoint.ADVANTAGE;
                            break;
                        case -1:
                            // we are disadvantaged
                            displayPoint = TennisPoint.FORTY;
                            break;
                        default:
                            // we are far enough ahead to have won the game
                            displayPoint = TennisPoint.GAME;
                            break;
                    }
            }
        }
        // return the string
        return displayPoint;
    }

    public int[] getPoints(int setIndex, int gameIndex) {
        // get the points in the set and games index specified
        int[] toReturn = null;
        // to get the points for this game, we need to find the index of that game
        // so for that we need to add up all the games for all the previous sets
        // before we get to this one
        List<int[]> gameResults = super.getPointHistory(LEVEL_POINT);
        List<int[]> setResults = super.getPointHistory(LEVEL_GAME);
        if (null != setResults && null != gameResults) {
            // there are results for the sets (a record of the games for each)
            // we need to add these up to find the start of the set as a number of games
            int gamesPlayed = 0;
            for (int i = 0; i < setIndex && i < setResults.size(); ++i) {
                for (int games : setResults.get(i)) {
                    gamesPlayed += games;
                }
            }
            // the index into the game results is the games played in previous sets
            // plus the index we are interested in
            gameIndex += gamesPlayed;
            if (gameIndex < gameResults.size()) {
                // this is ok
                toReturn = gameResults.get(gameIndex);
            }
        }
        if (null == toReturn) {
            // there are no points for this game, we can return the current points
            // instead as they are probably in progress of playing it then
            Team[] teams = getTeams();
            if (null != teams && teams.length > 1) {
                toReturn = new int[] { getPoints(teams[0]), getPoints(teams[1]) };
            }
        }
        // return the points required
        return toReturn;
    }

    public int getGames(Team team, int setIndex) {
        // get the games for the set index specified
        int toReturn;
        List<int[]> gameResults = super.getPointHistory(LEVEL_GAME);
        if (null == gameResults || setIndex < 0 || setIndex >= gameResults.size()) {
            // there is no history for this set, return the current games instead
            toReturn = super.getPoint(LEVEL_GAME, team);
        }
        else {
            int[] setGames = gameResults.get(setIndex);
            toReturn = setGames[getTeamIndex(team)];
        }
        return toReturn;
    }

    public int getSets(Team team) {
        // get the history of sets to get the last one
        List<int[]> setResults = super.getPointHistory(LEVEL_SET);
        int toReturn;
        if (null != setResults && false == setResults.isEmpty()) {
            int[] setGames = setResults.get(setResults.size() - 1);
            toReturn = setGames[getTeamIndex(team)];
        }
        else {
            // return the running set count
            toReturn = super.getPoint(LEVEL_SET, team);
        }
        return toReturn;
    }

    public boolean isSetTieBreak(int setIndex) {
        return this.tieBreakSets.contains(Integer.valueOf(setIndex));
    }

    public int getBreakPoints(int teamIndex) {
        return this.breakPoints[teamIndex];
    }

    public int getBreakPointsConverted(int teamIndex) {
        return this.breakPointsConverted[teamIndex];
    }

    @Override
    public int incrementPoint(Team team) {
        // add one to the point already stored
        int point = super.incrementPoint(team);
        Team otherTeam = getOtherTeam(team);
        int otherPoint = getPoints(getOtherTeam(team));
        int pointsAhead = point - otherPoint;
        // has this team won the game with this new point addition (can't be the other)
        if (false == this.isInTieBreak) {
            if (this.isDecidingPointOnDeuce && point == otherPoint && point >= TennisPoint.FORTY.val()) {
                // this is a draw in points, not enough to win but we are interested
                // if this is a deciding point, in order to tell people
                informListeners(ScoreChange.DECIDING_POINT);
                // this is a break point for the receiving team
                incrementBreakPoint(getReceivingTeam());
            }
            else {
                // now that is out of the way we can deal with actually winning the match
                if (point >= POINTS_TO_WIN_GAME
                    && ((this.isDecidingPointOnDeuce && pointsAhead > 0) || pointsAhead >= POINTS_AHEAD_IN_GAME)) {
                    // we have enough points to win, either we are 2 ahead (won the ad)
                    // or the deuce deciding point is on and we are 2 ahead
                    incrementGame(team);
                }
                else {
                    // if we just behind the points required to win the game and
                    // are just one behind, this is a break-point.
                    if (getServingTeam() == team) {
                        // team two are receiving, are they about to break?
                        if (otherPoint >= POINTS_TO_WIN_GAME - 1 && otherPoint - point >= POINTS_AHEAD_IN_GAME - 1) {
                            // they are about to win on this one
                            incrementBreakPoint(otherTeam);
                        }
                    }
                    else if (point >= POINTS_TO_WIN_GAME - 1 && pointsAhead >= POINTS_AHEAD_IN_GAME - 1) {
                        // team one are receiving and they are about to win on this one
                        incrementBreakPoint(team);
                    }
                }
            }
        }
        else {
            // are in a tie
            if (point >= POINTS_TO_WIN_TIE && pointsAhead >= POINTS_AHEAD_IN_TIE) {
                // in a tie and we have enough points (and enough ahead) to win the game
                // and move the game on
                incrementGame(team);
            }
            else {
                // we didn't win the tie, but are we about to?
                /*
                *
                * I don't think winning a tie is a break... it's a mini-break.
                *
                if (point >= POINTS_TO_WIN_TIE - 1 && pointsAhead >= POINTS_AHEAD_IN_TIE - 1) {
                    // one more point and we will have won this, this is a break-point
                    // if we are not serving, check this
                    incrementPotentialBreakPoint(team);
                }*/
                // after the first, and subsequent two points, we have to change servers in a tie
                int playedPoints = getPlayedPoints();
                if ((playedPoints - 1) % 2 == 0) {
                    // we are at point 1, 3, 5, 7 etc - change server
                    changeServer();
                }
                // also change ends every 6 points
                if (playedPoints % 6 == 0) {
                    // the set ended with
                    changeEnds();
                }
            }
        }
        return point;
    }

    private void incrementBreakPoint(Team team) {
        // this is a break-point - increment the counter and inform the listeners
        ++this.breakPoints[getTeamIndex(team)];
        informListeners(ScoreChange.BREAK_POINT);
    }

    public int getPlayedPoints() {
        int playedPoints = 0;
        for (Team team : getTeams()) {
            playedPoints += getPoints(team);
        }
        return playedPoints;
    }

    public int getPlayedGames(int setIndex) {
        int playedGames = 0;
        for (Team team : getTeams()) {
            playedGames += getGames(team, setIndex);
        }
        return playedGames;
    }

    public int getPlayedSets() {
        int playedSets = 0;
        for (Team team : getTeams()) {
            playedSets += getSets(team);
        }
        return playedSets;
    }

    private void incrementGame(Team team) {
        // is this a break-point converted to reality?
        if (false == isInTieBreak() && false == team.isPlayerInTeam(getServer())) {
            // the server is not in the winning team (not in a tie), this is a converted break
            ++this.breakPointsConverted[getTeamIndex(team)];
            informListeners(ScoreChange.BREAK_POINT_CONVERTED);
        }
        // add one to the game already stored
        int point = super.getPoint(LEVEL_GAME, team) + 1;
        // set this back on the score
        super.setPoint(LEVEL_GAME, team, point);
        // also clear the points
        super.clearLevel(LEVEL_POINT);

        boolean isSetChanged = false;
        int gamesPlayed = getPlayedGames(-1);
        if (point >= this.gamesInSet) {
            // this team have enough games to win, as long as the other don't have too many...
            Team other = getOtherTeam(team);
            int otherPoints = getGames(other, -1);
            if ((this.isInTieBreak && point != otherPoints)
                    || point - otherPoints >= GAMES_AHEAD_IN_SET) {
                // they are enough games ahead (2) so they have won
                incrementSet(team);
                // won the set, this is the end of the tie break
                this.isInTieBreak = false;
                isSetChanged = true;
            }
            else if (isTieBreak(point, otherPoints)){
                // we are not ahead enough, we both have more than 6 and are in a tie break set
                // time to initiate a tie break
                this.isInTieBreak = true;
                // inform listeners of this change
                informListeners(ScoreChange.TIE_BREAK);
                // record that this current set was settled with a tie
                this.tieBreakSets.add(getPlayedSets());
            }
        }
        if (false == isMatchOver()) {
            // every game we alternate the server
            changeServer();
            if (this.isInTieBreak && null == this.tieBreakServer) {
                // we just set the server to serve but we need to remember who starts
                this.tieBreakServer = getServer();
            }
        }
        if (isSetChanged) {
            // we want to change ends at the end of any set in which the score wasn't even
            if (gamesPlayed % 2 != 0) {
                // the set ended with odd number of games
                changeEnds();
            }
        }
        else {
            // we want to change ends at the end of the first, 3, 5 (every odd game) of each set
            if ((gamesPlayed - 1) % 2 == 0) {
                // this is an odd game, change ends
                changeEnds();
            }
        }
    }

    public boolean isInTieBreak() {
        return this.isInTieBreak;
    }

    private boolean isTieBreak(int games1, int games2) {
        // we are in a tie break set if not the final set, or the final set is a tie-break set
        if (games1 != games2) {
            // not equal - not a tie
            return false;
        }
        else if (getPlayedSets() == getSetsToPlay().val - 1) {
            // we are playing the final set
            if (this.finalSetTieTarget <= 0) {
                // we never tie
                return false;
            }
            else {
                // have we played enough games to initiate a tie?
                return games1 >= this.finalSetTieTarget;
            }
        }
        else {
            // not the final set, this is a tie if we played enough games
            return games1 >= this.gamesInSet;
        }
    }

    private void incrementSet(Team team) {
        // add one to the set already stored
        int point = super.getPoint(LEVEL_SET, team) + 1;
        // set this back on the score
        super.setPoint(LEVEL_SET, team, point);
        // also clear the games
        super.clearLevel(LEVEL_GAME);
    }

    @Override
    protected void changeServer() {
        // the current server must yield now to the new one
        if (!this.isInTieBreak && null != this.tieBreakServer) {
            // we were in a tie break, the next server should be the one after the player
            // that started the tie break, set the server back to the player that started it
            changeServer(this.tieBreakServer);
            this.tieBreakServer = null;
        }
        // and let the base change the server
        super.changeServer();
    }
}
