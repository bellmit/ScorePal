import 'package:flutter/material.dart';
import 'package:multiphone/helpers/preferences.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/match/point.dart';
import 'package:multiphone/match/score_state.dart';
import 'package:multiphone/providers/player.dart';

class StringBuilder {
  String content = '';

  void append(String string) {
    content += string;
  }

  void removeLastPause() {
    // we want to remove any pauses, trim any space
    if (content.endsWith(Point.K_SPEAKING_PAUSE)) {
      content =
          content.substring(0, content.length - Point.K_SPEAKING_PAUSE.length);
    }
    if (content.endsWith(Point.K_SPEAKING_PAUSE_SLIGHT)) {
      content = content.substring(
          0, content.length - Point.K_SPEAKING_PAUSE_SLIGHT.length);
    }
    if (content.endsWith(Point.K_SPEAKING_PAUSE_LONG)) {
      content = content.substring(
          0, content.length - Point.K_SPEAKING_PAUSE_LONG.length);
    }
  }

  bool endsWith(String end) {
    return content.endsWith(end);
  }

  String cleanUp() {
    String cleaned = content.replaceAll('  ', ' ');
    return cleaned.replaceAll(' . ', ' ');
  }

  @override
  String toString() {
    return content;
  }
}

abstract class MatchSpeaker<T extends ActiveMatch> {
  final Future<Preferences> initialized;
  Preferences prefs;

  MatchSpeaker() : initialized = Preferences.create() {
    // private constructor
    initialized.then((value) => prefs = value);
  }

  String createPointsPhrase(
      T match, BuildContext context, TeamIndex team, int level) {
    ActiveSetup setup = match.getSetup();

    TeamIndex otherTeam = setup.getOtherTeam(team);
    // formulate the message
    StringBuilder builder = StringBuilder();
    builder.append(getSpeakingTeamName(context, setup, team));
    builder.append(Point.K_SPEAKING_PAUSE_SLIGHT);
    builder.append(match.getPoint(level, team).toString());
    builder.append(Point.K_SPEAKING_PAUSE_SLIGHT);
    builder.append(Values(context).strings.speak_points);
    builder.append(Point.K_SPEAKING_PAUSE_SLIGHT);
    builder.append(getSpeakingTeamName(context, setup, otherTeam));
    builder.append(Point.K_SPEAKING_PAUSE_SLIGHT);
    builder.append(match.getPoint(level, otherTeam).toString());
    builder.append(Point.K_SPEAKING_PAUSE_SLIGHT);
    builder.append(Values(context).strings.speak_points);
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

  String getSpeakingTeamName(
      BuildContext context, ActiveSetup setup, TeamIndex team) {
    // use the player's names to speak only when allowed
    String teamName = prefs.soundUseSpeakingNames
        ? setup.getTeamName(team, context)
        : setup.getDefaultTeamName(team, context);
    // remove all the punctuation from the team name so there are no weird pauses in it.
    if (teamName == null || teamName.isEmpty) {
      return teamName;
    } else {
      // delete all the dot chars as they introduce pauses in the spoken text
      return teamName.replaceAll(".", "");
    }
  }

  String getSpeakingPlayerName(
      BuildContext context, ActiveSetup setup, PlayerIndex player) {
    // use the player's names to speak only when allowed
    String playerName = prefs.soundUseSpeakingNames
        ? setup.getPlayerName(player, context)
        : setup.getDefaultPlayerName(player, context);
    // remove all the punctuation from the team name so there are no weird pauses in it.
    if (playerName == null || playerName.isEmpty) {
      return playerName;
    } else {
      // delete all the dot chars as they introduce pauses in the spoken text
      return playerName.replaceAll(".", "");
    }
  }

  String createPointsAnnouncement(T match, BuildContext context) {
    int topLevel = match.getScoreLevels() - 1;
    TeamIndex team = match.getServingTeam();
    return createPointsPhrase(match, context, team, topLevel);
  }

  String getSpeakingStateMessage(
      BuildContext context, T match, ScoreState state) {
    // now handle the changes here to announce what happened
    StringBuilder spokenMessage = StringBuilder();
    ActiveSetup setup = match.getSetup();
    if (prefs.soundActionSpeak && null != context) {
      if (state.isChanged(ScoreChange.decrement)) {
        if (!prefs.soundAnnounceChange) {
          // we wont say the correction message - so say here
          appendPause(spokenMessage, Values(context).strings.correction,
              Point.K_SPEAKING_SPACE);
        }
      } else if (state.isChanged(ScoreChange.increment)) {
        // only speak the action if we are not speaking the score as points change
        if (state.getLevelChanged() == 0 ||
            !prefs.soundAnnounceChange ||
            !prefs.soundAnnounceChangePoints) {
          // the level is points - say, or we arn't announcing points - so announce that they pushed the button
          appendPause(
              spokenMessage,
              getLevelTitle(state.getLevelChanged(), context),
              Point.K_SPEAKING_SPACE);
          appendPause(
              spokenMessage,
              getSpeakingTeamName(context, setup, state.getTeamChanged()),
              Point.K_SPEAKING_PAUSE);
        }
      }
    }
    if (prefs.soundAnnounceChange && null != context) {
      // they want us to announce any change, so build the string to say
      if (state.isChanged(ScoreChange.decrement)) {
        // this is a correction
        appendPause(spokenMessage, Values(context).strings.correction,
            Point.K_SPEAKING_PAUSE);
        // and just remind them of the points
        if (prefs.soundAnnounceChangePoints) {
          // and we want to say it
          appendPause(
              spokenMessage,
              match.createPointsPhrase(context, state.getTeamChanged(), 0),
              Point.K_SPEAKING_PAUSE);
        }
      } else {
        if (state.isChanged(ScoreChange.increment)) {
          // this is a change in the score, add the score to the announcement
          if (prefs.soundAnnounceChangePoints) {
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
              prefs.soundAnnounceChangeEnds) {
            appendPause(spokenMessage, Values(context).strings.change_ends,
                Point.K_SPEAKING_SPACE);
          }
          if (state.isChanged(ScoreChange.server) &&
              prefs.soundAnnounceChangeServer) {
            // change the server
            //append(spokenMessage, Values(context).strings.change_server));
            // append the name of the server
            String serverName =
                getSpeakingPlayerName(context, setup, match.getServingPlayer());
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
          if (prefs.soundAnnounceChangeScore) {
            // remove any hanging pause
            spokenMessage.removeLastPause();
            // and put a long one in instead
            append(spokenMessage, Point.K_SPEAKING_PAUSE_LONG);
            // and the message
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
    return spokenMessage.cleanUp();
  }

  void append(StringBuilder message, String spokenMessage) {
    appendPause(message, spokenMessage, ' ');
  }

  void appendInt(StringBuilder message, int spokenNumber) {
    appendPause(message, spokenNumber.toString(), ' ');
  }

  void appendIntPause(StringBuilder message, int spokenNumber, String pause) {
    appendPause(message, spokenNumber.toString(), pause);
  }

  void appendPause(StringBuilder message, String spokenMessage, String pause) {
    if (null != spokenMessage && spokenMessage.isNotEmpty) {
      // there is a state showing and the match isn't over, speak it here
      message.append(spokenMessage + pause);
    }
  }
}
