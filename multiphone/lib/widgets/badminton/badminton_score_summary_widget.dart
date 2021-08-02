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
    // if we haven't finished, there are points currently in play
    var scoreCount = match.score.getPlayedGames();
    if (!match.score.isMatchOver(isCheckConceded: false)) {
      // the match isn't over - we have some current points to show too
      ++scoreCount;
    }
    return scoreCount;
  }

  @override
  String getScore(BuildContext context, int index, int row) {
    // return the points for each game
    return match.score
        .getGamePoints(row == 0 ? TeamIndex.T_ONE : TeamIndex.T_TWO, index)
        .toString();
  }

  @override
  String getScoreTitle(BuildContext context, int index, int row) {
    final values = Values(context);
    if (row == 1) {
      // no titles on the second row
      return '';
    } else if (!match.score.isMatchOver(isCheckConceded: false) &&
        index == match.score.getPlayedGames()) {
      // the match isn't over - and this is the current game
      return values.strings.title_badminton_points;
    } else {
      return values.construct(values.strings.display_round_number, [index + 1]);
    }
  }
}
