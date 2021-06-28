package uk.co.darkerwaters.scorepal.application;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import java.util.Locale;
import java.util.UUID;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.ui.UiHelper;

public class SpeakService implements TextToSpeech.OnInitListener {

    private static final long FOCUS_WAIT_TIMEOUT = 300;
    private final Context context;
    private final TextToSpeech ttsEngine;

    private String activeMessage;
    private int previousAudioVol;
    private AudioFocusRequest audioFocusRequest = null;
    private boolean haveAudioFocus = false;

    private final Handler delayHandler = new Handler();
    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = null;

    public SpeakService(Context context) {
        this.context = context;

        // initialise the engine
        this.ttsEngine = new TextToSpeech(this.context, this);
        this.previousAudioVol = -1;

        this.ttsEngine.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {
                // starting is ok, remember what we are saying here
            }
            @Override
            public void onDone(String s) {
                processRemainingMessage();
            }
            @Override
            public void onError(String s) {
                processRemainingMessage();
            }
        });
    }

    private synchronized void processRemainingMessage() {
        // process whatever message remains
        if (null != activeMessage) {
            // there is an active message to speak, speak it
            if (haveAudioFocus) {
                // we have audio focus, so just say the message here
                this.ttsEngine.speak(activeMessage, TextToSpeech.QUEUE_FLUSH,null, UUID.randomUUID().toString());
                // which wipes out the active message
                activeMessage = null;
            }
            else {
                // prepare the media volume which, when granted, means we can talk
                prepareMediaVolume();
            }
        }
        else {
            // we have nothing to say and we are not saying anything - so restore media volume
            restoreMediaVolume();
            haveAudioFocus = false;
        }
    }

    public synchronized void speakMessage(String message) {
        if (null != message && !message.trim().isEmpty()) {
            // there is a message to set, so set it
            activeMessage = message.trim();
            activeMessage = activeMessage.replaceAll(" ,", ",");
            activeMessage = activeMessage.replaceAll(" …", "…");
            activeMessage = activeMessage.trim();
            if (activeMessage.endsWith(",") || activeMessage.endsWith("…")) {
                activeMessage = activeMessage.substring(0, activeMessage.length() - 1);
            }
            // if we are saying something, when this ends it will say this thing, if we are not
            // we can say it now
            processRemainingMessage();
        }
    }

    private synchronized void onAudioFocusReceived(AudioManager audioManager, boolean isFromTimeout, int focusChange) {
        if (!haveAudioFocus) {
            ApplicationPreferences preferences = ApplicationState.Instance().getPreferences();
            int requiredVol = preferences.getSoundAnnounceVolume();
            if (requiredVol < 0) {
                // the media volume isn't set by the user, set it to the max!
                requiredVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            }
            // and set the volume to the max (or whatever the user set before)
            // so the user can hear what we are saying
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, requiredVol, 0);
            haveAudioFocus = true;
        }
        // we have focus, do the speaking here
        processRemainingMessage();
    }

    public void close() {
        restoreMediaVolume();
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
                UiHelper.showUserMessage(this.context, R.string.TTSLangNotSupported);
            }
            // else we are successfully initialised, start working
        }
        // process any messages already on this class that we had hanging around
        processRemainingMessage();
    }

    private synchronized void restoreMediaVolume() {
        // we are done talking, put the volume back to what it was before we messed with it
        Object service = this.context.getSystemService(Context.AUDIO_SERVICE);
        if (service instanceof AudioManager) {
            // there is a previous vol to return to and we have a service to do it on
            AudioManager audioManager = (AudioManager) service;
            if (this.previousAudioVol != -1) {
                ApplicationPreferences preferences = ApplicationState.Instance().getPreferences();
                // the vol we are now is the vol we want to return to the next time,
                // just in case the user asked us to shut the hell up
                int userVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                preferences.setSoundAnnounceVolume(userVolume, true);
                // and put the vol back for the media as it was
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, this.previousAudioVol, 0);
                // don't do this again
                this.previousAudioVol = -1;
            }
            // we also attempted a pause, release this pause
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (null != this.audioFocusRequest) {
                    audioManager.abandonAudioFocusRequest(this.audioFocusRequest);
                    this.audioFocusRequest = null;
                }
            } else {
                if (null != audioFocusChangeListener) {
                    audioManager.abandonAudioFocus(audioFocusChangeListener);
                    audioFocusChangeListener = null;
                }
            }
        }
    }

    private synchronized void prepareMediaVolume() {
        // before we do this we want to remember the current volume set
        Object service = this.context.getSystemService(Context.AUDIO_SERVICE);
        if (service instanceof AudioManager) {
            final AudioManager audioManager = (AudioManager) service;
            if (this.previousAudioVol == -1) {
                // remember the previous vol before we start our own
                this.previousAudioVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            }
            // Request audio focus to stop other music from playing
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (null == audioFocusRequest) {
                    this.audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                            .setAudioAttributes(
                                    new AudioAttributes.Builder()
                                            .setUsage(AudioAttributes.USAGE_MEDIA)
                                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                            .build()
                            )
                            .setAcceptsDelayedFocusGain(true)
                            .setOnAudioFocusChangeListener(new AudioManager.OnAudioFocusChangeListener() {
                                @Override
                                public void onAudioFocusChange(int focusChange) {
                                    // this is when we want to speak
                                    onAudioFocusReceived(audioManager, false, focusChange);
                                }
                            }).build();
                    // request this focus
                    audioManager.requestAudioFocus(audioFocusRequest);
                }
            } else {
                // do the request a little differently for older versions
                if (null == audioFocusChangeListener) {
                    audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
                        @Override
                        public void onAudioFocusChange(int i) {
                            // this is when we want to speak
                            onAudioFocusReceived(audioManager, false, i);
                        }
                    };
                    // request this focus
                    audioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
                }
            }
            // if it this takes too long we want to talk anyway
            delayHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    onAudioFocusReceived(audioManager, true, -1);
                }
            }, FOCUS_WAIT_TIMEOUT);
        }
    }
}