package uk.co.darkerwaters.scorepal.application;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.controllers.Controller;
import uk.co.darkerwaters.scorepal.data.Match;
import uk.co.darkerwaters.scorepal.data.MatchId;
import uk.co.darkerwaters.scorepal.data.MatchSetup;
import uk.co.darkerwaters.scorepal.data.Score;
import uk.co.darkerwaters.scorepal.data.ScoreState;
import uk.co.darkerwaters.scorepal.dataui.MatchSpeaker;
import uk.co.darkerwaters.scorepal.dataui.MatchWriter;

public class MatchService extends ReportingService implements Match.MatchListener<Score> {

    private MatchNotification matchNotification = null;
    private SpeakService speakService = null;
    private MatchServiceControllers controllers = null;
    private MatchServicePlayTracker playTracker = null;

    private boolean isMatchCancelled = false;

    public static void CreateService(Activity context, Match match) {
        RunningService.createService(context, match);
    }

    public static void SetRunningService(MatchService newService) {
        RunningService.setService(newService);
    }

    public static MatchService GetRunningService() {
        return RunningService.getService();
    }

    public static void PrepareNewMatch(MatchSetup matchSetup) {
        RunningService.setPreparedSetup(matchSetup);
    }

    public static MatchSetup GetPreparedMatch() {
        return RunningService.getPreparedSetup();
    }

    private static class Service {
        private MatchSetup preparedSetup;
        private Match preparedMatch = null;
        private MatchService runningService = null;
        private final Object lock = new Object();
        MatchService getService() {
            synchronized (lock) {
                return runningService;
            }
        }
        void setService(MatchService service) {
            synchronized (lock) {
                runningService = service;
                if (null != runningService && null != preparedMatch) {
                    runningService.setActiveMatch(preparedMatch);
                }
            }
        }
        MatchSetup getPreparedSetup() { return preparedSetup; }
        void setPreparedSetup(MatchSetup setup) { preparedSetup = setup; }
        Match getPreparedMatch() { return preparedMatch; }
        public void setPreparedMatch(Match match) { this.preparedMatch = match; }
        public void createService(Activity activity, Match match) {
            synchronized (lock) {
                // remember the match for it to use when created
                preparedMatch = match;
                // create one now
                Intent intent = new Intent(activity, MatchService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    activity.startForegroundService(intent);
                }
                else {
                    activity.startService(intent);
                }
            }
        }
    }

    private static final Service RunningService = new Service();

    private Match activeMatch = null;
    private Match.MatchListener<Score> scoreRedoListener = null;

    public MatchService() {
        // construct this
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent notificationIntent = new Intent(this, ReportingService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        // create the notification to show while this service is running
        matchNotification = new MatchNotification(this);
        // start in the foreground so this keeps going with the screen off etc
        startForeground(MatchNotification.K_MATCH_NOTIFICATION_ID,
                matchNotification.createNoficationBuilder(RunningService.getPreparedMatch()).build());
        // update any open notification with this new match data
        matchNotification.updateNotification(activeMatch);

        // and let the base do it's thing
        return super.onStartCommand(intent, flags, startId);
    }

    public void setActiveMatch(Match activeMatch) {
        if (null != this.activeMatch) {
            this.activeMatch.removeListener(this);
            this.activeMatch.removeListener(this.scoreRedoListener);
            this.scoreRedoListener = null;
        }
        // and set the new active match
        this.activeMatch = activeMatch;
        // this new match is not cancelled
        isMatchCancelled = false;
        // if we die and come back again, we want to use this match the same
        RunningService.setPreparedMatch(activeMatch);
        // we have an active match, listen to this
        this.activeMatch.addListener(this);
        this.scoreRedoListener = CreateMatchRedoHandler(this, this.activeMatch);
    }

    public boolean storeMatchState(boolean areResultsAccepted) {
        // no params in the data list
        if (null != playTracker && false == isMatchCancelled) {
            playTracker.storeCurrentState(areResultsAccepted);
        }
        return null != playTracker;
    }

    public static Match.MatchListener<Score> CreateMatchRedoHandler(final Context context, final Match match) {
        Match.MatchListener<Score> matchListener = new Match.MatchListener<Score>() {
            @Override
            public void onMatchStateChanged(Score score, ScoreState state) {
                if (match != null
                        && (state.isChanged(ScoreState.ScoreChange.INCREMENT)
                        || state.isChanged(ScoreState.ScoreChange.INCREMENT_REDO))) {
                    // this was an item of score incremented. The undo history of this is okay but
                    // we can do better - we know the match that is playing and have a context and
                    // everything so can well describe the score
                    String scoreString = match.getDescription(MatchWriter.DescriptionLevel.SCORE, context);
                    match.describeLastHistoryChange(state.getState(), scoreString);
                }
            }
        };
        match.addListener(matchListener);
        return matchListener;
    }

    @Override
    public void onMatchStateChanged(Score score, ScoreState state) {
        if (!state.isChanged(ScoreState.ScoreChange.INCREMENT_REDO)) {
            // this is not during a 'redo' so we need to process and display this change
            if (activeMatch != null && speakService != null) {
                // so speak this state
                speakService.speakMessage(activeMatch.getSpokenStateMessage(this));
            }
            if (null != playTracker) {
                // every time the points change we want to check to see if we have ended or not
                playTracker.handlePlayEnding();
            }
            // update any open notification with this new match data
            if (null != matchNotification) {
                matchNotification.updateNotification(activeMatch);
            }
        }
    }

    public void onMatchSetupChanged(MatchSetup changedSetup, MatchSetup.SetupChange change) {
        if (null != activeMatch) {
            // there is an active match, we need to update this for the setup
            if (changedSetup != activeMatch.getSetup()) {
                // but this isn't the setup for the match - weird
                Log.error("There is a setup changing that isn't relevant for the active match.");
            }
            else {
                // have the match update itself
                activeMatch.applyChangedMatchSettings();
                // if this is a change in the first server while we are running a match
                // then it might be nice to say this
                if (change == MatchSetup.SetupChange.FIRST_TEAM_SERVER) {
                    if (null != speakService && ApplicationState.Instance().getPreferences().getSoundAnnounceChangeServer()) {
                        //append(spokenMessage, context.getString(R.string.change_server));
                        // append the name of the server
                        String serverName = MatchSpeaker.getSpeakingPlayerName(this, changedSetup, activeMatch.getServingPlayer());
                        // and make the message include the name of the player to serve
                        speakService.speakMessage(getString(R.string.change_server_server, serverName));
                    }
                }
            }
        }
    }

    public MatchNotification getNotification() {
        return matchNotification;
    }


    public void onControllerInteraction(Controller.ControllerAction action) {
        // pass this message to our helper
        if (null != controllers) {
            controllers.onControllerInteraction(action);
        }
    }

    public void speakSpecialMessage(String messageString) {
        if (null != speakService) {
            speakService.speakMessage(messageString);
        }
    }

    public Match getActiveMatch() {
        return this.activeMatch;
    }

    public MatchSetup getPreparedMatchSetup() {
        return RunningService.getPreparedSetup();
    }

    public void endMatch(boolean areResultsAccepted) {
        // store any match so as to not throw anything away
        storeMatchState(areResultsAccepted);
        // and close down the service now we are done
        closeService();
    }

    public void cancelMatch(boolean isDeleteMatch) {
        // remember we cancelled this so we don't save it
        this.isMatchCancelled = isDeleteMatch;
        // delete any stored versions of this match
        MatchPersistenceManager persistenceManager = MatchPersistenceManager.GetInstance();
        if (isDeleteMatch && null != this.activeMatch && null != persistenceManager) {
            MatchId matchId = new MatchId(this.activeMatch);
            persistenceManager.deleteMatchFile(matchId, this);
        }
        // close down the service to just cancel the match
        closeService();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // we are the running service, store this for others to access us
        RunningService.setService(this);
        initialiseControllers();

        // and start tracking the match progress
        if (null != playTracker) {
            playTracker.destroy(!this.isMatchCancelled);
        }
        playTracker = new MatchServicePlayTracker(this);

        // setup the speaking service
        if (null == this.speakService) {
            // create the thing for speaking
            this.speakService = new SpeakService(this);
        }
        // and set our local match data to the static match data stored ready for us
        setActiveMatch(RunningService.getPreparedMatch());

        // update the notification
        if (null != this.matchNotification) {
            this.matchNotification.updateNotification(activeMatch);
        }
    }

    public void initialiseControllers() {
        // listen to any controllers properly here
        if (null != controllers) {
            controllers.destroy();
        }
        controllers = new MatchServiceControllers(this);
    }

    @Override
    public void onDestroy() {
        // stop listening to any controllers that are global
        if (null != controllers) {
            controllers.destroy();
            controllers = null;
        }
        // and the play tracker
        if (null != playTracker) {
            playTracker.destroy(!this.isMatchCancelled);
            playTracker = null;
        }

        if (null != activeMatch) {
            activeMatch.removeListener(this);
            activeMatch.removeListener(scoreRedoListener);
            scoreRedoListener = null;
            activeMatch = null;
            isMatchCancelled = false;
        }
        // stop speaking
        if (null != this.speakService) {
            this.speakService.close();
            this.speakService = null;
        }
        // close down the notification now this match is ended
        this.matchNotification.close();
        this.matchNotification = null;

        RunningService.setService(null);
        // and destroy
        super.onDestroy();
    }
}
