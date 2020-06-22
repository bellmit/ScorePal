package uk.co.darkerwaters.scorepal.score;

import android.content.Context;

import java.text.DateFormat;
import java.util.Date;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.players.Team;
import uk.co.darkerwaters.scorepal.score.base.Point;

public class MatchWriter<T extends Match> {

    public enum DescriptionLevel {
        SUMMARY,
        BRIEF,
        SHORT,
        LONG,
        ONELINETOP,
        ONELINEBOTTOM,
        TWOLINE
    }

    public String getDescription(T match, DescriptionLevel level, Context context) {
        // return the correct one
        switch (level) {
            case SUMMARY:
                return getMatchSummary(match, context);
            case BRIEF:
                return getDescriptionBrief(match, context);
            case SHORT:
                return getDescriptionShort(match, context);
            case LONG:
                return getDescriptionLong(match, context);
            case ONELINETOP:
                return getScoreStringOneLineTop(match, context);
            case ONELINEBOTTOM:
                return getScoreStringOneLineBottom(match, context);
            case TWOLINE:
                return getScoreStringTwoLine(match, context);
            default:
                return getScoreStringOneLineBottom(match, context);
        }

    }

    protected String getMatchSummary(T match, Context context) {
        Score score = match.getScore();
        //TODO show the time we play etc then add the score summary
        StringBuilder builder = new StringBuilder();
        builder.append(score.getPoint(0, match.getTeamOne()));
        builder.append(" - ");
        builder.append(score.getPoint(0, match.getTeamTwo()));
        return builder.toString();
    }

    protected String getDescriptionBrief(T match, Context context) {
        return this.getDescriptionShort(match, context);
    }

    protected String getDescriptionShort(T match, Context context) {
        // return a nice description
        int minutesPlayed = match.getMatchMinutesPlayed();
        int hoursPlayed = (int)(minutesPlayed / 60f);
        minutesPlayed = minutesPlayed - (hoursPlayed * 60);
        Date matchPlayedDate = match.getMatchPlayedDate();
        return String.format(context.getString(R.string.match_description)
                // line 1 - team1 beat team2
                , match.getMatchWinner().getTeamName()
                , match.isMatchOver() ? context.getString(R.string.match_beat) : context.getString(R.string.match_beating)
                , match.getOtherTeam(match.getMatchWinner()).getTeamName()
                // line 2 - lasting 2:15 minutes
                , String.format("%d",hoursPlayed)
                , String.format("%02d",minutesPlayed)
                // line 3 - played at 10:15 on 1 June 2016
                , DateFormat.getTimeInstance(DateFormat.SHORT).format(matchPlayedDate)
                , DateFormat.getDateInstance(DateFormat.LONG).format(matchPlayedDate));
    }

    protected String getDescriptionLong(T match, Context context) {
        // return a nice description
        return getDescriptionShort(match, context);
    }

    protected String getScoreStringOneLineTop(T match, Context context) {
        Score score = match.getScore();
        Team teamOne = match.getTeamOne();
        Team teamTwo = match.getTeamTwo();
        // get the highest score achieved
        Point teamOnePoint = null, teamTwoPoint = null;
        for (int i = score.getLevels() - 1; i >= 0; --i) {
            teamOnePoint = score.getDisplayPoint(i, teamOne);
            teamTwoPoint = score.getDisplayPoint(i, teamTwo);
            if (null != teamOnePoint && null != teamTwoPoint && (
                teamOnePoint.val() > 0 || teamTwoPoint.val() > 0)) {
                // we have two display points and one of them isn't zero, don't go lower
                break;
            }
        }
        return context.getString(R.string.scoreSummaryOneLine,
                teamOne.getTeamName(),
                teamTwo.getTeamName(),
                null == teamOnePoint ? "" : teamOnePoint.displayString(context),
                null == teamTwoPoint ? "" : teamTwoPoint.displayString(context));
    }

    protected String getScoreStringOneLineBottom(T match, Context context) {
        Score score = match.getScore();
        Team teamOne = match.getTeamOne();
        Team teamTwo = match.getTeamTwo();
        // get the lowest score currently being played
        Point teamOnePoint = null, teamTwoPoint = null;
        for (int i = 0; i < score.getLevels(); ++i) {
            teamOnePoint = score.getDisplayPoint(i, teamOne);
            teamTwoPoint = score.getDisplayPoint(i, teamTwo);
            if (null != teamOnePoint && null != teamTwoPoint && (
                    teamOnePoint.val() > 0 || teamTwoPoint.val() > 0)) {
                // we have two display points and one of them isn't zero, don't go lower
                break;
            }
        }
        return context.getString(R.string.scoreSummaryOneLine,
                teamOne.getTeamName(),
                teamTwo.getTeamName(),
                null == teamOnePoint ? "" : teamOnePoint.displayString(context),
                null == teamTwoPoint ? "" : teamTwoPoint.displayString(context));
    }

    protected String getScoreStringTwoLine(T match, Context context) {
        Score score = match.getScore();
        int scoreLevels = score.getLevels();
        StringBuilder lineOne = new StringBuilder();
        StringBuilder lineTwo = new StringBuilder();
        Team teamOne = match.getTeamOne();
        Team teamTwo = match.getTeamTwo();
        for (int i = scoreLevels - 1; i >= 0; --i) {
            if (i != 0) {
                // this is not the last, wrap in [ ]
                lineOne.append("[");
                lineOne.append(score.getDisplayPoint(i, teamOne).displayString(context));
                lineOne.append("] ");
                lineTwo.append("[");
                lineTwo.append(score.getDisplayPoint(i, teamTwo).displayString(context));
                lineTwo.append("] ");
            }
            else {
                // last one
                lineOne.append(score.getDisplayPoint(i, teamOne).displayString(context));
                lineOne.append("   ");
                lineTwo.append(score.getDisplayPoint(i, teamTwo).displayString(context));
                lineTwo.append("   ");
            }
        }
        // and the team names
        String teamName = teamOne.getTeamName();
        if (teamName.length() > 20) {
            teamName = teamName.substring(0, 19) + "...";
        }
        lineOne.append(teamName);
        teamName = teamTwo.getTeamName();
        if (teamName.length() > 20) {
            teamName = teamName.substring(0, 19) + "...";
        }
        lineTwo.append(teamName);
        return lineOne.toString() + "\n" + lineTwo.toString();
    }
}
