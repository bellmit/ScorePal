package uk.co.darkerwaters.scorepal.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import java.text.SimpleDateFormat;
import java.util.Date;

import uk.co.darkerwaters.scorepal.application.Application;
import uk.co.darkerwaters.scorepal.application.Log;
import uk.co.darkerwaters.scorepal.score.MatchStatistics;
import uk.co.darkerwaters.scorepal.score.base.Sport;

public class Settings {

    private final SharedPreferences preferences;
    private final Application application;
    private SettingsMatch lastMatchSettings;

    private static final SimpleDateFormat StoreDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    private final String K_ISLOGGING = "isLogging";
    private final String K_ISFIRSTRUN = "isFirstRun";
    private final String K_ISASKFORCONTACTS = "isAskForContacts";
    private final String K_ISASKFORFILEACCESS = "isAskForFileAccess";
    private final String K_ISSTORELOCATIONS = "isStoreLocations";
    private final String K_ISALLOWCLIENTMESSAGES = "isAllowClientsToChangeScore";

    private final String K_SPORTLASTPLAYED = "lastPlayed";

    private final String K_SELFNAME = "selfName";
    private final String K_ISSIGNEDON = "signedOn";
    private final String K_SYSTEMELFNAME = "systemSelfName";
    private final String K_SELFIMAGE = "selfImage";
    private final String K_SELFNAMEOVERRIDDEN = "isSelfNameOverridden";

    public Settings(Application app) {
        // get all the variables
        this.application = app;
        this.preferences = app.getSharedPreferences("MainPref", 0); // 0 - for private mode
        this.lastMatchSettings = null;
    }

    public void wipeAllSettings() {
        // do our settings
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.clear().commit();
        // do the sounds settings
        new SettingsSounds(this.application).wipeAllSettings();
        // do the control settings
        new SettingsControl(this.application).wipeAllSettings();
        // and the match settings
        for (SettingsMatch settingsMatch : getAllMatchSettings()) {
            settingsMatch.wipeAllSettings();
        }
    }

    public boolean isLogging() {
        return this.preferences.getBoolean(K_ISLOGGING, false);
    }

    public void setIsLogging(boolean isLogging) {
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putBoolean(K_ISLOGGING, isLogging);
        editor.commit();
    }

    public boolean isFirstRun() {
        return this.preferences.getBoolean(K_ISFIRSTRUN, false);
    }

    public void setIsFirstRun(boolean isFirstRun) {
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putBoolean(K_ISFIRSTRUN, isFirstRun);
        editor.commit();
    }

    public void setSelfName(String name, String imageUrl, boolean isOverriding, Context context) {
        if (!isOverriding) {
            // this is the system name
            setSystemSelfName(name);
        }
        if (isOverriding || !isSelfNameOverridden()) {
            // we are overriding the system name with that passed in
            // or we don't want to, just put the new one in the this.preferences
            SharedPreferences.Editor editor = this.preferences.edit();
            editor.putString(K_SELFNAME, name);
            editor.putString(K_SELFIMAGE, imageUrl);
            editor.putBoolean(K_SELFNAMEOVERRIDDEN, isOverriding);
            editor.commit();
        }
        // now there is a new name, we need to initialise the stats for this player
        String newSelfName = getSelfName();
        // it might be a bit annoying but let's force player one to be this person
        // every time
        for (SettingsMatch settingsMatch : getAllMatchSettings()) {
            settingsMatch.setPlayerName(newSelfName, 0, 0);
        }
        // and initialise the stats for this player (us)
        MatchStatistics.GetInstance(application, context);
    }

    public boolean isSignedOn() {
        return this.preferences.getBoolean(K_ISSIGNEDON, false);
    }

    public void setIsSignedOn(boolean isSignedOn) {
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putBoolean(K_ISSIGNEDON, isSignedOn);
        editor.commit();
    }

    public SettingsMatch[] getAllMatchSettings() {
        return new SettingsMatch[] {
                new SettingsPingPong(this.application),
                new SettingsPoints(this.application),
                new SettingsTennis(this.application),
                new SettingsSquash(this.application),
                new SettingsBadminton(this.application)
        };
    }

    public String getSelfName() {
        String selfName = this.preferences.getString(K_SELFNAME, "");
        String systemSelfName = getSystemSelfName();
        if (isSelfNameOverridden() || null == systemSelfName || systemSelfName.isEmpty()) {
            // return the self name from the this.preferences
            return selfName;
        }
        else {
            // return the system self name
            return systemSelfName;
        }
    }

    public String getSystemSelfName() {
        return this.preferences.getString(K_SYSTEMELFNAME, "");
    }

    public void setSystemSelfName(String name) {
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putString(K_SYSTEMELFNAME, name);
        editor.commit();
    }

    public String getSelfImage() {
        return this.preferences.getString(K_SELFIMAGE, "");
    }

    public Bitmap getSelfBitmap() {
        return Application.GetBitmapFromUrl(getSelfImage());
    }

    public boolean isSelfNameOverridden() {
        return this.preferences.getBoolean(K_SELFNAMEOVERRIDDEN, false);
    }

    public boolean getIsRequestContactsPermission() {
        return this.preferences.getBoolean(K_ISASKFORCONTACTS, true);
    }

    public void setIsRequestContactsPermission(boolean isAskForContacts) {
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putBoolean(K_ISASKFORCONTACTS, isAskForContacts);
        editor.commit();
    }

    public boolean getIsRequestFileAccessPermission() {
        return this.preferences.getBoolean(K_ISASKFORFILEACCESS, true);
    }

    public void setIsRequestFileAccessPermission(boolean isAskForFileAccess) {
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putBoolean(K_ISASKFORFILEACCESS, isAskForFileAccess);
        editor.commit();
    }

    public boolean getIsAllowClientsToChangeScore() {
        return this.preferences.getBoolean(K_ISALLOWCLIENTMESSAGES, true);
    }

    public void setIsAllowClientsToChangeScore(boolean isAllowClientMessages) {
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putBoolean(K_ISALLOWCLIENTMESSAGES, isAllowClientMessages);
        editor.commit();
    }

    public boolean getIsStoreLocations() {
        return this.preferences.getBoolean(K_ISSTORELOCATIONS, true);
    }

    public void setIsStoreLocations(boolean isStoreLocations) {
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putBoolean(K_ISSTORELOCATIONS, isStoreLocations);
        editor.commit();
    }

    public void setSportLastPlayedNow(Context context, Sport sport) {
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putString(sport.getTitle(context) + K_SPORTLASTPLAYED, StoreDateFormat.format(new Date()));
        editor.commit();
    }

    public Date getSportLastPlayed(Context context, Sport sport) {
        String dateVal = this.preferences.getString(sport.getTitle(context) + K_SPORTLASTPLAYED, "");
        Date toReturn = null;
        if (null != dateVal && !dateVal.isEmpty()) {
            try {
                toReturn = StoreDateFormat.parse(dateVal);
            }
            catch (Exception e) {
                Log.error("Settings date not properly formatted", e);
            }
        }
        return toReturn;
    }
}
