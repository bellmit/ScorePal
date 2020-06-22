package uk.co.darkerwaters.scorepal.score.tennis;

import android.content.Context;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.players.Team;
import uk.co.darkerwaters.scorepal.score.MatchWriter;

public class TennisMatchWriter extends MatchWriter<TennisMatch> {

    @Override
    protected String getMatchSummary(TennisMatch match, Context context) {
        // build the summary, team one then team two
        TennisScore score = match.getScore();
        StringBuilder builder = new StringBuilder();
        // put in the sets
        builder.append(context.getString(R.string.sets));
        builder.append(": ");
        builder.append(score.getSets(match.getTeamOne()));
        builder.append(" - ");
        builder.append(score.getSets(match.getTeamTwo()));

        //gap
        builder.append("   ");

        // put in the games
        builder.append(context.getString(R.string.games));
        builder.append(": ");
        builder.append(score.getGames(match.getTeamOne(), -1));
        builder.append(" - ");
        builder.append(score.getGames(match.getTeamTwo(), -1));

        // return the score string
        return builder.toString().trim();
    }

    @Override
    protected String getDescriptionBrief(TennisMatch match, Context context) {
        // return a nice brief description
        return String.format(context.getString(R.string.tennis_short_description), match.getScoreGoal());
    }

    @Override
    protected String getDescriptionShort(TennisMatch match, Context context) {
        // return a nice description
        int minutesPlayed = match.getMatchMinutesPlayed();
        int hoursPlayed = (int)(minutesPlayed / 60f);
        minutesPlayed = minutesPlayed - (hoursPlayed * 60);
        Date matchPlayedDate = match.getMatchPlayedDate();
        return String.format(context.getString(R.string.tennis_description)
                // line 1 - team1 beat team2
                , match.getMatchWinner().getTeamName()
                , match.isMatchOver() ? context.getString(R.string.match_beat) : context.getString(R.string.match_beating)
                , match.getOtherTeam(match.getMatchWinner()).getTeamName()
                // line 2 - 5 Set Tennis Match
                , match.getScoreGoal()
                // line 3 - lasting 2:15 minutes
                , String.format(Locale.getDefault(), "%d", hoursPlayed)
                , String.format(Locale.getDefault(), "%02d", minutesPlayed)
                // line 4 - played at 10:15 on 1 June 2016
                , matchPlayedDate == null ? "" : DateFormat.getTimeInstance(DateFormat.SHORT).format(matchPlayedDate)
                , matchPlayedDate == null ? "" : DateFormat.getDateInstance(DateFormat.LONG).format(matchPlayedDate));
    }

    @Override
    protected String getDescriptionLong(TennisMatch match, Context context) {
        // get the basic description
        StringBuilder stringBuilder = new StringBuilder(getDescriptionShort(match, context));
        // and we want to add a breakdown of the score here
        stringBuilder.append("\n\n");
        stringBuilder.append(context.getString(R.string.results));
        stringBuilder.append(": ");

        Team winner = match.getMatchWinner();
        Team loser = match.getOtherTeam(winner);
        TennisScore score = match.getScore();
        int totalSets = score.getPlayedSets();
        // go through the played sets - adding the games (+1 to show current games if there are any)
        for (int i = 0; i < totalSets + 1; ++i) {
            int winnerGames = score.getGames(winner, i);
            int loserGames = score.getGames(loser, i);
            if (winnerGames + loserGames > 0) {
                stringBuilder.append(winnerGames);
                stringBuilder.append("-");
                stringBuilder.append(loserGames);
                if (score.isSetTieBreak(i)) {
                    // we are in a tie break, show this data
                    int[] tiePoints = score.getPoints(i, winnerGames + loserGames - 1);
                    if (null != tiePoints && tiePoints.length > 1) {
                        // there are tie points - might not be if the tie isn't finished...
                        stringBuilder.append(" ");
                        stringBuilder.append(context.getString(R.string.tie_display, tiePoints[0], tiePoints[1]));
                    }
                }
                stringBuilder.append("   ");
            }
        }
        // and return the string
        return stringBuilder.toString();
    }
}
