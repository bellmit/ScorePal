package uk.co.darkerwaters.scorepal.data;

import android.os.Handler;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.MatchService;
import uk.co.darkerwaters.scorepal.dataui.PingPongMatchSpeaker;
import uk.co.darkerwaters.scorepal.dataui.PingPongMatchWriter;
import uk.co.darkerwaters.scorepal.points.Point;
import uk.co.darkerwaters.scorepal.points.SimplePoint;

public class PingPongMatch extends Match<PingPongSetup, PingPongScore> {

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
                MatchService service = MatchService.GetRunningService();
                if (null != service) {
                    // get the string to announce
                    String messageString = service.getString(R.string.speak_expedite_system);
                    // send this message to announce the points with this string to say right away
                    service.speakSpecialMessage(messageString);
                }
            }
        }
    };

    public PingPongMatch(PingPongSetup matchSetup) {
        super(matchSetup, new PingPongScore(matchSetup), new PingPongMatchSpeaker(), new PingPongMatchWriter());
    }

    @Override
    protected void storeJSONData(JSONObject data) throws JSONException {
        super.storeJSONData(data);
        // store our data in this object too - only things that will not be recreated when
        // the score is replayed in this match - ie, very little
    }

    protected void restoreFromJSON(JSONObject data, int version) throws JSONException {
        super.restoreFromJSON(data, version);
        // and get our data from this object that we stored here
    }

    @Override
    public void resetMatch() {
        super.resetMatch();

        // and we can reset our data too
        cancelExpediteSystem();
    }

    private boolean startExpediteSystem() {
        boolean isSystemStarted = false;
        // change the score to be in expedite system if we have scored enough
        if (null != score
                && this.expediteMinutesElapsed
                && !score.isExpediteSystemInEffect()) {
            // how many points are played
            int points = score.getPoints(MatchSetup.Team.T_ONE) + score.getPoints(MatchSetup.Team.T_TWO);
            PingPongSetup setup = getSetup();
            if (setup.isExpediteSystemEnabled() && points >= setup.getExpediteSystemPoints().num) {
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
            this.expediteHandler.postDelayed(this.expediteRunnable, getSetup().getExpediteSystemMinutes().num * 60000L);
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
        score.setExpediteSystemInEffect(false);
    }

    public boolean getIsAnnounceExpediteSystem() {
        return this.announceExpediteSystem;
    }

    public void expediteSystemAnnounced() {
        this.announceExpediteSystem = false;
    }

    public void incrementPoint(MatchSetup.Team team) {
        // reset the last state
        score.resetState();
        // affect the change
        score.incrementPoint(team, PingPongScore.LEVEL_POINT);
        PingPongSetup setup = getSetup();
        // when adding points we should be sure that the expedite system is started ok
        if (null == this.expediteHandler && setup.isExpediteSystemEnabled()) {
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
        // and inform all listeners of this change now that it is complete
        informListenersOfChange();
    }

    public void incrementGame(MatchSetup.Team team) {
        // reset the last state
        score.resetState();
        // affect the change
        score.incrementPoint(team, PingPongScore.LEVEL_ROUND);
        // when the round is won, we need to reset the old expedite system
        cancelExpediteSystem();
        // and inform all listeners of this change now that it is complete
        informListenersOfChange();
    }

    public Point getDisplayPoint(MatchSetup.Team team) {
        // just return the point as a string
        return new SimplePoint(score.getPoints(team));
    }

    public Point getDisplayRound(MatchSetup.Team team) {
        // just return the point as a string
        return new SimplePoint(score.getRounds(team));
    }

    public int getPointsTotal(int level, MatchSetup.Team team) {
        return score.getPointsTotal(level, team);
    }

}
