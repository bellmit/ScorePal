import 'package:flutter/material.dart';
import 'package:multiphone/match/badminton/badminton_match.dart';
import 'package:multiphone/match/badminton/badminton_score.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/widgets/badminton/badminton_score_widget.dart';
import 'package:multiphone/screens/play_match_screen.dart';

class PlayBadmintonScreen extends PlayMatchScreen {
  static const String routeName = "/play-badminton";

  PlayBadmintonScreen();

  @override
  void onScoreClicked(ActiveMatch match, TeamIndex team, int level) {
    // so a score was clicked, call the proper function
    BadmintonMatch badmintonMatch = match as BadmintonMatch;
    switch (level) {
      case BadmintonScore.LEVEL_POINT:
        badmintonMatch.incrementPoint(team);
        break;
      case BadmintonScore.LEVEL_GAME:
        badmintonMatch.incrementGame(team);
        break;
    }
  }

  @override
  Widget createScoreWidget(ActiveMatch match, TeamIndex teamIndex,
      void Function(int level) onScoreClicked) {
    // return each score widget we want to use
    var badmintonMatch = match as BadmintonMatch;
    return BadmintonScoreWidget(
      games:
          badmintonMatch.getDisplayPoint(BadmintonScore.LEVEL_GAME, teamIndex),
      points:
          badmintonMatch.getDisplayPoint(BadmintonScore.LEVEL_POINT, teamIndex),
      onScoreClicked: onScoreClicked,
    );
  }
}
