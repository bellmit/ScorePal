package uk.co.darkerwaters.scorepal.dataui;

import android.content.Context;

import java.text.DateFormat;
import java.util.Date;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.data.Match;
import uk.co.darkerwaters.scorepal.data.MatchSetup;
import uk.co.darkerwaters.scorepal.data.ScoreState;
import uk.co.darkerwaters.scorepal.points.Point;

public class MatchWriter<T extends Match> {

    public enum DescriptionLevel {
        SUMMARY,
        BRIEF,
        SHORT,
        SCORE,
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
            case SCORE:
                return getScoreString(match, context);
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
        //TODO show the time we play etc then add the score summary
        StringBuilder builder = new StringBuilder();
        builder.append(match.getPoint(0, MatchSetup.Team.T_ONE));
        builder.append(" - ");
        builder.append(match.getPoint(0, MatchSetup.Team.T_TWO));
        return builder.toString();
    }

    protected String getDescriptionBrief(T match, Context context) {
        return this.getDescriptionShort(match, context);
    }

    protected String getDescriptionShort(T match, Context context) {
        MatchSetup setup = match == null ? null : match.getSetup();
        if (null == setup || null == match || null == context) {
            return "";
        }
        // return a nice description
        int minutesPlayed = (int)(match.getMatchTimePlayed() / 60f);
        int hoursPlayed = (int)(minutesPlayed / 60f);
        minutesPlayed = minutesPlayed - (hoursPlayed * 60);
        Date matchPlayedDate = match.getDateMatchStarted();
        return String.format(context.getString(R.string.match_description)
                // line 2 - lasting 2:15 time
                , String.format("%d",hoursPlayed)
                , String.format("%02d",minutesPlayed)
                // line 3 - played at 10:15 on 1 June 2016
                , DateFormat.getTimeInstance(DateFormat.SHORT).format(matchPlayedDate)
                , DateFormat.getDateInstance(DateFormat.LONG).format(matchPlayedDate));
    }

    protected String getDescriptionLong(T match, Context context) {
        // return a nice description
        MatchSetup setup = match == null ? null : match.getSetup();
        if (null == setup || null == match || null == context) {
            return "";
        }
        // return a nice description
        int minutesPlayed = (int)(match.getMatchTimePlayed() / 60f);
        int hoursPlayed = (int)(minutesPlayed / 60f);
        minutesPlayed = minutesPlayed - (hoursPlayed * 60);
        Date matchPlayedDate = match.getDateMatchStarted();
        return String.format(context.getString(R.string.match_description_long)
                // line 1 - team1 beat team2
                , setup.getTeamName(context, match.getMatchWinner())
                , match.isMatchOver() ? context.getString(R.string.match_beat) : context.getString(R.string.match_beating)
                , setup.getTeamName(context, setup.getOtherTeam(match.getMatchWinner()))
                // line 2 - lasting 2:15 time
                , String.format("%d",hoursPlayed)
                , String.format("%02d",minutesPlayed)
                // line 3 - played at 10:15 on 1 June 2016
                , DateFormat.getTimeInstance(DateFormat.SHORT).format(matchPlayedDate)
                , DateFormat.getDateInstance(DateFormat.LONG).format(matchPlayedDate));
    }

    protected String getScoreString(T match, Context context) {
        MatchSetup setup = match.getSetup();
        // get the lowest score currently being played
        Point teamOnePoint = null, teamTwoPoint = null;
        int level = 0;
        for (int i = 0; i < match.getScoreLevels(); ++i) {
            teamOnePoint = match.getDisplayPoint(i, MatchSetup.Team.T_ONE);
            teamTwoPoint = match.getDisplayPoint(i, MatchSetup.Team.T_TWO);
            if (null != teamOnePoint && null != teamTwoPoint && (
                    teamOnePoint.val() > 0 || teamTwoPoint.val() > 0)) {
                // we have two display points and one of them isn't zero, don't go lower
                level = i;
                break;
            }
        }
        // to return a string that is helpful - and can be formatted nicely later,
        // just create a CSV of the data
        return createCSVString(new String[] {
                setup.getTeamName(context, MatchSetup.Team.T_ONE),
                setup.getTeamName(context, MatchSetup.Team.T_TWO),
                getLevelTitle(level, context),
                null == teamOnePoint ? "" : teamOnePoint.displayString(context),
                null == teamTwoPoint ? "" : teamTwoPoint.displayString(context),
        });
    }

    private String createCSVString(String[] parts) {
        StringBuilder builder = new StringBuilder();
        for (String part: parts) {
            builder.append(part.replaceAll(",", "") + ",");
        }
        return builder.toString();
    }

    public String getLevelTitle(int level, Context context) {
        if (null == context) {
            return "Level " + (level + 1);
        }
        else {
            return context.getString(R.string.scoreSummaryLevel, level);
        }
    }

    protected String getScoreStringOneLineTop(T match, Context context) {
        if (null == context) {
            return "";
        }
        // get the highest score achieved
        Point teamOnePoint = null, teamTwoPoint = null;
        for (int i = match.getScoreLevels() - 1; i >= 0; --i) {
            teamOnePoint = match.getDisplayPoint(i, MatchSetup.Team.T_ONE);
            teamTwoPoint = match.getDisplayPoint(i, MatchSetup.Team.T_TWO);
            if (null != teamOnePoint && null != teamTwoPoint && (
                teamOnePoint.val() > 0 || teamTwoPoint.val() > 0)) {
                // we have two display points and one of them isn't zero, don't go lower
                break;
            }
        }
        MatchSetup setup = match.getSetup();
        return context.getString(R.string.scoreSummaryOneLine,
                setup.getTeamName(context, MatchSetup.Team.T_ONE),
                setup.getTeamName(context, MatchSetup.Team.T_TWO),
                null == teamOnePoint ? "" : teamOnePoint.displayString(context),
                null == teamTwoPoint ? "" : teamTwoPoint.displayString(context));
    }

    protected String getScoreStringOneLineBottom(T match, Context context) {
        if (null == context) {
            return "";
        }
        MatchSetup setup = match.getSetup();
        // get the lowest score currently being played
        Point teamOnePoint = null, teamTwoPoint = null;
        for (int i = 0; i < match.getScoreLevels(); ++i) {
            teamOnePoint = match.getDisplayPoint(i, MatchSetup.Team.T_ONE);
            teamTwoPoint = match.getDisplayPoint(i, MatchSetup.Team.T_TWO);
            if (null != teamOnePoint && null != teamTwoPoint && (
                    teamOnePoint.val() > 0 || teamTwoPoint.val() > 0)) {
                // we have two display points and one of them isn't zero, don't go lower
                break;
            }
        }
        return context.getString(R.string.scoreSummaryOneLine,
                setup.getTeamName(context, MatchSetup.Team.T_ONE),
                setup.getTeamName(context, MatchSetup.Team.T_TWO),
                null == teamOnePoint ? "" : teamOnePoint.displayString(context),
                null == teamTwoPoint ? "" : teamTwoPoint.displayString(context));
    }

    protected String getScoreStringTwoLine(T match, Context context) {
        if (null == context) {
            return "";
        }
        StringBuilder lineOne = new StringBuilder();
        StringBuilder lineTwo = new StringBuilder();
        for (int i = match.getScoreLevels() - 1; i >= 0; --i) {
            if (i != 0) {
                // this is not the last, wrap in [ ]
                lineOne.append("[");
                lineOne.append(match.getDisplayPoint(i, MatchSetup.Team.T_ONE).displayString(context));
                lineOne.append("] ");
                lineTwo.append("[");
                lineTwo.append(match.getDisplayPoint(i, MatchSetup.Team.T_TWO).displayString(context));
                lineTwo.append("] ");
            }
            else {
                // last one
                lineOne.append(match.getDisplayPoint(i, MatchSetup.Team.T_ONE).displayString(context));
                lineOne.append("   ");
                lineTwo.append(match.getDisplayPoint(i, MatchSetup.Team.T_TWO).displayString(context));
                lineTwo.append("   ");
            }
        }
        // and the team names
        MatchSetup setup = match.getSetup();
        String teamName = setup.getTeamName(context, MatchSetup.Team.T_ONE);
        if (teamName.length() > 20) {
            teamName = teamName.substring(0, 19) + "...";
        }
        lineOne.append(teamName);
        teamName = setup.getTeamName(context, MatchSetup.Team.T_TWO);
        if (teamName.length() > 20) {
            teamName = teamName.substring(0, 19) + "...";
        }
        lineTwo.append(teamName);
        return lineOne.toString() + "\n" + lineTwo.toString();
    }

    public String getStateDescription(Context context, int state) {
        String response = "";
        // just take the most important and return it
        if (null != context) {
            if (ScoreState.Changed(state, ScoreState.ScoreChange.SERVER)) {
                response = context.getString(R.string.change_server);
            }
            if (ScoreState.Changed(state, ScoreState.ScoreChange.ENDS)) {
                response = context.getString(R.string.change_ends);
            }
            if (ScoreState.Changed(state, ScoreState.ScoreChange.DECIDING_POINT)) {
                response = context.getString(R.string.deciding_point);
            }
            if (ScoreState.Changed(state, ScoreState.ScoreChange.TIE_BREAK)) {
                response = context.getString(R.string.tie_break);
            }
            if (ScoreState.Changed(state, ScoreState.ScoreChange.BREAK_POINT)) {
                response = context.getString(R.string.break_point);
            }
        }
        return response;
    }
}
