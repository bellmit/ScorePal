package uk.co.darkerwaters.scorepal.score.badminton;

import android.content.Context;

import java.text.DateFormat;
import java.util.Date;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.players.Team;
import uk.co.darkerwaters.scorepal.score.MatchWriter;

public class BadmintonMatchWriter extends MatchWriter<BadmintonMatch> {

    @Override
    protected String getMatchSummary(BadmintonMatch match, Context context) {
        // build the summary, team one then team two
        StringBuilder builder = new StringBuilder();
        BadmintonScore score = match.getScore();

        // put in the games
        builder.append(context.getString(R.string.games));
        builder.append(": ");
        builder.append(score.getGames(match.getTeamOne()));
        builder.append(" - ");
        builder.append(score.getGames(match.getTeamTwo()));

        // return the score string
        return builder.toString();
    }

    @Override
    protected String getDescriptionBrief(BadmintonMatch match, Context context) {
        // return a nice brief description
        return String.format(context.getString(R.string.badminton_short_description), match.getScoreGoal());
    }

    @Override
    protected String getDescriptionShort(BadmintonMatch match, Context context) {
        // return a nice description
        int minutesPlayed = match.getMatchMinutesPlayed();
        int hoursPlayed = (int)(minutesPlayed / 60f);
        minutesPlayed = minutesPlayed - (hoursPlayed * 60);
        Date matchPlayedDate = match.getMatchPlayedDate();
        return String.format(context.getString(R.string.badminton_description)
                // line 1 - team1 beat team2
                , match.getMatchWinner().getTeamName()
                , match.isMatchOver() ? context.getString(R.string.match_beat) : context.getString(R.string.match_beating)
                , match.getOtherTeam(match.getMatchWinner()).getTeamName()
                // line 2 - 5 Game Badminton Match
                , match.getScoreGoal()
                // line 3 - lasting 2:15 minutes
                , String.format("%d", hoursPlayed)
                , String.format("%02d", minutesPlayed)
                // line 4 - played at 10:15 on 1 June 2016
                , matchPlayedDate == null ? "" : DateFormat.getTimeInstance(DateFormat.SHORT).format(matchPlayedDate)
                , matchPlayedDate == null ? "" : DateFormat.getDateInstance(DateFormat.LONG).format(matchPlayedDate));
    }

    @Override
    protected String getDescriptionLong(BadmintonMatch match, Context context) {
        // get the basic description
        StringBuilder stringBuilder = new StringBuilder(getDescriptionShort(match, context));
        // and we want to add a breakdown of the score here
        stringBuilder.append("\n\n");
        stringBuilder.append(context.getString(R.string.results));
        stringBuilder.append(": ");

        Team winner = match.getMatchWinner();
        Team loser = match.getOtherTeam(winner);
        BadmintonScore score = match.getScore();

        stringBuilder.append("[");
        stringBuilder.append(score.getGames(winner));
        stringBuilder.append("] ");
        stringBuilder.append(score.getPoints(winner));
        stringBuilder.append(" - ");
        stringBuilder.append("[");
        stringBuilder.append(score.getGames(loser));
        stringBuilder.append("] ");
        stringBuilder.append(score.getPoints(loser));

        // and return the string
        return stringBuilder.toString();
    }
}
