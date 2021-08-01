import 'package:flutter/material.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/screens/end_match_screen.dart';
import 'package:multiphone/widgets/badminton/badminton_score_summary_widget.dart';
import 'package:multiphone/widgets/match_score_summary_widget.dart';

class EndBadmintonScreen extends EndMatchScreen {
  static const String routeName = "/end-badminton";

  EndBadmintonScreen();

  @override
  MatchScoreSummaryWidget createScoreSummaryWidget(
    BuildContext context,
    ActiveMatch match,
  ) {
    return BadmintonScoreSummaryWidget(
      match: match,
      teamOneName: match.getSetup().getTeamName(TeamIndex.T_ONE, context),
      isTeamOneConceded: match.score.isTeamConceded(TeamIndex.T_ONE),
      teamTwoName: match.getSetup().getTeamName(TeamIndex.T_TWO, context),
      isTeamTwoConceded: match.score.isTeamConceded(TeamIndex.T_TWO),
    );
  }
}
