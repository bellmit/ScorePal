import 'package:flutter/material.dart';
import 'package:multiphone/match/badminton/badminton_match.dart';
import 'package:multiphone/match/badminton/badminton_score.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/widgets/badminton/badminton_score_widget.dart';
import 'package:multiphone/screens/play_match_screen.dart';
import 'package:multiphone/widgets/badminton/end_badminton_screen.dart';

class PlayBadmintonScreen extends PlayMatchScreen {
  static const String routeName = "/play-badminton";

  PlayBadmintonScreen();

  @override
  String getEndingRoute() {
    return EndBadmintonScreen.routeName;
  }

  @override
  void onScoreClicked(ActiveMatch match, TeamIndex team, int level) {
    // so a score was clicked, call the proper function
    if (match is BadmintonMatch) {
      switch (level) {
        case BadmintonScore.LEVEL_POINT:
          match.incrementPoint(team);
          break;
        case BadmintonScore.LEVEL_GAME:
          match.incrementGame(team);
          break;
      }
    }
  }

  @override
  Widget createScoreWidget(ActiveMatch match, TeamIndex teamIndex,
      void Function(int level) onScoreClicked) {
    // return each score widget we want to use
    if (match is BadmintonMatch) {
      return BadmintonScoreWidget(
        games: match.getDisplayPoint(BadmintonScore.LEVEL_GAME, teamIndex),
        points: match.getDisplayPoint(BadmintonScore.LEVEL_POINT, teamIndex),
        isServing: match.getServingTeam() == teamIndex,
        onScoreClicked: onScoreClicked,
      );
    } else {
      return Container();
    }
  }
}
