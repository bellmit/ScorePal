import 'package:flutter/material.dart';
import 'package:multiphone/helpers/preferences.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/match/point.dart';
import 'package:multiphone/match/score_state.dart';
import 'package:multiphone/providers/player.dart';
import 'package:provider/provider.dart';

class MatchSpeaker<T extends ActiveMatch> {
  MatchSpeaker();

  String createPointsPhrase(
      T match, BuildContext context, TeamIndex team, int level) {
    ActiveSetup setup = match.getSetup();

    TeamIndex otherTeam = setup.getOtherTeam(team);
    // formulate the message
    String builder = '';
    builder += getSpeakingTeamName(context, setup, team);
    builder += Point.K_SPEAKING_PAUSE_SLIGHT;
    builder += match.getPoint(level, team).toString();
    builder += Point.K_SPEAKING_PAUSE_SLIGHT;
    builder += Values(context).strings.speak_points;
    builder += Point.K_SPEAKING_PAUSE_SLIGHT;
    builder += getSpeakingTeamName(context, setup, otherTeam);
    builder += Point.K_SPEAKING_PAUSE_SLIGHT;
    builder += match.getPoint(level, otherTeam).toString();
    builder += Point.K_SPEAKING_PAUSE_SLIGHT;
    builder += Values(context).strings.speak_points;
    // and return this phrase as a nice string
    return builder.toString();
  }

  String createScorePhrase(
      T match, BuildContext context, TeamIndex team, int level) {
    return createPointsPhrase(match, context, team, level);
  }

  String getLevelTitle(int level, BuildContext context) {
    if (null == context) {
      return "Level ${level + 1}";
    } else {
      return Values(context)
          .construct(Values(context).strings.scoreSummaryLevel, [level]);
    }
  }

  static String getSpeakingTeamName(
      BuildContext context, ActiveSetup setup, TeamIndex team) {
    Preferences preferences = Provider.of<Preferences>(context, listen: false);
    if (preferences.soundUseSpeakingNames) {
      // use the player's names to speak
      String teamName = setup.getTeamName(team, context);
      // remove all the punctuation from the team name so there are no weird pauses in it.
      if (teamName == null || teamName.isEmpty) {
        return teamName;
      } else {
        return teamName.replaceAll("[.]", "");
      }
    } else {
      if (setup.singlesDoubles == MatchSinglesDoubles.doubles) {
        // return the pre-determined team string
        return BuildContext == null
            ? "Doubles"
            : setup.getTeamName(team, context);
      } else {
        // it's just the player on their own
        return BuildContext == null
            ? "Singles"
            : setup.getPlayerName(setup.getTeamPlayer(team), context);
      }
    }
  }

  static String getSpeakingPlayerName(
      BuildContext context, ActiveSetup setup, PlayerIndex player) {
    Preferences preferences = Provider.of<Preferences>(context, listen: false);
    if (preferences.soundUseSpeakingNames) {
      // use the player's names to speak
      String playerName = setup.getPlayerName(player, context);
      // remove all the punctuation from the team name so there are no weird pauses in it.
      if (playerName == null || playerName.isEmpty) {
        return playerName;
      } else {
        return playerName.replaceAll("[.]", "");
      }
    } else {
      // return the pre-determined string
      return BuildContext == null
          ? "Player"
          : setup.getPlayerName(player, context);
    }
  }

  String createPointsAnnouncement(T match, BuildContext context) {
    int topLevel = match.getScoreLevels() - 1;
    TeamIndex team = match.getServingTeam();
    return createPointsPhrase(match, context, team, topLevel);
  }

  String getSpeakingStateMessage(
      BuildContext context, T match, ScoreState state) {
    Preferences preferences = Provider.of<Preferences>(context, listen: false);
    // now handle the changes here to announce what happened
    String spokenMessage = '';
    ActiveSetup setup = match.getSetup();
    if (preferences.soundButtonClick) {
      //TODO the click of the button
    }
    if (preferences.soundActionSpeak && null != context) {
      if (state.isChanged(ScoreChange.decrement)) {
        if (!preferences.soundAnnounceChange) {
          // we wont say the correction message - so say here
          appendPause(spokenMessage, Values(context).strings.correction,
              Point.K_SPEAKING_SPACE);
        }
      } else if (state.isChanged(ScoreChange.increment)) {
        // only speak the action if we are not speaking the score as points change
        if (state.getLevelChanged() == 0 ||
            !preferences.soundAnnounceChange ||
            !preferences.soundAnnounceChangePoints) {
          // the level is points - say, or we arn't announcing points - so announce that they pushed the button
          appendPause(
              spokenMessage,
              getLevelTitle(state.getLevelChanged(), context),
              Point.K_SPEAKING_SPACE);
          appendPause(
              spokenMessage,
              MatchSpeaker.getSpeakingTeamName(
                  context, setup, state.getTeamChanged()),
              Point.K_SPEAKING_PAUSE);
        }
      }
    }
    if (preferences.soundAnnounceChange && null != context) {
      // they want us to announce any change, so build the string to say
      if (state.isChanged(ScoreChange.decrement)) {
        // this is a correction
        appendPause(spokenMessage, Values(context).strings.correction,
            Point.K_SPEAKING_PAUSE);
        // and just remind them of the points
        if (preferences.soundAnnounceChangePoints) {
          // and we want to say it
          appendPause(
              spokenMessage,
              match.createPointsPhrase(context, state.getTeamChanged(), 0),
              Point.K_SPEAKING_PAUSE);
        }
      } else {
        if (state.isChanged(ScoreChange.increment)) {
          // this is a change in the score, add the score to the announcement
          if (preferences.soundAnnounceChangePoints) {
            // and we want to say it
            appendPause(
                spokenMessage,
                match.createPointsPhrase(
                    context, state.getTeamChanged(), state.getLevelChanged()),
                Point.K_SPEAKING_PAUSE);
          }
        }
        // add the extra details here
        if (!match.isMatchOver()) {
          // don't announce any of this change in state if the match is over
          if (state.isChanged(ScoreChange.decidingPoint)) {
            appendPause(spokenMessage, Values(context).strings.deciding_point,
                Point.K_SPEAKING_SPACE);
          }
          if (state.isChanged(ScoreChange.ends) &&
              preferences.soundAnnounceChangeEnds) {
            appendPause(spokenMessage, Values(context).strings.change_ends,
                Point.K_SPEAKING_SPACE);
          }
          if (state.isChanged(ScoreChange.server) &&
              preferences.soundAnnounceChangeServer) {
            // change the server
            //append(spokenMessage, Values(context).strings.change_server));
            // append the name of the server
            String serverName = MatchSpeaker.getSpeakingPlayerName(
                context, setup, match.getServingPlayer());
            // and make the message include the name of the player to serve
            appendPause(
                spokenMessage,
                Values(context).construct(
                    Values(context).strings.change_server_server, [serverName]),
                Point.K_SPEAKING_SPACE);
          }
          if (state.isChanged(ScoreChange.tieBreak)) {
            // this is a tie, say this as it's super important
            appendPause(spokenMessage, Values(context).strings.tie_break,
                Point.K_SPEAKING_SPACE);
          }
        }
        if (state.isChanged(ScoreChange.increment)) {
          // and summarise the larger score
          if (preferences.soundAnnounceChangeScore) {
            append(spokenMessage, Point.K_SPEAKING_PAUSE_LONG);
            appendPause(
                spokenMessage,
                match.createScorePhrase(
                    context, state.getTeamChanged(), state.getLevelChanged()),
                Point.K_SPEAKING_SPACE);
          }
        }
      }
    }
    // and speak this
    return spokenMessage.toString();
  }

  void append(String message, String spokenMessage) {
    appendPause(message, spokenMessage, "");
  }

  void appendInt(String message, int spokenNumber) {
    appendPause(message, spokenNumber.toString(), "");
  }

  void appendIntPause(String message, int spokenNumber, String pause) {
    appendPause(message, spokenNumber.toString(), pause);
  }

  void appendPause(String message, String spokenMessage, String pause) {
    if (null != spokenMessage && false == spokenMessage.isEmpty) {
      // there is a state showing and the match isn't over, speak it here
      message += spokenMessage + pause;
    }
  }
}
