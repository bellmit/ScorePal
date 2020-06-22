package uk.co.darkerwaters.scorepal.settings;

import android.content.SharedPreferences;

import uk.co.darkerwaters.scorepal.application.Application;
import uk.co.darkerwaters.scorepal.score.points.PointsMatchSettings;

public class SettingsPoints extends SettingsMatch {

    private final String K_POINTS = "points";
    private final String K_ISTWOPOINTSAHEADREQ = "isTwoPointsAhead";
    private final String K_POINTSCHANGEEND = "pointsToChangeEnds";
    private final String K_POINTSCHANGESERVER = "pointsToChangeServer";

    public SettingsPoints(Application app) {
        super(app, "PointsPref");
    }

    public void setIsTwoPointsAheadRequired(boolean isTwoPointsAheadRequired) {
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putBoolean(K_ISTWOPOINTSAHEADREQ, isTwoPointsAheadRequired);
        editor.commit();
    }

    public boolean getIsTwoPointsAheadRequired() {
        return this.preferences.getBoolean(K_ISTWOPOINTSAHEADREQ, true);
    }

    public void setPointsToChangeEnds(int points) {
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putInt(K_POINTSCHANGEEND, points);
        editor.commit();
    }

    public int getPointsToChangeEnds() {
        return this.preferences.getInt(K_POINTSCHANGEEND, 6);
    }

    public void setPointsToChangeServer(int points) {
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putInt(K_POINTSCHANGESERVER, points);
        editor.commit();
    }

    public int getPointsToChangeServer() {
        return this.preferences.getInt(K_POINTSCHANGESERVER, 2);
    }

    public void setPointsGoal(int points) {
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putInt(K_POINTS, points);
        editor.commit();
    }

    public int getPointsGoal() {
        return this.preferences.getInt(K_POINTS, PointsMatchSettings.K_DEFAULT_POINTS);
    }
}
