import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/ping_pong/ping_pong_match.dart';
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
    if (!match.isMatchOver(isCheckConceded: false)) {
      // the match isn't over - we have some current points to show too
      ++scoreCount;
    }
    return scoreCount;
  }

  @override
  MatchScoreSummaryItem getScoreItem(BuildContext context, int index, int row) {
    // return the points for each round
    String points = match.score
        .getRoundPoints(row == 0 ? TeamIndex.T_ONE : TeamIndex.T_TWO, index)
        .toString();
    // and calculate the correct title for this
    final values = Values(context);
    String title;
    bool isWinner;
    if (!match.isMatchOver(isCheckConceded: false) &&
        index == match.score.getPlayedRounds()) {
      // the match isn't over - and this is the current round
      if (row == 0) {
        title = values.strings.title_ping_pong_points;
      }
    } else {
      // this is a finished game
      title =
          values.construct(values.strings.display_round_number, [index + 1]);
      // for which we can show who won
      final tOneRounds = match.score.getRoundPoints(TeamIndex.T_ONE, index);
      final tTwoRounds = match.score.getRoundPoints(TeamIndex.T_TWO, index);
      isWinner =
          row == 0 ? (tOneRounds > tTwoRounds) : (tTwoRounds > tOneRounds);
    }
    // and return all the data collected
    return MatchScoreSummaryItem(
        score: points, title: title, isWinner: isWinner);
  }
}
