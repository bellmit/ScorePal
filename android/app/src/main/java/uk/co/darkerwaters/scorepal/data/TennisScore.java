package uk.co.darkerwaters.scorepal.data;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.co.darkerwaters.scorepal.points.TennisPoint;

public class TennisScore extends Score<TennisSetup> {

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
    private boolean isTeamServerChangeAllowed;
    private MatchSetup.Player tieBreakServer;

    private List<Integer> tieBreakSets;
    private int[] breakPoints;
    private int[] breakPointsConverted;

    public TennisScore(TennisSetup setup) {
        super(setup, K_LEVELS);
    }

    @Override
    protected int getScoreGoal() {
        return setup.getNumberSets().num;
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
            this.breakPoints = new int[NO_TEAMS];
            this.breakPointsConverted = new int[NO_TEAMS];
        }
        // and reset our data
        this.isInTieBreak = false;
        this.isTeamServerChangeAllowed = false;
        this.tieBreakServer = null;
    }

    @Override
    public boolean isMatchOver() {
        boolean isMatchOver = false;
        TennisSetup.TennisSet setsToPlay = setup.getNumberSets();
        // return if a player has reached the number of sets required (this is just over half)
        for (int i = 0; i < NO_TEAMS; ++i) {
            if (getSets(MatchSetup.Team.values()[i]) >= setsToPlay.target) {
                // this team has reached the limit, match is over
                isMatchOver = true;
            }
        }
        return isMatchOver;
    }

    protected TennisSetup.TennisSet getSetsToPlay() {
        // the sets to play are set from the score goal
        return TennisSetup.TennisSet.fromValue(getScoreGoal());
    }

    public int getPoints(MatchSetup.Team team) {
        return super.getPoint(LEVEL_POINT, team);
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
            toReturn = new int[] { getPoints(MatchSetup.Team.T_ONE), getPoints(MatchSetup.Team.T_TWO) };
        }
        // return the points required
        return toReturn;
    }

    public int getGames(MatchSetup.Team team, int setIndex) {
        // get the games for the set index specified
        int toReturn;
        List<int[]> gameResults = super.getPointHistory(LEVEL_GAME);
        if (null == gameResults || setIndex < 0 || setIndex >= gameResults.size()) {
            // there is no history for this set, return the current games instead
            toReturn = super.getPoint(LEVEL_GAME, team);
        }
        else {
            int[] setGames = gameResults.get(setIndex);
            toReturn = setGames[team.index];
        }
        return toReturn;
    }

    public int getSets(MatchSetup.Team team) {
        // get the history of sets to get the last one
        List<int[]> setResults = super.getPointHistory(LEVEL_SET);
        int toReturn;
        if (null != setResults && false == setResults.isEmpty()) {
            int[] setGames = setResults.get(setResults.size() - 1);
            toReturn = setGames[team.index];
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

    public int getBreakPoints(MatchSetup.Team team) {
        return this.breakPoints[team.index];
    }

    public int getBreakPointsConverted(MatchSetup.Team team) {
        return this.breakPointsConverted[team.index];
    }

    @Override
    public int incrementPoint(MatchSetup.Team team, int level) {
        // do the work
        if (level == LEVEL_GAME) {
            // pre changing the point, a game has been won
            onGameWon(team);
        }
        // now we can change the point
        int point = super.incrementPoint(team, level);
        // but we have to handle things specially here
        switch (level) {
            case LEVEL_POINT:
                onPointIncremented(team, point);
                break;
            case LEVEL_GAME:
                onGameIncremented(team, point);
                break;
            case LEVEL_SET:
                onSetIncremented(team, point);
                break;
        }
        return point;
    }

    /**
     * only call this privately as a game is won by winning points to prevent it going in the history
     * as some user entry that they won the set
     * @param team is the team that has won the game
     */
    private void incrementGame(MatchSetup.Team team) {
        // is this a break-point converted to reality?
        onGameWon(team);
        // add one to the game already stored
        int point = super.getPoint(LEVEL_GAME, team) + 1;
        // set this back on the score
        super.setPoint(LEVEL_GAME, team, point);
        // and handle this
        onGameIncremented(team, point);
    }

    /**
     * only called privately as a set is won by winning a set to prevent it going in the history
     * as some user entry that they won the set
     * @param team is the team that has won the set
     */
    private void incrementSet(MatchSetup.Team team) {
        // add one to the set already stored
        int point = super.getPoint(LEVEL_SET, team) + 1;
        // set this back on the score
        super.setPoint(LEVEL_SET, team, point);
        // and handle this
        onSetIncremented(team, point);
    }

    private void onPointIncremented(MatchSetup.Team team, int point) {
        MatchSetup.Team otherTeam = setup.getOtherTeam(team);
        int otherPoint = getPoints(setup.getOtherTeam(team));
        int pointsAhead = point - otherPoint;
        // as soon as a point is played, you cannot change the server in the team
        isTeamServerChangeAllowed = false;
        // has this team won the game with this new point addition (can't be the other)
        if (false == this.isInTieBreak) {
            if (setup.getIsDeuceSuddenDeath() && point == otherPoint && point >= TennisPoint.FORTY.val()) {
                // this is a draw in points, not enough to win but we are interested
                // if this is a deciding point, in order to tell people
                state.addChange(ScoreState.ScoreChange.DECIDING_POINT);
                // this is a break point for the receiving team
                incrementBreakPoint(setup.getOtherTeam(servingTeam));
            } else {
                // now that is out of the way we can deal with actually winning the match
                if (point >= POINTS_TO_WIN_GAME
                        && ((setup.getIsDeuceSuddenDeath() && pointsAhead > 0) || pointsAhead >= POINTS_AHEAD_IN_GAME)) {
                    // we have enough points to win, either we are 2 ahead (won the ad)
                    // or the deuce deciding point is on and we are 2 ahead
                    incrementGame(team);
                } else {
                    // if we just behind the points required to win the game and
                    // are just one behind, this is a break-point.
                    if (servingTeam == team) {
                        // team two are receiving, are they about to break?
                        if (otherPoint >= POINTS_TO_WIN_GAME - 1 && otherPoint - point >= POINTS_AHEAD_IN_GAME - 1) {
                            // they are about to win on this one
                            incrementBreakPoint(otherTeam);
                        }
                    } else if (point >= POINTS_TO_WIN_GAME - 1 && pointsAhead >= POINTS_AHEAD_IN_GAME - 1) {
                        // team one are receiving and they are about to win on this one
                        incrementBreakPoint(team);
                    }
                }
            }
        } else {
            // are in a tie
            if (point >= POINTS_TO_WIN_TIE && pointsAhead >= POINTS_AHEAD_IN_TIE) {
                // in a tie and we have enough points (and enough ahead) to win the game
                // and move the game on
                incrementGame(team);
            } else {
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
    }

    private void onGameWon(MatchSetup.Team team) {
        if (false == isInTieBreak() && false == setup.isPlayerInTeam(team, servingPlayers[servingTeam.index])) {
            // the server is not in the winning team (not in a tie), this is a converted break
            ++this.breakPointsConverted[team.index];
            state.addChange(ScoreState.ScoreChange.BREAK_POINT_CONVERTED);
        }
    }

    private void onGameIncremented(MatchSetup.Team team, int point) {
        // clear the points
        super.clearLevel(LEVEL_POINT);
        // and handle if this won a set
        boolean isSetChanged = false;
        int gamesPlayed = getPlayedGames(-1);
        if (point >= setup.getNumberGames().num) {
            // this team have enough games to win, as long as the other don't have too many...
            MatchSetup.Team other = setup.getOtherTeam(team);
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
                state.addChange(ScoreState.ScoreChange.TIE_BREAK);
                // record that this current set was settled with a tie
                this.tieBreakSets.add(getPlayedSets());
            }
        }
        if (false == isMatchOver()) {
            // every game we alternate the server
            changeServer();
            if (this.isInTieBreak && null == this.tieBreakServer) {
                // we just set the server to serve but we need to remember who starts
                this.tieBreakServer = servingPlayers[servingTeam.index];
            }
        }
        isTeamServerChangeAllowed = gamesPlayed == 1
                && setup.getType() == MatchSetup.MatchType.DOUBLES
                && getPlayedSets() == 0;
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

    private void onSetIncremented(MatchSetup.Team team, int point) {
        // clear the games
        super.clearLevel(LEVEL_GAME);
    }

    private void incrementBreakPoint(MatchSetup.Team team) {
        // this is a break-point - increment the counter and inform the listeners
        ++this.breakPoints[team.index];
        state.addChange(ScoreState.ScoreChange.BREAK_POINT);
    }

    public int getPlayedPoints() {
        int playedPoints = 0;
        for (int i = 0; i < NO_TEAMS; ++i) {
            playedPoints += getPoints(MatchSetup.Team.values()[i]);
        }
        return playedPoints;
    }

    public int getPlayedGames(int setIndex) {
        int playedGames = 0;
        for (int i = 0; i < NO_TEAMS; ++i) {
            playedGames += getGames(MatchSetup.Team.values()[i], setIndex);
        }
        return playedGames;
    }

    public int getPlayedSets() {
        int playedSets = 0;
        for (int i = 0; i < NO_TEAMS; ++i) {
            playedSets += getSets(MatchSetup.Team.values()[i]);
        }
        return playedSets;
    }

    public boolean isInTieBreak() {
        return this.isInTieBreak;
    }

    public boolean isTeamServerChangeAllowed() { return this.isTeamServerChangeAllowed; }

    private boolean isTieBreak(int games1, int games2) {
        // we are in a tie break set if not the final set, or the final set is a tie-break set
        if (games1 != games2) {
            // not equal - not a tie
            return false;
        }
        else if (getPlayedSets() == getSetsToPlay().num - 1) {
            // we are playing the final set
            if (setup.getFinalSetTieGame() <= 0) {
                // we never tie
                return false;
            }
            else {
                // have we played enough games to initiate a tie?
                return games1 >= setup.getFinalSetTieGame();
            }
        }
        else {
            // not the final set, this is a tie if we played enough games
            return games1 >= setup.getNumberGames().num;
        }
    }

    @Override
    protected void changeServer() {
        // the current server must yield now to the new one
        if (!this.isInTieBreak && null != this.tieBreakServer) {
            // we were in a tie break, the next server should be the one after the player
            // that started the tie break, set the server back to the player that started it
            setServer(this.tieBreakServer);
            this.tieBreakServer = null;
        } else {
            // or let the base change the server
            super.changeServer();
        }
    }
}
