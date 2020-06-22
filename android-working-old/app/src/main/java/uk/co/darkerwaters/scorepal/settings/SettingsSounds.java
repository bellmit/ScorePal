package uk.co.darkerwaters.scorepal.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;

import uk.co.darkerwaters.scorepal.application.Application;

public class SettingsSounds {

    private static final int K_MIN_VOLUME = 2;
    private final String K_ISBEEPINGSOUNDS = "isSounds";
    private final String K_ISSOUNDACTION = "isSoundingAction";
    private final String K_ISSOUNDSPEAKINGACTION = "isSoundSpeakingAction";
    private final String K_ISVIBRATEACTION = "isVibrate";
    private final String K_ISSPEAKING = "isSpeaking";
    private final String K_ISSPEAKINGMESSAGES = "isSpeakingMessages";
    private final String K_MEDIAVOLUME = "mediaVolume";

    private final Application application;
    private final SharedPreferences preferences;

    public SettingsSounds(Application app) {
        // get all the variables
        this.application = app;
        this.preferences = this.application.getSharedPreferences("SoundsPref", 0); // 0 - for private mode
    }

    public void wipeAllSettings() {
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.clear().commit();
    }

    public boolean getIsMakingBeepingSounds() {
        return this.preferences.getBoolean(K_ISBEEPINGSOUNDS, false);
    }

    public boolean setIsMakingBeepingSounds(boolean isBeepingSounds) {
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putBoolean(K_ISBEEPINGSOUNDS, isBeepingSounds);
        return editor.commit();
    }

    public boolean getIsMakingSoundingAction() {
        return this.preferences.getBoolean(K_ISSOUNDACTION, true);
    }

    public boolean setIsMakingSoundingAction(boolean isSoundingAction) {
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putBoolean(K_ISSOUNDACTION, isSoundingAction);
        return editor.commit();
    }

    public boolean getIsMakingSoundSpeakingAction() {
        return this.preferences.getBoolean(K_ISSOUNDSPEAKINGACTION, true);
    }

    public boolean setIsMakingSoundSpeakingAction(boolean isSoundSpeakingAction) {
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putBoolean(K_ISSOUNDSPEAKINGACTION, isSoundSpeakingAction);
        return editor.commit();
    }

    public boolean getIsMakingVibrateAction() {
        return this.preferences.getBoolean(K_ISVIBRATEACTION, true);
    }

    public boolean setIsMakingVibrateAction(boolean isVibrateAction) {
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putBoolean(K_ISVIBRATEACTION, isVibrateAction);
        return editor.commit();
    }

    public boolean getIsSpeakingPoints() {
        return this.preferences.getBoolean(K_ISSPEAKING, true);
    }

    public boolean setIsSpeakingPoints(boolean isSpeaking) {
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putBoolean(K_ISSPEAKING, isSpeaking);
        return editor.commit();
    }

    public boolean getIsSpeakingMessages() {
        return this.preferences.getBoolean(K_ISSPEAKINGMESSAGES, true);
    }

    public boolean setIsSpeakingMessages(boolean isSpeakingMessages) {
        SharedPreferences.Editor editor = this.preferences.edit();
        editor.putBoolean(K_ISSPEAKINGMESSAGES, isSpeakingMessages);
        return editor.commit();
    }

    public boolean setMediaVolume(int mediaVolume) {
        SharedPreferences.Editor editor = this.preferences.edit();
        // don't let them mute it by accident
        if (mediaVolume != -1) {
            // not specifically setting to be max, limit the min
            mediaVolume = Math.max(K_MIN_VOLUME, mediaVolume);
        }
        editor.putInt(K_MEDIAVOLUME, mediaVolume);
        return editor.commit();
    }

    public int getMediaVolume() {
        return this.preferences.getInt(K_MEDIAVOLUME, -1);
    }

    public int getMaxMediaVolume(Context context) {
        Object service = context.getSystemService(Context.AUDIO_SERVICE);
        if (service instanceof AudioManager) {
            return ((AudioManager)service).getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        } else {
            // just use 15, this seems standard
            return 15;
        }
    }
}
