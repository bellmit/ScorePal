package uk.co.darkerwaters.scorepal.score.pingpong;

import android.content.Context;

import java.text.DateFormat;
import java.util.Date;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.players.Team;
import uk.co.darkerwaters.scorepal.score.MatchWriter;

public class PingPongMatchWriter extends MatchWriter<PingPongMatch> {

    @Override
    protected String getMatchSummary(PingPongMatch match, Context context) {
        // build the summary, team one then team two
        StringBuilder builder = new StringBuilder();
        PingPongScore score = match.getScore();

        // put in the rounds
        builder.append(context.getString(R.string.rounds));
        builder.append(": ");
        builder.append(score.getRounds(match.getTeamOne()));
        builder.append(" - ");
        builder.append(score.getRounds(match.getTeamTwo()));

        // return the score string
        return builder.toString();
    }

    @Override
    protected String getDescriptionBrief(PingPongMatch match, Context context) {
        // return a nice brief description
        return String.format(context.getString(R.string.ping_pong_short_description), match.getScoreGoal());
    }

    @Override
    protected String getDescriptionShort(PingPongMatch match, Context context) {
        // return a nice description
        int minutesPlayed = match.getMatchMinutesPlayed();
        int hoursPlayed = (int)(minutesPlayed / 60f);
        minutesPlayed = minutesPlayed - (hoursPlayed * 60);
        Date matchPlayedDate = match.getMatchPlayedDate();
        return String.format(context.getString(R.string.ping_pong_description)
                // line 1 - team1 beat team2
                , match.getMatchWinner().getTeamName()
                , match.isMatchOver() ? context.getString(R.string.match_beat) : context.getString(R.string.match_beating)
                , match.getOtherTeam(match.getMatchWinner()).getTeamName()
                // line 2 - 5 Round PingPong Match
                , match.getScoreGoal()
                // line 3 - lasting 2:15 minutes
                , String.format("%d", hoursPlayed)
                , String.format("%02d", minutesPlayed)
                // line 4 - played at 10:15 on 1 June 2016
                , matchPlayedDate == null ? "" : DateFormat.getTimeInstance(DateFormat.SHORT).format(matchPlayedDate)
                , matchPlayedDate == null ? "" : DateFormat.getDateInstance(DateFormat.LONG).format(matchPlayedDate));
    }

    @Override
    protected String getDescriptionLong(PingPongMatch match, Context context) {
        // get the basic description
        StringBuilder stringBuilder = new StringBuilder(getDescriptionShort(match, context));
        // and we want to add a breakdown of the score here
        stringBuilder.append("\n\n");
        stringBuilder.append(context.getString(R.string.results));
        stringBuilder.append(": ");

        Team winner = match.getMatchWinner();
        Team loser = match.getOtherTeam(winner);
        PingPongScore score = match.getScore();

        stringBuilder.append("[");
        stringBuilder.append(score.getRounds(winner));
        stringBuilder.append("] ");
        stringBuilder.append(score.getPoints(winner));
        stringBuilder.append(" - ");
        stringBuilder.append("[");
        stringBuilder.append(score.getRounds(loser));
        stringBuilder.append("] ");
        stringBuilder.append(score.getPoints(loser));

        // and return the string
        return stringBuilder.toString();
    }
}
