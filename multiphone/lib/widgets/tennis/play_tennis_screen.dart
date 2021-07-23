import 'package:flutter/material.dart';
import 'package:multiphone/providers/team.dart';
import 'package:multiphone/widgets/play_match_screen.dart';
import 'package:multiphone/widgets/tennis/tennis_score_widget.dart';

class PlayTennisScreen extends PlayMatchScreen {
  static const String routeName = "/play-tennis";
  PlayTennisScreen();

  @override
  Widget createScoreWidget(TeamIndex teamIndex) {
    // return each score widget we want to use
    return TennisScoreWidget(team: teamIndex);
  }
}
