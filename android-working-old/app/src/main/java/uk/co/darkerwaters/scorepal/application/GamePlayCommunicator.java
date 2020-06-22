package uk.co.darkerwaters.scorepal.application;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import uk.co.darkerwaters.scorepal.activities.BaseActivity;
import uk.co.darkerwaters.scorepal.controllers.Controller;
import uk.co.darkerwaters.scorepal.controllers.MediaController;
import uk.co.darkerwaters.scorepal.players.CourtPosition;
import uk.co.darkerwaters.scorepal.players.Player;
import uk.co.darkerwaters.scorepal.players.Team;
import uk.co.darkerwaters.scorepal.score.bluetooth.BluetoothMatch;
import uk.co.darkerwaters.scorepal.score.Match;
import uk.co.darkerwaters.scorepal.score.base.MatchMessage;
import uk.co.darkerwaters.scorepal.score.MatchSettings;
import uk.co.darkerwaters.scorepal.score.base.PointChange;
import uk.co.darkerwaters.scorepal.settings.Settings;
import uk.co.darkerwaters.scorepal.settings.SettingsControl;
import uk.co.darkerwaters.scorepal.settings.SettingsSounds;

import static android.content.Context.VIBRATOR_SERVICE;

public class GamePlayCommunicator implements Controller.ControllerListener {

    private final Application application;
    private Location location;
    private GamePlayService service;
    private BaseActivity activeActivity;
    private BaseActivity currentActivity;
    private MediaController mediaController;

    private final ToneGenerator toneGenerator;
    private final Vibrator vibrator;
    private GamePlayNotification notification = null;

    private final FusedLocationProviderClient fusedLocationClient;

    private static final long K_TIME = 250L;
    private static final long K_GAP = 200L;

    private static final long[][] Vibrations = new long[][]{
            {0, K_TIME},                                            // SINGLE
            {0, K_TIME, K_GAP, K_TIME},                             // DOUBLE
            {0, K_TIME, K_GAP, K_TIME, K_GAP, K_TIME},              // TRIPLE
            {0, K_TIME * 4}                                         // LONG
    };

    private final VibrationEffect[] vibrationEffects;

    private static final int K_VIBRATE_SINGLE = 0;
    private static final int K_VIBRATE_DOUBLE = 1;
    private static final int K_VIBRATE_TRIPLE = 2;
    private static final int K_VIBRATE_LONG = 3;

    private Match currentMatch;
    private MatchSettings currentSettings;

    private static class ActiveCommunicator {
        GamePlayCommunicator communicator = null;
    }

    private static final ActiveCommunicator ActiveCommunicator = new ActiveCommunicator();

    public static GamePlayCommunicator ActivateCommunicator(BaseActivity activity) {
        synchronized (ActiveCommunicator) {
            // create only one communicator for everyone
            if (activity == null) {
                if (ActiveCommunicator.communicator != null) {
                    // no valid activity - kill it
                    ActiveCommunicator.communicator.close();
                    ActiveCommunicator.communicator = null;
                }
            } else if (ActiveCommunicator.communicator == null) {
                // there is a new activity, but no communicator, remedy this now
                ActiveCommunicator.communicator = new GamePlayCommunicator(activity);
            }
            if (null != ActiveCommunicator.communicator
                    && ActiveCommunicator.communicator.getCurrentActivity() != activity) {
                // remember the current activity
                ActiveCommunicator.communicator.setCurrentActivity(activity, activity.getGamePlayService());
            }
            // and return the valid communicator
            return ActiveCommunicator.communicator;
        }
    }

    private GamePlayCommunicator(BaseActivity activity) {
        // setup this communicator for the specified activity
        this.application = Application.getApplication(activity);
        this.mediaController = null;
        this.currentMatch = null;
        setCurrentActivity(activity, activity.getGamePlayService());
        this.location = null;

        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.currentActivity);

        // might want to beep a little
        this.toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        // Get instance of Vibrator from current Context
        this.vibrator = (Vibrator) activity.getSystemService(VIBRATOR_SERVICE);

        if (Build.VERSION.SDK_INT >= 26) {
            this.vibrationEffects = new VibrationEffect[Vibrations.length];
            // create the effects for this version of android
            for (int i = 0; i < Vibrations.length; ++i) {
                this.vibrationEffects[i] = VibrationEffect.createWaveform(Vibrations[i], -1);
            }
        } else {
            // else we will use the long arrays directly
            this.vibrationEffects = null;
        }
    }

    public Application getCurrentApplication() { return this.application; }

    public Match getCurrentMatch() {
        return this.currentMatch;
    }

    public MatchSettings getCurrentSettings() {
        return this.currentSettings;
    }

    public static GamePlayCommunicator GetActiveCommunicator() {
        synchronized (ActiveCommunicator) {
            return ActiveCommunicator.communicator;
        }
    }

    public static void SilenceCommunications() {
        synchronized (ActiveCommunicator) {
            if (null != ActiveCommunicator.communicator) {
                // silence the communicator
                ActiveCommunicator.communicator.silenceCommunications();
            }
        }
    }

    public void updateActiveMatchWithSettings() {
        // push the data in the settings we have to the current match
        if (null != this.currentMatch && null != this.currentSettings) {
            this.currentMatch.resetMatchSettings(this.currentActivity, this.currentSettings);
        }
    }

    private void setCurrentActivity(BaseActivity activity, GamePlayService service) {
        this.currentActivity = activity;
        // and get the service from this activity
        this.service = service;
    }

    public boolean isActive() {
        return null != this.activeActivity;
    }

    public boolean isMatchOver() {
        // return if this match is over or not
        return null != service && service.isMatchOver();
    }

    public int getMatchMinutesPlayed() {
        // return the number of minutes played already
        return service == null ? 0 : service.getMatchMinutesPlayed();
    }

    private Activity getActiveActivity() {
        return this.activeActivity;
    }

    public Activity getCurrentActivity() {
        return this.currentActivity;
    }

    public boolean isPlayStarted() {
        // return if play is started or not
        return null != service && service.isPlayStarted();
    }

    public boolean isMatchStarted() {
        // return if the match is started
        return null != service && service.isMatchStarted();
    }

    public boolean sendRequest(MatchMessage message, String[] stringParams) throws Exception {
        // convert the string (serialised) params into their correct classes
        MatchMessage.Param[] dataParams = new MatchMessage.Param[stringParams.length];
        // this is a request with the dataParams in their serialised state, we need to deserialise this
        boolean isAllowMessage = true;
        boolean isClientChangesAllowed = false;
        // we only want to send the request if we are accepting them
        Settings settings = application.getSettings();
        if (null == settings || settings.getIsAllowClientsToChangeScore()) {
            // clients are allowed to send us messages, so this change is okay
            isClientChangesAllowed = true;
        }
        switch (message) {
            case SETUP_NEW_MATCH:
            case SETUP_EXISTING_MATCH:
                // these are complicated parameters, match and match settings, don't bother with this
                //TODO load in the match and match settings we are sen't here maybe
                break;
            case INCREMENT_POINT:
                // the param is a team, we can load this and do the work here
                Team receivedInvalidTeam = new Team(0, new Player[0], CourtPosition.NORTH);
                receivedInvalidTeam.deserialiseFromString(this.currentActivity, 1, stringParams[0]);
                // we can deserialise this team just fine but there is a problem with that - the team is a new object
                // and not the team that we are using in the match, so we need to determine if this is team 1 or 2
                // and replace with the actual team this represents, use it by default
                dataParams[0] = receivedInvalidTeam;
                if (null != this.currentMatch) {
                    // is this the same as team on in the match?
                    Team teamOne = this.currentMatch.getTeamOne();
                    if (teamOne.getTeamName().equals(receivedInvalidTeam.getTeamName())) {
                        // this is team one
                        dataParams[0] = teamOne;
                    } else {
                        // this isn't the same, use team two
                        dataParams[0] = this.currentMatch.getTeamTwo();
                        if (teamOne.getTeamName().equals(receivedInvalidTeam.getTeamName())) {
                            // no match at all - grr
                            Log.error("Team received and processed in INCREMENT doesn't match either");
                        }
                    }
                }
                // this is a score change, only allow if they are allowed
                isAllowMessage = isClientChangesAllowed;
                break;
            case RESET:
            case UNDO_POINT:
            case CHANGE_STARTING_SERVER:
            case CHANGE_STARTER:
            case CHANGE_STARTING_ENDS:
                // this is a change to the score, only allow if they are allowed
                isAllowMessage = isClientChangesAllowed;
                break;
            case ANNOUNCE_POINTS:
                // announce the current points, the param is a string param
                dataParams[0] = new MatchMessage.StringParam();
                dataParams[0] = dataParams[0].deserialiseFromString(this.currentActivity, 1, stringParams[0]);
                break;
            case CREATE_MATCH:
            case START_PLAY:
            case STOP_PLAY:
            case STORE_STATE:
            case REQUEST_MATCH_UPDATE:
            default:
                // none of these have any parameters
                break;
        }

        if (isAllowMessage) {
            // return the result of the original, without sending it back out as we just received it
            return sendRequest(message, false, dataParams);
        }
        else {
            // we are not passing this on, fine, but return false
            return false;
        }
    }

    public boolean sendRequest(MatchMessage message, MatchMessage.Param... dataParams) {
        // outside callers always communicate their data elsewhere
        return this.sendRequest(message, true, dataParams);
    }

    private boolean sendRequest(MatchMessage message, boolean isCommunicateToSockets, MatchMessage.Param... dataParams) {
        // all changes to the match come through this point so we can inform other's
        // of these changes.
        boolean result = false;
        switch (message) {
            case RESET:
                // reset the match, no params
                result = resetMatch();
                break;
            case START_PLAY:
                // start the match playing, no params
                result = startPlay();
                break;
            case STOP_PLAY:
                // stop play, no params
                result = stopPlay();
                break;
            case STORE_STATE:
                // store the state of the match, no params
                result = storeMatchState();
                break;
            case SETUP_NEW_MATCH:
                // setup the match, the first param is the settings
                result = setupNewMatch(dataParams.length > 0 ? (MatchSettings) dataParams[0] : null);
                break;
            case SETUP_EXISTING_MATCH:
                // setup the match, match / settings / file
                result = setupExistingMatch(
                        dataParams.length > 0 ? (Match) dataParams[0] : null,
                        dataParams.length > 1 ? (MatchSettings) dataParams[1] : null);
                break;
            case CREATE_MATCH:
                // start the match
                result = createMatch();
                break;
            case INCREMENT_POINT:
                if (!isPlayStarted()) {
                    // play isn't started, let's take the liberty of starting it here
                    startPlay();
                }
                // add a point for the team (param[0] is the team
                result = incrementPoint(dataParams.length > 0 ? (Team) dataParams[0] : null);
                break;
            case UNDO_POINT:
                // undo the last point
                result = undoPoint();
                break;
            case CHANGE_STARTING_SERVER:
                // change starting server
                result = changeStartingServer();
                break;
            case CHANGE_STARTER:
                // change the starting server in the serving team
                result = changeStartingStarter();
                break;
            case CHANGE_STARTING_ENDS:
                // change starting ends
                result = changeStartingEnds();
                break;
            case REQUEST_MATCH_UPDATE:
                // have the broadcaster send out an update of the current match
                result = requestMatchUpdate();
                break;
            case ANNOUNCE_POINTS:
                // announce the current points
                result = announcePoints(dataParams.length > 0 ? ((MatchMessage.StringParam) dataParams[0]) : null);
                break;
        }
        /*
        if (isCommunicateToSockets && GamePlayBroadcaster.IsBroadcasting()) {
            // we are broadcasting the current state of the match, tell the broadcaster that
            // we just changed something that it can communicate out to listening sockets

            // we could send the entire message data, but this would be challenging to maintain the connection
            // therefore we will just send the whole match and settings every time
            //GamePlayBroadcaster.MatchChanged(message, dataParams);
        }*/
        // not recognised
        return result;
    }

    public void onMatchReceived(MatchSettings matchSettings, Match match, PointChange[] levelsChanged) {
        if (this.currentMatch instanceof BluetoothMatch) {
            // our current match is a BT connect match, give it this data to show
            ((BluetoothMatch) this.currentMatch).setReceivedMatchData(matchSettings, match, levelsChanged);
        }
    }

    public void onMatchChangeReceived(Match.MatchChange change) {
        if (this.currentMatch instanceof BluetoothMatch) {
            // our current match is a BT connect match, give it this data to show
            ((BluetoothMatch) this.currentMatch).setReceivedMatchData(change);
        }

    }

    private boolean resetMatch() {
        boolean isSuccess = false;
        if (null != this.currentMatch) {
            this.currentMatch.resetMatch();
            isSuccess = true;
        }
        return isSuccess;
    }

    private boolean startPlay() {
        // setup the service with the data that was passed us in the setup messages
        GamePlayService.SetupService(this.currentActivity, this.currentMatch, this.currentSettings);
        // start playing
        if (null != this.service) {
            // have the service, start playing what it has.
            this.service.startPlay();
        }
        // every time we start play, get the location that play was started to store
        // on the match
        if (null != this.currentActivity) {
            // check we have permission for this, quietly
            if (ActivityCompat.checkSelfPermission(this.currentActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this.currentActivity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // we have permission to ask for location of some kind, get the location for this match being started
                this.fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this.currentActivity, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations this can be null.
                                GamePlayCommunicator.this.setCurrentLocation(location);
                            }
                        });
            }
        }

        return null != this.service;
    }

    private synchronized void setCurrentLocation(Location location) {
        this.location = location;
    }

    private synchronized Location getCurrentLocation() {
        return this.application.getSettings().getIsStoreLocations() ? this.location : null;
    }

    public void showMatchNotification() {
        if (null != this.service && null != this.currentMatch && null != this.currentSettings) {
            if (null == this.notification) {
                // and the notification to show we are playing a match
                this.notification = new GamePlayNotification(this.service, this.application);
            }
            // and create the notification for the settings and match we now have
            this.notification.createMatchNotification(this.currentSettings, this.currentMatch);
        }
    }

    private boolean stopPlay() {
        // start playing
        if (null != this.service) {
            this.service.stopPlay();
        }
        // are stopped, silence!
        silenceCommunications();

        return null != this.service;
    }

    private boolean storeMatchState() {
        // no params in the data list
        if (null != service) {
            service.storeCurrentState(getCurrentLocation());
        }
        return null != service;
    }

    public boolean addStateListener(GamePlayService.GamePlayListener listener) {
        return null != this.service && this.service.addStateListener(listener);
    }

    public boolean removeStateListener(GamePlayService.GamePlayListener listener) {
        return null != this.service && this.service.removeStateListener(listener);
    }

    public boolean addMatchListener(Match.MatchListener listener) {
        return null != this.service && this.service.addMatchListener(listener);
    }

    public boolean removeMatchListener(Match.MatchListener listener) {
        return null != this.service && this.service.removeMatchListener(listener);
    }

    private boolean setupNewMatch(MatchSettings param1) {
        // the data is the settings to create the match from
        try {
            if (null == param1) {
                Log.error("cannot setup a new match communicated to us without the type");
            }
            else {
                // get rid of the old one hanging around
                if (null != this.currentMatch) {
                    sendRequest(MatchMessage.STOP_PLAY);
                    // and clear the match and the file
                    this.currentMatch = null;
                }
                // get the settings from the passed parameters
                this.currentSettings = param1;
                // setup the service with this new data now to clear the old stuff out
                GamePlayService.SetupService(this.currentActivity, this.currentMatch, this.currentSettings);
            }
        }
        catch (Exception e) {
            Log.error("unable to setup the match", e);
        }
        // return if this is null or not
        return null != this.currentSettings;
    }

    private boolean setupExistingMatch(Match param1, MatchSettings param2) {
        // the data is the match / settings / file to create the match from
        try {
            // get the settings from the passed parameters
            if (null == param1 || null == param2) {
                Log.error("cannot setup a match communicated to us without the type");
            }
            else {
                // we have a new (existing) match to setup
                this.currentMatch = param1;
                this.currentSettings = param2;
                // setup the service with this new data now to clear the old stuff out
                GamePlayService.SetupService(this.currentActivity, this.currentMatch, this.currentSettings);
            }
        }
        catch (Exception e) {
            Log.error("unable to setup the match", e);
        }
        // return if this is null or not
        return null != this.currentMatch && null != this.currentSettings;
    }

    private boolean incrementPoint(Team param1) {
        // the data is the team to increment the point for
        boolean isSuccess = false;
        if (!this.isMatchOver()) {
            // only do this if the match isn't over
            try {
                // get the team from the passed params
                this.currentMatch.incrementPoint(this.currentActivity, param1);
                isSuccess = true;
            } catch (Exception e) {
                Log.error("unable to increment the point", e);
            }
        }
        // return if we made it all the way through what we needed to do
        return isSuccess;
    }

    private boolean undoPoint() {
        // undo the last point, there is no data
        boolean isSuccess = false;
        try {
            // undo the point
            this.currentMatch.undoLastPoint(this.currentActivity);
            isSuccess = true;
        }
        catch (Exception e) {
            Log.error("unable to undo the point", e);
        }
        // return if we made it all the way through what we needed to do
        return isSuccess;
    }

    private boolean changeStartingEnds() {
        // change the starting end of play
        boolean isSuccess = false;
        try {
            // change this on the settings
            this.currentSettings.cycleTeamStartingEnds();
            // and update the match from the changed settings
            this.currentMatch.resetMatchSettings(this.currentActivity, this.currentSettings);
            isSuccess = true;
        }
        catch (Exception e) {
            Log.error("unable to change starting ends", e);
        }
        // return if we made it all the way through what we needed to do
        return isSuccess;
    }

    private boolean changeStartingServer() {
        // change the starting server
        boolean isSuccess = false;
        try {
            // swap over the team that is starting the match
            Team teamStarting = this.currentSettings.getStartingTeam();
            if (teamStarting == this.currentSettings.getTeamOne()) {
                // team one is starting, change this to team two
                this.currentSettings.setStartingTeam(this.currentSettings.getTeamTwo());
            }
            else {
                // team two is starting, change this
                this.currentSettings.setStartingTeam(this.currentSettings.getTeamOne());
            }
            // and update the match from the changed settings
            this.currentMatch.resetMatchSettings(this.currentActivity, this.currentSettings);
            isSuccess = true;
        }
        catch (Exception e) {
            Log.error("unable to change starting server", e);
        }
        // return if we made it all the way through what we needed to do
        return isSuccess;
    }

    private boolean changeStartingStarter() {
        // change the starting server for the current team
        boolean isSuccess = false;
        try {
            // swap over the server in the current serving team
            Team teamServing = this.currentMatch.getTeamServing();
            Player currentServer = teamServing.getServingPlayer();
            // use the other player from the team as the starting server
            for (Player player : teamServing.getPlayers()) {
                if (player != currentServer) {
                    // this is the other player
                    this.currentSettings.setTeamStartingServer(player);
                    break;
                }
            }
            // and update the match from the changed settings
            this.currentMatch.resetMatchSettings(this.currentActivity, this.currentSettings);
            isSuccess = true;
        }
        catch (Exception e) {
            Log.error("unable to change starting server in serving team", e);
        }
        // return if we made it all the way through what we needed to do
        return isSuccess;
    }

    private boolean requestMatchUpdate() {
        // have the broadcaster send a match update
        boolean isSuccess = false;
        try {
            // have the broadcaster send a match update
            GamePlayBroadcaster.BroadcastMatchUpdate();
            isSuccess = true;
        }
        catch (Exception e) {
            Log.error("unable to broadcast the match data", e);
        }
        // return if we made it all the way through what we needed to do
        return isSuccess;
    }

    private boolean announcePoints(MatchMessage.StringParam param1) {
        // the data is a string to say
        boolean isSuccess = false;
        try {
            // get the message from the param
            String message = param1.content;
            if (null != this.service) {
                this.service.speakMessage(message);
                isSuccess = true;
            }
        }
        catch (Exception e) {
            Log.error("unable to announce the score message", e);
        }
        // return if we made it all the way through what we needed to do
        return isSuccess;
    }


    private boolean createMatch() {
        // there is no data in this message
        try {
            // create the match
            if (null != this.currentSettings) {
                this.currentMatch = this.currentSettings.createMatch();
            }
            else {
                this.currentMatch = null;
            }
            // now we have a match, setup the match on the game play service, this is new so there is no file
            // yet, but this will start the match playing
            GamePlayService.SetupService(this.currentActivity, this.currentMatch, this.currentSettings);
        }
        catch (Exception e) {
            Log.error("unable to start the match", e);
        }
        // return if this is null or not
        return null != this.currentMatch;
    }

    public void onServicePlayInitiated(GamePlayService service) {
        // use this new service
        this.service = service;
        if (isActive()) {
            // and listen to it
            startCommunications(this.activeActivity);
        }
    }

    public boolean getIsBroadcastingMatch() {
        return GamePlayBroadcaster.IsBroadcasting();
    }

    public void setIsBroadcastingMatch(boolean isBroadcastingMatch) {
        if (!isBroadcastingMatch) {
            // silence the broadcaster
            GamePlayBroadcaster.SilenceCommunications();
        }
        else {
            // activate the broadcaster for this communicator
            GamePlayBroadcaster broadcaster = GamePlayBroadcaster.ActivateBroadcaster(this.activeActivity);
            // and start broadcasting
            broadcaster.startBroadcasting();
        }
    }

    public void startCommunications(BaseActivity activeActivity) {
        // remember for who we are communicating
        this.activeActivity = activeActivity;
        // start any required controllers to listen for inputs to change the score
        initialiseControllers(false);
    }

    boolean onGamePlayNotificationReceived(int extraNotificationId) {
        // pass this to the notification to parse and deal with
        if (null != this.notification) {
            return this.notification.onGamePlayNotificationReceived(extraNotificationId);
        }
        else {
            return false;
        }
    }

    public void silenceCommunications() {
        // we are no longer active, just dumb
        this.activeActivity = null;
        // kill any controllers so we fall silent
        initialiseControllers(true);

        if (null != this.notification) {
            this.notification.close();
            this.notification = null;
        }

        // stop the broadcasting too
        GamePlayBroadcaster.SilenceCommunications();
    }

    private void close() {
        // stop all communications to classes and remote devices
        silenceCommunications();
        // stop the beeping already
        if (null != this.toneGenerator) {
            this.toneGenerator.stopTone();
            this.toneGenerator.release();
        }
        if (null != this.vibrator) {
            this.vibrator.cancel();
        }
    }

    public void initialiseControllers() {
        // different to prevent someone else accidentally closing the sleeping media controller
        initialiseControllers(false);
    }

    private void initialiseControllers(boolean isForceClose) {
        // setup the controller here
        if (false == isForceClose && null != this.activeActivity) {
            // start the media controller
            SettingsControl settingsControl = new SettingsControl(this.application);
            if (null == this.mediaController) {
                this.mediaController = new MediaController(settingsControl, this.currentActivity);
                // and initialise it
                this.mediaController.addListener(this);
                this.mediaController.start(settingsControl);
            }
            else {
                // just set the new buttons to be sure they are up-to-date
                this.mediaController.setButtons(settingsControl);
            }
        }
        else {
            // close controllers
            if (null != this.mediaController) {
                // close the controller
                this.mediaController.removeListener(this);
                this.mediaController.close();
                this.mediaController = null;
            }
        }
    }

    public void onControllerInputFlic(Controller.ControllerPattern pattern) {
        // flic sends us a pattern, we need to process it just as any other controller
        if (null != this.application && isActive() && null != pattern) {
            SettingsControl settingsControl = new SettingsControl(this.application);
            if (settingsControl.getIsControlUseFlic1() || settingsControl.getIsControlUseFlic2()) {
                // get the correct action for one and two clicks, hold always being undo...
                Controller.ControllerAction action;
                switch (pattern) {
                    case SingleClick:
                        action = settingsControl.getIsControlTeams() ? Controller.ControllerAction.PointTeamOne : Controller.ControllerAction.PointServer;
                        break;
                    case DoubleClick:
                        action = settingsControl.getIsControlTeams() ? Controller.ControllerAction.PointTeamTwo : Controller.ControllerAction.PointReceiver;
                        break;
                    case LongClick:
                        action = Controller.ControllerAction.UndoLastPoint;
                        break;
                    default:
                        // there are no types left for flic, but let's be safe
                        action = Controller.ControllerAction.AnnouncePoints;
                        break;
                }
                // key codes are irrelevant for flic
                int[] keyCodes = new int[0];
                // and process the action just as any other remote control would
                onControllerInput(action);
            }
        }
        // else we are ignoring flic right now
    }

    @Override
    public void onControllerInput(Controller.ControllerAction action) {
        if (null != this.application) {
            SettingsSounds settingsSounds = new SettingsSounds(this.application);
            if (settingsSounds.getIsMakingSoundingAction()) {
                playTone(action);
            }
            if (settingsSounds.getIsMakingVibrateAction()) {
                vibrate(action);
            }
        }
        // and process the action
        processControllerAction(action);
    }

    public void vibrate(Controller.ControllerAction action) {
        if (null != this.vibrator && this.vibrator.hasVibrator()) {
            this.vibrator.cancel();
            switch (action) {
                case PointServer:
                case PointTeamOne:
                    // make the vibration for a single click;
                    if (Build.VERSION.SDK_INT >= 26) {
                        this.vibrator.vibrate(this.vibrationEffects[K_VIBRATE_SINGLE]);
                    } else {
                        this.vibrator.vibrate(Vibrations[K_VIBRATE_SINGLE], -1);
                    }
                    break;
                case PointReceiver:
                case PointTeamTwo:
                    // make the vibration for a double click;
                    if (Build.VERSION.SDK_INT >= 26) {
                        this.vibrator.vibrate(this.vibrationEffects[K_VIBRATE_DOUBLE]);
                    } else {
                        this.vibrator.vibrate(Vibrations[K_VIBRATE_DOUBLE], -1);
                    }
                    break;
                case AnnouncePoints:
                    // make the vibration for a triple click;
                    if (Build.VERSION.SDK_INT >= 26) {
                        this.vibrator.vibrate(this.vibrationEffects[K_VIBRATE_TRIPLE]);
                    } else {
                        this.vibrator.vibrate(Vibrations[K_VIBRATE_TRIPLE], -1);
                    }
                    break;
                case UndoLastPoint:
                    // make the vibration for a long click;
                    if (Build.VERSION.SDK_INT >= 26) {
                        this.vibrator.vibrate(this.vibrationEffects[K_VIBRATE_LONG]);
                    } else {
                        this.vibrator.vibrate(Vibrations[K_VIBRATE_LONG], -1);
                    }
                    break;
            }
        }
    }

    public void playTone(Controller.ControllerAction action) {
        if (null != this.toneGenerator) {
            // stop the tone
            this.toneGenerator.stopTone();
            switch (action) {
                case PointServer:
                case PointTeamOne:
                    // make the noise for a single click;
                    this.toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 100);
                    break;
                case PointReceiver:
                case PointTeamTwo:
                    // make the noise for a double click;
                    this.toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP2, 350);
                    break;
                case AnnouncePoints:
                    // make the noise for a triple click;
                    this.toneGenerator.startTone(ToneGenerator.TONE_CDMA_MED_L, 150);
                    break;
                case UndoLastPoint:
                    // make the noise for a long click;
                    this.toneGenerator.startTone(ToneGenerator.TONE_CDMA_CONFIRM, 200);
                    break;
            }
        }
    }

    public void playTone(Controller.KeyPress keyPress) {
        if (null != this.toneGenerator) {
            // stop the tone
            this.toneGenerator.stopTone();
            switch (keyPress) {
                case Short:
                    this.toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP, 150);
                    break;
                case Long:
                    this.toneGenerator.startTone(ToneGenerator.TONE_CDMA_CONFIRM, 200);
                    break;
            }
        }
    }

    @Override
    public void onControllerKeyPress(Controller.KeyPress[] keyPresses) {
        SettingsSounds settingsSounds = new SettingsSounds(this.application);
        if (null != keyPresses && settingsSounds.getIsMakingBeepingSounds()) {
            for (Controller.KeyPress keyPress : keyPresses) {
                // play the tone for each press
                playTone(keyPress);
            }
        }
    }

    private void processControllerAction(Controller.ControllerAction action) {
        if (null != this.currentMatch && null != action) {
            switch (action) {
                case PointServer:
                    incrementPoint(this.currentMatch.getTeamServing());
                    break;
                case PointReceiver:
                    Team otherTeam = this.currentMatch.getOtherTeam(this.currentMatch.getTeamServing());
                    incrementPoint(otherTeam);
                    break;
                case PointTeamOne:
                    incrementPoint(this.currentMatch.getTeamOne());
                    break;
                case PointTeamTwo:
                    incrementPoint(this.currentMatch.getTeamTwo());
                    break;
                case UndoLastPoint:
                    undoPoint();
                    break;
                case AnnouncePoints:
                    if (null != this.currentActivity) {
                        announcePoints(new MatchMessage.StringParam(this.currentMatch.createPointsAnnouncement(this.currentActivity)));
                    }
                    else {
                        Log.error("Cannot announce points as there is no activity operational to create the string from");
                    }
                    break;
            }
        }
    }
}
