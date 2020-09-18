package uk.co.darkerwaters.scorepal.data;

import android.content.Context;
import android.location.Location;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import uk.co.darkerwaters.scorepal.application.MatchService;
import uk.co.darkerwaters.scorepal.controllers.Controller;
import uk.co.darkerwaters.scorepal.dataui.MatchSpeaker;
import uk.co.darkerwaters.scorepal.dataui.MatchWriter;
import uk.co.darkerwaters.scorepal.points.Point;
import uk.co.darkerwaters.scorepal.points.SimplePoint;
import uk.co.darkerwaters.scorepal.points.Sport;

public abstract class Match<TSetup extends MatchSetup, TScore extends Score> {

    public interface MatchListener<TScore> {
        void onMatchStateChanged(TScore score, ScoreState state);
    }

    private TSetup setup;
    protected final TScore score;

    private final MatchSpeaker speaker;
    private final MatchWriter writer;

    private final List<MatchListener<TScore>> listeners = new ArrayList<>();

    private Date dateMatchStarted;
    private int matchTimePlayed;
    private boolean isDataPersisted = false;
    private Location playedLocation;

    public Match(TSetup matchSetup, TScore score, MatchSpeaker speaker, MatchWriter writer) {
        this.setup = matchSetup;
        this.score = score;
        this.speaker = speaker;
        this.writer = writer;
        // and the time played
        this.matchTimePlayed = 0;
        this.dateMatchStarted = new Date();
        // this data is not saved yet
        this.isDataPersisted = false;
    }

    public void resetMatch() {
        // reset the last state
        score.resetState();
        // the position and server and stuff will have changed on the teams, reset them here
        score.resetScore();
        // and the time played
        this.matchTimePlayed = 0;
        // this data is not saved yet
        this.isDataPersisted = false;
        // inform listeners so they can set the player who is starting serve, location etc.
        informListenersOfChange();
    }

    public void applyChangedMatchSettings() {
        // this is a little different to the reset as we want to keep the score
        // so we can just restore the point history, which has the side-effect of doing just this
        score.restorePointHistory(new Score.RedoListener() {
            @Override
            public void pointIncremented() {
                // every time a point is incremented inform listeners
                informListenersOfChange();
            }
        });
        // reset any state as nothing actually changed
        score.resetState();
        // inform listeners of this change to the score
        informListenersOfChange();
    }

    public void describeLastHistoryChange(int state, String description) {
        score.describeLastPoint(state, description);
    }

    public void addMatchTimePlayed(int timePlayed) {
        this.matchTimePlayed += timePlayed;
        // this data is not saved yet
        this.isDataPersisted = false;
    }

    public final JSONObject getAsJSON() {
        JSONObject dataObject = new JSONObject();
        try {
            // store all our data into this object
            storeJSONData(dataObject);
            // and put this in a top-level named object of the classname
            JSONObject topLevel = new JSONObject();
            // version it
            topLevel.put("ver", 1);
            // store the sport so we can create the proper classes from it
            topLevel.put("sport", getSport().name());
            // and the actual data
            topLevel.put("data", dataObject);
            // and return this top level object
            return topLevel;
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    public static Match CreateFromJSON(String jsonString, MatchSetup setup) {
        Match newInstance = null;
        try {
            // get the data as JSON for a start
            JSONObject topLevel = new JSONObject(jsonString);
            // the top level has the sport, we can get the settings type from this
            Sport sport = Sport.valueOf(topLevel.getString("sport"));
            // there has to be an empty constructor for the most derived settings classes
            Constructor<? extends Match> ctor = sport.matchClass.getConstructor(sport.setupClass);
            // create the setup class from the default constructor for the match (takes a settings class)
            newInstance = ctor.newInstance(setup);
            // and set the member data from this
            newInstance.setFromJSON(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return newInstance;
    }

    public final void setFromJSON(String jsonString) {
        try {
            // the top level just contains everything
            JSONObject topLevel = new JSONObject(jsonString);
            // get data object
            JSONObject dataObject = topLevel.getJSONObject("data");
            // and set our data from this
            restoreFromJSON(dataObject, topLevel.getInt("ver"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void storeJSONData(JSONObject data) throws JSONException {
        data.put("id", new MatchId(this).toString());
        data.put("secs", matchTimePlayed);
        data.put("locn", new LocationWrapper(playedLocation).serialiseToString());
        // most importantly store the score so we can re-establish the state of this match
        // when we reload it
        JSONObject scoreJSON = new JSONObject();
        this.score.storeJSONData(scoreJSON);
        data.put("score", scoreJSON);
    }

    protected void restoreFromJSON(JSONObject data, int version) throws JSONException {
        MatchId matchId = new MatchId(data.getString("id"));
        this.dateMatchStarted = matchId.getDate();
        this.matchTimePlayed = data.getInt("secs");
        this.playedLocation = new LocationWrapper().deserialiseFromString(data.getString("locn")).content;
        // most importantly we want to put the score in. Then we can replay the score to
        // put the state of this match back to how it was when we saved it
        this.score.restoreFromJSON(data.getJSONObject("score"), version, new Score.RedoListener() {
            @Override
            public void pointIncremented() {
                // every time a point is incremented inform listeners
                informListenersOfChange();
            }
        });
    }

    public boolean isMatchPlayStarted() {
        // we are started if there are points at the top level for either team
        for (int i = 0; i < getScoreLevels(); ++i) {
            if (this.score.getPointsTotal(i, MatchSetup.Team.T_ONE) > 0 ||
                    this.score.getPointsTotal(i, MatchSetup.Team.T_TWO) > 0) {
                // someone has scored something
                return true;
            }
        }
        // if here, there is no score on any level
        return false;
    }

    public boolean isMatchOver() {
        return this.score.isMatchOver();
    }

    public int getMatchTimePlayed() {
        return this.matchTimePlayed;
    }

    public boolean isDataPersisted() { return this.isDataPersisted; }

    public void setDataPersisted() { this.isDataPersisted = true; }

    public Date getDateMatchStarted() { return this.dateMatchStarted; }

    public void setDateMatchStarted(Date value) { this.dateMatchStarted = value == null ? this.dateMatchStarted : value; }

    public int getPoint(int level, MatchSetup.Team team) {
        return score.getPoint(level, team);
    }

    public Point getDisplayPoint(int level, MatchSetup.Team team) {
        return new SimplePoint(getPoint(level, team));
    }

    public String getLevelTitle(int level, Context context) {
        return writer.getLevelTitle(level, context);
    }

    public int getScoreLevels() {
        return score.getLevels();
    }

    public MatchSetup.Team getMatchWinner() {
        return this.score.getWinner(this.score.getLevels() - 1);
    }

    public ScoreHistory.HistoryValue[] getWinnersHistory() {
        return this.score.getWinnersHistory();
    }

    public Location getPlayedLocation() {
        return this.playedLocation;
    }

    public void setPlayedLocation(Location currentLocation) {
        this.playedLocation = currentLocation;
    }

    public void undoLastPoint() {
        // reset the last state
        score.resetState();
        // affect the change
        MatchSetup.Team undoTeam = score.undoLastPoint(new Score.RedoListener() {
            @Override
            public void pointIncremented() {
                // inform listeners of this reconstruction of the undo stack
                informListenersOfChange();
            }
        });
        if (null != undoTeam) {
            // and inform all listeners of this change now that it is complete
            informListenersOfChange();
        }
    }

    /*
    this is a private function so that it is done from the input which we trust,
    incrementing requires care and usually a derived class to do it
     */
    private void incrementPoint(MatchSetup.Team team) {
        // reset the last state
        score.resetState();
        // affect the change
        score.incrementPoint(team, 0);
        // and inform all listeners of this change now that it is complete
        informListenersOfChange();
    }

    public void onControllerInput(Controller.ControllerAction selectedAction) {
        // reset any state built up at this point
        score.resetState();
        // now do the action
        switch (selectedAction) {
            case PointServer:
                if (!isMatchOver()) {
                    incrementPoint(score.getServingTeam());
                }
                break;
            case PointReceiver:
                if (!isMatchOver()) {
                    incrementPoint(setup.getOtherTeam(score.getServingTeam()));
                }
                break;
            case PointTeamOne:
                if (!isMatchOver()) {
                    incrementPoint(MatchSetup.Team.T_ONE);
                }
                break;
            case PointTeamTwo:
                if (!isMatchOver()) {
                    incrementPoint(MatchSetup.Team.T_TWO);
                }
                break;
            case UndoLastPoint:
                undoLastPoint();
                break;
            case AnnouncePoints:
                // announce the current score then
                MatchService service = MatchService.GetRunningService();
                if (null != service) {
                    service.speakSpecialMessage(createPointsAnnouncement(service));
                }
                break;
        }
    }

    public boolean addListener(MatchListener<TScore> listener) {
        synchronized (this.listeners) {
            return this.listeners.add(listener);
        }
    }

    public boolean removeListener(MatchListener<TScore> listener) {
        synchronized (this.listeners) {
            return this.listeners.remove(listener);
        }
    }

    public void informListenersOfChange() {
        synchronized (this.listeners) {
            if (!this.score.state.isEmpty()) {
                // there was a change in state, inform listeners of this
                for (MatchListener<TScore> listener : this.listeners) {
                    listener.onMatchStateChanged(score, score.state);
                }
            }
        }
        // this data is not saved yet
        this.isDataPersisted = false;
    }

    public TSetup getSetup() {
        return this.setup;
    }

    public void endMatch() {
        // end this match here
    }

    public Sport getSport() {
        return null != setup ? setup.getSport() : null;
    }

    public MatchSetup.Team getServingTeam() {
        return score.getServingTeam();
    }

    public MatchSetup.Player getServingPlayer() {
        return score.getServingPlayer();
    }

    public String getDescription(MatchWriter.DescriptionLevel level, Context context) {
        return this.writer.getDescription(this, level, context);
    }

    public String getStateDescription(Context context) {
        return getStateDescription(context, this.score.state.getState());
    }

    public String getStateDescription(Context context, int state) {
        return writer.getStateDescription(context, state);
    }

    public String getSpokenStateMessage(Context context) {
        return this.speaker.getSpeakingStateMessage(context, this, score.state);
    }

    public String createPointsPhrase(Context context, MatchSetup.Team team, int level) {
        return this.speaker.createPointsPhrase(this, context, team, level);
    }

    public String createScorePhrase(Context context, MatchSetup.Team team, int level) {
        return this.speaker.createScorePhrase(this, context, team, level);
    }

    public String createPointsAnnouncement(Context context) {
        return this.speaker.createPointsAnnouncement(this, context);
    }
}
