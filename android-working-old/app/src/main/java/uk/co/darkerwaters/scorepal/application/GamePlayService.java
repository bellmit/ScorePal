package uk.co.darkerwaters.scorepal.application;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.announcer.SpeakService;
import uk.co.darkerwaters.scorepal.players.Team;
import uk.co.darkerwaters.scorepal.score.Match;
import uk.co.darkerwaters.scorepal.score.MatchPersistenceManager;
import uk.co.darkerwaters.scorepal.score.MatchSettings;
import uk.co.darkerwaters.scorepal.score.base.Point;
import uk.co.darkerwaters.scorepal.score.base.PointChange;
import uk.co.darkerwaters.scorepal.score.tennis.TennisPoint;
import uk.co.darkerwaters.scorepal.settings.SettingsSounds;

public class GamePlayService extends Service implements Match.MatchListener {
    public static final String CHANNEL_ID = "GamePlayForegroundServiceChannel";

    public static final String ACTION_PING = GamePlayService.class.getName() + ".PING";
    public static final String ACTION_PONG = GamePlayService.class.getName() + ".PONG";

    private Application application;
    private final ActiveMatch activeMatch;

    private boolean isMessageStarted = false;

    private Date playStarted;
    private Date playEnded;

    private SpeakService speakService = null;
    private String spokenMessage = "";
    private String changeServerMessage = null;

    public interface GamePlayListener {
        void onPlayStateChanged(Date playStarted, Date playEnded);
    }

    private final Set<GamePlayListener> playListeners;
    private final Set<Match.MatchListener> matchListeners;

    private static class ServiceState {
        GamePlayService activeService;
        final ActiveMatch serviceMatch = new ActiveMatch();

    }
    private static class ActiveMatch {
        Match match;
        MatchSettings settings;
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public GamePlayService getService() {
            // Return this instance of LocalService so clients can call public methods
            return GamePlayService.this;
        }
    }

    private static final ServiceState ActiveService = new ServiceState();

    public GamePlayService() {
        // create the members
        this.playListeners = new HashSet<>();
        this.matchListeners = new HashSet<>();
        this.activeMatch = new ActiveMatch();
        this.activeMatch.match = null;
        this.activeMatch.settings = null;
    }

    /**
     * the receiver for being pinged - so we respond accordingly
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive (Context context, Intent intent)
        {
            if (intent.getAction().equals(ACTION_PING)) {
                // when we get pinged, respond wit a pong
                LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getApplicationContext());
                manager.sendBroadcast(new Intent(ACTION_PONG));
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        // listen for pings so we can respond with a pong when we are running
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(ACTION_PING));
        // and get the things we need from it
        this.application = Application.getApplication(this);

        synchronized (ActiveService) {
            // setup the static class here nicely
            ActiveService.activeService = this;
            // setup play for this newly created service
            setupPlay(ActiveService.serviceMatch.match,
                    ActiveService.serviceMatch.settings);
        }
    }

    @Override
    public void onDestroy() {
        // no longer the active service
        synchronized (ActiveService) {
            ActiveService.activeService = null;
        }
        // stop any active controllers that might be pointing to us
        GamePlayCommunicator.SilenceCommunications();
        // stop responding to ping messages
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);

        // stop speaking
        if (null != this.speakService) {
            this.speakService.close();
            this.speakService = null;
        }
        // store these results for sure
        storeMatchResults(true, null);

        // and remove us as a listener on the match
        synchronized (this.activeMatch) {
            if (null != this.activeMatch.match) {
                // remove us as a listener
                this.activeMatch.match.removeListener(this);
            }
        }
        // and destroy
        super.onDestroy();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // and start the service
        String input = intent.getStringExtra("inputExtra");
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, GamePlayService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Foreground Service")
                .setContentText(input)
                .setSmallIcon(R.drawable.ic_tennis)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);
        //do heavy work on a background thread
        //stopSelf();
        return START_NOT_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    static void SetupService(Context context, Match match, MatchSettings settings) {
        synchronized (ActiveService) {
            if (null != ActiveService.activeService) {
                // there is an active service already, set this
                ActiveService.activeService.setupPlay(match, settings);
            } else {
                // there is a match, use this when we start
                ActiveService.serviceMatch.match = match;
                ActiveService.serviceMatch.settings = settings;
            }
        }
    }

    private void setupPlay(Match match, MatchSettings settings) {
        // setup our initial members
        this.playStarted = null;
        this.playEnded = null;
        this.isMessageStarted = false;

        // setup the speaking service
        if (null == this.speakService) {
            // create the thing for speaking
            this.speakService = new SpeakService(this, this.application);
        }
        synchronized (this.activeMatch) {
            if (null != this.activeMatch.match) {
                // stop listening to the old one
                this.activeMatch.match.removeListener(this);
            }
            // and setup the active match for which we will be doing things
            this.activeMatch.match = match;
            this.activeMatch.settings = settings;

            if (null != this.activeMatch.match) {
                // we want to listen to this match to show the score as it changes
                this.activeMatch.match.addListener(this);
            }
        }

        GamePlayCommunicator communicator = GamePlayCommunicator.GetActiveCommunicator();
        if (null != communicator && communicator.isActive()) {
            // a communicator is active, be sure it is listening to us
            communicator.onServicePlayInitiated(this);
        }
    }

    boolean addStateListener(GamePlayListener listener) {
        synchronized (this.playListeners) {
            return this.playListeners.add(listener);
        }
    }

    boolean removeStateListener(GamePlayListener listener) {
        synchronized (this.playListeners) {
            return this.playListeners.remove(listener);
        }
    }

    boolean addMatchListener(Match.MatchListener listener) {
        synchronized (this.matchListeners) {
            return this.matchListeners.add(listener);
        }
    }

    boolean removeMatchListener(Match.MatchListener listener) {
        synchronized (this.matchListeners) {
            return this.matchListeners.remove(listener);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    boolean isPlayStarted() {
        return null != this.playStarted;
    }

    boolean isMatchStarted() {
        synchronized (this.activeMatch) {
            return null != this.activeMatch.match && this.activeMatch.match.isMatchStarted();
        }
    }

    Match getActiveMatch() {
        synchronized (this.activeMatch) {
            return this.activeMatch.match;
        }
    }

    private void informListeners() {
        synchronized (this.playListeners) {
            for (GamePlayListener listener : playListeners) {
                listener.onPlayStateChanged(this.playStarted, this.playEnded);
            }
        }
    }

    void startPlay() {
        this.playStarted = Calendar.getInstance().getTime();
        // set this on the settings as we change it
        if (null != this.playStarted
                && null != this.activeMatch.settings
                && null == this.activeMatch.settings.getMatchPlayedDate()) {
            // there are settings, but there is no match played date, set one
            this.activeMatch.settings.setMatchPlayedDate(playStarted);
            // and record in the settings that we last played this sport now
            this.application.getSettings().setSportLastPlayedNow(this, this.activeMatch.settings.getSport());
        }
        // inform listeners of this change
        informListeners();
    }

    void stopPlay() {
        // set the time at which play ended
        this.playEnded = Calendar.getInstance().getTime();
        // inform listeners of this change
        informListeners();
    }

    private void clearStartedPlay() {
        if (null != this.playStarted) {
            this.playStarted = null;
            // inform listeners of this change
            informListeners();
        }
    }

    private void clearEndedPlay() {
        if (null != this.playEnded) {
            this.playEnded = null;
            // inform listeners of this change
            informListeners();
        }
    }

    boolean isMatchOver() {
        synchronized (this.activeMatch) {
            return null == this.activeMatch.match || this.activeMatch.match.isMatchOver();
        }
    }

    private void clearSpokenMessage() {
        this.spokenMessage = "";
        // reset the message started flag that we were saying before
        this.isMessageStarted = false;
    }

    @Override
    public void onMatchChanged(Match.MatchChange type) {
        if (isPlayStarted()) {
            // now handle the tennis things here
            switch (type) {
                case BREAK_POINT:
                case BREAK_POINT_CONVERTED:
                    // this might be a little more interesting, either way (at the moment) not a msg
                    break;
                case DECREMENT:
                    // inform the players that there was a correction to the score
                    // ** this doesn't change any points in any way, so say this now
                    SettingsSounds soundsSettings = new SettingsSounds(this.application);
                    if (soundsSettings.getIsSpeakingMessages()) {
                        speakMessage(getString(R.string.correction));
                    }
                    // this is special as an undo doesn't send a message about changing the score
                    // so let's check if we are starting again here
                    handlePlayEnding();
                    break;
                case DECIDING_POINT:
                    // inform the players that this is 'sudden death'
                    appendSpokenMessage(R.string.deciding_point);
                    break;
                case ENDS:
                    // this requires a message, so send it
                    appendSpokenMessage(R.string.change_ends);
                    break;
                case SERVER:
                    // change server, remove any change server string that used to be there
                    String oldMessage = this.changeServerMessage;
                    this.changeServerMessage = getString(R.string.change_server);
                    if (null != this.activeMatch && null != this.activeMatch.match) {
                        String serverName = this.activeMatch.match.getCurrentServer().getSpeakingName();
                        // and make the message include the name of the player to serve
                        this.changeServerMessage = getString(R.string.change_server_server, serverName);
                    }
                    appendSpokenMessage(oldMessage, this.changeServerMessage);
                    break;
                case TIE_BREAK:
                    appendSpokenMessage(R.string.tie_break);
                    break;
            }
        }
        // and pass on to all our listeners
        synchronized (this.matchListeners) {
            for (Match.MatchListener listener : this.matchListeners) {
                listener.onMatchChanged(type);
            }
        }
        // update any open notification with this new match data
        updateMatchNotification();
    }

    private void appendSpokenMessage(int stringId) {
        appendSpokenMessage(getString(stringId));
    }

    private void appendSpokenMessage(String message) {
        if (!this.spokenMessage.contains(message)) {
            // are not already saying it, add it now
            this.spokenMessage += Point.K_SPEAKING_PAUSE + message;
        }
    }

    private void appendSpokenMessage(String oldMessage, String message) {
        if (null != oldMessage && this.spokenMessage.contains(oldMessage)) {
            // the spoken message contains the old, replace it with this
            this.spokenMessage = this.spokenMessage.replace(oldMessage, message);
        }
        else if (!this.spokenMessage.contains(message)) {
            // are not already saying it, add it now
            this.spokenMessage += Point.K_SPEAKING_PAUSE + message;
        }
    }

    private void updateMatchNotification() {
        GamePlayCommunicator communicator = GamePlayCommunicator.GetActiveCommunicator();
        if (null != communicator && communicator.isActive()) {
            // show the state of this match in the notification bar
            communicator.showMatchNotification();
        }
    }

    @Override
    public void onMatchPointsChanged(PointChange[] levelsChanged) {
        // need to announce this change in the score, find the top level
        // that changed, if we won a set we don't care about the game...
        PointChange topChange = null;
        for (PointChange change : levelsChanged) {
            // we want to find the highest change
            if (topChange == null || change.level > topChange.level) {
                // this is the biggest - remember this
                topChange = change;
            }
        }
        Match match;
        synchronized (this.activeMatch) {
            match = this.activeMatch.match;
        }
        if (null != topChange && null != match && topChange.level != -1) {
            if (!isPlayStarted()) {
                // there is a change in points, but we are not started, this is a start really
                startPlay();
            }
            String message = "";
            SettingsSounds soundsSettings = new SettingsSounds(this.application);
            if (soundsSettings.getIsMakingSoundSpeakingAction() && topChange.level == 0) {
                // want so speak the action that initiated this, removing periods as they cause pauses in speech
                message += TennisPoint.POINT.speakString(this)
                        + Point.K_SPEAKING_SPACE
                        + topChange.team.getSpeakingTeamName()
                        + Point.K_SPEAKING_PAUSE;
            }
            if (soundsSettings.getIsSpeakingPoints()) {
                // we want to say the points, create the phrase to say
                message += match.createPointsPhrase(this, topChange) + Point.K_SPEAKING_PAUSE;
            }
            if (!this.spokenMessage.isEmpty() && soundsSettings.getIsSpeakingMessages()) {
                // we might also be changing ends
                // or something, get from the score fragment the state it is showing
                message = match.appendSpokenMessage(this, message, this.spokenMessage);
            }
            // clear any message to speak, will speak it or will be silent
            clearSpokenMessage();
            // speak this message on the play service
            speakMessage(message);
            // every time the points change we want to check to see if we have ended or not
            handlePlayEnding();
        }
        // and pass on to all our listeners
        synchronized (this.matchListeners) {
            for (Match.MatchListener listener : this.matchListeners) {
                listener.onMatchPointsChanged(levelsChanged);
            }
        }
        // update any open notification with this new match data
        updateMatchNotification();
    }

    private void handlePlayEnding() {
        if (false == isMatchStarted()) {
            // we are not started now (could have undone to the last), clear the start time
            clearStartedPlay();
        }
        // is the match over
        if (isMatchOver()) {
            // the match is over
            if (null == this.playEnded) {
                // the match is over but play hasn't ended - yes it has!
                stopPlay();
            }
        } else {
            // the match is not over, this might be from an undo, get rid of the time either way
            clearEndedPlay();
        }
    }

    void speakMessage(String spokenMessage) {
        speakMessage(spokenMessage, true);
    }

    void speakMessage(String spokenMessage, boolean isFlushOld) {
        if (null != this.speakService && null != spokenMessage && false == spokenMessage.isEmpty()) {
            // speak what we have made, overriding everything before it
            this.speakService.speakMessage(spokenMessage, isFlushOld);
        }
    }

    int getMatchMinutesPlayed() {
        int minutesPlayed;
        synchronized (this.activeMatch) {
            minutesPlayed = this.activeMatch.match == null ? 0 : this.activeMatch.match.getMatchMinutesPlayed();
        }
        int activityMinutes = getMinutesPlayed();
        if (activityMinutes >= 0) {
            minutesPlayed += activityMinutes;
        }
        return minutesPlayed;
    }

    void storeCurrentState(Location currentLocation) {
        if (null != this.playStarted) {
            // and add the time played in this session to the active match
            int activityMinutes = getMinutesPlayed();
            synchronized (this.activeMatch) {
                if (activityMinutes > 0 && null != this.activeMatch.match) {
                    this.activeMatch.match.addMatchMinutesPlayed(activityMinutes);
                }
            }
            // now we added these minutes, we need to not add them again, reset the
            // play started time to be now
            this.playStarted = Calendar.getInstance().getTime();
        }
        // store the match results
        storeMatchResults(true, currentLocation);
    }

    private int getMinutesPlayed() {
        if (null == this.playStarted) {
            return 0;
        }
        else {
            long playEndedMs;
            if (null == this.playEnded) {
                // play isn't over yet, use now
                playEndedMs = Calendar.getInstance().getTimeInMillis();
            } else {
                // use the play ended time
                playEndedMs = this.playEnded.getTime();
            }
            // Calculate difference in milliseconds
            long diff = playEndedMs - this.playStarted.getTime();
            // and add the time played to the active match
            return (int) (diff / 60000L);
        }
    }

    private void storeMatchResults(boolean storeIfPersisted, Location currentLocation) {
        synchronized (this.activeMatch) {
            if (null != this.activeMatch.match
                    && this.activeMatch.match.isMatchStarted()
                    && this.activeMatch.match.isSerialiseMatch()
                    && null != this.activeMatch.settings) {
                // set the location of this active match
                if (null != currentLocation) {
                    this.activeMatch.match.setPlayedLocation(currentLocation);
                }
                // store the results of the match we started
                MatchPersistenceManager persistenceManager = MatchPersistenceManager.GetInstance();
                if (storeIfPersisted || false == persistenceManager.isMatchDataPersisted(this.activeMatch.match)) {
                    // we are forcing a save, or the data is different, so save
                    persistenceManager.saveMatchToFile(this.activeMatch.match, this.activeMatch.settings, this);
                }
            }
        }
    }

    void incrementPoint(Team team) {
        synchronized (this.activeMatch) {
            if (null != this.activeMatch.match && false == isMatchOver()) {
                this.activeMatch.match.incrementPoint(this, team);
            }
        }
    }

    void undoLastPoint() {
        // undo the last point
        synchronized (this.activeMatch) {
            if (null != this.activeMatch.match && null == this.activeMatch.match.undoLastPoint(this)) {
                // the undo action failed, nothing to undo
                // but the user might be wanting the editing controls
                // back, send the message as if we undone to loop
                // through and reset everything as if they had
                this.activeMatch.match.informListeners(Match.MatchChange.DECREMENT);
            }
        }
    }
}
