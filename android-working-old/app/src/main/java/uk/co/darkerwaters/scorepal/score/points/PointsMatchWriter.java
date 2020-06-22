package uk.co.darkerwaters.scorepal.score.points;

import android.content.Context;

import java.text.DateFormat;
import java.util.Date;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.players.Team;
import uk.co.darkerwaters.scorepal.score.Match;
import uk.co.darkerwaters.scorepal.score.MatchWriter;
import uk.co.darkerwaters.scorepal.score.Score;

public class PointsMatchWriter extends MatchWriter {

    @Override
    protected String getMatchSummary(Match match, Context context) {
        // build the summary, team one then team two
        Score score = match.getScore();
        StringBuilder builder = new StringBuilder();
        // put in the sets
        builder.append(context.getString(R.string.points));
        builder.append(": ");
        builder.append(score.getPoint(0, match.getTeamOne()));
        builder.append(" - ");
        builder.append(score.getPoint(0, match.getTeamTwo()));

        // return the score string
        return builder.toString();
    }

    @Override
    protected String getDescriptionBrief(Match match, Context context) {
        // return a nice brief description
        int scoreGoal = match.getScoreGoal();
        if (scoreGoal < 0) {
            // this is a point to infinity
            return context.getString(R.string.points_short_infinite_description);
        }
        else {
            // format the string with the points we are playing to
            return String.format(context.getString(R.string.points_short_description), match.getScoreGoal());
        }
    }

    @Override
    protected String getDescriptionShort(Match match, Context context) {
        // return a nice description
        int minutesPlayed = match.getMatchMinutesPlayed();
        int hoursPlayed = (int)(minutesPlayed / 60f);
        minutesPlayed = minutesPlayed - (hoursPlayed * 60);
        Date matchPlayedDate = match.getMatchPlayedDate();
        return String.format(context.getString(R.string.points_description)
                // line 1 - team1 beat team2
                , match.getMatchWinner().getTeamName()
                , match.isMatchOver() ? context.getString(R.string.match_beat) : context.getString(R.string.match_beating)
                , match.getOtherTeam(match.getMatchWinner()).getTeamName()
                // line 2 - 5 Match to 21 Points
                , match.getScoreGoal()
                // line 3 - lasting 2:15 minutes
                , String.format("%d", hoursPlayed)
                , String.format("%02d", minutesPlayed)
                // line 4 - played at 10:15 on 1 June 2016
                , matchPlayedDate == null ? "" : DateFormat.getTimeInstance(DateFormat.SHORT).format(matchPlayedDate)
                , matchPlayedDate == null ? "" : DateFormat.getDateInstance(DateFormat.LONG).format(matchPlayedDate));
    }

    @Override
    protected String getDescriptionLong(Match match, Context context) {
        // get the basic description
        StringBuilder stringBuilder = new StringBuilder(getDescriptionShort(match, context));
        // and we want to add a breakdown of the score here
        stringBuilder.append("\n\n");
        stringBuilder.append(context.getString(R.string.results));
        stringBuilder.append(": ");

        Team winner = match.getMatchWinner();
        Team loser = match.getOtherTeam(winner);
        Score score = match.getScore();

        stringBuilder.append(score.getPoint(0, winner));
        stringBuilder.append("-");
        stringBuilder.append(score.getPoint(0, loser));

        // and return the string
        return stringBuilder.toString();
    }
}
