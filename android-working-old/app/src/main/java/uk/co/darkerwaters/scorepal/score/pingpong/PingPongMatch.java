package uk.co.darkerwaters.scorepal.score.pingpong;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.GamePlayCommunicator;
import uk.co.darkerwaters.scorepal.players.Team;
import uk.co.darkerwaters.scorepal.score.base.HistoryValue;
import uk.co.darkerwaters.scorepal.score.Match;
import uk.co.darkerwaters.scorepal.score.base.MatchMessage;

public class PingPongMatch extends Match<PingPongScore, PingPongMatchSettings> {

    private int expeditePoints;
    private int expediteMinutes;
    private boolean expediteMinutesElapsed = false;
    private boolean announceExpediteSystem = false;

    private Handler expediteHandler = null;
    private Runnable expediteRunnable = new Runnable() {
        @Override
        public void run() {
            // the time has elapsed
            expediteMinutesElapsed = true;
            // this is called as the expedite system comes into effect
            if (startExpediteSystem()) {
                // this is started right now half way through anything else
                // announce it immediately
                GamePlayCommunicator communicator = GamePlayCommunicator.GetActiveCommunicator();
                if (null != communicator) {
                    // get the string to announce
                    Activity currentActivity = communicator.getCurrentActivity();
                    String messageString;
                    if (null != currentActivity) {
                        messageString = currentActivity.getString(R.string.speak_expedite_system);
                    }
                    else {
                        // no context - let's do our best instead
                        messageString = "Expedite System to commence…";
                    }
                    // send this message to announce the points with this string to say right away
                    communicator.sendRequest(MatchMessage.ANNOUNCE_POINTS, new MatchMessage.StringParam(messageString));
                }
            }
        }
    };

    public PingPongMatch(PingPongMatchSettings settings) {
        super(settings, new PingPongMatchSpeaker(), new PingPongMatchWriter());
    }

    @Override
    protected PingPongScore createScore(Team[] teams, PingPongMatchSettings settings) {
        return new PingPongScore(teams, settings);
    }

    @Override
    protected void resetScoreToStartingPosition() {
        // let the base reset itself
        super.resetScoreToStartingPosition();
        // and we can reset ours too
        PingPongMatchSettings settings = getMatchSettings();
        if (null != settings && settings.isExpediteSystemEnabled()) {
            // need to remember when to start the expedite system off
            this.expeditePoints = settings.getExpediteSystemPoints();
            boolean minutesChanged = this.expediteMinutes != settings.getExpediteSystemMinutes();
            this.expediteMinutes = settings.getExpediteSystemMinutes();

            if (null != this.expediteHandler && minutesChanged) {
                // the old handler is no longer valid, cancel it.
                cancelExpediteSystem();
            }
        }
        else {
            // both are -1, to ignore
            this.expeditePoints = -1;
            this.expediteMinutes = -1;
            cancelExpediteSystem();
        }
    }

    private boolean startExpediteSystem() {
        boolean isSystemStarted = false;
        // change the score to be in expedite system if we have scored enough
        PingPongScore score = this.getScore();
        if (null != score
                && this.expediteMinutesElapsed
                && !score.isExpediteSystemInEffect()) {
            // how many points are played
            int points = score.getPoints(getTeamOne()) + score.getPoints(getTeamTwo());
            if (points >= this.expeditePoints) {
                 // there are enough points played to start, start it now
                score.setExpediteSystemInEffect(true);
                isSystemStarted = true;
            }
        }
        return isSystemStarted;
    }

    private void scheduleExpediteSystem() {
        if (null == this.expediteHandler) {
            // we need to kick off the handler to speed up play at the correct time
            this.expediteHandler = new Handler();
            this.expediteHandler.postDelayed(this.expediteRunnable, this.expediteMinutes * 60000L);
        }
    }

    private void cancelExpediteSystem() {
        if (null != this.expediteHandler) {
            // kill the handler by removing the callbacks sent
            this.expediteHandler.removeCallbacks(this.expediteRunnable);
            this.expediteHandler = null;
        }
        this.expediteMinutesElapsed = false;
        // cancel the system in the score
        this.getScore().setExpediteSystemInEffect(false);
    }

    boolean getIsAnnounceExpediteSystem() {
        return this.announceExpediteSystem;
    }

    void expediteSystemAnnounced() {
        this.announceExpediteSystem = false;
    }

    int getPointsInRound() {
        PingPongScore score = getScore();
        if (null != score) {
            return score.getPointsInRound();
        }
        else {
            return getMatchSettings().getPointsInRound();
        }
    }

    int getDecidingPoint() {
        PingPongScore score = getScore();
        if (null != score) {
            return score.getDecidingPoint();
        }
        else {
            return getMatchSettings().getDecidingPoint();
        }
    }

    @Override
    public void onScoreChanged(Team team, int level, int newPoint) {
        // let the base deal with this
        super.onScoreChanged(team, level, newPoint);
        switch (level) {
            case PingPongScore.LEVEL_POINT:
                // when adding points we should be sure that the expedite system is started ok
                if (null == this.expediteHandler && this.expediteMinutes > 0) {
                    // we are playing and want to expedite at some time but we are not
                    // expecting it to happen, schedule it to happen from now
                    scheduleExpediteSystem();
                }
                if (this.expediteMinutesElapsed) {
                    // start expedite system if we have earned enough points
                    if (startExpediteSystem()) {
                        // this is started as points become enough, remember to announce this
                        // as we announce the change in points
                        this.announceExpediteSystem = true;
                    }
                }
                break;
            case PingPongScore.LEVEL_ROUND:
                // when the round is won, we need to reset the old expedite system
                cancelExpediteSystem();
                break;
        }
    }

    @Override
    protected void updateHistoryValue(Context context, HistoryValue history) {
        // let the base do it's thing
        super.updateHistoryValue(context, history);
        // and update if the game was just won
        history.importance = getCurrentHistoryImportance();
    }

    private HistoryValue.Importance getCurrentHistoryImportance() {
        if (0 == this.getScore().getPoints(getTeamOne()) && 0 == this.getScore().getPoints(getTeamTwo())) {
            // this is a new round, this is fairly important
            return HistoryValue.Importance.MEDIUM;
        }
        else {
            // this is normal - low
            return HistoryValue.Importance.LOW;
        }
    }

    @Override
    protected HistoryValue createHistoryValue(int teamIndex, String scoreString) {
        // instead of creating the base history value, we want to create
        // a pingPong one that includes the flag if a game was just started
        HistoryValue historyValue = super.createHistoryValue(teamIndex, scoreString);
        // set the importance
        historyValue.importance = getCurrentHistoryImportance();
        // and return
        return historyValue;
    }
}
