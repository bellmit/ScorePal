import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/ping_pong/ping_pong_match.dart';
import 'package:multiphone/match/ping_pong/ping_pong_score.dart';
import 'package:multiphone/providers/active_setup.dart';
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
    var scoreCount = match.score.getPlayedRounds();
    if (!match.score.isMatchOver(isCheckConceded: false)) {
      // the match isn't over - we have some current points to show too
      ++scoreCount;
    }
    return scoreCount;
  }

  @override
  String getScore(BuildContext context, int index, int row) {
    var roundIndex = index;
    if (!match.score.isMatchOver(isCheckConceded: false)) {
      // want the points currently in play for this row
      if (index == 0) {
        // just return the points for the current match (correct team)
        return match
            .getDisplayPoint(PingPongScore.LEVEL_POINT,
                row == 0 ? TeamIndex.T_ONE : TeamIndex.T_TWO)
            .displayString(context);
      }
      // the round index is one lower than the score index then
      --roundIndex;
    }
    // return the points for each round
    return match.score
        .getRoundPoints(
            row == 0 ? TeamIndex.T_ONE : TeamIndex.T_TWO, roundIndex)
        .toString();
  }

  @override
  String getScoreTitle(BuildContext context, int index, int row) {
    final values = Values(context);
    var roundIndex = index;
    if (!match.score.isMatchOver(isCheckConceded: false)) {
      if (index == 0) {
        return row == 0 ? values.strings.title_ping_pong_points : '';
      }
      // the round index is one lower than the score index then
      --roundIndex;
    }
    if (row == 0) {
      return values
          .construct(values.strings.display_round_number, [roundIndex + 1]);
    }
    // finally, if here then there's no title to show
    return '';
  }
}
