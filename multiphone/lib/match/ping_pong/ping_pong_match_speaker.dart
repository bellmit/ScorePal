import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/match/ping_pong/ping_pong_match.dart';
import 'package:multiphone/match/ping_pong/ping_pong_match_setup.dart';
import 'package:multiphone/match/ping_pong/ping_pong_score.dart';
import 'package:multiphone/match/ping_pong/ping_pong_point.dart';
import 'package:multiphone/match/point.dart';
import 'package:multiphone/match/match_speaker.dart';

class PingPongMatchSpeaker extends MatchSpeaker<PingPongMatch> {
  // special string to announce our expedite system
  String _expediteMessageString;

  @override
  String createPointsPhrase(PingPongMatch match, BuildContext context,
      TeamIndex changeTeam, int level) {
    PingPongMatchSetup setup = match.getSetup();
    StringBuilder message = StringBuilder();
    String teamOneString = getSpeakingTeamName(context, setup, TeamIndex.T_ONE);
    String teamTwoString = getSpeakingTeamName(context, setup, TeamIndex.T_TWO);
    String changeTeamString = getSpeakingTeamName(context, setup, changeTeam);
    switch (level) {
      case PingPongScore.LEVEL_POINT:
        // the points changed, announce the points
        Point t1Point =
            match.getDisplayPoint(PingPongScore.LEVEL_POINT, TeamIndex.T_ONE);
        Point t2Point =
            match.getDisplayPoint(PingPongScore.LEVEL_POINT, TeamIndex.T_TWO);
        // read out the points, winner first
        if (t1Point.val() > t2Point.val()) {
          // player one has more
          appendPause(
              message, t1Point.speakString(context), Point.K_SPEAKING_SPACE);
          appendPause(
              message, t2Point.speakString(context), Point.K_SPEAKING_SPACE);
          append(message, teamOneString);
        } else if (t2Point.val() > t1Point.val()) {
          // player two has more
          appendPause(
              message, t2Point.speakString(context), Point.K_SPEAKING_SPACE);
          appendPause(
              message, t1Point.speakString(context), Point.K_SPEAKING_SPACE);
          append(message, teamTwoString);
        } else {
          // they are the same, read this out
          if (t1Point.val() == setup.decidingPoint) {
            // we have a draw at deciding point, this is called 'deuce' in ping-pong
            append(message, PingPongPoint.deuce.speakString(context));
          } else {
            // the points are the same, say 15-all or whatever
            append(message, t1Point.speakAllString(context));
          }
        }
        break;
      case PingPongScore.LEVEL_ROUND:
        // the rounds changed, announce who won the game
        appendPause(message, PingPongPoint.round.speakString(context),
            Point.K_SPEAKING_PAUSE);
        // also match?
        if (match.isMatchOver()) {
          appendPause(message, PingPongPoint.match.speakString(context),
              Point.K_SPEAKING_PAUSE);
        }
        appendPause(message, changeTeamString, Point.K_SPEAKING_PAUSE_LONG);
        // also we want to say the rounds as they stand

        // team one first
        int rounds = match.getPoint(PingPongScore.LEVEL_ROUND, TeamIndex.T_ONE);
        append(message, teamOneString);
        appendInt(message, rounds);
        appendPause(
            message,
            PingPongPoint.round.speakNumberString(context, rounds),
            Point.K_SPEAKING_PAUSE);

        // team two now
        rounds = match.getPoint(PingPongScore.LEVEL_ROUND, TeamIndex.T_TWO);
        append(message, teamTwoString);
        appendInt(message, rounds);
        appendPause(
            message,
            PingPongPoint.round.speakNumberString(context, rounds),
            Point.K_SPEAKING_PAUSE);
        break;
    }
    if (match.getIsAnnounceExpediteSystem()) {
      // we are to announce the commencement of the expedite system
      append(message, Point.K_SPEAKING_PAUSE);
      if (_expediteMessageString == null) {
        _expediteMessageString = BuildContext == null
            ? "commence expedite"
            : Values(context).strings.speak_expedite_system;
      }
      append(message, _expediteMessageString);
      match.expediteSystemAnnounced();
    }
    // and return the complicated message to speak
    return message.toString();
  }

  @override
  String getLevelTitle(int level, BuildContext context) {
    switch (level) {
      case PingPongScore.LEVEL_POINT:
        return PingPongPoint.point.speakString(context);
      case PingPongScore.LEVEL_ROUND:
        return PingPongPoint.round.speakString(context);
    }
    return super.getLevelTitle(level, context);
  }

  @override
  String createPointsAnnouncement(PingPongMatch match, BuildContext context) {
    PingPongMatchSetup setup = match.getSetup();

    TeamIndex teamServing = match.getServingTeam();
    TeamIndex teamReceiving = setup.getOtherTeam(teamServing);
    int serverPoints = match.getPoint(PingPongScore.LEVEL_POINT, teamServing);
    int receiverPoints =
        match.getPoint(PingPongScore.LEVEL_POINT, teamReceiving);

    String serverString = getSpeakingTeamName(context, setup, teamServing);
    String receiverString = getSpeakingTeamName(context, setup, teamReceiving);

    String message;
    if (serverPoints + receiverPoints > 0) {
      // there are points, say the points
      message = createPointsPhrase(
          match, context, teamServing, PingPongScore.LEVEL_POINT);
    } else {
      // no points scored, are there any rounds?
      int serverRounds = match.getPoint(PingPongScore.LEVEL_ROUND, teamServing);
      int receiverRounds =
          match.getPoint(PingPongScore.LEVEL_ROUND, teamReceiving);
      StringBuilder messageBuilder = StringBuilder();
      if (serverRounds + receiverRounds > 0) {
        // announce the games
        appendPause(
            messageBuilder, serverString, Point.K_SPEAKING_PAUSE_SLIGHT);
        appendIntPause(
            messageBuilder, serverRounds, Point.K_SPEAKING_PAUSE_SLIGHT);
        appendPause(
            messageBuilder,
            PingPongPoint.round.speakNumberString(context, serverRounds),
            Point.K_SPEAKING_PAUSE);
        appendPause(
            messageBuilder, receiverString, Point.K_SPEAKING_PAUSE_SLIGHT);
        appendIntPause(
            messageBuilder, receiverRounds, Point.K_SPEAKING_PAUSE_SLIGHT);
        append(messageBuilder,
            PingPongPoint.round.speakNumberString(context, receiverRounds));
        append(messageBuilder,
            BuildContext == null ? "rounds" : Values(context).strings.rounds);
      }
      message = messageBuilder.toString();
    }
    // return the message created
    return message;
  }
}
