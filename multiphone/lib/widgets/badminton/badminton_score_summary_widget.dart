import 'package:flutter/material.dart';
import 'package:multiphone/match/badminton/badminton_match.dart';
import 'package:multiphone/widgets/match_score_summary.dart';

class BadmintonScoreSummaryWidget extends MatchScoreSummary {
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
    // if we haven't finished, there are points currently in play
    return 1;
  }

  @override
  String getScore(BuildContext context, int index, int row) {
    return '0';
  }

  @override
  String getScoreTitle(BuildContext context, int index, int row) {
    return '';
  }
}
