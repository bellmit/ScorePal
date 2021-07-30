import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/match/point.dart';
import 'package:multiphone/match/score_state.dart';

enum DescriptionLevel {
  SUMMARY,
  BRIEF,
  SHORT,
  SCORE,
  LONG,
  ONELINETOP,
  ONELINEBOTTOM,
  TWOLINE
}

class MatchWriter<T extends ActiveMatch> {
  final NumberFormat minutesFormat = NumberFormat("00");
  final DateFormat timeFormat = DateFormat('hh:mm');
  final DateFormat dateFormat = DateFormat('dd MMM yyyy');

  String getDescription(T match, DescriptionLevel level, BuildContext context) {
    // return the correct one
    switch (level) {
      case DescriptionLevel.SUMMARY:
        return getMatchSummary(match, context);
      case DescriptionLevel.BRIEF:
        return getDescriptionBrief(match, context);
      case DescriptionLevel.SHORT:
        return getDescriptionShort(match, context);
      case DescriptionLevel.SCORE:
        return getScoreString(match, context);
      case DescriptionLevel.LONG:
        return getDescriptionLong(match, context);
      case DescriptionLevel.ONELINETOP:
        return getScoreStringOneLineTop(match, context);
      case DescriptionLevel.ONELINEBOTTOM:
        return getScoreStringOneLineBottom(match, context);
      case DescriptionLevel.TWOLINE:
        return getScoreStringTwoLine(match, context);
      default:
        return getScoreStringOneLineBottom(match, context);
    }
  }

  String getMatchSummary(T match, BuildContext context) {
    //TODO show the time we play etc then add the score summary
    String builder = '';
    builder += match.getPoint(0, TeamIndex.T_ONE).toString();
    builder += " - ";
    builder += match.getPoint(0, TeamIndex.T_TWO).toString();
    return builder.toString();
  }

  String getDescriptionBrief(T match, BuildContext context) {
    return this.getDescriptionShort(match, context);
  }

  String getDescriptionShort(T match, BuildContext context) {
    ActiveSetup setup = match == null ? null : match.getSetup();
    if (null == setup || null == match || null == context) {
      return "";
    }
    // return a nice description
    int minutesPlayed = (match.getMatchTimePlayed() / 60.0).floor();
    int hoursPlayed = (minutesPlayed / 60.0).floor();
    minutesPlayed = minutesPlayed - (hoursPlayed * 60);
    DateTime matchPlayedDate = match.getDateMatchStarted();
    return Values(context)
        .construct(Values(context).strings.match_description, [
      // line 2 - lasting 2:15 time
      hoursPlayed.toString(),
      minutesFormat.format(minutesPlayed),
      // line 3 - played at 10:15 on 1 June 2016
      timeFormat.format(matchPlayedDate),
      dateFormat.format(matchPlayedDate),
    ]);
  }

  String getDescriptionLong(T match, BuildContext context) {
    // return a nice description
    ActiveSetup setup = match == null ? null : match.getSetup();
    if (null == setup || null == match || null == context) {
      return "";
    }
    // return a nice description
    int minutesPlayed = (match.getMatchTimePlayed() / 60.0).floor();
    int hoursPlayed = (minutesPlayed / 60.0).floor();
    minutesPlayed = minutesPlayed - (hoursPlayed * 60);
    DateTime matchPlayedDate = match.getDateMatchStarted();
    return Values(context).construct(
      Values(context).strings.match_description_long,
      [
        // line 1 - team1 beat team2
        setup.getTeamName(match.getMatchWinner(), context),
        match.isMatchOver()
            ? Values(context).strings.match_beat
            : Values(context).strings.match_beating,
        setup.getTeamName(setup.getOtherTeam(match.getMatchWinner()), context),
        // line 2 - lasting 2:15 time
        hoursPlayed.toString(),
        minutesFormat.format(minutesPlayed),
        // line 3 - played at 10:15 on 1 June 2016
        timeFormat.format(matchPlayedDate),
        dateFormat.format(matchPlayedDate),
      ],
    );
  }

  String getScoreString(T match, BuildContext context) {
    ActiveSetup setup = match.getSetup();
    // get the lowest score currently being played
    Point teamOnePoint, teamTwoPoint;
    int level = 0;
    for (int i = 0; i < match.getScoreLevels(); ++i) {
      teamOnePoint = match.getDisplayPoint(i, TeamIndex.T_ONE);
      teamTwoPoint = match.getDisplayPoint(i, TeamIndex.T_TWO);
      if (null != teamOnePoint &&
          null != teamTwoPoint &&
          (teamOnePoint.val() > 0 || teamTwoPoint.val() > 0)) {
        // we have two display points and one of them isn't zero, don't go lower
        level = i;
        break;
      }
    }
    // to return a string that is helpful - and can be formatted nicely later,
    // just create a CSV of the data
    return createCSVString([
      setup.getTeamName(TeamIndex.T_ONE, context),
      setup.getTeamName(TeamIndex.T_TWO, context),
      getLevelTitle(level, context),
      null == teamOnePoint ? "" : teamOnePoint.displayString(context),
      null == teamTwoPoint ? "" : teamTwoPoint.displayString(context),
    ]);
  }

  String createCSVString(List<String> parts) {
    String builder = '';
    for (int i = 0; i < parts.length; ++i) {
      builder += parts[i].replaceAll(",", "") + ",";
    }
    return builder.toString();
  }

  String getLevelTitle(int level, BuildContext context) {
    if (null == context) {
      return "Level ${level + 1}";
    } else {
      return Values(context)
          .construct(Values(context).strings.scoreSummaryLevel, [level]);
    }
  }

  String getScoreStringOneLineTop(T match, BuildContext context) {
    if (null == context) {
      return "";
    }
    // get the highest score achieved
    Point teamOnePoint, teamTwoPoint;
    for (int i = match.getScoreLevels() - 1; i >= 0; --i) {
      teamOnePoint = match.getDisplayPoint(i, TeamIndex.T_ONE);
      teamTwoPoint = match.getDisplayPoint(i, TeamIndex.T_TWO);
      if (null != teamOnePoint &&
          null != teamTwoPoint &&
          (teamOnePoint.val() > 0 || teamTwoPoint.val() > 0)) {
        // we have two display points and one of them isn't zero, don't go lower
        break;
      }
    }
    ActiveSetup setup = match.getSetup();
    return Values(context)
        .construct(Values(context).strings.scoreSummaryOneLine, [
      setup.getTeamName(TeamIndex.T_ONE, context),
      setup.getTeamName(TeamIndex.T_TWO, context),
      null == teamOnePoint ? "" : teamOnePoint.displayString(context),
      null == teamTwoPoint ? "" : teamTwoPoint.displayString(context),
    ]);
  }

  String getScoreStringOneLineBottom(T match, BuildContext context) {
    if (null == context) {
      return "";
    }
    ActiveSetup setup = match.getSetup();
    // get the lowest score currently being played
    Point teamOnePoint, teamTwoPoint;
    for (int i = 0; i < match.getScoreLevels(); ++i) {
      teamOnePoint = match.getDisplayPoint(i, TeamIndex.T_ONE);
      teamTwoPoint = match.getDisplayPoint(i, TeamIndex.T_TWO);
      if (null != teamOnePoint &&
          null != teamTwoPoint &&
          (teamOnePoint.val() > 0 || teamTwoPoint.val() > 0)) {
        // we have two display points and one of them isn't zero, don't go lower
        break;
      }
    }
    return Values(context).construct(
      Values(context).strings.scoreSummaryOneLine,
      [
        setup.getTeamName(TeamIndex.T_ONE, context),
        setup.getTeamName(TeamIndex.T_TWO, context),
        null == teamOnePoint ? "" : teamOnePoint.displayString(context),
        null == teamTwoPoint ? "" : teamTwoPoint.displayString(context),
      ],
    );
  }

  String getScoreStringTwoLine(T match, BuildContext context) {
    if (null == context) {
      return "";
    }
    String lineOne = '';
    String lineTwo = '';
    for (int i = match.getScoreLevels() - 1; i >= 0; --i) {
      if (i != 0) {
        // this is not the last, wrap in [ ]
        lineOne += "[";
        lineOne +=
            match.getDisplayPoint(i, TeamIndex.T_ONE).displayString(context);
        lineOne += "] ";
        lineTwo += "[";
        lineTwo +=
            match.getDisplayPoint(i, TeamIndex.T_TWO).displayString(context);
        lineTwo += "] ";
      } else {
        // last one
        lineOne +=
            match.getDisplayPoint(i, TeamIndex.T_ONE).displayString(context);
        lineOne += "   ";
        lineTwo +=
            match.getDisplayPoint(i, TeamIndex.T_TWO).displayString(context);
        lineTwo += "   ";
      }
    }
    // and the team names
    ActiveSetup setup = match.getSetup();
    String teamName = setup.getTeamName(TeamIndex.T_ONE, context);
    if (teamName.length > 20) {
      teamName = teamName.substring(0, 19) + "...";
    }
    lineOne += teamName;
    teamName = setup.getTeamName(TeamIndex.T_TWO, context);
    if (teamName.length > 20) {
      teamName = teamName.substring(0, 19) + "...";
    }
    lineTwo += teamName;
    return lineOne.toString() + "\n" + lineTwo.toString();
  }

  String getStateDescription(BuildContext context, int state) {
    String response = "";
    // just take the most important and return it
    if (null != context) {
      if (ScoreState.changed(state, ScoreChange.server)) {
        response = Values(context).strings.change_server;
      }
      if (ScoreState.changed(state, ScoreChange.ends)) {
        response = Values(context).strings.change_ends;
      }
      if (ScoreState.changed(state, ScoreChange.decidingPoint)) {
        response = Values(context).strings.deciding_point;
      }
      if (ScoreState.changed(state, ScoreChange.tieBreak)) {
        response = Values(context).strings.tie_break;
      }
      if (ScoreState.changed(state, ScoreChange.breakPoint)) {
        response = Values(context).strings.break_point;
      }
    }
    return response;
  }
}
