package uk.co.darkerwaters.scorepal.score;

import android.content.Context;
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.co.darkerwaters.scorepal.activities.BaseActivity;
import uk.co.darkerwaters.scorepal.application.Application;
import uk.co.darkerwaters.scorepal.application.Log;
import uk.co.darkerwaters.scorepal.players.Player;
import uk.co.darkerwaters.scorepal.players.Team;
import uk.co.darkerwaters.scorepal.score.base.Sport;

public class MatchStatistics {

    private static final String K_STATS_EXT = ".mstats";
    private static final int K_VERSION = 2;

    private static final int K_RECENT_DAYS_THRESHOLD = 30;

    private static MatchStatistics INSTANCE = null;

    private final String playerName;
    
    public class Result {
        final String matchId;
        final boolean isWin;
        final boolean isLoss;
        final Sport sport;
        
        Result(Match match, Sport sport, String matchId) {
            this(sport, matchId, isPlayerWinner(match), isPlayerLoser(match));
        }

        Result(Result toCopy) {
            this.matchId = toCopy.matchId;
            this.isLoss = toCopy.isLoss;
            this.isWin = toCopy.isWin;
            this.sport = toCopy.sport;
        }

        Result(Sport sport, String matchId, boolean isWin, boolean isLoss) {
            this.sport = sport;
            this.matchId = matchId;
            this.isWin = isWin;
            this.isLoss = isLoss;
        }

        boolean isRecent() {
            // return if this result is within our (recent) period
            Date matchDate = MatchSettings.DateFromMatchId(this.matchId);
            return MatchPersistenceManager.DaysDifference(new Date(), matchDate) <= K_RECENT_DAYS_THRESHOLD;
        }
    }

    private boolean isPlayerWinner(Match match) {
        Team matchWinner = match.getMatchWinner();
        boolean winningPlayerFound = false;
        for (Player winningPlayer : matchWinner.getPlayers()) {
            if (winningPlayer.getName().equals(playerName)) {
                // we are on the winning team
                winningPlayerFound = true;
                break;
            }
        }
        // this is a win if we were in the winning team
        return winningPlayerFound;
    }

    private boolean isPlayerLoser(Match match) {
        // we maybe didn't find the winning player, sounds like a loss but we might not
        // have played at all
        boolean losingPlayerFound = false;
        Team matchLoser = match.getOtherTeam(match.getMatchWinner());
        for (Player losingPlayer : matchLoser.getPlayers()) {
            if (losingPlayer.getName().equals(playerName)) {
                // we are on the winning team
                losingPlayerFound = true;
                break;
            }
        }
        // this is a loss if the losing player was found
        return losingPlayerFound;
    }

    private class OpponentResult {
        final String opponentName;
        final Map<String, Result> results;
        int noWinsAgainst = 0;
        int noLossesAgainst = 0;
        OpponentResult(String opponentName) {
            this.opponentName = opponentName;
            this.results = new HashMap();
        }
        void putResult(Result result) {
            // put this result in the list, replacing what is already there
            this.results.put(result.matchId, result);
        }
        Result getResult(String matchId) {
            return this.results.get(matchId);
        } 
    }

    private final int[] totalLossesBySport;
    private final int[] totalWinsBySport;

    private final int[] recentLossesBySport;
    private final int[] recentWinsBySport;

    private final Map<String, OpponentResult> opponentResults;

    private int matchesRecorded = 0;
    private int recentMatchesRecorded = 0;

    private boolean isDataLoaded;
    private boolean isDataDirty;

    public static MatchStatistics GetInstance(Application application, Context context) {
        String playerName = application.getSettings().getSelfName();
        if (INSTANCE == null) {
            // create a new one
            INSTANCE = new MatchStatistics(playerName);
        }
        else if (!INSTANCE.getPlayerName().equals(playerName)) {
            // this is not the one we are looking for
            INSTANCE.close(context);
            INSTANCE = new MatchStatistics(playerName);
        }
        return INSTANCE;
    }

    private MatchStatistics(String playerName) {
        // set the members
        this.playerName = playerName;
        // and create the lists, all will be zeroed
        int noSports = Sport.GetSports().length;
        this.totalLossesBySport = new int[noSports];
        this.totalWinsBySport = new int[noSports];
        this.recentLossesBySport = new int[noSports];
        this.recentWinsBySport = new int[noSports];
        // and the recent opponent results
        this.opponentResults = new HashMap();
        
        // we will load the statistics for this player a little later (only when we have to)
        resetStats();
    }

    private void resetStats() {
        // reset everything / fill with good default starting data
        this.matchesRecorded = 0;
        this.recentMatchesRecorded = 0;
        // and clear the lists
        Arrays.fill(this.totalLossesBySport, 0);
        Arrays.fill(this.totalWinsBySport, 0);
        Arrays.fill(this.recentLossesBySport, 0);
        Arrays.fill(this.recentWinsBySport, 0);
        // and clear the list of opponent results
        synchronized (this.opponentResults) {
            this.opponentResults.clear();
        }
        // reset the flags
        this.isDataDirty = false;
        this.isDataLoaded = false;
    }

    public void close(Context context) {
        // save this data
        if (this.isDataDirty) {
            saveStatisticsData(context);
        }
        // and clear all our data
        resetStats();
    }
    
    public int getTotalWins(Sport sport) {
        return this.totalWinsBySport[sport.value];
    }

    public int getTotalLosses(Sport sport) {
        return this.totalLossesBySport[sport.value];
    }

    public int getRecentWins(Sport sport) {
        if (sport.value < this.recentWinsBySport.length) {
            return this.recentWinsBySport[sport.value];
        }
        else {
            // was collated before this existed
            return 0;
        }
    }

    public int getRecentLosses(Sport sport) {
        return this.recentLossesBySport[sport.value];
    }

    public int getRecentWinsTotal() {
        int total = 0;
        for (Sport sport : Sport.GetSports()) {
            total += getRecentWins(sport);
        }
        return total;
    }

    public int getRecentLossesTotal() {
        int total = 0;
        for (Sport sport : Sport.GetSports()) {
            total += getRecentLosses(sport);
        }
        return total;
    }

    public int getMatchesRecorded() {
        return this.matchesRecorded;
    }

    public int getRecentMatchesRecorded() {
        return this.recentMatchesRecorded;
    }

    public String[] getOpponents() {
        synchronized (this.opponentResults) {
            Set<String> strings = this.opponentResults.keySet();
            return strings.toArray(new String[0]);
        }
    }

    public Result[] getRecentResultsAgainstOpponent(String opponentName) {
        Result[] results;
        synchronized (this.opponentResults) {
            OpponentResult opponentResult = this.opponentResults.get(opponentName);
            if (null != opponentResult) {
                Set<String> matchIds = opponentResult.results.keySet();
                results = new Result[matchIds.size()];
                int index = 0;
                for (String matchId : matchIds) {
                    // make a copy of each as they are prone to change in this class
                    // and we don't want that messing up the calling class
                    results[index++] = new Result(opponentResult.results.get(matchId));
                }
            }
            else {
                // no results to copy for this person
                results = new Result[0];
            }
        }
        // return our found results
        return results;
    }

    public Pair<Integer, Integer> getWinsLossesAgainstOpponent(String opponentName) {
        Pair<Integer, Integer> result = null;
        synchronized (this.opponentResults) {
            OpponentResult opponentResult = this.opponentResults.get(opponentName);
            if (null != opponentResult) {
                result = new Pair<>(opponentResult.noWinsAgainst, opponentResult.noLossesAgainst);
            }
        }
        return result;
    }

    public void wipeStatisticsFile(Context context) {
        File file = new File(context.getFilesDir(), this.playerName + K_STATS_EXT);
        if (!file.delete()) {
            // failed to delete for some reason, try to delete on exit
            file.deleteOnExit();
        }
        this.resetStats();
    }

    private boolean loadStatisticsFile(Context context) {
        File file = new File(context.getFilesDir(), this.playerName + K_STATS_EXT);
        this.isDataLoaded = false;
        if (file.exists()) {
            // this is cool, this exists so we can load it
            // load the data in I suppose then...
            StringBuilder json = new StringBuilder();
            try {
                FileInputStream stream = new FileInputStream(file);
                int size;
                while ((size = stream.available()) > 0) {
                    // while there is data, get it out
                    byte[] buffer = new byte[size];
                    stream.read(buffer);
                    // append this buffer to the string builder
                    json.append(new String(buffer, StandardCharsets.UTF_8));
                }
                stream.close();

                // now we have ths string, we can create JSON from it
                JSONObject obj = new JSONObject(json.toString());
                // get the version number
                final int version = obj.getInt("ver");
                // and load in the data for this gathering of statistics
                isDataLoaded = deserialiseFromString(version, obj.getString("data"));
            } catch (FileNotFoundException e) {
                Log.error("Failed to read the file", e);
            } catch (IOException e) {
                Log.error("Failed to read the JSON", e);
            } catch (JSONException e) {
                Log.error("Failed to create the JSON", e);
            } catch (Throwable e) {
                Log.error("Failed match loading seriously: " + e.getMessage());
            }
        }
        return this.isDataLoaded;
    }

    private boolean saveStatisticsData(Context context) {
        // create the file here from the ID and the correct extension
        boolean isSaved = false;
        try {
            File file = new File(context.getFilesDir(), this.playerName + K_STATS_EXT);
            Writer output = new BufferedWriter(new FileWriter(file));
            JSONObject obj = new JSONObject();
            // put the version number in
            obj.put("ver", K_VERSION);
            // put the settings in the object first, these will determine the match created too
            obj.put("data", serialiseToString());
            // close the file
            output.write(BaseActivity.JSONToString(obj));
            output.close();
            // if here then all was good
            isSaved = true;
            this.isDataDirty = false;
        } catch (FileNotFoundException e) {
            Log.error("Failed to read the file", e);
        } catch (IOException e) {
            Log.error("Failed to read the JSON", e);
        } catch (JSONException e) {
            Log.error("Failed to create the JSON", e);
        } catch (Throwable e) {
            Log.error("Failed match saving seriously: " + e.getMessage());
        }
        return isSaved;
    }

    private String serialiseToString() throws JSONException {
        // we use a nice JSON array for brevity here, create this to populate it
        JSONArray data = new JSONArray();

        //*** VERSION 2
        // to the total matches recorded first
        data.put(this.matchesRecorded);

        //*** VERSION 1
        // do the totals first
        for (Sport sport : Sport.GetSports()) {
            data.put(totalWinsBySport[sport.value]);
            data.put(totalLossesBySport[sport.value]);
        }

        synchronized (this.opponentResults) {
            // now the opponents
            Set<String> opponents = this.opponentResults.keySet();
            // tell the loader how many
            data.put(opponents.size());
            // now do them all
            for (String opponent : opponents) {
                // store each opponent
                OpponentResult opponentResult = this.opponentResults.get(opponent);
                data.put(opponentResult.opponentName);
                data.put(opponentResult.noWinsAgainst);
                data.put(opponentResult.noLossesAgainst);
                // and we have to do the results in here too
                Set<String> results = opponentResult.results.keySet();
                // tell the loader how many
                data.put(results.size());
                // now do them all
                for (String resultKey : results) {
                    Result result = opponentResult.results.get(resultKey);
                    data.put(result.matchId);
                    data.put(result.sport.value);
                    data.put(result.isWin);
                    data.put(result.isLoss);
                }
            }
        }

        // return this as a nice compact string
        return BaseActivity.JSONToString(data);
    }

    private boolean deserialiseFromString(int version, String dataString) throws JSONException {
        // we use a nice JSON array for brevity here, create this
        JSONArray data = new JSONArray(dataString);
        boolean successfulLoad = true;
        // do the version one loading here
        int dataIndex = 0;

        switch (version) {
            case 2:
                // version 2 added the matches recorded
                this.matchesRecorded = data.getInt(dataIndex++);

                // and flow through to version 1
            case 1:
                // get the totals first
                for (Sport sport : Sport.GetSports()) {
                    totalWinsBySport[sport.value] = data.getInt(dataIndex++);
                    totalLossesBySport[sport.value] = data.getInt(dataIndex++);
                }

                synchronized (this.opponentResults) {
                    // now the opponents, get how many
                    int noOpponents = data.getInt(dataIndex++);
                    // now do them all
                    for (int i = 0; i < noOpponents; ++i) {
                        // load each opponent
                        String opponentName = data.getString(dataIndex++);
                        OpponentResult opponentResult = new OpponentResult(opponentName);
                        opponentResult.noWinsAgainst = data.getInt(dataIndex++);
                        opponentResult.noLossesAgainst = data.getInt(dataIndex++);
                        // add the opponent to the list
                        this.opponentResults.put(opponentName, opponentResult);

                        // and we have to do the results in here too
                        int noResults = data.getInt(dataIndex++);
                        // load each result
                        for (int j = 0; j < noResults; ++j) {
                            String matchId = data.getString(dataIndex++);
                            Sport sport = Sport.from(data.getInt(dataIndex++));
                            boolean isWin = data.getBoolean(dataIndex++);
                            boolean isLoss = data.getBoolean(dataIndex++);
                            Result result = new Result(sport, matchId, isWin, isLoss);
                            // and put this in the map
                            opponentResult.putResult(result);
                            // is this a recent result?
                            if (result.isRecent()) {
                                // we don't save / load this data, we calculate it each time
                                ++this.recentMatchesRecorded;
                                // we load because the window moves every day
                                if (result.isWin) {
                                    ++this.recentWinsBySport[result.sport.value];
                                }
                                if (result.isLoss) {
                                    ++this.recentLossesBySport[result.sport.value];
                                }
                            }
                        }
                    }
                }

                break;
        }

        // if here then it was all good (ie we didn't throw);
        return successfulLoad;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public static void OnMatchResultsAccepted(Application application, Match match, MatchSettings matchSettings, Context context) {
        MatchStatistics matchStatistics = GetInstance(application, context);
        if (null != matchStatistics && null != match && null != matchSettings) {
            matchStatistics.updateMatchResults(match, matchSettings, context);
            // and store this right away, let's be safe people!
            matchStatistics.saveStatisticsData(context);
        }
        else {
            Log.error("Updating stats for a match but there are no stats to update");
        }
    }

    public void loadIfNeeded(Context context) {
        if (!this.isDataLoaded) {
            // we haven't loaded any yet, load it now before we go changing anything
            loadStatisticsFile(context);
            clearExcessiveData();
            // we tried - this is enough
            this.isDataLoaded = true;
        }
    }

    private void updateMatchResults(Match match, MatchSettings matchSettings, Context context) {
        // if not loaded the loas
        loadIfNeeded(context);
        // this is some new data, it might be replacing old stuff, find the old stuff if there is any
        String matchId = match.getMatchId(context);
        synchronized (this.opponentResults) {
            // we store results based on the opponents, so get the results here
            for (String opponent : getOpponentNames(match, matchSettings)) {
                // get the results for each
                OpponentResult opponentResult = this.opponentResults.get(opponent);
                if (null != opponentResult) {
                    // this opponent has some results, is this one there?
                    Result result = opponentResult.getResult(matchId);
                    if (null != result) {
                        // need to remove what was set from the totals
                        removeLeavingResultData(opponentResult, result);
                    }
                    // no need to actually remove it as we will overwrite it in a sec
                } else {
                    // no results for this opponent, create one
                    opponentResult = new OpponentResult(opponent);
                    this.opponentResults.put(opponent, opponentResult);
                }
                // create the new result
                Result result = new Result(match, matchSettings.getSport(), matchId);
                // add to the opponent's list
                opponentResult.putResult(result);
                // and add the arriving new data to our totals
                addArrivingResultData(opponentResult, result);
            }
        }
        // this makes me feel all dirty (O;
        this.isDataDirty = true;
    }

    private void clearExcessiveData() {
        // we need to get all the results by opponent and clear the old stuff from
        // the recent list
        Date now = new Date();
        synchronized (this.opponentResults) {
            for (String opponent : this.opponentResults.keySet()) {
                OpponentResult opponentResult = this.opponentResults.get(opponent);
                List<String> toRemove = new ArrayList<>();
                for (String matchId : opponentResult.results.keySet()) {
                    if (MatchPersistenceManager.DaysDifference(
                            now, MatchSettings.DateFromMatchId(matchId))
                            > K_RECENT_DAYS_THRESHOLD) {
                        // this match id is for a match that is not recent
                        toRemove.add(matchId);
                    }
                }
                for (String matchId : toRemove) {
                    opponentResult.results.remove(matchId);
                }
            }
        }
    }

    private void addArrivingResultData(OpponentResult opponentResult, Result result) {
        boolean isResultRecent = result.isRecent();
        if (isResultRecent) {
            ++this.recentMatchesRecorded;
        }
        // add the numbers from this result from our totals
        if (result.isWin) {
            // add this
            ++this.totalWinsBySport[result.sport.value];
            ++opponentResult.noWinsAgainst;
            if (isResultRecent) {
                ++this.recentWinsBySport[result.sport.value];
            }
        }
        if (result.isLoss) {
            // add this
            ++this.totalLossesBySport[result.sport.value];
            ++opponentResult.noLossesAgainst;
            if (isResultRecent) {
                ++this.recentLossesBySport[result.sport.value];
            }
        }
    }

    private void removeLeavingResultData(OpponentResult opponentResult, Result result) {
        // remove the numbers from this result from our totals
        boolean isResultRecent = result.isRecent();
        if (isResultRecent) {
            --this.recentMatchesRecorded;
        }
        if (result.isWin) {
            // remove this
            --this.totalWinsBySport[result.sport.value];
            --opponentResult.noWinsAgainst;
            if (isResultRecent) {
                --this.recentWinsBySport[result.sport.value];
            }
        }
        if (result.isLoss) {
            // remove this
            --this.totalLossesBySport[result.sport.value];
            --opponentResult.noLossesAgainst;
            if (isResultRecent) {
                --this.recentLossesBySport[result.sport.value];
            }
        }
    }

    private String[] getOpponentNames(Match match, MatchSettings matchSettings) {
        // see if we are in team one
        Team teamOne = match.getTeamOne();
        for (Player player : teamOne.getPlayers()) {
            if (player.getName().equals(this.playerName)) {
                // we are in team one
                return getOpponentNames(matchSettings, match.getTeamTwo());
            }
        }
        // see if we are in team two
        Team teamTwo = match.getTeamOne();
        for (Player player : teamTwo.getPlayers()) {
            if (player.getName().equals(this.playerName)) {
                // we are in team two
                return getOpponentNames(matchSettings, match.getTeamOne());
            }
        }
        // if here then we are not playing at all, no opponents then
        return new String[0];
    }

    private String[] getOpponentNames(MatchSettings matchSettings, Team team) {
        if (matchSettings.getIsDoubles()) {
            // we are playing doubles, return both names
            Player[] players = team.getPlayers();
            String[] toReturn = new String[players.length];
            for (int i = 0; i < toReturn.length; ++i) {
                toReturn[i] = players[i].getName();
            }
            return toReturn;
        }
        else {
            // just the first
            return new String[] {team.getPlayer(0).getName()};
        }
    }
}
