import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/match/point.dart';
import 'package:multiphone/match/tennis/tennis_match.dart';
import 'package:multiphone/match/tennis/tennis_match_setup.dart';
import 'package:multiphone/match/tennis/tennis_score.dart';
import 'package:multiphone/match/match_writer.dart';

class TennisMatchWriter extends MatchWriter<TennisMatch> {
  @override
  String getMatchSummary(TennisMatch match, BuildContext context) {
    // build the summary, team one then team two
    String builder = '';
    // put in the sets
    builder += context == null ? "sets" : Values(context).strings.sets;
    builder += ": ";
    builder +=
        match.getPoint(TennisScore.LEVEL_SET, TeamIndex.T_ONE).toString();
    builder += " - ";
    builder +=
        match.getPoint(TennisScore.LEVEL_SET, TeamIndex.T_TWO).toString();

    //gap
    builder += "   ";

    // put in the games
    builder += context == null ? "games" : Values(context).strings.games;
    builder += ": ";
    builder += match.getGames(TeamIndex.T_ONE, -1).toString();
    builder += " - ";
    builder += match.getGames(TeamIndex.T_TWO, -1).toString();

    // return the score string
    return builder.toString().trim();
  }

  @override
  String getLevelTitle(int level, BuildContext context) {
    switch (level) {
      case TennisScore.LEVEL_POINT:
        return BuildContext == null ? "points" : Values(context).strings.points;
      case TennisScore.LEVEL_GAME:
        return BuildContext == null ? "games" : Values(context).strings.games;
      case TennisScore.LEVEL_SET:
        return BuildContext == null ? "sets" : Values(context).strings.sets;
    }
    return super.getLevelTitle(level, context);
  }

  @override
  String getDescriptionBrief(TennisMatch match, BuildContext context) {
    if (null == BuildContext || null == match) {
      return "";
    }
    // return a nice brief description
    return Values(context).construct(
        Values(context).strings.tennis_short_description,
        [TennisMatchSetup.setsValue(match.getSetup().sets)]);
  }

  @override
  String getDescriptionShort(TennisMatch match, BuildContext context) {
    TennisMatchSetup setup = match == null ? null : match.getSetup();
    if (null == BuildContext || null == setup) {
      return "";
    }
    // return a nice description
    int minutesPlayed = (match.getMatchTimePlayedMs() / 60000.0).floor();
    int hoursPlayed = (minutesPlayed / 60.0).floor();
    minutesPlayed = minutesPlayed - (hoursPlayed * 60);
    DateTime matchPlayedDate = match.getDateMatchStarted();
    return Values(context)
        .construct(Values(context).strings.tennis_description, [
      // line 2 - 5 Set Tennis Match
      TennisMatchSetup.setsValue(setup.sets),
      // line 3 - lasting 2:15 time
      hoursPlayed.toString(),
      minutesFormat.format(minutesPlayed),
      // line 4 - played at 10:15 on 1 June 2016
      matchPlayedDate == null ? "" : timeFormat.format(matchPlayedDate),
      matchPlayedDate == null ? "" : dateFormat.format(matchPlayedDate)
    ]);
  }

  @override
  String getDescriptionLong(TennisMatch match, BuildContext context) {
    TennisMatchSetup setup = match == null ? null : match.getSetup();
    if (null == BuildContext || null == setup) {
      return "";
    }
    // get the basic description
    int minutesPlayed = (match.getMatchTimePlayedMs() / 60000.0).floor();
    int hoursPlayed = (minutesPlayed / 60.0).floor();
    minutesPlayed = minutesPlayed - (hoursPlayed * 60);
    DateTime matchPlayedDate = match.getDateMatchStarted();
    String descriptionLong = Values(context)
        .construct(Values(context).strings.tennis_description_long, [
      // line 1 - team1 beat team2
      setup.getTeamName(match.getMatchWinner(), context),
      match.isMatchOver()
          ? Values(context).strings.match_beat
          : Values(context).strings.match_beating,
      setup.getTeamName(setup.getOtherTeam(match.getMatchWinner()), context),
      // line 2 - 5 Set Tennis Match
      TennisMatchSetup.setsValue(setup.sets),
      // line 3 - lasting 2:15 time
      hoursPlayed.toString(),
      minutesFormat.format(minutesPlayed),
      // line 4 - played at 10:15 on 1 June 2016
      matchPlayedDate == null ? "" : timeFormat.format(matchPlayedDate),
      matchPlayedDate == null ? "" : dateFormat.format(matchPlayedDate),
    ]);
    String builder = descriptionLong;
    // and we want to add a breakdown of the score here
    builder += "\n\n";
    builder += Values(context).strings.results;
    builder += ": ";

    TeamIndex winner = match.getMatchWinner();
    TeamIndex loser = setup.getOtherTeam(winner);
    int totalSets = match.getPlayedSets();
    // go through the played sets - adding the games (+1 to show current games if there are any)
    for (int i = 0; i < totalSets + 1; ++i) {
      int winnerGames = match.getGames(winner, i).val();
      int loserGames = match.getGames(loser, i).val();
      if (winnerGames + loserGames > 0) {
        builder += winnerGames.toString();
        builder += "-";
        builder += loserGames.toString();
        if (match.isSetTieBreak(i)) {
          // we are in a tie break, show this data
          PointPair tiePoints =
              match.getPoints(i, winnerGames + loserGames - 1);
          if (null != tiePoints) {
            // there are tie points - might not be if the tie isn't finished...
            builder += " ";
            builder += Values(context).construct(
                Values(context).strings.tie_display,
                [tiePoints.first.val(), tiePoints.second.val()]);
          }
        }
        builder += "   ";
      }
    }
    // and return the string
    return builder;
  }
}
