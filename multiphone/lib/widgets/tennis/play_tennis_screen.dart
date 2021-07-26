import 'package:flutter/material.dart';
import 'package:multiphone/match/tennis/tennis_match.dart';
import 'package:multiphone/match/tennis/tennis_score.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/screens/play_match_screen.dart';
import 'package:multiphone/widgets/tennis/tennis_score_widget.dart';

class PlayTennisScreen extends PlayMatchScreen {
  static const String routeName = "/play-tennis";
  PlayTennisScreen();

  @override
  void onScoreClicked(ActiveMatch match, TeamIndex team, int level) {
    // so a score was clicked, call the proper function
    TennisMatch tennisMatch = match as TennisMatch;
    print('$level clicked');
    switch (level) {
      case TennisScore.LEVEL_POINT:
        tennisMatch.incrementPoint(team);
        break;
      case TennisScore.LEVEL_GAME:
        tennisMatch.incrementGame(team);
        break;
      case TennisScore.LEVEL_SET:
        tennisMatch.incrementSet(team);
        break;
    }
  }

  @override
  Widget createScoreWidget(
      TeamIndex teamIndex, void Function(int level) onScoreClicked) {
    // return each score widget we want to use
    return TennisScoreWidget(
      team: teamIndex,
      onScoreClicked: onScoreClicked,
    );
  }
}
