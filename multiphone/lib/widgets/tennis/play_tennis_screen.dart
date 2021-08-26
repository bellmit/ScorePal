import 'package:flutter/material.dart';
import 'package:multiphone/match/tennis/tennis_match.dart';
import 'package:multiphone/match/tennis/tennis_score.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/screens/play_match_screen.dart';
import 'package:multiphone/widgets/tennis/end_tennis_screen.dart';
import 'package:multiphone/widgets/tennis/tennis_score_widget.dart';

class PlayTennisScreen extends PlayMatchScreen {
  static const String routeName = "/play-tennis";
  PlayTennisScreen();

  @override
  String getEndingRoute() {
    return EndTennisScreen.routeName;
  }

  @override
  void onScoreClicked(ActiveMatch match, TeamIndex team, int level) {
    // so a score was clicked, call the proper function
    if (match is TennisMatch) {
      switch (level) {
        case TennisScore.LEVEL_POINT:
          match.incrementPoint(team);
          break;
        case TennisScore.LEVEL_GAME:
          match.incrementGame(team);
          break;
        case TennisScore.LEVEL_SET:
          match.incrementSet(team);
          break;
      }
    }
  }

  @override
  Widget createScoreWidget(ActiveMatch match, TeamIndex teamIndex,
      void Function(int level) onScoreClicked) {
    // return each score widget we want to use
    if (match is TennisMatch) {
      return TennisScoreWidget(
        sets: match.getDisplayPoint(TennisScore.LEVEL_SET, teamIndex),
        games: match.getDisplayPoint(TennisScore.LEVEL_GAME, teamIndex),
        points: match.getDisplayPoint(TennisScore.LEVEL_POINT, teamIndex),
        isServing: match.getServingTeam() == teamIndex,
        onScoreClicked: onScoreClicked,
      );
    } else {
      return Container();
    }
  }
}
