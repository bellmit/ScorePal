package uk.co.darkerwaters.scorepal.players;

import android.content.Context;

import org.json.JSONArray;

import uk.co.darkerwaters.scorepal.activities.BaseActivity;
import uk.co.darkerwaters.scorepal.score.base.MatchMessage;

public class Team implements MatchMessage.Param {

    public static final String K_NAME_SEPARATOR = " / ";

    private Player[] players;
    private CourtPosition currentPosition;
    private CourtPosition initialPosition;

    private final int teamId;
    private String teamName;

    private Player servingPlayer;

    public Team(int teamId, Player[] players, CourtPosition initialPosition) {
        this.teamId = teamId;
        this.players = players;
        this.initialPosition = initialPosition;
        this.teamName = "";
    }

    public Team(Team toCopy) {
        // if there is no serving player, we want to have no serving player
        this.teamId = toCopy.teamId;
        this.servingPlayer = null;
        // copy all the players
        this.players = new Player[toCopy.players.length];
        for (int i = 0; i < this.players.length; ++i) {
            // copy the player
            this.players[i] = new Player(toCopy.players[i]);
            // is this the serving player?
            if (toCopy.players[i] == toCopy.servingPlayer) {
                // this new player is our serving player
                this.servingPlayer = this.players[i];
            }
        }
        // and all the normal members
        this.currentPosition = toCopy.currentPosition;
        this.initialPosition = toCopy.initialPosition;
        this.teamName = toCopy.teamName;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Team && ((Team)obj).teamId == this.teamId;
    }

    @Override
    public String toString() {
        return getTeamName();
    }

    public static int GetTeamId(int index) {
        // we want the index to be 0, 1, 2 etc
        return index;
    }

    public static int GetPlayerId(int teamId, int playerIndex) {
        // we want the index to be 0..1..2 or 10..11..12 etc in the team
        return (10 * teamId) + playerIndex;
    }

    private int getPlayerId(int playerIndex) {
        return GetPlayerId(this.teamId, playerIndex);
    }

    @Override
    public String serialiseToString(Context context) throws Exception {
        // we have some data in here, let's use a JSON array for nice
        JSONArray dataArray = new JSONArray();
        dataArray.put(players.length);
        // the serving player
        dataArray.put(this.servingPlayer == null ? 0 : this.servingPlayer.getPlayerId());
        // and all the players
        for (Player player : players) {
            dataArray.put(player.serialiseToString(context));
        }
        // and the simple stuff
        dataArray.put(this.currentPosition == null ? "" : this.currentPosition.toString());
        dataArray.put(this.initialPosition == null ? "" : this.initialPosition.toString());
        dataArray.put(this.teamName);
        // return all this as a string
        return BaseActivity.JSONToString(dataArray);
    }

    @Override
    public MatchMessage.Param deserialiseFromString(Context context, int version, String string) throws Exception {
        JSONArray dataArray = new JSONArray(string);
        // the number of players
        int noPlayers = dataArray.getInt(0); dataArray.remove(0);
        this.players = new Player[noPlayers];
        // the serving player - was stored as an integer - the id of the player serving
        int servingPlayerId = dataArray.getInt(0); dataArray.remove(0);
        this.servingPlayer = null;
        // the players themselves, the ID is simply the id of this team * the index + 1
        // so the id will be 1 and 2 for teamOne and 2 and 3 for teamTwo
        for (int i = 0; i < this.players.length; ++i) {
            this.players[i] = new Player(getPlayerId(i), "");
            // put the data on the player back
            this.players[i].deserialiseFromString(context, version, dataArray.getString(0));
            dataArray.remove(0);
            if (this.players[i].getPlayerId() == servingPlayerId) {
                // this is the serving player
                this.servingPlayer = this.players[i];
            }
        }
        // finally the simple stuff
        String positionString = dataArray.getString(0);
        this.currentPosition = positionString.isEmpty() ? null : CourtPosition.fromString(positionString); dataArray.remove(0);
        positionString = dataArray.getString(0);
        this.initialPosition = positionString.isEmpty() ? null : CourtPosition.fromString(positionString); dataArray.remove(0);
        this.teamName = dataArray.getString(0); dataArray.remove(0);

        // we are done
        return this;
    }

    public void setTeamName(String name) {
        this.teamName = name;
    }

    public String getTeamName() {
        return this.teamName;
    }

    public String getSpeakingTeamName() {
        // remove all the punctuation from the team name so there are no weird pauses in it.
        if (this.teamName == null || this.teamName.isEmpty()) {
            return this.teamName;
        }
        else {
            return this.teamName.replaceAll("[.]", "");
        }
    }

    public String getPlayerName(int playerIndex) {
        // the player name is in the player, which is fine, but that is got by getting
        // the player then the name of them, this is for getting the part of the player
        // that is represented in the team name - ie separated by the separator
        String[] split = this.teamName.split(K_NAME_SEPARATOR);
        if (playerIndex < split.length) {
            return split[playerIndex];
        }
        else {
            // no good
            return getPlayer(playerIndex).getName();
        }
    }

    public Player[] getPlayers() {
        return this.players;
    }

    public CourtPosition getInitialPosition() { return this.initialPosition; }

    public void setInitialCourtPosition(CourtPosition position) {
        this.initialPosition = position;
    }

    public void setCourtPosition(CourtPosition position) {
        this.currentPosition = position;
    }

    public CourtPosition getCourtPosition() {
        if (null == this.currentPosition) {
            return this.initialPosition;
        }
        else {
            return this.currentPosition;
        }
    }

    public boolean isServingPlayerSet() {
        return null != this.servingPlayer;
    }

    public void setServingPlayer(Player startingServer) {
        // override the member to set the player to start serving
        this.servingPlayer = startingServer;
    }

    public void resetServingPlayer() {
        this.servingPlayer = null;
    }

    public Player getServingPlayer() {
        if (null == this.servingPlayer && this.players.length > 0) {
            // no-one served yet, first to start is first in the list
            return this.players[0];
        }
        else {
            return this.servingPlayer;
        }
    }

    public Player getPlayer(int index) {
        if (index >= 0 && index < this.players.length) {
            return this.players[index];
        }
        else {
            return null;
        }
    }

    public int getPlayerIndex(Player player) {
        for (int i = 0; i < this.players.length; ++i) {
            if (this.players[i] == player) {
                // found it
                return i;
            }
        }
        return -1;
    }

    public Player getNextServer() {
        if (this.servingPlayer == null) {
            // no-one has served, return the first
            this.servingPlayer = getServingPlayer();
        }
        else {
            // return the next from the last server
            this.servingPlayer = getNextPlayer(this.servingPlayer);
        }
        return this.servingPlayer;
    }

    public Player getNextPlayer(Player player) {
        Player nextPlayer = null;
        boolean isPlayerFound = false;
        for (Player test : getPlayers()) {
            if (nextPlayer == null) {
                // just take the first for now
                nextPlayer = test;
            }
            else if (isPlayerFound) {
                // take this one instead, this is the one after
                nextPlayer = test;
                break;
            }
            // if this player is serving, we want the next one
            if (player == test) {
                isPlayerFound = true;
            }
        }
        return nextPlayer;
    }

    public boolean isPlayerInTeam(Player player) {
        boolean isPlayerFound = false;
        for (Player test : getPlayers()) {
            if (player == test) {
                isPlayerFound = true;
                break;
            }
        }
        return isPlayerFound;
    }
}
