package uk.co.darkerwaters.scorepal.players;

import android.content.Context;

import org.json.JSONArray;

import uk.co.darkerwaters.scorepal.activities.BaseActivity;
import uk.co.darkerwaters.scorepal.score.base.MatchMessage;

public class Player implements MatchMessage.Param {

    private boolean isServing;
    private final int playerId;
    private String playerName;

    public Player(int playerId) {
        this(playerId, "");
    }

    public Player(int playerId, String playerName) {
        this.playerId = playerId;
        this.playerName = playerName;
        // ensure all our defaults are set here
        isServing = false;
    }

    public Player(Player toCopy) {
        this.playerId = toCopy.playerId;
        this.playerName = toCopy.playerName;
        this.isServing = toCopy.isServing;
    }

    @Override
    public String serialiseToString(Context context) throws Exception {
        // we have some data in here, let's use a JSON array for nice
        JSONArray dataArray = new JSONArray();
        // and the simple stuff
        dataArray.put(this.playerName);
        dataArray.put(this.isServing);
        // return all this as a string
        return BaseActivity.JSONToString(dataArray);
    }

    @Override
    public MatchMessage.Param deserialiseFromString(Context context, int version, String string) throws Exception {
        JSONArray dataArray = new JSONArray(string);
        switch (version) {
            case 1:
                // load the version 1 stuff
                this.playerName = dataArray.getString(0); dataArray.remove(0);
                this.isServing = dataArray.getBoolean(0); dataArray.remove(0);
                break;
        }
        // we are done
        return this;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Player && ((Player)obj).playerId == this.playerId;
    }

    public int getPlayerId() { return this.playerId; }

    public void setName(String name) {
        this.playerName = name;
    }

    public String getName() {
        return this.playerName;
    }

    public String getSpeakingName() {
        // remove all the punctuation from the name so there are no weird pauses in it.
        if (this.playerName == null || this.playerName.isEmpty()) {
            return this.playerName;
        }
        else {
            return this.playerName.replaceAll("[.]", "");
        }
    }

    public void setIsServing(boolean isServing) {
        this.isServing = isServing;
    }

    public boolean getIsServing() {
        return this.isServing;
    }
}
