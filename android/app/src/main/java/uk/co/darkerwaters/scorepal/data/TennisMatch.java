package uk.co.darkerwaters.scorepal.data;

import android.util.Pair;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.darkerwaters.scorepal.dataui.TennisMatchSpeaker;
import uk.co.darkerwaters.scorepal.dataui.TennisMatchWriter;
import uk.co.darkerwaters.scorepal.points.Point;
import uk.co.darkerwaters.scorepal.points.SimplePoint;
import uk.co.darkerwaters.scorepal.points.TennisPoint;

public class TennisMatch extends Match<TennisSetup, TennisScore> {

    public TennisMatch(TennisSetup matchSetup) {
        super(matchSetup, new TennisScore(matchSetup),
                new TennisMatchSpeaker(), new TennisMatchWriter());
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
        score.incrementPoint(team, TennisScore.LEVEL_POINT);
        // and inform all listeners of this change now that it is complete
        informListenersOfChange();
    }

    public void incrementGame(MatchSetup.Team team) {
        // reset the last state
        score.resetState();
        // affect the change
        score.incrementPoint(team, TennisScore.LEVEL_GAME);
        // and inform all listeners of this change now that it is complete
        informListenersOfChange();
    }

    public void incrementSet(MatchSetup.Team team) {
        // reset the last state
        score.resetState();
        // affect the change
        score.incrementPoint(team, TennisScore.LEVEL_SET);
        // and inform all listeners of this change now that it is complete
        informListenersOfChange();
    }

    @Override
    public Point getDisplayPoint(int level, MatchSetup.Team team) {
        Point displayPoint;
        if (!isInTieBreak() && level == TennisScore.LEVEL_POINT) {
            // points are special, handle them here
            int points = score.getPoints(team);
            MatchSetup.Team opposition = getSetup().getOtherTeam(team);
            int otherPoints = score.getPoints(opposition);
            switch (points) {
                case 0: // love
                case 1: // 15
                case 2: // 30
                    // we are less than 40, just return the string from the array
                    displayPoint = TennisPoint.fromVal(points);
                    break;
                case 3:
                    // we have 40, if the other player has 40 too, we are at deuce
                    if (otherPoints == 3) {
                        // this is 40-40
                        displayPoint = TennisPoint.DEUCE;
                    } else {
                        // they have fewer, or advantage, we just have 40
                        displayPoint = TennisPoint.FORTY;
                    }
                    break;
                default:
                    // if we are one ahead we have advantage
                    int delta = points - otherPoints;
                    switch(delta) {
                        case 0 :
                            //this is deuce
                            displayPoint = TennisPoint.DEUCE;
                            break;
                        case 1:
                            // we have ad
                            displayPoint = TennisPoint.ADVANTAGE;
                            break;
                        case -1:
                            // we are disadvantaged
                            displayPoint = TennisPoint.FORTY;
                            break;
                        default:
                            // we are far enough ahead to have won the game
                            displayPoint = TennisPoint.GAME;
                            break;
                    }
            }
        }
        else {
            // do at the base does
            displayPoint = super.getDisplayPoint(level, team);
        }
        return displayPoint;
    }

    public Pair<Point,Point> getPoints(int setIndex, int gameIndex) {
        int[] points = score.getPoints(setIndex, gameIndex);
        return new Pair<Point,Point>(new SimplePoint(points[0]), new SimplePoint(points[1]));
    }

    public int getPointsTotal(int level, MatchSetup.Team team) {
        return score.getPointsTotal(level, team);
    }

    public Point getGames(MatchSetup.Team team, int setIndex) {
        return new SimplePoint(score.getGames(team, setIndex));
    }

    public int getPlayedSets() {
        return score.getPlayedSets();
    }

    public boolean isSetTieBreak(int setIndex) {
        return score.isSetTieBreak(setIndex);
    }

    public int getBreakPoints(MatchSetup.Team team) {
        return score.getBreakPoints(team);
    }

    public int getBreakPointsConverted(MatchSetup.Team team) {
        return score.getBreakPointsConverted(team);
    }

    public boolean isInTieBreak() {
        return score.isInTieBreak();
    }
}
