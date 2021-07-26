import 'package:flutter/material.dart';
import 'package:multiphone/match/ping_pong/ping_pong_match.dart';
import 'package:multiphone/match/ping_pong/ping_pong_score.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/widgets/pingpong/ping_pong_score_widget.dart';
import 'package:multiphone/screens/play_match_screen.dart';

class PlayPingPongScreen extends PlayMatchScreen {
  static const String routeName = "/play-ping-pong";

  PlayPingPongScreen();

  @override
  void onScoreClicked(ActiveMatch match, TeamIndex team, int level) {
    // so a score was clicked, call the proper function
    PingPongMatch pingPongMatch = match as PingPongMatch;
    switch (level) {
      case PingPongScore.LEVEL_POINT:
        pingPongMatch.incrementPoint(team);
        break;
      case PingPongScore.LEVEL_ROUND:
        pingPongMatch.incrementRound(team);
        break;
    }
  }

  @override
  Widget createScoreWidget(
      TeamIndex teamIndex, void Function(int level) onScoreClicked) {
    // return each score widget we want to use
    return PingPongScoreWidget(
      team: teamIndex,
      onScoreClicked: onScoreClicked,
    );
  }
}
