import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/badminton/badminton_match.dart';
import 'package:multiphone/match/badminton/badminton_match_setup.dart';
import 'package:multiphone/match/badminton/badminton_score.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/match/badminton/badminton_point.dart';
import 'package:multiphone/match/point.dart';
import 'package:multiphone/match/match_speaker.dart';

class BadmintonMatchSpeaker extends MatchSpeaker<BadmintonMatch> {
  @override
  String createPointsPhrase(BadmintonMatch match, BuildContext context,
      TeamIndex changeTeam, int level) {
    BadmintonMatchSetup setup = match.getSetup();
    StringBuilder message = StringBuilder();
    String teamOneString = getSpeakingTeamName(context, setup, TeamIndex.T_ONE);
    String teamTwoString = getSpeakingTeamName(context, setup, TeamIndex.T_TWO);
    String changeTeamString = getSpeakingTeamName(context, setup, changeTeam);
    switch (level) {
      case BadmintonScore.LEVEL_POINT:
        // the points changed, announce the points
        Point t1Point = match.getDisplayPoint(0, TeamIndex.T_ONE);
        Point t2Point = match.getDisplayPoint(0, TeamIndex.T_TWO);
        // just read the numbers out, but we want to say the server first
        // so who is that?
        if (t1Point.val() == t2Point.val()) {
          // we are drawing
          append(message, t1Point.speakAllString(context));
        } else if (match.getServingTeam() == TeamIndex.T_ONE) {
          // team one is serving
          appendPause(
              message, t1Point.speakString(context), Point.K_SPEAKING_SPACE);
          append(message, t2Point.speakString(context));
        } else {
          // team two is serving
          appendPause(
              message, t2Point.speakString(context), Point.K_SPEAKING_SPACE);
          append(message, t1Point.speakString(context));
        }
        break;
      case BadmintonScore.LEVEL_GAME:
        // the games changed, announce who won the game
        appendPause(message, BadmintonPoint.game.speakString(context),
            Point.K_SPEAKING_PAUSE);
        // also match?
        if (match.isMatchOver()) {
          appendPause(message, BadmintonPoint.match.speakString(context),
              Point.K_SPEAKING_PAUSE);
        }
        append(message, changeTeamString);
        // also we want to say the games as they stand
        append(message, Point.K_SPEAKING_PAUSE_LONG);

        // team one first
        int games = match.getPoint(BadmintonScore.LEVEL_GAME, TeamIndex.T_ONE);
        append(message, teamOneString);
        appendInt(message, games);
        appendPause(
            message,
            BadmintonPoint.game.speakNumberString(context, games),
            Point.K_SPEAKING_PAUSE);

        // team two now
        games = match.getPoint(BadmintonScore.LEVEL_GAME, TeamIndex.T_TWO);
        append(message, teamTwoString);
        appendInt(message, games);
        appendPause(
            message,
            BadmintonPoint.game.speakNumberString(context, games),
            Point.K_SPEAKING_PAUSE);
        break;
    }
    // and return the complicated message to speak
    return message.toString();
  }

  @override
  String getLevelTitle(int level, BuildContext context) {
    switch (level) {
      case BadmintonScore.LEVEL_POINT:
        return BadmintonPoint.point.speakString(context);
      case BadmintonScore.LEVEL_GAME:
        return BadmintonPoint.game.speakString(context);
    }
    return super.getLevelTitle(level, context);
  }

  @override
  String createPointsAnnouncement(BadmintonMatch match, BuildContext context) {
    BadmintonMatchSetup setup = match.getSetup();

    TeamIndex teamServing = match.getServingTeam();
    TeamIndex teamReceiving = setup.getOtherTeam(teamServing);
    int serverPoints = match.getPoint(BadmintonScore.LEVEL_POINT, teamServing);
    int receiverPoints =
        match.getPoint(BadmintonScore.LEVEL_POINT, teamReceiving);

    String serverString = getSpeakingTeamName(context, setup, teamServing);
    String receiverString = getSpeakingTeamName(context, setup, teamReceiving);

    String message;
    if (serverPoints + receiverPoints > 0) {
      // there are points, say the points
      message = createPointsPhrase(
          match, context, teamServing, BadmintonScore.LEVEL_POINT);
    } else {
      // no points scored, are there any games?
      int serverGames = match.getPoint(BadmintonScore.LEVEL_GAME, teamServing);
      int receiverGames =
          match.getPoint(BadmintonScore.LEVEL_GAME, teamReceiving);
      StringBuilder messageBuilder = StringBuilder();
      if (serverGames + receiverGames > 0) {
        // announce the games
        appendPause(
            messageBuilder, serverString, Point.K_SPEAKING_PAUSE_SLIGHT);
        appendIntPause(
            messageBuilder, serverGames, Point.K_SPEAKING_PAUSE_SLIGHT);
        appendPause(
            messageBuilder,
            BadmintonPoint.game.speakNumberString(context, serverGames),
            Point.K_SPEAKING_PAUSE);
        appendPause(
            messageBuilder, receiverString, Point.K_SPEAKING_PAUSE_SLIGHT);
        appendIntPause(
            messageBuilder, receiverGames, Point.K_SPEAKING_PAUSE_SLIGHT);
        append(messageBuilder,
            BadmintonPoint.game.speakNumberString(context, receiverGames));
        append(messageBuilder, Values(context).strings.games);
      }
      message = messageBuilder.toString();
    }
    // return the message created
    return message;
  }
}
