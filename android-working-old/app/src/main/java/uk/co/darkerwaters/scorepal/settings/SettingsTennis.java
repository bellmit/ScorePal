package uk.co.darkerwaters.scorepal.settings;

import android.content.SharedPreferences;

import uk.co.darkerwaters.scorepal.application.Application;
import uk.co.darkerwaters.scorepal.score.tennis.TennisSets;

public class SettingsTennis extends SettingsMatch {

    private final String K_SETS = "sets";
    private final String K_FINALSETTIETARGET = "finalSetTieTarget";
    private final String K_GAMESINSET = "gamesInSet";
    private final String K_ISDECIDERONDEUCE = "deciderOnDeuce";

    public SettingsTennis(Application app) {
        super(app, "TennisPref");
    }

    public void setIsDecidingPointOnDeuce(boolean isDeciderOnDeuce) {
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putBoolean(K_ISDECIDERONDEUCE, isDeciderOnDeuce);
        editor.commit();
    }

    public boolean getIsDecidingPointOnDeuce() {
        return this.preferences.getBoolean(K_ISDECIDERONDEUCE, false);
    }

    public void setFinalSetTieTarget(int finalSetTieTarget) {
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putInt(K_FINALSETTIETARGET, finalSetTieTarget);
        editor.commit();
    }

    public int getFinalSetTieTarget() {
        return this.preferences.getInt(K_FINALSETTIETARGET, -1);
    }

    public void setGamesInSet(int gamesInSet) {
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putInt(K_GAMESINSET, gamesInSet);
        editor.commit();
    }

    public int getGamesInSet() {
        return this.preferences.getInt(K_GAMESINSET, 6);
    }

    public TennisSets getTennisSets() {
        int setsValue = this.preferences.getInt(K_SETS, TennisSets.K_DEFAULT.val);
        // set the member from this value
        return TennisSets.fromValue(setsValue);
    }

    public void setTennisSets(TennisSets sets) {
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putInt(K_SETS, sets.val);
        editor.commit();
    }
}
