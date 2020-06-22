package uk.co.darkerwaters.scorepal.settings;

import android.content.SharedPreferences;

import uk.co.darkerwaters.scorepal.activities.fragments.FragmentTeam;
import uk.co.darkerwaters.scorepal.application.Application;

public abstract class SettingsMatch {

    protected final SharedPreferences preferences;

    private final String K_ISDOUBLES = "isDoubles";
    private final String K_PLAYERNAME = "playerName";
    private final String K_TEAMNAMINGMODE = "currentTeamNamingMode";

    public SettingsMatch(Application app, String preferencesFile) {
        // use the preferences from the parent class
        this.preferences = app.getSharedPreferences(preferencesFile, 0); // 0 - for private mode
    }

    public void wipeAllSettings() {
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.clear().commit();
    };

    public FragmentTeam.TeamNamingMode getCurrentTeamNameMode() {
        String currentTeamNamingMode = this.preferences.getString(K_TEAMNAMINGMODE, FragmentTeam.TeamNamingMode.SURNAME_INITIAL.toString());
        return FragmentTeam.TeamNamingMode.valueOf(currentTeamNamingMode);
    }

    public void setCurrentTeamNameMode(FragmentTeam.TeamNamingMode newMode) {
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putString(K_TEAMNAMINGMODE, newMode.toString());
        editor.commit();
    }

    public boolean getIsDoubles() {
        return this.preferences.getBoolean(K_ISDOUBLES, false);
    }

    public void setIsDoubles(boolean isDoubles) {
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putBoolean(K_ISDOUBLES, isDoubles);
        editor.commit();
    }

    public String getPlayerName(int teamIndex, int playerIndex, String defaultName) {
        return this.preferences.getString(K_PLAYERNAME + "-" + teamIndex + "-" + playerIndex, defaultName);
    }

    public void setPlayerName(String name, int teamIndex, int playerIndex) {
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putString(K_PLAYERNAME + "-" + teamIndex + "-" + playerIndex, name);
        editor.commit();
    }
}
