package uk.co.darkerwaters.scorepal.settings;

import android.content.SharedPreferences;

import uk.co.darkerwaters.scorepal.application.Application;
import uk.co.darkerwaters.scorepal.score.badminton.BadmintonMatchSettings;

public class SettingsBadminton extends SettingsMatch {

    private final String K_POINTS = "points";
    private final String K_GAMES = "games";

    public SettingsBadminton(Application app) {
        super(app, "BadmintonPref");
    }
    
    public void setPointsGoal(int points) {
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putInt(K_POINTS, points);
        editor.commit();
    }

    public int getPointsGoal() {
        return this.preferences.getInt(K_POINTS, BadmintonMatchSettings.K_DEFAULT_POINTS);
    }

    public void setGamesGoal(int games) {
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putInt(K_GAMES, games);
        editor.commit();
    }

    public int getGamesGoal() {
        return this.preferences.getInt(K_GAMES, BadmintonMatchSettings.K_DEFAULT_GAMES);
    }
}
