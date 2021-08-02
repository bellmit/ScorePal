import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/match/ping_pong/ping_pong_match.dart';
import 'package:multiphone/match/ping_pong/ping_pong_match_setup.dart';
import 'package:multiphone/match/ping_pong/ping_pong_score.dart';
import 'package:multiphone/match/match_writer.dart';

class PingPongMatchWriter extends MatchWriter<PingPongMatch> {
  @override
  String getMatchSummary(PingPongMatch match, BuildContext context) {
    // build the summary, team one then team two
    String builder = '';

    // put in the rounds
    builder += context == null ? "rounds" : Values(context).strings.rounds;
    builder += ": ";
    builder +=
        match.getPoint(PingPongScore.LEVEL_ROUND, TeamIndex.T_ONE).toString();
    builder += " - ";
    builder +=
        match.getPoint(PingPongScore.LEVEL_ROUND, TeamIndex.T_TWO).toString();

    // return the score string
    return builder.toString();
  }

  @override
  String getLevelTitle(int level, BuildContext context) {
    switch (level) {
      case PingPongScore.LEVEL_POINT:
        return BuildContext == null ? "points" : Values(context).strings.points;
      case PingPongScore.LEVEL_ROUND:
        return BuildContext == null ? "rounds" : Values(context).strings.rounds;
    }
    return super.getLevelTitle(level, context);
  }

  @override
  String getDescriptionBrief(PingPongMatch match, BuildContext context) {
    if (null == context) {
      return "";
    }
    // return a nice brief description
    return Values(context)
        .construct(Values(context).strings.ping_pong_short_description, [
      PingPongMatchSetup.roundsValue(match.getSetup().rounds),
    ]);
  }

  @override
  String getDescriptionShort(PingPongMatch match, BuildContext context) {
    if (null == context) {
      return "";
    }
    PingPongMatchSetup setup = match.getSetup();
    // return a nice description
    int minutesPlayed = (match.getMatchTimePlayedMs() / 60000.0).floor();
    int hoursPlayed = (minutesPlayed / 60.0).floor();
    minutesPlayed = minutesPlayed - (hoursPlayed * 60);
    DateTime matchPlayedDate = match.getDateMatchStarted();
    return Values(context)
        .construct(Values(context).strings.ping_pong_description, [
      // line 2 - 5 Round PingPong Match
      PingPongMatchSetup.roundsValue(setup.rounds),
      // line 3 - lasting 2:15 time
      hoursPlayed.toString(),
      minutesFormat.format(minutesPlayed),
      // line 4 - played at 10:15 on 1 June 2016
      matchPlayedDate == null ? "" : timeFormat.format(matchPlayedDate),
      matchPlayedDate == null ? "" : dateFormat.format(matchPlayedDate),
    ]);
  }

  @override
  String getDescriptionLong(PingPongMatch match, BuildContext context) {
    if (null == context) {
      return "";
    }
    PingPongMatchSetup setup = match.getSetup();
    // get the basic description
    int minutesPlayed = (match.getMatchTimePlayedMs() / 60000.0).floor();
    int hoursPlayed = (minutesPlayed / 60.0).floor();
    minutesPlayed = minutesPlayed - (hoursPlayed * 60);
    DateTime matchPlayedDate = match.getDateMatchStarted();
    String description = Values(context)
        .construct(Values(context).strings.ping_pong_description_long, [
      // line 1 - team1 beat team2
      setup.getTeamName(match.getMatchWinner(), context),
      match.isMatchOver()
          ? Values(context).strings.match_beat
          : Values(context).strings.match_beating,
      setup.getTeamName(setup.getOtherTeam(match.getMatchWinner()), context),
      // line 2 - 5 Round PingPong Match
      PingPongMatchSetup.roundsValue(setup.rounds),
      // line 3 - lasting 2:15 time
      hoursPlayed.toString(),
      minutesFormat.format(minutesPlayed),
      // line 4 - played at 10:15 on 1 June 2016
      matchPlayedDate == null ? "" : timeFormat.format(matchPlayedDate),
      matchPlayedDate == null ? "" : dateFormat.format(matchPlayedDate),
    ]);
    String stringBuilder = description;
    // and we want to add a breakdown of the score here
    stringBuilder += "\n\n";
    stringBuilder += Values(context).strings.results;
    stringBuilder += ": ";

    TeamIndex winner = match.getMatchWinner();
    TeamIndex loser = setup.getOtherTeam(winner);

    stringBuilder += "[";
    stringBuilder +=
        match.getPoint(PingPongScore.LEVEL_ROUND, winner).toString();
    stringBuilder += "] ";
    stringBuilder +=
        match.getPoint(PingPongScore.LEVEL_POINT, winner).toString();
    stringBuilder += " - ";
    stringBuilder += "[";
    stringBuilder +=
        match.getPoint(PingPongScore.LEVEL_ROUND, loser).toString();
    stringBuilder += "] ";
    stringBuilder +=
        match.getPoint(PingPongScore.LEVEL_POINT, loser).toString();

    // and return the string
    return stringBuilder.toString();
  }
}
