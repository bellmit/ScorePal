import 'package:flutter/material.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/widgets/badminton/badminton_score_widget.dart';
import 'package:multiphone/screens/play_match_screen.dart';

class PlayBadmintonScreen extends PlayMatchScreen {
  static const String routeName = "/play-badminton";

  PlayBadmintonScreen();

  @override
  Widget createScoreWidget(TeamIndex teamIndex) {
    // return each score widget we want to use
    return BadmintonScoreWidget(team: teamIndex);
  }
}
