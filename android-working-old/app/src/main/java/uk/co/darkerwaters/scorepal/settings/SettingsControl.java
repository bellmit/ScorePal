package uk.co.darkerwaters.scorepal.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import java.text.SimpleDateFormat;
import java.util.Date;

import uk.co.darkerwaters.scorepal.activities.fragments.FragmentTeam;
import uk.co.darkerwaters.scorepal.application.Application;
import uk.co.darkerwaters.scorepal.score.MatchStatistics;
import uk.co.darkerwaters.scorepal.score.base.Sport;
import uk.co.darkerwaters.scorepal.score.pingpong.PingPongMatchSettings;
import uk.co.darkerwaters.scorepal.score.points.PointsMatchSettings;
import uk.co.darkerwaters.scorepal.score.tennis.TennisSets;

public class SettingsControl {

    private final SharedPreferences preferences;

    private final String K_ISCONTROLUSEFLIC1 = "isControlFlic";
    private final String K_ISCONTROLUSEFLIC2 = "isControlFlic2";
    private final String K_ISCONTROLUSEPUCK = "isControlPuck";
    private final String K_ISCONTROLUSEVOL = "isControlVol";
    private final String K_ISCONTROLUSEMEDIA = "isControlMedia";
    private final String K_ISCONTROLTEAMS = "isControlTeams";

    public SettingsControl(Application app) {
        // get all the variables
        this.preferences = app.getSharedPreferences("ControlPref", 0); // 0 - for private mode
    }

    public void wipeAllSettings() {
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.clear().commit();
    }

    public boolean getIsControlUseFlic1() {
        return this.preferences.getBoolean(K_ISCONTROLUSEFLIC1, false);
    }

    public boolean getIsControlUseFlic2() {
        return this.preferences.getBoolean(K_ISCONTROLUSEFLIC2, false);
    }

    public boolean getIsControlUsePuck() {
        return this.preferences.getBoolean(K_ISCONTROLUSEPUCK, true);
    }

    public boolean getIsControlUseVol() {
        return this.preferences.getBoolean(K_ISCONTROLUSEVOL, true);
    }

    public boolean getIsControlUseMedia() {
        return this.preferences.getBoolean(K_ISCONTROLUSEMEDIA, false);
    }

    public boolean getIsControlTeams() {
        return this.preferences.getBoolean(K_ISCONTROLTEAMS, true);
    }

    public void setIsControlUseFlic1(boolean isControl) {
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putBoolean(K_ISCONTROLUSEFLIC1, isControl);
        if (isControl) {
            // can't use 2 then...
            editor.putBoolean(K_ISCONTROLUSEFLIC2, false);
        }
        editor.commit();
    }

    public void setIsControlUseFlic2(boolean isControl) {
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putBoolean(K_ISCONTROLUSEFLIC2, isControl);
        if (isControl) {
            // can't use 1 then...
            editor.putBoolean(K_ISCONTROLUSEFLIC1, false);
        }
        editor.commit();
    }

    public void setIsControlUsePuck(boolean isControl) {
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putBoolean(K_ISCONTROLUSEPUCK, isControl);
        editor.commit();
    }

    public void setIsControlUseVol(boolean isControl) {
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putBoolean(K_ISCONTROLUSEVOL, isControl);
        editor.commit();
    }

    public void setIsControlUseMedia(boolean isControl) {
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putBoolean(K_ISCONTROLUSEMEDIA, isControl);
        editor.commit();
    }

    public void setIsControlTeams(boolean isControl) {
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putBoolean(K_ISCONTROLTEAMS, isControl);
        editor.commit();
    }
}
