package uk.co.darkerwaters.scorepal.dataui;

import android.content.Context;

import java.text.DateFormat;
import java.util.Date;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.data.BadmintonMatch;
import uk.co.darkerwaters.scorepal.data.BadmintonScore;
import uk.co.darkerwaters.scorepal.data.BadmintonSetup;
import uk.co.darkerwaters.scorepal.data.MatchSetup;

public class BadmintonMatchWriter extends MatchWriter<BadmintonMatch> {

    @Override
    protected String getMatchSummary(BadmintonMatch match, Context context) {
        // build the summary, team one then team two
        StringBuilder builder = new StringBuilder();

        // put in the games
        builder.append(context.getString(R.string.games));
        builder.append(": ");
        builder.append(match.getPoint(BadmintonScore.LEVEL_GAME, MatchSetup.Team.T_ONE));
        builder.append(" - ");
        builder.append(match.getPoint(BadmintonScore.LEVEL_GAME, MatchSetup.Team.T_TWO));

        // return the score string
        return builder.toString();
    }

    @Override
    public String getLevelTitle(int level, Context context) {
        switch (level) {
            case BadmintonScore.LEVEL_POINT :
                return context.getString(R.string.points);
            case BadmintonScore.LEVEL_GAME :
                return context.getString(R.string.games);
        }
        return super.getLevelTitle(level, context);
    }

    @Override
    protected String getDescriptionBrief(BadmintonMatch match, Context context) {
        // return a nice brief description
        return String.format(context.getString(R.string.badminton_short_description), match.getSetup().getGamesInMatch().num);
    }

    @Override
    protected String getDescriptionShort(BadmintonMatch match, Context context) {
        BadmintonSetup setup = match.getSetup();
        // return a nice description
        int minutesPlayed = (int)(match.getMatchTimePlayed() / 60f);
        int hoursPlayed = (int)(minutesPlayed / 60f);
        minutesPlayed = minutesPlayed - (hoursPlayed * 60);
        Date matchPlayedDate = match.getDateMatchStarted();
        return String.format(context.getString(R.string.badminton_description)
                // line 2 - 5 Game Badminton Match
                , setup.getGamesInMatch().num
                // line 3 - lasting 2:15 time
                , String.format("%d", hoursPlayed)
                , String.format("%02d", minutesPlayed)
                // line 4 - played at 10:15 on 1 June 2016
                , matchPlayedDate == null ? "" : DateFormat.getTimeInstance(DateFormat.SHORT).format(matchPlayedDate)
                , matchPlayedDate == null ? "" : DateFormat.getDateInstance(DateFormat.LONG).format(matchPlayedDate));
    }

    @Override
    protected String getDescriptionLong(BadmintonMatch match, Context context) {
        BadmintonSetup setup = match.getSetup();
        // get the basic description
        int minutesPlayed = (int)(match.getMatchTimePlayed() / 60f);
        int hoursPlayed = (int)(minutesPlayed / 60f);
        minutesPlayed = minutesPlayed - (hoursPlayed * 60);
        Date matchPlayedDate = match.getDateMatchStarted();
        String longDescription = String.format(context.getString(R.string.badminton_description_long)
                // line 1 - team1 beat team2
                , setup.getTeamName(context, match.getMatchWinner())
                , match.isMatchOver() ? context.getString(R.string.match_beat) : context.getString(R.string.match_beating)
                , setup.getTeamName(context, setup.getOtherTeam(match.getMatchWinner()))
                // line 2 - 5 Game Badminton Match
                , setup.getGamesInMatch().num
                // line 3 - lasting 2:15 time
                , String.format("%d", hoursPlayed)
                , String.format("%02d", minutesPlayed)
                // line 4 - played at 10:15 on 1 June 2016
                , matchPlayedDate == null ? "" : DateFormat.getTimeInstance(DateFormat.SHORT).format(matchPlayedDate)
                , matchPlayedDate == null ? "" : DateFormat.getDateInstance(DateFormat.LONG).format(matchPlayedDate));
        StringBuilder stringBuilder = new StringBuilder(longDescription);
        // and we want to add a breakdown of the score here
        stringBuilder.append("\n\n");
        stringBuilder.append(context.getString(R.string.results));
        stringBuilder.append(": ");

        MatchSetup.Team winner = match.getMatchWinner();
        MatchSetup.Team loser = setup.getOtherTeam(winner);

        stringBuilder.append("[");
        stringBuilder.append(match.getPoint(BadmintonScore.LEVEL_GAME, winner));
        stringBuilder.append("] ");
        stringBuilder.append(match.getPoint(BadmintonScore.LEVEL_POINT, winner));
        stringBuilder.append(" - ");
        stringBuilder.append("[");
        stringBuilder.append(match.getPoint(BadmintonScore.LEVEL_GAME, loser));
        stringBuilder.append("] ");
        stringBuilder.append(match.getPoint(BadmintonScore.LEVEL_POINT, loser));

        // and return the string
        return stringBuilder.toString();
    }
}
