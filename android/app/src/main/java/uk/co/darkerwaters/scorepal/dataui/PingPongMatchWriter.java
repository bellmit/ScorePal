package uk.co.darkerwaters.scorepal.dataui;

import android.content.Context;

import java.text.DateFormat;
import java.util.Date;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.data.MatchSetup;
import uk.co.darkerwaters.scorepal.data.PingPongMatch;
import uk.co.darkerwaters.scorepal.data.PingPongScore;
import uk.co.darkerwaters.scorepal.data.PingPongSetup;

public class PingPongMatchWriter extends MatchWriter<PingPongMatch> {

    @Override
    protected String getMatchSummary(PingPongMatch match, Context context) {
        // build the summary, team one then team two
        StringBuilder builder = new StringBuilder();

        // put in the rounds
        builder.append(context == null ? "rounds" : context.getString(R.string.rounds));
        builder.append(": ");
        builder.append(match.getPoint(PingPongScore.LEVEL_ROUND, MatchSetup.Team.T_ONE));
        builder.append(" - ");
        builder.append(match.getPoint(PingPongScore.LEVEL_ROUND, MatchSetup.Team.T_TWO));

        // return the score string
        return builder.toString();
    }

    @Override
    public String getLevelTitle(int level, Context context) {
        switch (level) {
            case PingPongScore.LEVEL_POINT :
                return context == null ? "points" : context.getString(R.string.points);
            case PingPongScore.LEVEL_ROUND :
                return context == null ? "rounds" : context.getString(R.string.rounds);
        }
        return super.getLevelTitle(level, context);
    }

    @Override
    protected String getDescriptionBrief(PingPongMatch match, Context context) {
        if (null == context) {
            return "";
        }
        // return a nice brief description
        return String.format(context.getString(R.string.ping_pong_short_description), match.getSetup().getRoundsInMatch().num);
    }

    @Override
    protected String getDescriptionShort(PingPongMatch match, Context context) {
        if (null == context) {
            return "";
        }
        PingPongSetup setup = match.getSetup();
        // return a nice description
        int minutesPlayed = (int)(match.getMatchTimePlayed() / 60f);
        int hoursPlayed = (int)(minutesPlayed / 60f);
        minutesPlayed = minutesPlayed - (hoursPlayed * 60);
        Date matchPlayedDate = match.getDateMatchStarted();
        return String.format(context.getString(R.string.ping_pong_description)
                // line 2 - 5 Round PingPong Match
                , setup.getRoundsInMatch().num
                // line 3 - lasting 2:15 time
                , String.format("%d", hoursPlayed)
                , String.format("%02d", minutesPlayed)
                // line 4 - played at 10:15 on 1 June 2016
                , matchPlayedDate == null ? "" : DateFormat.getTimeInstance(DateFormat.SHORT).format(matchPlayedDate)
                , matchPlayedDate == null ? "" : DateFormat.getDateInstance(DateFormat.LONG).format(matchPlayedDate));
    }

    @Override
    protected String getDescriptionLong(PingPongMatch match, Context context) {
        if (null == context) {
            return "";
        }
        PingPongSetup setup = match.getSetup();
        // get the basic description
        int minutesPlayed = (int)(match.getMatchTimePlayed() / 60f);
        int hoursPlayed = (int)(minutesPlayed / 60f);
        minutesPlayed = minutesPlayed - (hoursPlayed * 60);
        Date matchPlayedDate = match.getDateMatchStarted();
        String description = String.format(context.getString(R.string.ping_pong_description_long)
                // line 1 - team1 beat team2
                , setup.getTeamName(context, match.getMatchWinner())
                , match.isMatchOver() ? context.getString(R.string.match_beat) : context.getString(R.string.match_beating)
                , setup.getTeamName(context, setup.getOtherTeam(match.getMatchWinner()))
                // line 2 - 5 Round PingPong Match
                , setup.getRoundsInMatch().num
                // line 3 - lasting 2:15 time
                , String.format("%d", hoursPlayed)
                , String.format("%02d", minutesPlayed)
                // line 4 - played at 10:15 on 1 June 2016
                , matchPlayedDate == null ? "" : DateFormat.getTimeInstance(DateFormat.SHORT).format(matchPlayedDate)
                , matchPlayedDate == null ? "" : DateFormat.getDateInstance(DateFormat.LONG).format(matchPlayedDate));
        StringBuilder stringBuilder = new StringBuilder(description);
        // and we want to add a breakdown of the score here
        stringBuilder.append("\n\n");
        stringBuilder.append(context.getString(R.string.results));
        stringBuilder.append(": ");

        MatchSetup.Team winner = match.getMatchWinner();
        MatchSetup.Team loser = setup.getOtherTeam(winner);

        stringBuilder.append("[");
        stringBuilder.append(match.getPoint(PingPongScore.LEVEL_ROUND, winner));
        stringBuilder.append("] ");
        stringBuilder.append(match.getPoint(PingPongScore.LEVEL_POINT, winner));
        stringBuilder.append(" - ");
        stringBuilder.append("[");
        stringBuilder.append(match.getPoint(PingPongScore.LEVEL_ROUND, loser));
        stringBuilder.append("] ");
        stringBuilder.append(match.getPoint(PingPongScore.LEVEL_POINT, loser));

        // and return the string
        return stringBuilder.toString();
    }
}
