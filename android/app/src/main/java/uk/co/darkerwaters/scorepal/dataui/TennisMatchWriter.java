package uk.co.darkerwaters.scorepal.dataui;

import android.content.Context;
import android.util.Pair;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.data.MatchSetup;
import uk.co.darkerwaters.scorepal.data.TennisMatch;
import uk.co.darkerwaters.scorepal.data.TennisScore;
import uk.co.darkerwaters.scorepal.data.TennisSetup;
import uk.co.darkerwaters.scorepal.points.Point;

public class TennisMatchWriter extends MatchWriter<TennisMatch> {

    @Override
    protected String getMatchSummary(TennisMatch match, Context context) {
        // build the summary, team one then team two
        StringBuilder builder = new StringBuilder();
        // put in the sets
        builder.append(context == null ? "sets" : context.getString(R.string.sets));
        builder.append(": ");
        builder.append(match.getPoint(TennisScore.LEVEL_SET, MatchSetup.Team.T_ONE));
        builder.append(" - ");
        builder.append(match.getPoint(TennisScore.LEVEL_SET, MatchSetup.Team.T_TWO));

        //gap
        builder.append("   ");

        // put in the games
        builder.append(context == null ? "games" : context.getString(R.string.games));
        builder.append(": ");
        builder.append(match.getGames(MatchSetup.Team.T_ONE, -1));
        builder.append(" - ");
        builder.append(match.getGames(MatchSetup.Team.T_TWO, -1));

        // return the score string
        return builder.toString().trim();
    }

    @Override
    public String getLevelTitle(int level, Context context) {
        switch (level) {
            case TennisScore.LEVEL_POINT :
                return context == null ? "points" : context.getString(R.string.points);
            case TennisScore.LEVEL_GAME :
                return context == null ? "games" : context.getString(R.string.games);
            case TennisScore.LEVEL_SET :
                return context == null ? "sets" : context.getString(R.string.sets);
        }
        return super.getLevelTitle(level, context);
    }

    @Override
    protected String getDescriptionBrief(TennisMatch match, Context context) {
        if (null == context || null == match) {
            return "";
        }
        // return a nice brief description
        return String.format(context.getString(R.string.tennis_short_description), match.getSetup().getNumberSets().num);
    }

    @Override
    protected String getDescriptionShort(TennisMatch match, Context context) {
        TennisSetup setup = match == null ? null : match.getSetup();
        if (null == context || null == setup) {
            return "";
        }
        // return a nice description
        int minutesPlayed = (int)(match.getMatchTimePlayed() / 60f);
        int hoursPlayed = (int)(minutesPlayed / 60f);
        minutesPlayed = minutesPlayed - (hoursPlayed * 60);
        Date matchPlayedDate = match.getDateMatchStarted();
        return String.format(context.getString(R.string.tennis_description)
                // line 2 - 5 Set Tennis Match
                , match.getSetup().getNumberSets().num
                // line 3 - lasting 2:15 time
                , String.format(Locale.getDefault(), "%d", hoursPlayed)
                , String.format(Locale.getDefault(), "%02d", minutesPlayed)
                // line 4 - played at 10:15 on 1 June 2016
                , matchPlayedDate == null ? "" : DateFormat.getTimeInstance(DateFormat.SHORT).format(matchPlayedDate)
                , matchPlayedDate == null ? "" : DateFormat.getDateInstance(DateFormat.LONG).format(matchPlayedDate));
    }

    @Override
    protected String getDescriptionLong(TennisMatch match, Context context) {
        TennisSetup setup = match == null ? null : match.getSetup();
        if (null == context || null == setup) {
            return "";
        }
        // get the basic description
        int minutesPlayed = (int)(match.getMatchTimePlayed() / 60f);
        int hoursPlayed = (int)(minutesPlayed / 60f);
        minutesPlayed = minutesPlayed - (hoursPlayed * 60);
        Date matchPlayedDate = match.getDateMatchStarted();
        String descriptionLong = String.format(context.getString(R.string.tennis_description_long)
                // line 1 - team1 beat team2
                , setup.getTeamName(context, match.getMatchWinner())
                , match.isMatchOver() ? context.getString(R.string.match_beat) : context.getString(R.string.match_beating)
                , setup.getTeamName(context, setup.getOtherTeam(match.getMatchWinner()))
                // line 2 - 5 Set Tennis Match
                , match.getSetup().getNumberSets().num
                // line 3 - lasting 2:15 time
                , String.format(Locale.getDefault(), "%d", hoursPlayed)
                , String.format(Locale.getDefault(), "%02d", minutesPlayed)
                // line 4 - played at 10:15 on 1 June 2016
                , matchPlayedDate == null ? "" : DateFormat.getTimeInstance(DateFormat.SHORT).format(matchPlayedDate)
                , matchPlayedDate == null ? "" : DateFormat.getDateInstance(DateFormat.LONG).format(matchPlayedDate));
        StringBuilder stringBuilder = new StringBuilder(descriptionLong);
        // and we want to add a breakdown of the score here
        stringBuilder.append("\n\n");
        stringBuilder.append(context.getString(R.string.results));
        stringBuilder.append(": ");

        MatchSetup.Team winner = match.getMatchWinner();
        MatchSetup.Team loser = setup.getOtherTeam(winner);
        int totalSets = match.getPlayedSets();
        // go through the played sets - adding the games (+1 to show current games if there are any)
        for (int i = 0; i < totalSets + 1; ++i) {
            int winnerGames = match.getGames(winner, i).val();
            int loserGames = match.getGames(loser, i).val();
            if (winnerGames + loserGames > 0) {
                stringBuilder.append(winnerGames);
                stringBuilder.append("-");
                stringBuilder.append(loserGames);
                if (match.isSetTieBreak(i)) {
                    // we are in a tie break, show this data
                    Pair<Point, Point> tiePoints = match.getPoints(i, winnerGames + loserGames - 1);
                    if (null != tiePoints) {
                        // there are tie points - might not be if the tie isn't finished...
                        stringBuilder.append(" ");
                        stringBuilder.append(context.getString(R.string.tie_display, tiePoints.first.val(), tiePoints.second.val()));
                    }
                }
                stringBuilder.append("   ");
            }
        }
        // and return the string
        return stringBuilder.toString();
    }
}
