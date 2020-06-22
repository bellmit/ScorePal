package uk.co.darkerwaters.scorepal.settings;

import android.content.SharedPreferences;

import uk.co.darkerwaters.scorepal.application.Application;
import uk.co.darkerwaters.scorepal.score.pingpong.PingPongMatchSettings;

public class SettingsPingPong extends SettingsMatch {

    private final String K_PPPOINTS = "pp_points";
    private final String K_PPROUNDS = "pp_rounds";
    private final String K_EXPEDITESYSTEM = "isExpediteSystem";
    private final String K_EXPEDITEMINUTES = "expediteSystemMinutes";
    private final String K_EXPEDITEPOINTS = "expediteSystemPoints";

    public SettingsPingPong(Application app) {
        super(app, "PingPongPref");
    }
    
    public void setPointsGoal(int points) {
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putInt(K_PPPOINTS, points);
        editor.commit();
    }

    public int getPointsGoal() {
        return this.preferences.getInt(K_PPPOINTS, PingPongMatchSettings.K_DEFAULT_POINTS);
    }

    public void setRoundsGoal(int rounds) {
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putInt(K_PPROUNDS, rounds);
        editor.commit();
    }

    public int getRoundsGoal() {
        return this.preferences.getInt(K_PPROUNDS, PingPongMatchSettings.K_DEFAULT_ROUNDS);
    }

    public void setExpediteSystemMinutes(int minutes) {
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putInt(K_EXPEDITEMINUTES, minutes);
        editor.commit();
    }

    public int getExpediteSystemMinutes() {
        return this.preferences.getInt(K_EXPEDITEMINUTES, PingPongMatchSettings.K_DEFAULT_EXP_MINUTES);
    }

    public void setExpediteSystemPoints(int points) {
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putInt(K_EXPEDITEPOINTS, points);
        editor.commit();
    }

    public int getExpediteSystemPoints() {
        return this.preferences.getInt(K_EXPEDITEPOINTS, PingPongMatchSettings.K_DEFAULT_EXP_POINTS);
    }

    public boolean getExpediteSystemEnabled() {
        return this.preferences.getBoolean(K_EXPEDITESYSTEM, PingPongMatchSettings.K_DEFAULT_EXP_ENABLED);
    }

    public void setExpediteSystemEnabled(boolean isEnabled) {
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putBoolean(K_EXPEDITESYSTEM, isEnabled);
        editor.commit();
    }
}
