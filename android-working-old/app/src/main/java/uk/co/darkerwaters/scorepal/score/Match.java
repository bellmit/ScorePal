package uk.co.darkerwaters.scorepal.score;

import android.content.Context;
import android.location.Location;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import uk.co.darkerwaters.scorepal.activities.BaseActivity;
import uk.co.darkerwaters.scorepal.application.Log;
import uk.co.darkerwaters.scorepal.players.CourtPosition;
import uk.co.darkerwaters.scorepal.players.Player;
import uk.co.darkerwaters.scorepal.players.Team;
import uk.co.darkerwaters.scorepal.score.base.HistoryValue;
import uk.co.darkerwaters.scorepal.score.base.MatchMessage;
import uk.co.darkerwaters.scorepal.score.base.Point;
import uk.co.darkerwaters.scorepal.score.base.PointChange;

public class Match<T extends Score, S extends MatchSettings> implements
        Score.ScoreListener,
        MatchMessage.Param {

    private int matchMinutesPlayed;
    private final T score;
    private final Stack<HistoryValue> pointHistory;
    private ArrayList<PointChange> pointLevelsChanged;
    private Location playedLocation;

    private S matchSettings;
    private final Team[] teams;

    private final MatchSpeaker speaker;
    private final MatchWriter writer;

    private boolean isDataPersisted = false;

    public enum MatchChange {
        RESET, SETTINGS_CHANGED, INCREMENT, DECREMENT, ENDS, SERVER, DECIDING_POINT, TIE_BREAK, BREAK_POINT, BREAK_POINT_CONVERTED
    }

    public interface MatchListener {
        void onMatchChanged(MatchChange type);
        void onMatchPointsChanged(PointChange[] levelsChanged);
    }

    private final Set<MatchListener> listeners;

    public Match(S settings, MatchSpeaker speaker, MatchWriter writer) {
        // store these static settings, will use to reset and setup this match
        this.matchSettings = settings;
        this.listeners = new HashSet<>();
        this.speaker = speaker;
        this.writer = writer;

        // we are starting up, need to take a copy of the teams in the settings
        // as we will change an awful lot about them (servers and ends basically)
        Team[] startingTeams = this.matchSettings.getTeams();
        this.teams = new Team[startingTeams.length];
        for (int i = 0; i < this.teams.length; ++i) {
            // copy the team over so we don't change the settings as we play the match
            this.teams[i] = new Team(startingTeams[i]);
        }
        // create the score here
        this.score = createScore(this.teams, this.matchSettings);
        // listen to this score to pass on the information to our listeners
        this.score.addListener(this);
        // we will also store the entire history played
        this.pointHistory = new Stack<>();
        // and the score to the current settings
        resetScoreToStartingPosition();
    }

    public boolean isMatchOver() {
        return this.score.isMatchOver();
    }

    public Date getMatchPlayedDate() {
        return this.matchSettings.getMatchPlayedDate();
    }

    public String getMatchId(Context context) {
        return this.matchSettings.getMatchId(context);
    }

    public boolean isCacheMatch() {
        return true;
    }

    public boolean isSerialiseMatch() {
        return true;
    }

    public void resetMatch() {
        // the position and server and stuff will have changed on the teams, reset them here
        resetScoreToStartingPosition();
        // and clear the history
        this.pointHistory.clear();
        // and the time played
        this.matchMinutesPlayed = 0;
        // this data is not saved yet
        this.isDataPersisted = false;
        // inform listeners so they can set the player who is starting serve, location etc.
        informListeners(MatchChange.RESET);
    }

    protected HistoryValue createHistoryValue(int teamIndex, String scoreString) {
        // return a new value to store history in
        return new HistoryValue(teamIndex, scoreString, HistoryValue.Importance.LOW);
    }

    protected void updateHistoryValue(Context context, HistoryValue history) {
        // update the current history value with the current state of this match
        if (history.scoreString == null) {
            history.scoreString = this.writer.getScoreStringTwoLine(this, context);
        }
    }

    public void resetMatchSettings(Context context, S newSettings) {
        // store these static settings, will use to reset and setup this match
        this.matchSettings = newSettings;
        // this is a little different to the reset as we want to keep the score,
        // so we can just restore the point history, which has the side-effect of doing just this
        restorePointHistory(context);
        // inform listeners of this change to the score
        informListeners(MatchChange.SETTINGS_CHANGED);
    }

    protected S getMatchSettings() {
        return this.matchSettings;
    }

    protected void resetScoreToStartingPosition() {
        // reset all the team data to as it started (from the settings)
        Team[] startingTeams = this.matchSettings.getTeams();
        for (int i = 0; i < this.teams.length; ++i) {
            // just make a new copy of each team
            this.teams[i] = new Team(startingTeams[i]);
        }
        // reset the score to the starting state in order to reset properly
        this.score.resetScore(this.teams, this.matchSettings);
        // set the goal on the score to that in the settings
        this.score.setScoreGoal(this.matchSettings.getScoreGoal());

        // need to setup the starting positions from the settings now
        Team startingTeam = this.matchSettings.getStartingTeam();
        if (null != startingTeam) {
            // have to use indexes as the teams in the settings are different to the teams here
            int teamIndex = this.matchSettings.getTeamOne().equals(startingTeam) ? 0 : 1;
            int playerIndex = startingTeam.getPlayerIndex(this.matchSettings.getStartingServer(startingTeam));
            // set the server to be the server that is starting for the starting team
            this.score.changeServer(this.teams[teamIndex].getPlayer(playerIndex));
        }
        // do the ends too
        CourtPosition teamOneEnd = this.matchSettings.getStartingEnd(this.matchSettings.getTeamOne());
        if (getTeamOne().getCourtPosition() != teamOneEnd) {
            // not at the correct ends
            this.score.changeEnds();
        }
        if (getTeamOne().getCourtPosition() != teamOneEnd) {
            Log.error("Failed to set the correct team end when resetting the score");
        }
    }

    @Override
    public String serialiseToString(Context context) throws Exception {
        // serialise the data in the match to a nice string
        JSONArray dataArray = new JSONArray();
        serialiseToJson(context, dataArray);
        // and return the data as a string
        return BaseActivity.JSONToString(dataArray);
    }

    public void serialiseToJson(Context context, JSONArray dataArray) throws JSONException {
        // add our data to this array
        dataArray.put(this.matchMinutesPlayed);
        dataArray.put(getPointHistoryAsString());
        dataArray.put(new MatchMessage.LocationParam(this.playedLocation).serialiseToString(context));
        // add any data from the score
        this.score.serialiseToJson(context, dataArray);
    }

    @Override
    public Match deserialiseFromString(Context context, int version, String string) throws JSONException {
        deserialiseFromJson(context, version, new JSONArray(string));
        // and return ourselves
        return this;
    }

    public void deserialiseFromJson(Context context, int version, JSONArray dataArray) throws JSONException {
        // load the data from the data array into ourselves
        switch (version) {
            case 1:
                // load the version 1 data
                this.matchMinutesPlayed = dataArray.getInt(0);
                dataArray.remove(0);
                String scoreHistory = dataArray.getString(0);
                dataArray.remove(0);
                String locationString = dataArray.getString(0);
                dataArray.remove(0);
                // create the location from this
                MatchMessage.LocationParam param = new MatchMessage.LocationParam();
                param.deserialiseFromString(context, version, locationString);
                this.playedLocation = param.content;
                // and set the history from that retrieved
                restorePointHistoryFromString(new StringBuilder(scoreHistory));
                // and any data from the score
                this.score.deserialiseFromJson(context, version, dataArray);
                break;
        }
        // finally - LAST THING TO DO - restore our state from this history
        restorePointHistory(context);
    }

    public String getPointHistoryAsString() {
        StringBuilder recDataString = new StringBuilder();
        int noHistoricPoints = this.pointHistory.size();
        // first write the number of historic points we are going to store
        recDataString.append(noHistoricPoints);
        recDataString.append(':');
        // and then all the historic points we have
        int bitCounter = 0;
        int dataPacket = 0;
        for (int i = 0; i < noHistoricPoints; ++i) {
            // get the team as a binary value
            int binaryValue = this.pointHistory.get(i).teamIndex;
            // add this value to the data packet
            dataPacket |= binaryValue << bitCounter;
            // and increment the counter, sending as radix32 number means we can store 10 bits of data (up to 1023 base 10)
            if (++bitCounter >= 10) {
                // exceeded the size for next time, send this packet
                if (dataPacket < 32) {
                    // this will be print as '0' up to 'F' but we need it to be '0F' as expecting a fixed length...
                    // this is true for hex - who knows how a radix32 number is printed - but whatever nice that we get 10 values
                    recDataString.append('0');
                }
                recDataString.append(Integer.toString(dataPacket, 32));
                // and reset the counter and data
                bitCounter = 0;
                dataPacket = 0;
            }
        }
        if (bitCounter > 0) {
            // there was data we failed to send, only partially filled - send this anyway
            if (dataPacket < 32) {
                // this will be print as '0' up to 'F' but we need it to be '0F' as expecting a fixed length...
                // this is true for hex - who knows how a radix64 number is printed - but whatever nice that we get 10 values
                recDataString.append('0');
            }
            recDataString.append(Integer.toString(dataPacket, 32));
        }
        return recDataString.toString();
    }

    public Team getTeamOne() {
        return this.teams[0];
    }

    public Team getTeamTwo() {
        return this.teams[1];
    }

    public Location getPlayedLocation() {
        return this.playedLocation;
    }

    public void setPlayedLocation(Location currentLocation) {
        this.playedLocation = currentLocation;
    }

    public Team getOtherTeam(Team team) {
        if (team.equals(this.teams[0])) {
            return this.teams[1];
        }
        else {
            return this.teams[0];
        }
    }

    private void restorePointHistoryFromString(StringBuilder pointHistoryString) {
        // the value before the colon is the number of subsequent values
        int noHistoricPoints = extractValueToColon(pointHistoryString);
        int dataCounter = 0;
        while (dataCounter < noHistoricPoints) {
            // while there are points to get, get them
            int dataReceived = extractHistoryValue(pointHistoryString);
            // this char contains somewhere between one and eight values all bit-shifted, extract them now
            int bitCounter = 0;
            while (bitCounter < 10 && dataCounter < noHistoricPoints) {
                int bitValue = 1 & (dataReceived >> bitCounter++);
                // add this to the list of value received and inc the counter of data
                this.pointHistory.push(createHistoryValue(bitValue, null));
                // increment the counter
                ++dataCounter;
            }
        }
    }

    private int extractHistoryValue(StringBuilder recDataString) {
        // get the string as a double char value
        String hexString = extractChars(2, recDataString);
        return Integer.parseInt(hexString, 32);
    }

    private int extractValueToColon(StringBuilder recDataString) {
        int colonIndex = recDataString.indexOf(":");
        if (colonIndex == -1) {
            throw new StringIndexOutOfBoundsException();
        }
        // extract this data as a string
        String extracted = extractChars(colonIndex, recDataString);
        // and the colon
        recDataString.delete(0, 1);
        // return the data as an integer
        return Integer.parseInt(extracted);
    }

    private String extractChars(int charsLength, StringBuilder recDataString) {
        String extracted;
        if (recDataString.length() >= charsLength) {
            extracted = recDataString.substring(0, charsLength);
        }
        else {
            throw new StringIndexOutOfBoundsException();
        }
        recDataString.delete(0, charsLength);
        return extracted;
    }

    public boolean addListener(MatchListener listener) {
        synchronized (this.listeners) {
            return this.listeners.add(listener);
        }
    }

    public boolean removeListener(MatchListener listener) {
        synchronized (this.listeners) {
            return this.listeners.remove(listener);
        }
    }

    public void informListeners(MatchChange type) {
        if (null == this.score || this.score.isInformActive()) {
            synchronized (this.listeners) {
                for (MatchListener listener : this.listeners) {
                    listener.onMatchChanged(type);
                }
            }
            // this data is not saved yet
            this.isDataPersisted = false;
        }
    }

    protected void informListenersOfPointChange() {
        if (null != this.pointLevelsChanged
                && (null == this.score || this.score.isInformActive())) {
            synchronized (this.listeners) {
                for (MatchListener listener : this.listeners) {
                    listener.onMatchPointsChanged(this.pointLevelsChanged.toArray(new PointChange[0]));
                }
            }
            // clear the list of levels that changed
            this.pointLevelsChanged = null;
            // this data is not saved yet
            this.isDataPersisted = false;
        }
    }

    public Team getTeamServing() {
        Team servingTeam = null;
        Player currentServer = this.getCurrentServer();
        for (Team team : this.teams) {
            if (team.isPlayerInTeam(currentServer)) {
                // this is the serving team
                servingTeam = team;
                break;
            }
        }
        return servingTeam;
    }

    public void addMatchMinutesPlayed(int minutesPlayed) {
        this.matchMinutesPlayed += minutesPlayed;
        // this data is not saved yet
        this.isDataPersisted = false;
    }

    public int getMatchMinutesPlayed() {
        return this.matchMinutesPlayed;
    }

    boolean isDataPersisted() { return this.isDataPersisted; }

    void setDataPersisted() { this.isDataPersisted = true; }

    @Override
    public void onScoreChanged(Team team, int level, int newPoint) {
        // this is an actual change in score, store all the changes so we can inform
        // in a nice informative batch of levels that changed during an increment of score
        if (null != this.pointLevelsChanged) {
            this.pointLevelsChanged.add(new PointChange(team, level, newPoint));
        }
        // this data is not saved yet
        this.isDataPersisted = false;
    }

    @Override
    public void onScoreChanged(Score.ScoreChange type) {
        // pass on the interesting changes to our listener
        switch (type) {
            case POINTS_SET:
            case INCREMENT:
            case GOAL:
            case RESET:
                // none of this is interesting, we did all that from here and informed correctly
                break;
            case BREAK_POINT:
                // pass this on
                informListeners(MatchChange.BREAK_POINT);
                break;
            case BREAK_POINT_CONVERTED:
                // pass this on
                informListeners(MatchChange.BREAK_POINT_CONVERTED);
                break;
            case DECIDING_POINT:
                // this is interesting, someone will want to know this
                informListeners(MatchChange.DECIDING_POINT);
                break;
            case TIE_BREAK:
                // this is interesting, someone will want to know this
                informListeners(MatchChange.TIE_BREAK);
                break;
            case ENDS :
                // this is interesting, someone will want to know this
                informListeners(MatchChange.ENDS);
                break;
            case SERVER:
                // this is interesting too, pass it on
                informListeners(MatchChange.SERVER);
                break;
        }
        // this data is not saved yet
        this.isDataPersisted = false;
    }

    protected void preparePointsForLevelChange() {
        // before we increment, create the list that will be added to as points change
        this.pointLevelsChanged = new ArrayList<>();
    }

    public int incrementPoint(Context context, Team team) {
        // before we increment, create the list that will be added to as points change
        preparePointsForLevelChange();
        // just add a point to the base level
        int pointToReturn = this.score.incrementPoint(team);
        // and store the history of this action in this scorer
        this.pointHistory.push(createHistoryValue(getTeamIndex(team), this.writer.getScoreStringTwoLine(this, context)));
        // inform listeners of this change to the score
        informListeners(MatchChange.INCREMENT);
        // now we will have gathered all the changes, inform the listeners
        informListenersOfPointChange();
        // and return the new points for the team
        return pointToReturn;
    }

    private int getTeamIndex(Team team) {
        for (int i = 0; i < this.teams.length; ++i) {
            if (this.teams[i].equals(team)) {
                return i;
            }
        }
        // oops
        Log.error("Looking for a team in the match that is not there");
        return 0;
    }

    public Team undoLastPoint(Context context) {
        // we want to remove the last point, this can be tricky as can effect an awful lot of things
        // like are we in a tie-break, serving end, number of sets games, etc. This is hard to undo
        // so instead we are being lazy and using the power of the device you are on. ie, reset
        // the score and re-populate based on the history
        Team teamThatWonPoint = null;
        if (false == this.pointHistory.isEmpty()) {
            // pop the last point from the history
            teamThatWonPoint = this.teams[this.pointHistory.pop().teamIndex];
            // and restore the history that remains
            restorePointHistory(context);
            // inform listeners of this change to the score
            informListeners(MatchChange.DECREMENT);
        }
        // return the team who's point was popped
        return teamThatWonPoint;
    }

    private void restorePointHistory(Context context) {
        // stop the score from sending out update messages while we reconstruct it
        this.score.silenceInformers(true);
        // reset the score
        resetScoreToStartingPosition();
        // and restore the rest
        for (HistoryValue history : this.pointHistory) {
            // increment the point
            this.score.incrementPoint(this.teams[history.teamIndex]);
            // and set the score string if not in the history
            updateHistoryValue(context, history);
        }
        // stop the score from sending out update messages while we reconstruct it
        this.score.silenceInformers(false);
    }

    public T getScore() {
        return this.score;
    }

    public HistoryValue[] getWinnersHistory() {
        // return the history as an array of which team won each point and the score at the time
        HistoryValue[] toReturn;
        synchronized (this.pointHistory) {
            int index = 0;
            toReturn = new HistoryValue[this.pointHistory.size()];
            for (HistoryValue historyValue : this.pointHistory) {
                toReturn[index++] = historyValue.copy();
            }
        }
        return toReturn;
    }

    public boolean isMatchStarted() {
        // match is started when there are points in the history
        return false == this.pointHistory.empty();
    }

    protected T createScore(Team[] teams, S matchSettings) {
        return null;
    }

    public Player getCurrentServer() {
        return this.score.getServer();
    }

    public int getScoreGoal() {
        return this.score.getScoreGoal();
    }

    public void setScoreGoal(int goal) {
        this.score.setScoreGoal(goal);
    }

    public int getPointsTotal(int level, int teamIndex) {
        // count all the points in the levels
        return getPointsTotal(level, this.teams[teamIndex]);
    }

    public int getPointsTotal(int level, Team team) {
        // count all the points in the levels
        return this.score.getPointsTotal(level, team);
    }

    public Team getMatchWinner() {
        return this.score.getWinner(this.score.getLevels() - 1);
    }

    public String getDescription(MatchWriter.DescriptionLevel level, Context context) {
        return this.writer.getDescription(this, level, context);
    }

    public String createPointsPhrase(Context context, PointChange topChange) {
        return this.speaker.createPointsPhrase(this, context, topChange);
    }

    public String createPointsAnnouncement(Context context) {
        return this.speaker.createPointsAnnouncement(this, context);
    }

    public String appendSpokenMessage(Context context, String message, String spokenMessage) {
        if (null != spokenMessage
                && false == spokenMessage.isEmpty()
                && false == isMatchOver()) {
            // there is a state showing and the match isn't over, speak it here
            message += spokenMessage + Point.K_SPEAKING_PAUSE;
        }
        return message;
    }
}
