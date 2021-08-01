import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/badminton/badminton_match.dart';
import 'package:multiphone/match/badminton/badminton_score.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/widgets/match_score_summary_widget.dart';

class BadmintonScoreSummaryWidget extends MatchScoreSummaryWidget {
  final BadmintonMatch match;
  const BadmintonScoreSummaryWidget({
    Key key,
    @required this.match,
    @required teamOneName,
    @required teamTwoName,
    @required isTeamOneConceded,
    @required isTeamTwoConceded,
  }) : super(
          key: key,
          teamOneName: teamOneName,
          teamTwoName: teamTwoName,
          isTeamOneConceded: isTeamOneConceded,
          isTeamTwoConceded: isTeamTwoConceded,
        );

  @override
  int getScoreCount() {
    // if we haven't finished, we want to show the points, else just the games
    return 1 + (match.score.isMatchOver(isCheckConceded: false) ? 0 : 1);
  }

  @override
  String getScore(BuildContext context, int index, int row) {
    switch (index) {
      case 0:
        // first column is the games each team has won
        return match
            .getDisplayGame(row == 0 ? TeamIndex.T_ONE : TeamIndex.T_TWO)
            .displayString(context);
      case 1:
        // and this is the current points
        return match
            .getTeamDisplayPoint(row == 0 ? TeamIndex.T_ONE : TeamIndex.T_TWO)
            .displayString(context);
    }
    // shouldn't get here
    return '';
  }

  @override
  String getScoreTitle(BuildContext context, int index, int row) {
    if (row == 1) {
      // this is the title between the boxes (for a tie display)
      return '';
    }
    switch (index) {
      case 0:
        // this is the games
        return Values(context).strings.title_badminton_games;
      case 1:
        // this is the points
        return Values(context).strings.title_badminton_points;
    }
    return '';
  }
}
