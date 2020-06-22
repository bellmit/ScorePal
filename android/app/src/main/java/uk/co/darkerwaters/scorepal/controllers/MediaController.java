package uk.co.darkerwaters.scorepal.controllers;

import android.content.Context;
import android.content.Intent;
import android.media.session.PlaybackState;
import android.os.Handler;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Pair;
import android.view.KeyEvent;

import androidx.media.VolumeProviderCompat;

import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.scorepal.application.ApplicationPreferences;
import uk.co.darkerwaters.scorepal.application.ApplicationState;
import uk.co.darkerwaters.scorepal.application.Log;

public class MediaController extends KeyController {

    private static final long K_LONGPRESSTIME = 1000;

    private long pressedTime;
    private int pressedDirection;

    private final Context context;

    private final MediaSessionCompat mediaSession;
    private final VolumeProviderCompat mediaVolumeProvider;

    private final Handler checkLongPressHandler;

    private static ControllerButton[] GetControllerButtons() {
        ApplicationPreferences preferences = ApplicationState.Instance().getPreferences();
        // we are either doing team one / team two, or we are doing server / receiver
        ControllerAction actionOne = preferences.getIsControlTeams() ? ControllerAction.PointTeamOne : ControllerAction.PointServer;
        ControllerAction actionTwo = preferences.getIsControlTeams() ? ControllerAction.PointTeamTwo : ControllerAction.PointReceiver;
        // and create the buttons for this
        List<ControllerButton> buttons = new ArrayList<ControllerButton>();
        if (preferences.getIsControlMedia()) {
            // add the media buttons
            buttons.add(new ControllerButton(
                    new int[] { KeyEvent.KEYCODE_MEDIA_PLAY,
                            KeyEvent.KEYCODE_MEDIA_PAUSE,
                            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE },
                    new Pair[] {
                            new Pair(ControllerPattern.SingleClick, ControllerAction.UndoLastPoint)
                    })
            );
            buttons.add(new ControllerButton(
                    new int[] { KeyEvent.KEYCODE_CAMERA,
                            KeyEvent.KEYCODE_MEDIA_STOP },
                    new Pair[]{
                            new Pair(ControllerPattern.SingleClick, ControllerAction.AnnouncePoints)
                    })
            );
            buttons.add(new ControllerButton(KeyEvent.KEYCODE_MEDIA_NEXT, new Pair[]{
                    new Pair(ControllerPattern.SingleClick, actionOne)
            }));
            buttons.add(new ControllerButton(KeyEvent.KEYCODE_MEDIA_PREVIOUS, new Pair[]{
                    new Pair(ControllerPattern.SingleClick, actionTwo)
            }));
        }
        if (preferences.getIsControlVol()) {
            // add the volume buttons
            buttons.add(new ControllerButton(
                    new int[] {KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN },
                    new Pair[]{
                            new Pair(ControllerPattern.SingleClick, actionOne),
                            new Pair(ControllerPattern.DoubleClick, actionTwo),
                            new Pair(ControllerPattern.LongClick, ControllerAction.UndoLastPoint),
                            new Pair(ControllerPattern.TripleClick, ControllerAction.AnnouncePoints)
                    })
            );
        }
        // and return these buttons
        return buttons.toArray(new ControllerButton[0]);
    }

    //TODO could potentially hide the volume controls by managing with a better media service setup
    // as described
    // @link{https://code.tutsplus.com/tutorials/background-audio-in-android-with-mediasessioncompat--cms-27030}

    public MediaController(Context context) {
        super(GetControllerButtons());
        this.context = context;
        this.checkLongPressHandler = new Handler();

        // set the members
        this.pressedTime = 0;
        this.pressedDirection = 0;

        // create the media session we we can intercept media and volume buttons
        mediaSession = new MediaSessionCompat(context, Log.K_APPLICATION);

        // Overridden methods in the MediaSession.Callback class.
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public boolean onMediaButtonEvent(Intent mediaButtonIntent) {
                // get the simple key event and process it like any other key
                KeyEvent event = mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                if (event != null) {
                    // get the code from the event and pass it down
                    int keyCode = event.getKeyCode();
                    processKeyEvent(keyCode, event);
                }
                return super.onMediaButtonEvent(mediaButtonIntent);
            }
        });

        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        PlaybackStateCompat state = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                        | PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                        | PlaybackStateCompat.ACTION_STOP | PlaybackStateCompat.ACTION_PLAY_PAUSE)
                .setState(PlaybackStateCompat.STATE_PLAYING, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 0)
                .build();
        mediaSession.setPlaybackState(state);

        //this will only work on Lollipop and up, see https://code.google.com/p/android/issues/detail?id=224134
        this.mediaVolumeProvider =
                new VolumeProviderCompat(VolumeProviderCompat.VOLUME_CONTROL_RELATIVE,100,50) {
                    @Override
                    public void onAdjustVolume(int direction) {
                        // have a message, pass this on to the active controller
                        processVolumePress(direction);
                    }
                };
    }

    public boolean start() {
        // start the media session
        mediaSession.setActive(true);
        if (ApplicationState.Instance().getPreferences().getIsControlVol()) {
            // start the media session playback
            mediaSession.setPlaybackToRemote(mediaVolumeProvider);
        }
        // and start the base to process our click times
        return super.start();
    }

    @Override
    public boolean close() {
        // close the media session
        mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_STOPPED, 0, 0) //you simulate a player which plays something.
                .build());
        mediaSession.setFlags(0);
        mediaSession.setCallback(null);
        mediaSession.setActive(false);
        mediaSession.release();
        // and let the base stop
        return super.close();
    }

    void processVolumePress(int direction) {
        KeyEvent event = null;
        if (direction == 0) {
            // direction of zero is a release
            if (this.pressedDirection != 0) {
                // this is not the release of a long click, this is a release of a short, process this
                event = new KeyEvent(
                        this.pressedTime,
                        this.pressedTime,
                        KeyEvent.ACTION_UP,
                        this.pressedDirection < 0 ? KeyEvent.KEYCODE_VOLUME_DOWN : KeyEvent.KEYCODE_VOLUME_UP,
                        0);
            }
            else {
                // this is the release of a long-click, inform listeners this is cleared now
                informListeners(new KeyPress[0]);
            }
            // reset the time to process the next click properly
            this.pressedTime = 0;
        }
        else {
            // remember the down time and direction
            if (this.pressedTime == 0) {
                this.pressedTime = System.currentTimeMillis();
                this.pressedDirection = direction;
                // send the pressed down messages
                if (direction > 0) {
                    // direction of 1 is a volume up
                    event = new KeyEvent(this.pressedTime, this.pressedTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_UP, 0);
                }
                else {
                    // direction of -1 is a volume down
                    event = new KeyEvent(this.pressedTime, this.pressedTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_DOWN, 0);
                }
                // now, we can normally rely on getting lots of pressed down messages
                // but at some point, when the screen is off, we only get one - breaking this
                // therefore instead of just seeing what we get, we can check to see if
                // the button is still down after the long press time
                this.checkLongPressHandler.removeCallbacks(null);
                this.checkLongPressHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        handleLongPress();
                    }
                }, K_LONGPRESSTIME);
            }
            else {
                // this was a long click released, process this alone
                handleLongPress();
            }

        }
        if (null != event) {
            // and process this event here, will ignore and handle long clicks elsewhere above
            processKeyEvent(event.getKeyCode(), event);
        }
    }

    private synchronized void handleLongPress() {
        if (this.pressedTime != 0
            && System.currentTimeMillis() - this.pressedTime > K_LONGPRESSTIME) {
            // this was a long click released, process this alone
            processLongButtonKeyEvent(
                    this.pressedDirection < 0 ? KeyEvent.KEYCODE_VOLUME_DOWN : KeyEvent.KEYCODE_VOLUME_UP,
                    this.pressedTime);
            this.pressedTime = System.currentTimeMillis();
            this.pressedDirection = 0;
        }
    }
}
