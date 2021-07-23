import 'package:flutter/material.dart';
import 'package:multiphone/providers/team.dart';
import 'package:multiphone/widgets/pingpong/ping_pong_score_widget.dart';
import 'package:multiphone/widgets/play_match_screen.dart';

class PlayPingPongScreen extends PlayMatchScreen {
  static const String routeName = "/play-ping-pong";
  PlayPingPongScreen();

  @override
  Widget createScoreWidget(TeamIndex teamIndex) {
    // return each score widget we want to use
    return PingPongScoreWidget(team: teamIndex);
  }
}
