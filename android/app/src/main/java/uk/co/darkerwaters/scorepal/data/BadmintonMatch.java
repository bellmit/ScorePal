package uk.co.darkerwaters.scorepal.data;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.darkerwaters.scorepal.dataui.BadmintonMatchSpeaker;
import uk.co.darkerwaters.scorepal.dataui.BadmintonMatchWriter;
import uk.co.darkerwaters.scorepal.points.Point;
import uk.co.darkerwaters.scorepal.points.SimplePoint;

public class BadmintonMatch extends Match<BadmintonSetup, BadmintonScore> {

    public BadmintonMatch(BadmintonSetup matchSetup) {
        super(matchSetup, new BadmintonScore(matchSetup),
                new BadmintonMatchSpeaker(), new BadmintonMatchWriter());
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

    public void incrementPoint(MatchSetup.Team team) {
        // reset the last state
        score.resetState();
        // affect the change
        score.incrementPoint(team, BadmintonScore.LEVEL_POINT);
        // and inform all listeners of this change now that it is complete
        informListenersOfChange();
    }

    public void incrementGame(MatchSetup.Team team) {
        // reset the last state
        score.resetState();
        // affect the change
        score.incrementPoint(team, BadmintonScore.LEVEL_GAME);
        // and inform all listeners of this change now that it is complete
        informListenersOfChange();
    }

    public Point getDisplayPoint(MatchSetup.Team team) {
        // just return the point as a string
        return new SimplePoint(score.getPoints(team));
    }

    public Point getDisplayGame(MatchSetup.Team team) {
        // just return the point as a string
        return new SimplePoint(score.getGames(team));
    }

    public int getPointsTotal(int level, MatchSetup.Team team) {
        return score.getPointsTotal(level, team);
    }
}
