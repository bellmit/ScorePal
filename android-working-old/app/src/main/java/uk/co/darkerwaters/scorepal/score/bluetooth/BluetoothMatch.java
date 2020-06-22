package uk.co.darkerwaters.scorepal.score.bluetooth;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Date;

import uk.co.darkerwaters.scorepal.application.GamePlayBroadcaster;
import uk.co.darkerwaters.scorepal.players.Team;
import uk.co.darkerwaters.scorepal.score.Match;
import uk.co.darkerwaters.scorepal.score.base.MatchMessage;
import uk.co.darkerwaters.scorepal.score.MatchSettings;
import uk.co.darkerwaters.scorepal.score.MatchSpeaker;
import uk.co.darkerwaters.scorepal.score.MatchWriter;
import uk.co.darkerwaters.scorepal.score.base.PointChange;

public class BluetoothMatch extends Match<BluetoothScore, BluetoothMatchSettings> {

    private Match containedMatch;
    private BluetoothMatchSettings settings;

    private final Object matchLock = new Object();

    public BluetoothMatch(BluetoothMatchSettings settings) {
        super(settings, new MatchSpeaker<BluetoothMatch>(), new MatchWriter<BluetoothMatch>());

        this.settings = settings;
        // initially we have no data, nothing contained
        this.containedMatch = null;
    }

    @Override
    public boolean isCacheMatch() {
        // don't cache this match - it is the contained match we are interested in
        return false;
    }

    public boolean isSerialiseMatch() {
        synchronized (this.matchLock) {
            // don't save this match if there is no contained match to save
            return null != this.containedMatch && super.isSerialiseMatch();
        }
    }

    @Override
    public String getMatchId(Context context) {
        // overridden to save the wrapped match instead of this one
        synchronized (this.matchLock) {
            if (null != this.containedMatch) {
                return this.containedMatch.getMatchId(context);
            } else {
                return super.getMatchId(context);
            }
        }
    }

    @Override
    public Date getMatchPlayedDate() {
        // overridden to save the wrapped match instead of this one
        synchronized (this.matchLock) {
            if (null != this.containedMatch){
                return this.containedMatch.getMatchPlayedDate();
            }
            else {
                return super.getMatchPlayedDate();
            }
        }
    }

    @Override
    public void serialiseToJson(Context context, JSONArray dataArray) throws JSONException {
        // overridden to save the wrapped match instead of this one
        synchronized (this.matchLock) {
            if (null != this.containedMatch){
                this.containedMatch.serialiseToJson(context, dataArray);
            }
            else {
                super.serialiseToJson(context, dataArray);
            }
        }
    }

    @Override
    public void deserialiseFromJson(Context context, int version, JSONArray dataArray) throws JSONException {
        // overridden to save the wrapped match instead of this one
        synchronized (this.matchLock) {
            if (null != this.containedMatch){
                this.containedMatch.deserialiseFromJson(context, version, dataArray);
            }
            else {
                super.deserialiseFromJson(context, version, dataArray);
            }
        }
    }

    @Override
    public int incrementPoint(Context context, Team team) {
        // do nothing, can't change this match data, but we can ask the broadcaster to
        // to do this we need the team from the match we contain that corresponds to the one
        // we were just sent as winning the point
        synchronized (this.matchLock) {
            if (null != this.containedMatch) {
                Team winningTeam;
                if (team == getTeamOne()) {
                    // team one
                    winningTeam = this.containedMatch.getTeamOne();
                }
                else {
                    // will be team two then
                    winningTeam = this.containedMatch.getTeamTwo();
                }
                GamePlayBroadcaster.MatchChanged(MatchMessage.INCREMENT_POINT, winningTeam);
            }
        }
        // we don't change this match, we ask the broadcaster to do their thing instead
        return 0;
    }

    public Match getContainedMatch() {
        synchronized (this.matchLock) {
            return this.containedMatch;
        }
    }

    public MatchSettings getContainedMatchSettings() {
        synchronized (this.matchLock) {
            return this.settings.getContainedMatchSettings();
        }
    }

    @Override
    public Team undoLastPoint(Context context) {
        // do nothing, can't change this match data, but we can ask the broadcaster to
        GamePlayBroadcaster.MatchChanged(MatchMessage.UNDO_POINT);
        // we don't change this match, we ask the broadcaster to do their thing instead
        return null;
    }

    @Override
    protected BluetoothScore createScore(Team[] teams, BluetoothMatchSettings settings) {
        return new BluetoothScore(teams, settings);
    }

    @Override
    public boolean isMatchStarted() {
        // this match is started if we received something
        boolean isMatchStarted = super.isMatchStarted();
        synchronized (this.matchLock) {
            isMatchStarted = isMatchStarted || null != this.containedMatch;
        }
        return isMatchStarted;
    }

    public void setReceivedMatchData(MatchSettings matchSettings, Match match, PointChange[] levelsChanged) {
        // remember the match data we want to use
        synchronized (this.matchLock) {
            this.settings.setContainedMatchSettings(matchSettings);
            this.containedMatch = match;
        }
        // just remember this data for when the score is queried as the listeners will
        // also be receiving a match_change message from the broadcaster that corresponds to their
        // changes as they heard them. So the play activity should work as the sending application.
        if (null != levelsChanged && levelsChanged.length > 0) {
            // inform the base of this change
            preparePointsForLevelChange();
            for (PointChange change : levelsChanged) {
                onScoreChanged(change.team, change.level, change.point);
            }
            // and inform listeners of the change that occurred from the other match
            informListenersOfPointChange();
        }
    }

    public void setReceivedMatchData(MatchChange change) {
        // this is a remote change to this match, be it from another source - tell everyone this
        informListeners(change);
    }

    @Override
    public String getDescription(MatchWriter.DescriptionLevel level, Context context) {
        synchronized (this.matchLock) {
            if (null != this.containedMatch) {
                return this.containedMatch.getDescription(level, context);
            }
        }
        // if here then there is no contained match, just copy the base
        return super.getDescription(level, context);
    }

    @Override
    public String createPointsPhrase(Context context, PointChange topChange) {
        synchronized (this.matchLock) {
            if (null != this.containedMatch) {
                return this.containedMatch.createPointsPhrase(context, topChange);
            }
        }
        // if here then there is no contained match, just copy the base
        return super.createPointsPhrase(context, topChange);
    }

    @Override
    public String createPointsAnnouncement(Context context) {
        synchronized (this.matchLock) {
            if (null != this.containedMatch) {
                return this.containedMatch.createPointsAnnouncement(context);
            }
        }
        // if here then there is no contained match, just copy the base
        return super.createPointsAnnouncement(context);
    }
}
