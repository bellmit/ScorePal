package uk.co.darkerwaters.scorepal.data;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.MatchService;
import uk.co.darkerwaters.scorepal.dataui.TeamNamer;
import uk.co.darkerwaters.scorepal.points.Sport;

public abstract class MatchSetup<TMatch extends Match> {

    public enum SetupChange {
        FIRST_TEAM_SERVER,
        TEAM_FIRST_SERVING,
        MATCH_TYPE,
        PLAYER_NAME, POINTS_STRUCTURE,
    }

    public enum MatchType {
        SINGLES(1), DOUBLES(2);
        public final int num;
        MatchType(int num) {
            this.num = num;
        }
    }

    public enum Player {
        P_ONE(0, R.string.title_playerOne),
        P_TWO(1, R.string.title_playerTwo),
        PT_ONE(2, R.string.title_partnerOne),
        PT_TWO(3, R.string.title_partnerTwo);

        public final int index;
        public final int stringRes;

        Player(int index, int stringRes) {
            this.index = index;
            this.stringRes = stringRes;
        }
    }

    public enum Team {
        T_ONE(0, R.string.title_teamOne),
        T_TWO(1, R.string.title_teamTwo);
        public final int index;
        public final int stringRes;
        Team(int index, int stringRes) {
            this.index = index;
            this.stringRes = stringRes;
        }
    }

    private final String[] playerNames = new String[Player.values().length];

    private final Sport sport;

    /** someone is serving first */
    private Team firstServingTeam = Team.T_ONE;
    /** each team has a first server */
    private Player[] firstServer = new Player[] { Player.P_ONE, Player.P_TWO };

    private MatchType type = MatchType.SINGLES;

    private TeamNamer teamNamer = new TeamNamer(this);

    public abstract TMatch createNewMatch();

    public abstract int[] getStraightPointsToWin();

    public MatchSetup(Sport sport) {
        this.sport = sport;
    }

    public final JSONObject getAsJSON() {
        JSONObject dataObject = new JSONObject();
        try {
            // store all our data into this object
            storeJSONData(dataObject);
            // and put this in a top-level named object of the classname
            JSONObject topLevel = new JSONObject();
            // version it
            topLevel.put("ver", 1);
            // store the sport so we can create the proper classes from it
            topLevel.put("sport", sport.name());
            // and the actual data
            topLevel.put("data", dataObject);
            // and return this top level object
            return topLevel;
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    public static MatchSetup CreateFromJSON(String jsonString) {
        MatchSetup newInstance = null;
        try {
            // get the data as JSON for a start
            JSONObject topLevel = new JSONObject(jsonString);
            // the top level has the sport, we can get the settings type from this
            Sport sport = Sport.valueOf(topLevel.getString("sport"));
            // there has to be an empty constructor for the most derived settings classes
            Constructor<? extends MatchSetup> ctor = sport.setupClass.getConstructor();
            // create the setup class from the default empty constructor for the settings
            newInstance = ctor.newInstance();
            // and set the member data from this
            newInstance.setFromJSON(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return newInstance;
    }

    public final void setFromJSON(String jsonString) {
        try {
            // the top level just contains everything
            JSONObject topLevel = new JSONObject(jsonString);
            // get data object
            JSONObject dataObject = topLevel.getJSONObject("data");
            // and set our data from this
            restoreFromJSON(dataObject, topLevel.getInt("ver"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void storeJSONData(JSONObject data) throws JSONException {
        data.put("type", this.type.name());
        for(int i = 0; i < this.playerNames.length; ++i) {
            data.put("player" + (i + 1), this.playerNames[i]);
        }
    }

    protected void restoreFromJSON(JSONObject data, int version) throws JSONException {
        this.type = MatchType.valueOf(data.getString("type"));
        for(int i = 0; i < this.playerNames.length; ++i) {
            this.playerNames[i] = data.optString("player" + (i + 1));
        }
    }

    public void informMatchSetupChanged(SetupChange change) {
        // every time the setup changes, the contents of the active match can change
        MatchService service = MatchService.GetRunningService();
        if (null != service) {
            // inform the service of this change
            service.onMatchSetupChanged(this, change);
        }
    }

    public TeamNamer getNamer() { return teamNamer; }

    public Team getFirstServingTeam() {
        return this.firstServingTeam;
    }

    public Player getFirstServingPlayer(Team team) {
        // return the player that serves first for this team
        return this.firstServer[team.index];
    }

    public void setFirstServingTeam(Team value) {
        if (this.firstServingTeam != value) {
            this.firstServingTeam = value;
            // this changes the setup
            informMatchSetupChanged(SetupChange.FIRST_TEAM_SERVER);
        }
    }

    public Team getPlayerTeam(Player player) {
        switch (player) {
            case P_ONE:
            case PT_ONE:
                return Team.T_ONE;
            case P_TWO:
            case PT_TWO:
                return Team.T_TWO;
        }
        return null;
    }

    public Team getOtherTeam(Team team) {
        if (team == MatchSetup.Team.T_ONE) {
            return Team.T_TWO;
        } else {
            return Team.T_ONE;
        }
    }

    public Player getOtherPlayer(Player player) {
        switch (player) {
            case P_ONE:
                return Player.PT_ONE;
            case PT_ONE:
                return Player.P_ONE;
            case P_TWO:
                return Player.PT_TWO;
            case PT_TWO:
                return Player.P_TWO;
        }
        return null;
    }

    public boolean isPlayerInTeam(Team team, Player player) {
        switch (team) {
            case T_ONE:
                return player == Player.P_ONE || player == Player.PT_ONE;
            case T_TWO:
                return player == Player.P_TWO || player == Player.PT_TWO;
        }
        return false;
    }

    public MatchSetup.Player getTeamPlayer(MatchSetup.Team team) {
        if (team == MatchSetup.Team.T_ONE) {
            return MatchSetup.Player.P_ONE;
        } else {
            return MatchSetup.Player.P_TWO;
        }
    }

    public MatchSetup.Player getTeamPartner(MatchSetup.Team team) {
        if (team == MatchSetup.Team.T_ONE) {
            return MatchSetup.Player.PT_ONE;
        } else {
            return MatchSetup.Player.PT_TWO;
        }
    }

    public void setFirstTeamServer(Player server) {
        Team playerTeam = getPlayerTeam(server);
        if (this.firstServer[playerTeam.index] != server) {
            // get the team for the player and set accordingly
            this.firstServer[playerTeam.index] = server;
            // this changes the setup
            informMatchSetupChanged(SetupChange.FIRST_TEAM_SERVER);
        }
    }

    public void correctPlayerErrors() {
        // changing the type from doubles to singles etc, can effect some settings
        // a player no longer playing could be serving - correct those here
        if (type == MatchType.SINGLES) {
            // this is singles, the first server has to be the player of that team
            if (this.firstServer[Team.T_ONE.index] != Player.P_ONE) {
                // partner can't be serving - this is a singles match
                this.firstServer[Team.T_ONE.index] = Player.P_ONE;
            }
            if (this.firstServer[Team.T_TWO.index] != Player.P_TWO) {
                // partner can't be serving - this is a singles match
                this.firstServer[Team.T_TWO.index] = Player.P_TWO;
            }
        }
    }

    public Sport getSport() {
        return this.sport;
    }

    public MatchType getType() {
        return type;
    }

    public void setType(MatchType type) {
        if (this.type != type) {
            this.type = type;
            // this changes the setup
            informMatchSetupChanged(SetupChange.MATCH_TYPE);
        }
    }

    public static boolean usernameEquals(String username, String compare) {
        if (username == null && compare == null) {
            return true;
        }
        else if (username != null && compare == null) {
            // not the same
            return false;
        }
        else if (username == null && compare != null) {
            // not the same
            return false;
        }
        else {
            return username.trim().equalsIgnoreCase(compare.trim());
        }
    }

    public void setUsernameInTeamOne(String userName) {
        // first move if they are playing in team two
        boolean isUserFound = false;
        if (usernameEquals(userName, this.playerNames[Player.P_TWO.index])
            || usernameEquals(userName, this.playerNames[Player.PT_TWO.index])) {
            // the user is playing in team two - boo
            String player = this.playerNames[Player.P_TWO.index];
            String partner = this.playerNames[Player.PT_TWO.index];
            // swap the names
            this.playerNames[Player.P_TWO.index] = this.playerNames[Player.P_ONE.index];
            this.playerNames[Player.PT_TWO.index] = this.playerNames[Player.PT_ONE.index];
            this.playerNames[Player.P_ONE.index] = player;
            this.playerNames[Player.PT_ONE.index] = partner;
            isUserFound = true;
        }
        // then move if they are the partner
        if (usernameEquals(userName, this.playerNames[Player.PT_ONE.index])) {
            // the user is the partner - less boo but still boo
            String player = this.playerNames[Player.P_ONE.index];
            this.playerNames[Player.P_ONE.index] = this.playerNames[Player.PT_ONE.index];
            this.playerNames[Player.PT_ONE.index] = player;
            isUserFound = true;
        }
        if (!isUserFound) {
            // the user isn't anywhere (or in P_ONE co-incidentally) set P_ONE to be perfect anyway
            this.playerNames[Player.P_ONE.index] = userName;
        }
    }

    public String getPlayerName(Player player) {
        return this.playerNames[player.index];
    }

    public void setPlayerName(String name, Player player) {
        boolean isInformChange = !usernameEquals(name, this.playerNames[player.index]);
        this.playerNames[player.index] = name.trim();
        if (isInformChange) {
            // this changes the setup
            informMatchSetupChanged(SetupChange.PLAYER_NAME);
        }
    }

    public String getTeamName(Context context, Team team) { return this.teamNamer.getTeamName(context, team); }
}
