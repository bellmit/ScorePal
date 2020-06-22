package uk.co.darkerwaters.scorepal.application;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import uk.co.darkerwaters.scorepal.dataui.TeamNamer;
import uk.co.darkerwaters.scorepal.points.Sport;

public class ApplicationPreferences {

    private final SharedPreferences preferences;

    public ApplicationPreferences(Context context) {
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void wipeAllSettings() {
        // do our settings
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.clear().commit();
    }

    public void commitNow() {
        this.preferences.edit().commit();
    }

    public String getUserName() {
        return preferences.getString("username", "").trim();
    }

    public void setUserName(String value) { preferences.edit().putString("username", value.trim()).apply(); }

    public boolean getIsUseGoogleLogin() {
        return preferences.getBoolean("useGoogleLogin", false);
    }

    public void setIsUseGoogleLogin(boolean value) { preferences.edit().putBoolean("useGoogleLogin", value).apply(); }

    public boolean getIsUseContacts() {
        return preferences.getBoolean("useContacts", false);
    }

    public void setIsUseContacts(boolean value) { preferences.edit().putBoolean("useContacts", value).apply(); }

    public boolean getIsStoreLocations() {
        return preferences.getBoolean("storeLocations", false);
    }

    public void setIsStoreLocations(boolean value) { preferences.edit().putBoolean("storeLocations", value).apply(); }

    public Sport getLastSport() { return Sport.valueOf(preferences.getInt("lastSport", Sport.TENNIS.id)); }

    public void setLastSport(Sport value) { setLastSport(value, false); }

    public void setLastSport(Sport value, boolean isSetImmediate) {
        SharedPreferences.Editor editor = preferences.edit().putInt("lastSport", value.id);
        if (isSetImmediate) editor.commit(); else editor.apply();
    }

    public TeamNamer.TeamNamingMode getNamingStyle() {
        return TeamNamer.TeamNamingMode.valueOf(preferences.getString("namingMode", TeamNamer.TeamNamingMode.SURNAME_INITIAL.name()));
    }

    public void setNamingStyle(TeamNamer.TeamNamingMode value) { preferences.edit().putString("namingMode", value.name()).apply(); }

    public boolean getIsControlTeams() { return preferences.getBoolean("isControlTeams", true); }

    public void setIsControlTeams(boolean value) { preferences.edit().putBoolean("isControlTeams", value).apply(); }

    public boolean getIsControlVol() { return preferences.getBoolean("isControlVol", false); }

    public void setIsControlVol(boolean value) { preferences.edit().putBoolean("isControlVol", value).apply(); }

    public boolean getIsControlMedia() { return preferences.getBoolean("isControlMedia", false); }

    public boolean getIsAllowMedia() { return preferences.getBoolean("isAllowMedia", false); }

    public void setIsControlMedia(boolean value) { preferences.edit().putBoolean("isControlMedia", value).apply(); }

    public boolean getIsControlFlic1() {
        return preferences.getBoolean("isControlFlic1", false);
    }

    public void setIsControlFlic1(boolean value) { preferences.edit().putBoolean("isControlFlic1", value).apply(); }

    public boolean getIsControlFlic2() { return preferences.getBoolean("isControlFlic2", true); }

    public void setIsControlFlic2(boolean value) { preferences.edit().putBoolean("isControlFlic2", value).apply(); }

    public boolean isLogging() {
        return true;
    }

    public void setSoundButtonClick(boolean value) { preferences.edit().putBoolean("isSoundBtnClick", value).apply(); }

    public boolean getSoundButtonClick() { return preferences.getBoolean("isSoundBtnClick", false); }

    public void setSoundActionSpeak(boolean value) { preferences.edit().putBoolean("isSoundActionSpeak", value).apply(); }

    public boolean getSoundActionSpeak() { return preferences.getBoolean("isSoundActionSpeak", true); }

    public void setSoundAnnounceChange(boolean value) { preferences.edit().putBoolean("isSoundAnncChange", value).apply(); }

    public boolean getSoundAnnounceChange() { return preferences.getBoolean("isSoundAnncChange", true); }

    public void setSoundAnnounceVolume(int value, boolean isSetImmediate) {
        SharedPreferences.Editor editor = preferences.edit().putInt("isSoundAnncVol", value);
        if (isSetImmediate) editor.commit(); else editor.apply();
    }

    public boolean getSoundUseSpeakingNames() { return preferences.getBoolean("isSoundUseSpeakingNames", true); }

    public void setSoundUseSpeakingNames(boolean value) { preferences.edit().putBoolean("isSoundUseSpeakingNames", value).apply(); }

    public int getSoundAnnounceVolume() { return preferences.getInt("isSoundAnncVol", -1); }

    public void setSoundAnnounceChangePoints(boolean value) { preferences.edit().putBoolean("isSoundAnncChangePt", value).apply(); }

    public boolean getSoundAnnounceChangePoints() { return preferences.getBoolean("isSoundAnncChangePt", true); }

    public void setSoundAnnounceChangeEnds(boolean value) { preferences.edit().putBoolean("isSoundAnncChangeEnd", value).apply(); }

    public boolean getSoundAnnounceChangeEnds() { return preferences.getBoolean("isSoundAnncChangeEnd", true); }

    public void setSoundAnnounceChangeServer(boolean value) { preferences.edit().putBoolean("isSoundAnncChangeSvr", value).apply(); }

    public boolean getSoundAnnounceChangeServer() { return preferences.getBoolean("isSoundAnncChangeSvr", true); }

    public void setSoundAnnounceChangeScore(boolean value) { preferences.edit().putBoolean("isSoundAnncChangeScore", value).apply(); }

    public boolean getSoundAnnounceChangeScore() { return preferences.getBoolean("isSoundAnncChangeScore", false); }
}
