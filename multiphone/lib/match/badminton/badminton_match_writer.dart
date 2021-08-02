import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/badminton/badminton_match.dart';
import 'package:multiphone/match/badminton/badminton_match_setup.dart';
import 'package:multiphone/match/badminton/badminton_score.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/match/match_writer.dart';

class BadmintonMatchWriter extends MatchWriter<BadmintonMatch> {
  @override
  String getMatchSummary(BadmintonMatch match, BuildContext context) {
    // build the summary, team one then team two
    String builder = '';

    // put in the games
    builder += Values(context).strings.games;
    builder += ": ";
    builder +=
        match.getPoint(BadmintonScore.LEVEL_GAME, TeamIndex.T_ONE).toString();
    builder += " - ";
    builder +=
        match.getPoint(BadmintonScore.LEVEL_GAME, TeamIndex.T_TWO).toString();

    // return the score string
    return builder.toString();
  }

  @override
  String getLevelTitle(int level, BuildContext context) {
    switch (level) {
      case BadmintonScore.LEVEL_POINT:
        return Values(context).strings.points;
      case BadmintonScore.LEVEL_GAME:
        return Values(context).strings.games;
    }
    return super.getLevelTitle(level, context);
  }

  @override
  String getDescriptionBrief(BadmintonMatch match, BuildContext context) {
    // return a nice brief description
    return Values(context).construct(
        Values(context).strings.badminton_short_description,
        [BadmintonMatchSetup.gamesValue(match.getSetup().games)]);
  }

  @override
  String getDescriptionShort(BadmintonMatch match, BuildContext context) {
    BadmintonMatchSetup setup = match.getSetup();
    // return a nice description
    int minutesPlayed = (match.getMatchTimePlayedMs() / 60000.0).floor();
    int hoursPlayed = (minutesPlayed / 60.0).floor();
    minutesPlayed = minutesPlayed - (hoursPlayed * 60);
    DateTime matchPlayedDate = match.getDateMatchStarted();
    return Values(context)
        .construct(Values(context).strings.badminton_description, [
      // line 2 - 5 Game Badminton Match
      BadmintonMatchSetup.gamesValue(setup.games),
      // line 3 - lasting 2:15 time
      hoursPlayed.toString(),
      minutesFormat.format(minutesPlayed),
      // line 4 - played at 10:15 on 1 June 2016
      matchPlayedDate == null ? "" : timeFormat.format(matchPlayedDate),
      matchPlayedDate == null ? "" : dateFormat.format(matchPlayedDate),
    ]);
  }

  @override
  String getDescriptionLong(BadmintonMatch match, BuildContext context) {
    BadmintonMatchSetup setup = match.getSetup();
    // get the basic description
    int minutesPlayed = (match.getMatchTimePlayedMs() / 60000.0).floor();
    int hoursPlayed = (minutesPlayed / 60.0).floor();
    minutesPlayed = minutesPlayed - (hoursPlayed * 60);
    DateTime matchPlayedDate = match.getDateMatchStarted();
    String longDescription = Values(context)
        .construct(Values(context).strings.badminton_description_long, [
      // line 1 - team1 beat team2
      setup.getTeamName(match.getMatchWinner(), context),
      match.isMatchOver()
          ? Values(context).strings.match_beat
          : Values(context).strings.match_beating,
      setup.getTeamName(setup.getOtherTeam(match.getMatchWinner()), context),
      // line 2 - 5 Game Badminton Match
      BadmintonMatchSetup.gamesValue(setup.games),
      // line 3 - lasting 2:15 time
      hoursPlayed.toString(),
      minutesFormat.format(minutesPlayed),
      // line 4 - played at 10:15 on 1 June 2016
      matchPlayedDate == null ? "" : timeFormat.format(matchPlayedDate),
      matchPlayedDate == null ? "" : dateFormat.format(matchPlayedDate),
    ]);
    String stringBuilder = longDescription;
    // and we want to add a breakdown of the score here
    stringBuilder += "\n\n";
    stringBuilder += Values(context).strings.results;
    stringBuilder += ": ";

    TeamIndex winner = match.getMatchWinner();
    TeamIndex loser = setup.getOtherTeam(winner);

    stringBuilder += "[";
    stringBuilder +=
        match.getPoint(BadmintonScore.LEVEL_GAME, winner).toString();
    stringBuilder += "] ";
    stringBuilder +=
        match.getPoint(BadmintonScore.LEVEL_POINT, winner).toString();
    stringBuilder += " - ";
    stringBuilder += "[";
    stringBuilder +=
        match.getPoint(BadmintonScore.LEVEL_GAME, loser).toString();
    stringBuilder += "] ";
    stringBuilder +=
        match.getPoint(BadmintonScore.LEVEL_POINT, loser).toString();

    // and return the string
    return stringBuilder.toString();
  }
}
