package uk.co.darkerwaters.scorepal.score;

import android.content.Context;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.players.Team;
import uk.co.darkerwaters.scorepal.score.base.Point;
import uk.co.darkerwaters.scorepal.score.base.PointChange;

public class MatchSpeaker<T extends Match> {

    public MatchSpeaker() {
    }

    public String createPointsPhrase(T match, Context context, PointChange topChange) {
        Team otherTeam = match.getOtherTeam(topChange.team);
        Score score = match.getScore();
        // formulate the message
        StringBuilder builder = new StringBuilder();
        builder.append(topChange.team.getSpeakingTeamName());
        builder.append(Point.K_SPEAKING_PAUSE_SLIGHT);
        builder.append(score.getPoint(topChange.level, topChange.team));
        builder.append(Point.K_SPEAKING_PAUSE_SLIGHT);
        builder.append(context.getString(R.string.speak_points));
        builder.append(Point.K_SPEAKING_PAUSE_SLIGHT);
        builder.append(otherTeam.getSpeakingTeamName());
        builder.append(Point.K_SPEAKING_PAUSE_SLIGHT);
        builder.append(score.getPoint(topChange.level, otherTeam));
        builder.append(Point.K_SPEAKING_PAUSE_SLIGHT);
        builder.append(context.getString(R.string.speak_points));
        // and return this phrase as a nice string
        return builder.toString();
    }

    public String createPointsAnnouncement(T match, Context context) {
        Score score = match.getScore();
        int topLevel = score.getLevels() - 1;
        Team team = match.getTeamServing();
        int points = match.getPointsTotal(topLevel, team);
        return createPointsPhrase(match, context, new PointChange(team, topLevel, points));
    }
}
