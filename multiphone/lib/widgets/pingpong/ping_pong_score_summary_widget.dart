import 'package:flutter/material.dart';
import 'package:multiphone/match/ping_pong/ping_pong_match.dart';
import 'package:multiphone/widgets/match_score_summary_widget.dart';

class PingPongScoreSummaryWidget extends MatchScoreSummaryWidget {
  final PingPongMatch match;
  const PingPongScoreSummaryWidget({
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
