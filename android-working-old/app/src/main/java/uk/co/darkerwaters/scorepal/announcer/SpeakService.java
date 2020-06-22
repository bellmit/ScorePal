package uk.co.darkerwaters.scorepal.announcer;

import android.content.Context;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

import uk.co.darkerwaters.scorepal.application.Application;
import uk.co.darkerwaters.scorepal.settings.SettingsSounds;

public class SpeakService implements TextToSpeech.OnInitListener {

    private final Context context;
    private final TextToSpeech ttsEngine;
    private final Application application;

    private volatile int activeMessages = -1;

    final ArrayList<ToSpeak> toSpeakList = new ArrayList<>();

    private int previousAudioVol;

    private static class ToSpeak {
        final String message;
        final boolean isFlush;
        final boolean isMessagePart;
        ToSpeak(String message, boolean isFlush, boolean isMessagePart) {
            this.message = message;
            this.isFlush = isFlush;
            this.isMessagePart = isMessagePart;
        }
    }

    public SpeakService(Context context, Application application) {
        this.context = context;
        this.application = application;

        // initialise the engine
        this.ttsEngine = new TextToSpeech(this.context, this);
        this.previousAudioVol = -1;

        this.ttsEngine.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {
                // starting is ok, remember what we are saying here
                ++activeMessages;
            }
            @Override
            public void onDone(String s) {
                --activeMessages;
                // now we can flush any new messages out
                if (!flushMessages()) {
                    // and there were none new started, put the volume back to as it was
                    restoreMediaVolume();
                }

            }
            @Override
            public void onError(String s) {
                --activeMessages;
                // now we can flush any new messages out
                if (!flushMessages()) {
                    // and there were none new started, put the volume back to as it was
                    restoreMediaVolume();
                }
            }
        });
    }

    public void speakMessage(String message, boolean isFlushOld) {
        synchronized (this.toSpeakList) {
            if (isFlushOld) {
                // remove anything proceeding this
                this.toSpeakList.clear();
            }
            // add the new one to speak to the end
            this.toSpeakList.add(new ToSpeak(message, isFlushOld, false));
        }
        // speak all in the list
        flushMessages();
    }

    public void close() {
        this.ttsEngine.stop();
        this.ttsEngine.shutdown();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = this.ttsEngine.setLanguage(Locale.getDefault());
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // show the user that this doesn't work
                Toast.makeText(this.context, "TTS language is not supported", Toast.LENGTH_LONG).show();
            }
            // else we are successfully initialised, start working
            this.activeMessages = 0;
        }
        flushMessages();
    }

    private boolean flushMessages() {
        ToSpeak toSpeak;
        boolean isSpokenMessage = false;
        // while we are without error and initialised, flush the next message from the queue
        while (this.activeMessages >= 0) {
            synchronized (this.toSpeakList) {
                if (false == this.toSpeakList.isEmpty()) {
                    // get the thing to speak
                    toSpeak = this.toSpeakList.remove(0);
                } else {
                    // we are done, stop looking for any to speak
                    break;
                }
            }
            if (null != toSpeak) {
                // we have something to say, prepare the volume
                prepareMediaVolume();
                // speak this
                this.ttsEngine.speak(toSpeak.message,
                        toSpeak.isFlush ? TextToSpeech.QUEUE_FLUSH : TextToSpeech.QUEUE_ADD,
                        null, UUID.randomUUID().toString());
                isSpokenMessage = true;
                if (toSpeak.isMessagePart) {
                    // we are just a part of a message, break from the loop
                    // we we speak this part and come back here as we ended
                    break;
                }
            }
        }
        return isSpokenMessage;
    }

    private synchronized void restoreMediaVolume() {
        // we are done talking, put the volume back to what it was before we messed with it
        Object service = this.context.getSystemService(Context.AUDIO_SERVICE);
        if (this.previousAudioVol != -1 && service instanceof AudioManager) {
            // there is a previous vol to return to and we have a service to do it on
            AudioManager audioManager = (AudioManager)service;
            SettingsSounds soundsSettings = new SettingsSounds(this.application);
            // the vol we are now is the vol we want to return to the next time,
            // just in case the user asked us to shut the hell up
            int userVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            soundsSettings.setMediaVolume(userVolume);
            // and put the vol back for the media as it was
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, this.previousAudioVol, 0);
            // we also attempted a pause, release this pause
            audioManager.abandonAudioFocus(null);
            // don't do this again
            this.previousAudioVol = -1;
        }
    }

    private synchronized void prepareMediaVolume() {
        // before we do this we want to remember the current volume set
        Object service = this.context.getSystemService(Context.AUDIO_SERVICE);
        if (service instanceof AudioManager) {
            AudioManager audioManager = (AudioManager) service;
            SettingsSounds soundsSettings = new SettingsSounds(this.application);
            if (this.previousAudioVol == -1) {
                // remember the previous vol before we start our own
                this.previousAudioVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                // Request audio focus to stop other music from playing
                audioManager.requestAudioFocus(null,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            }
            int requiredVol = soundsSettings.getMediaVolume();
            if (requiredVol < 0) {
                // the media volume isn't set by the user, set it to the max!
                requiredVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            }
            // and set the volume to the max (or whatever the user set before)
            // so the user can hear what we are saying
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, requiredVol, 0);
        }
    }
}