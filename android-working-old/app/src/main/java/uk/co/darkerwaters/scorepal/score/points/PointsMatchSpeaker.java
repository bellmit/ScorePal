package uk.co.darkerwaters.scorepal.score.points;

import android.content.Context;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.players.Team;
import uk.co.darkerwaters.scorepal.score.MatchSpeaker;
import uk.co.darkerwaters.scorepal.score.base.Point;
import uk.co.darkerwaters.scorepal.score.base.PointChange;

public class PointsMatchSpeaker extends MatchSpeaker<PointsMatch> {

    PointsMatchSpeaker() {
        super();
    }

    public String createPointsPhrase(PointsMatch match, Context context, PointChange topChange) {
        // get the data we need to build the string
        PointsScore score = match.getScore();
        Team teamOne = match.getTeamOne();
        Team teamTwo = match.getTeamTwo();
        String teamOneString = teamOne.getSpeakingTeamName();
        String teamTwoString = teamTwo.getSpeakingTeamName();
        Point t1Point = score.getDisplayPoint(teamOne);
        Point t2Point = score.getDisplayPoint(teamTwo);
        // build the string from here
        StringBuilder message = new StringBuilder();
        if (t1Point.val() > t2Point.val()) {
            // player one has more
            message.append(t1Point.speakString(context));
            message.append(Point.K_SPEAKING_SPACE);
            message.append(t2Point.speakString(context));
            message.append(Point.K_SPEAKING_SPACE);
            message.append(teamOneString);
        } else if (t2Point.val() > t1Point.val()){
            // player two has more
            message.append(t2Point.speakString(context));
            message.append(Point.K_SPEAKING_SPACE);
            message.append(t1Point.speakString(context));
            message.append(Point.K_SPEAKING_SPACE);
            message.append(teamTwoString);
        } else {
            // the points are the same
            message.append(t1Point.speakAllString(context));
        }
        // and add if we have won the match!
        if (match.isMatchOver()) {
            // inform the players that the match is over
            message.append(Point.K_SPEAKING_PAUSE_LONG);
            message.append(context.getString(R.string.match_completed));
        }
        // and return the message
        return message.toString();
    }
}
