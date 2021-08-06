import 'package:flutter/material.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/screens/end_match_screen.dart';
import 'package:multiphone/widgets/match_score_summary_widget.dart';
import 'package:multiphone/widgets/pingpong/ping_pong_score_summary_widget.dart';

class EndPingPongScreen extends EndMatchScreen {
  static const String routeName = "/end-ping-pong";

  EndPingPongScreen();

  @override
  MatchScoreSummaryWidget createScoreSummaryWidget(
    BuildContext context,
    ActiveMatch match,
  ) {
    return PingPongScoreSummaryWidget(
      match: match,
      teamOneName: match.getSetup().getTeamName(TeamIndex.T_ONE, context),
      isTeamOneConceded: match.isTeamConceded(TeamIndex.T_ONE),
      teamTwoName: match.getSetup().getTeamName(TeamIndex.T_TWO, context),
      isTeamTwoConceded: match.isTeamConceded(TeamIndex.T_TWO),
    );
  }
}
