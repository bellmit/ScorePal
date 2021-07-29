import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/match/point.dart';
import 'package:multiphone/match/tennis/tennis_point.dart';
import 'package:multiphone/match/tennis/tennis_match.dart';
import 'package:multiphone/match/tennis/tennis_match_setup.dart';
import 'package:multiphone/match/tennis/tennis_score.dart';
import 'package:multiphone/match/match_speaker.dart';

class TennisMatchSpeaker extends MatchSpeaker<TennisMatch> {
  @override
  String createPointsPhrase(
      TennisMatch match, BuildContext context, TeamIndex team, int level) {
    String message = "";
    switch (level) {
      case TennisScore.LEVEL_POINT:
        // the points changed, announce the points
        message = createPhrasePoints(match, context, team);
        break;
      case TennisScore.LEVEL_GAME:
        // the games changed, announce who won the game
        message = createPhraseGames(match, context, team);
        break;
      case TennisScore.LEVEL_SET:
        // the sets changed, announce who won the game and the set
        message = createPhraseSets(match, context, team);
        break;
    }
    // and return the complicated message to speak
    return message;
  }

  @override
  String getLevelTitle(int level, BuildContext context) {
    switch (level) {
      case TennisScore.LEVEL_POINT:
        return TennisPoint.point.speakString(context);
      case TennisScore.LEVEL_GAME:
        return TennisPoint.game.speakString(context);
      case TennisScore.LEVEL_SET:
        return TennisPoint.set.speakString(context);
    }
    return super.getLevelTitle(level, context);
  }

  String createScorePhrase(
      TennisMatch match, BuildContext context, TeamIndex team, int level) {
    String message = "";
    switch (level) {
      case TennisScore.LEVEL_POINT:
        // the points changed, don't say anything else
        break;
      case TennisScore.LEVEL_GAME:
        // the games changed, tell them the current score state
        message = createScorePhraseGames(match, context, team);
        break;
      case TennisScore.LEVEL_SET:
        // the sets changed, tell them the current score state
        message = createScorePhraseSets(match, context, team);
        break;
    }
    // and return the complicated message to speak
    return message;
  }

  String createPhraseSets(
      TennisMatch match, BuildContext context, TeamIndex changeTeam) {
    TennisMatchSetup setup = match.getSetup();
    // create the announcement of the set result on this match
    String changeTeamString = getSpeakingTeamName(context, setup, changeTeam);
    StringBuilder message = StringBuilder();

    appendPause(
        message, TennisPoint.game.speakString(context), Point.K_SPEAKING_PAUSE);
    appendPause(
        message, TennisPoint.set.speakString(context), Point.K_SPEAKING_PAUSE);
    // also match?
    if (match.isMatchOver()) {
      appendPause(message, TennisPoint.match.speakString(context),
          Point.K_SPEAKING_PAUSE);
    }
    // add the winner's name
    append(message, changeTeamString);
    // and return this
    return message.toString();
  }

  String createScorePhraseSets(
      TennisMatch match, BuildContext context, TeamIndex changeTeam) {
    TennisMatchSetup setup = match.getSetup();
    // create the announcement of the set result on this match
    String teamOneString = getSpeakingTeamName(context, setup, TeamIndex.T_ONE);
    String teamTwoString = getSpeakingTeamName(context, setup, TeamIndex.T_TWO);
    StringBuilder message = StringBuilder();

    if (null != match && !match.isMatchOver()) {
      // match isn't over, want to read out the sets we have one
      append(message, Point.K_SPEAKING_PAUSE);

      int setsOne = match.getPoint(TennisScore.LEVEL_SET, TeamIndex.T_ONE);
      int setsTwo = match.getPoint(TennisScore.LEVEL_SET, TeamIndex.T_TWO);

      if (setsOne == setsTwo) {
        // we are equal, say '1 set(s) all'
        appendPause(
            message,
            null == BuildContext
                ? (setsOne.toString() + " all")
                : Values(context)
                    .construct(Values(context).strings.speak_number_all, [
                    setsOne,
                    TennisPoint.set.speakNumberString(context, setsOne),
                  ]),
            Point.K_SPEAKING_PAUSE);
      } else {
        // team one first
        append(message, teamOneString);
        appendInt(message, setsOne);
        appendPause(
            message,
            TennisPoint.set.speakNumberString(context, setsOne),
            Point.K_SPEAKING_PAUSE);

        // team two first
        append(message, teamTwoString);
        appendInt(message, setsTwo);
        appendPause(
            message,
            TennisPoint.set.speakNumberString(context, setsTwo),
            Point.K_SPEAKING_PAUSE);
      }
    } else {
      // we want to also read out the games from each set
      append(message, Point.K_SPEAKING_PAUSE_LONG);

      TeamIndex winnerTeam;
      TeamIndex loserTeam;
      if (changeTeam == TeamIndex.T_ONE) {
        // team one is the winner
        winnerTeam = TeamIndex.T_ONE;
        loserTeam = TeamIndex.T_TWO;
      } else {
        // team two is the winner
        winnerTeam = TeamIndex.T_TWO;
        loserTeam = TeamIndex.T_ONE;
      }
      for (int i = 0; i < match.getPlayedSets(); ++i) {
        append(message, Point.K_SPEAKING_PAUSE_LONG);
        appendPause(message, match.getGames(winnerTeam, i).speakString(context),
            Point.K_SPEAKING_PAUSE_SLIGHT);
        appendPause(message, match.getGames(loserTeam, i).speakString(context),
            Point.K_SPEAKING_PAUSE_SLIGHT);
      }
    }
    return message.toString();
  }

  String createPhraseGames(
      TennisMatch match, BuildContext context, TeamIndex changeTeam) {
    TennisMatchSetup setup = match.getSetup();
    // create the announcement of games on this match
    String changeTeamString = getSpeakingTeamName(context, setup, changeTeam);
    StringBuilder message = StringBuilder();
    appendPause(
        message, TennisPoint.game.speakString(context), Point.K_SPEAKING_PAUSE);
    append(message, changeTeamString);
    return message.toString();
  }

  String createScorePhraseGames(
      TennisMatch match, BuildContext context, TeamIndex changeTeam) {
    TennisMatchSetup setup = match.getSetup();
    // create the announcement of games on this match
    String teamOneString = getSpeakingTeamName(context, setup, TeamIndex.T_ONE);
    String teamTwoString = getSpeakingTeamName(context, setup, TeamIndex.T_TWO);
    String changeTeamString = getSpeakingTeamName(context, setup, changeTeam);
    StringBuilder message = StringBuilder();
    // so we want to say the games as they stand
    append(message, Point.K_SPEAKING_PAUSE_LONG);

    int gamesOne = match.getGames(TeamIndex.T_ONE, -1).val();
    int gamesTwo = match.getGames(TeamIndex.T_TWO, -1).val();

    if (gamesOne == gamesTwo) {
      // we are equal, say '1 game(s) all'
      appendPause(
          message,
          BuildContext == null
              ? (gamesOne.toString() + " all")
              : Values(context).construct(
                  Values(context).strings.speak_number_all, [
                  gamesOne,
                  TennisPoint.game.speakNumberString(context, gamesOne)
                ]),
          Point.K_SPEAKING_PAUSE);
    } else {
      // team one first
      append(message, teamOneString);
      appendInt(message, gamesOne);
      appendPause(
          message,
          TennisPoint.game.speakNumberString(context, gamesOne),
          Point.K_SPEAKING_PAUSE);

      // team two first
      append(message, teamTwoString);
      appendInt(message, gamesTwo);
      appendPause(
          message,
          TennisPoint.game.speakNumberString(context, gamesTwo),
          Point.K_SPEAKING_PAUSE);
    }

    return message.toString();
  }

  String createPhrasePoints(
      TennisMatch match, BuildContext context, TeamIndex changeTeam) {
    TennisMatchSetup setup = match.getSetup();
    // create the announcement of points on this match
    String teamOneString = getSpeakingTeamName(context, setup, TeamIndex.T_ONE);
    String teamTwoString = getSpeakingTeamName(context, setup, TeamIndex.T_TWO);
    Point t1Point =
        match.getDisplayPoint(TennisScore.LEVEL_POINT, TeamIndex.T_ONE);
    Point t2Point =
        match.getDisplayPoint(TennisScore.LEVEL_POINT, TeamIndex.T_TWO);
    StringBuilder message = StringBuilder();

    if (t1Point == TennisPoint.advantage) {
      // read advantage team one
      appendPause(
          message, t1Point.speakString(context), Point.K_SPEAKING_SPACE);
      append(message, teamOneString);
    } else if (t2Point == TennisPoint.advantage) {
      // read advantage team two
      appendPause(
          message, t2Point.speakString(context), Point.K_SPEAKING_SPACE);
      append(message, teamTwoString);
    } else if (t1Point == TennisPoint.deuce && t2Point == TennisPoint.deuce) {
      // read deuce
      append(message, t1Point.speakString(context));
    } else if (t1Point.val() == t2Point.val()) {
      // they have the same score, use the special "all" values
      append(message, t1Point.speakAllString(context));
    } else if (match.isInTieBreak()) {
      // in a tie-break we read the score with the winner first
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
        // the points are the same
        append(message, t1Point.speakAllString(context));
      }
    } else {
      // just read the numbers out, but we want to say the server first
      // so who is that?
      if (match.getServingTeam() == TeamIndex.T_ONE) {
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
    }
    return message.toString();
  }

  @override
  String createPointsAnnouncement(TennisMatch match, BuildContext context) {
    TennisMatchSetup setup = match.getSetup();

    TeamIndex teamServing = match.getServingTeam();
    TeamIndex teamReceiving = setup.getOtherTeam(teamServing);
    int serverPoints = match.getPoint(TennisScore.LEVEL_POINT, teamServing);
    int receiverPoints = match.getPoint(TennisScore.LEVEL_POINT, teamReceiving);

    String serverString = getSpeakingTeamName(context, setup, teamServing);
    String receiverString = getSpeakingTeamName(context, setup, teamReceiving);

    String message;
    if (serverPoints + receiverPoints > 0) {
      // there are points, say the points
      message = createPointsPhrase(
          match, context, teamServing, TennisScore.LEVEL_POINT);
    } else {
      // no points scored, are there any games?
      int serverGames = match.getGames(teamServing, -1).val();
      int receiverGames = match.getGames(teamReceiving, -1).val();
      StringBuilder messageBuilder = StringBuilder();
      if (serverGames + receiverGames > 0) {
        // announce the games
        appendPause(
            messageBuilder, serverString, Point.K_SPEAKING_PAUSE_SLIGHT);
        appendIntPause(
            messageBuilder, serverGames, Point.K_SPEAKING_PAUSE_SLIGHT);
        appendPause(
            messageBuilder,
            TennisPoint.game.speakNumberString(context, serverGames),
            Point.K_SPEAKING_PAUSE);
        appendPause(
            messageBuilder, receiverString, Point.K_SPEAKING_PAUSE_SLIGHT);
        appendIntPause(
            messageBuilder, receiverGames, Point.K_SPEAKING_PAUSE_SLIGHT);
        append(messageBuilder,
            TennisPoint.game.speakNumberString(context, receiverGames));
      } else {
        // do the sets
        int serverSets = match.getPoint(TennisScore.LEVEL_SET, teamServing);
        int receiverSets = match.getPoint(TennisScore.LEVEL_SET, teamReceiving);
        if (receiverSets + serverSets > 0) {
          // announce the sets
          appendPause(
              messageBuilder, serverString, Point.K_SPEAKING_PAUSE_SLIGHT);
          appendIntPause(
              messageBuilder, serverSets, Point.K_SPEAKING_PAUSE_SLIGHT);
          appendPause(
              messageBuilder,
              TennisPoint.set.speakNumberString(context, serverSets),
              Point.K_SPEAKING_PAUSE);
          appendPause(
              messageBuilder, receiverString, Point.K_SPEAKING_PAUSE_SLIGHT);
          appendIntPause(
              messageBuilder, receiverSets, Point.K_SPEAKING_PAUSE_SLIGHT);
          append(messageBuilder,
              TennisPoint.set.speakNumberString(context, receiverSets));
        } else {
          append(
              messageBuilder,
              BuildContext == null
                  ? "game not started"
                  : Values(context).strings.speak_no_score);
        }
      }
      message = messageBuilder.toString();
    }
    // return the message created
    return message;
  }
}
