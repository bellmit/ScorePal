package uk.co.darkerwaters.scorepal.score;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import uk.co.darkerwaters.scorepal.activities.BaseActivity;
import uk.co.darkerwaters.scorepal.application.Log;
import uk.co.darkerwaters.scorepal.players.CourtPosition;
import uk.co.darkerwaters.scorepal.players.Player;
import uk.co.darkerwaters.scorepal.players.Team;
import uk.co.darkerwaters.scorepal.score.base.MatchMessage;
import uk.co.darkerwaters.scorepal.score.base.Sport;

public abstract class MatchSettings<T extends Match> implements MatchMessage.Param {

    private static final SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    private Team startingTeam;
    private final Player[] startingServers;
    private final Team[] teams;
    private String description;
    private boolean isDoubles;
    private String matchPlayedDate;
    private final CourtPosition[] startingEnds;
    private final Sport sport;
    private int scoreGoal;

    public MatchSettings(Sport sport) {
        // this is only for a single, immutable sport
        this.sport = sport;
        // setup the teams, will replace players in the structures as required
        int teamOneId = Team.GetTeamId(0);
        int teamTwoId = Team.GetTeamId(1);
        this.teams = new Team[] {
                new Team(teamOneId,
                        new Player[] {
                        new Player(Team.GetPlayerId(teamOneId, 0)),
                        new Player(Team.GetPlayerId(teamOneId, 1))
                }, CourtPosition.DEFAULT),
                new Team(teamTwoId,
                        new Player[] {
                        new Player(Team.GetPlayerId(teamTwoId, 0)),
                        new Player(Team.GetPlayerId(teamTwoId, 1))
                }, CourtPosition.DEFAULT.getNext()),
        };
        // set the description
        this.description = "A match played";

        // make the lists
        this.startingServers = new Player[this.teams.length];
        this.startingEnds = new CourtPosition[this.teams.length];

        // and set all the defaults
        resetSettings();
    }

    public void resetSettings() {
        // setup the starting server
        Arrays.fill(this.startingServers, null);
        // and the starting ends
        Arrays.fill(this.startingEnds, null);
        // set the default starting team to start with each time
        this.startingTeam = this.teams[0];
    }


    public abstract T createMatch();

    public Sport getSport() {
        return this.sport;
    }

    public int getScoreGoal() {
        return this.scoreGoal;
    }

    public void setScoreGoal(int goal) {
        this.scoreGoal = goal;
    }

    public Team[] getTeams() {
        return this.teams;
    }

    @Override
    public String serialiseToString(Context context) throws Exception {
        // this is required to send this data around and put it into a file for storage
        // because we have lots of data, let's use JSON to be helpful
        JSONArray dataArray = new JSONArray();
        // use the helper function
        serialiseToJson(context, dataArray);
        return BaseActivity.JSONToString(dataArray);
    }

    public void serialiseToJson(Context context, JSONArray dataArray) throws JSONException {
        // put all our data in to the array
        dataArray.put(this.sport.getTitle(context));
        dataArray.put(this.scoreGoal);
        dataArray.put(this.description);
        dataArray.put(this.isDoubles);
        dataArray.put(this.matchPlayedDate);

        // do the team names
        dataArray.put(getTeamOne().getTeamName());
        dataArray.put(getTeamTwo().getTeamName());

        // do the player names
        dataArray.put(getPlayerOne().getName());
        dataArray.put(getPlayerOnePartner().getName());
        dataArray.put(getPlayerTwo().getName());
        dataArray.put(getPlayerTwoPartner().getName());

        // starting servers
        dataArray.put(getTeamOne().getPlayerIndex(this.startingServers[0]));
        dataArray.put(getTeamTwo().getPlayerIndex(this.startingServers[1]));
        // starting ends
        dataArray.put(this.startingEnds[0] == null ? -1 : this.startingEnds[0].ordinal());
        dataArray.put(this.startingEnds[1] == null ? -1 : this.startingEnds[1].ordinal());

        // and do the starting team as a nice number to
        dataArray.put(this.startingTeam.equals(getTeamOne()) ? 1 : 2);
    }

    public static MatchSettings createFromSerialisedString(Context context, int version, String serialisedString) throws Exception {
        // the serialised string is all the data we serialised, so it contains the sport
        // that we are, so we can use this to create ourselves
        JSONArray dataArray = new JSONArray(serialisedString);
        Sport sport = Sport.from(dataArray.getString(0), context);
        MatchSettings settings = sport.createMatchSettings(context);
        settings.deserialiseFromJson(context, version, dataArray);
        return settings;
    }

    @Override
    public MatchSettings deserialiseFromString(Context context, int version, String string) throws Exception {
        return deserialiseFromJson(context, version, new JSONArray(string));
    }

    public MatchSettings deserialiseFromJson(Context context, int version, JSONArray dataArray) throws JSONException {
        switch (version) {
            case 1:
                // load the version 1 stuff
                Sport sport = Sport.from(dataArray.getString(0), context);
                dataArray.remove(0);
                if (sport != this.sport) {
                    // loaded the wrong thing
                    Log.error("loaded sport of " + sport + " into settings for " + this.sport);
                }
                this.scoreGoal = dataArray.getInt(0);
                dataArray.remove(0);
                this.description = dataArray.getString(0);
                dataArray.remove(0);
                this.isDoubles = dataArray.getBoolean(0);
                dataArray.remove(0);
                this.matchPlayedDate = dataArray.getString(0);
                dataArray.remove(0);

                // do the team names
                setTeamOneName(dataArray.getString(0));
                dataArray.remove(0);
                setTeamTwoName(dataArray.getString(0));
                dataArray.remove(0);

                // do the player names
                setPlayerOneName(dataArray.getString(0));
                dataArray.remove(0);
                setPlayerOnePartnerName(dataArray.getString(0));
                dataArray.remove(0);
                setPlayerTwoName(dataArray.getString(0));
                dataArray.remove(0);
                setPlayerTwoPartnerName(dataArray.getString(0));
                dataArray.remove(0);

                // starting servers
                this.startingServers[0] = getTeamOne().getPlayer(dataArray.getInt(0));
                dataArray.remove(0);
                this.startingServers[1] = getTeamTwo().getPlayer(dataArray.getInt(0));
                dataArray.remove(0);
                // starting ends
                int position = dataArray.getInt(0);
                dataArray.remove(0);
                this.startingEnds[0] = position < 0 ? null : CourtPosition.values()[position];
                position = dataArray.getInt(0);
                dataArray.remove(0);
                this.startingEnds[1] = position < 0 ? null : CourtPosition.values()[position];

                // and do the starting team as a nice number to
                // and do the starting team
                this.startingTeam = dataArray.getInt(0) == 1 ? getTeamOne() : getTeamTwo();
                dataArray.remove(0);
                break;
        }

        // and return the param created - just little old us
        return this;
    }

    public Team getStartingTeam() { return this.startingTeam; }

    public Team getReceivingTeam() {
        for (Team team : this.teams) {
            if (team != this.startingTeam) {
                // this is the other team
                return team;
            }
        }
        Log.error("failed to find the receiving team in the match settings");
        return this.teams[1];
    }

    public void setStartingTeam(Team startingTeam) {
        this.startingTeam = startingTeam;
    }

    public Player getStartingServer(Team team) {
        int teamIndex = findTeamIndex(team);
        if (null == this.startingServers[teamIndex]) {
            // there isn't one specified
            return team.getServingPlayer();
        }
        else {
            // return the one specified
            return this.startingServers[teamIndex];
        }
    }

    public CourtPosition getStartingEnd(Team team) {
        int teamIndex = findTeamIndex(team);
        if (null == this.startingEnds[teamIndex]) {
            // there isn't one specified
            return team.getCourtPosition();
        }
        else {
            // return the one specified
            return this.startingEnds[teamIndex];
        }
    }

    protected int findTeamIndex(Team team) {
        int teamIndex;
        if (team.equals(getTeamOne())) {
            teamIndex = 0;
        }
        else if (team.equals(getTeamTwo())) {
            teamIndex = 1;
        }
        else {
            Log.error("Searching for a team in match settings that is not there");
            teamIndex = 0;
        }
        return teamIndex;
    }

    public CourtPosition[] getStartingEnds() {
        return this.startingEnds;
    }

    public String getMatchId(Context context) {
        return this.matchPlayedDate + "_" + this.sport.getTitle(context);
    }

    public static Date DateFromMatchId(String matchId) {
        Date played = null;
        if (null != matchId) {
            int sportSep = matchId.indexOf('_');
            String dateString = matchId;
            if (sportSep >= 0) {
                // there is an underscore, after this is the sport, before is the date
                dateString = matchId.substring(0, sportSep);
            }
            try {
                played = fileDateFormat.parse(dateString);
            } catch (ParseException e) {
                Log.error("Failed to create the match date from the match id " + matchId, e);
            }
        }
        return played;
    }

    public static Sport SportFromMatchId(String matchId, Context context) {
        Sport sport = null;
        if (null != matchId) {
            int sportSep = matchId.indexOf('_');
            String sportString = matchId;
            if (sportSep >= 0) {
                // there is an underscore, after this is the sport, before is the date
                sportString = matchId.substring(sportSep + 1);
            }
            try {
                sport = Sport.from(sportString, context);
            } catch (Exception e) {
                Log.error("Failed to create the sport from the match id " + matchId, e);
            }
        }
        return sport;
    }

    public static boolean isMatchIdValid(String matchId) {
        boolean isValid = false;
        try {
            fileDateFormat.parse(matchId);
            isValid = true;
        } catch (ParseException e) {
            // whatever, just isn't valid is all
        }
        return isValid;
    }

    public Date getMatchPlayedDate() {
        return DateFromMatchId(this.matchPlayedDate);
    }

    public boolean getIsDoubles() {
        return this.isDoubles;
    }

    public Team getTeamOne() {
        return this.teams[0];
    }

    public Team getTeamTwo() {
        return this.teams[1];
    }

    public Player getPlayerOne() {
        return this.teams[0].getPlayers()[0];
    }

    public Player getPlayerTwo() {
        return this.teams[1].getPlayers()[0];
    }

    public Player getPlayerOnePartner() {
        return this.teams[0].getPlayers()[1];
    }

    public Player getPlayerTwoPartner() {
        return this.teams[1].getPlayers()[1];
    }

    public void setTeamOneName(String name) {
        // can change names whenever we like... just change it
        this.teams[0].setTeamName(name);
    }

    public void setTeamTwoName(String name) {
        // can change names whenever we like... just change it
        this.teams[1].setTeamName(name);
    }

    public void setPlayerOneName(String name) {
        // can change names whenever we like... just change it
        this.teams[0].getPlayers()[0].setName(name);
    }

    public void setPlayerOnePartnerName(String name) {
        // can change names whenever we like... just change it
        this.teams[0].getPlayers()[1].setName(name);
    }

    public void setPlayerTwoName(String name) {
        // can change names whenever we like... just change it
        this.teams[1].getPlayers()[0].setName(name);
    }

    public void setPlayerTwoPartnerName(String name) {
        // can change names whenever we like... just change it
        this.teams[1].getPlayers()[1].setName(name);
    }

    public void setMatchPlayedDate(Date date) {
        this.matchPlayedDate = fileDateFormat.format(date);
    }

    public void setIsDoubles(boolean isDoubles) {
        this.isDoubles = isDoubles;
        // when we change, we might have messed with the servers (the partner starting)
        // so we need to reset this here
        for (Team team : this.teams) {
            team.resetServingPlayer();
        }
    }

    public void cycleTeamStartingEnds() {
        // set the starting ends for each team
        for (int i = 0; i < this.teams.length; ++i) {
            CourtPosition newPosition = this.teams[i].getCourtPosition().getNext();
            this.teams[i].setInitialCourtPosition(newPosition);
            // and remember this for when we have to reset back to the start of the match
            this.startingEnds[i] = newPosition;
        }
    }

    public void cycleStartingServer() {
        // cycle the serving player
        if (getIsDoubles()) {
            // we are playing doubles, get the other team, then the other server, first
            // we can set the server on the current serving team to remember they did
            Team startingTeam = getStartingTeam();
            startingTeam.setServingPlayer(startingTeam.getServingPlayer());
            // now get the other team
            Team newTeam = getReceivingTeam();
            // and get the new server on this new team
            newTeam.getNextServer();
            // and set this team to start
            setStartingTeam(newTeam);
        }
        else {
            // just set the other team to serve
            setStartingTeam(getReceivingTeam());
        }
    }

    public void setTeamStartingServer(Player startingServer) {
        // set the starting server for their team

        //TODO you can also do this on the first game of each set, choose who starts
        // on the settings, but also on the score to update the server accordingly
        for (int i = 0; i < this.teams.length; ++i) {
            if (this.teams[i].isPlayerInTeam(startingServer)) {
                // found their team, set their starting server
                this.teams[i].setServingPlayer(startingServer);
                // also remember this for when we have to reset back to the start of the match
                this.startingServers[i] = startingServer;
                break;
            }
        }
    }

    public static boolean isFileDatesSame(Date fileDate1, Date fileDate2) {
        // compare only up to seconds as only up to seconds stored in the filename
        // for simplicities sake we can use the same formatter we use for the filename and compare strings
        if (fileDate1 != null && fileDate2 == null) {
            return false;
        }
        else if (fileDate1 == null && fileDate2 != null) {
            return false;
        }
        else if (fileDate1 == null && fileDate2 == null) {
            return true;
        }
        else {
            // do the actual comparing then
            String stringDate1 = fileDateFormat.format(fileDate1);
            String stringDate2 = fileDateFormat.format(fileDate2);
            return stringDate1.equals(stringDate2);
        }
    }
}
