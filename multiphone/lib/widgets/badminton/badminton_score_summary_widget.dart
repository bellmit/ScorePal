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
  MatchScoreSummaryItem getScoreItem(BuildContext context, int index, int row) {
    // return the points for each game
    String points = match.score
        .getGamePoints(row == 0 ? TeamIndex.T_ONE : TeamIndex.T_TWO, index)
        .toString();
    // and calculate the correct title for this
    final values = Values(context);
    String title;
    bool isWinner;
    if (!match.score.isMatchOver(isCheckConceded: false) &&
        index == match.score.getPlayedGames()) {
      // the match isn't over - and this is the current game
      if (row == 0) {
        title = values.strings.title_badminton_points;
      }
    } else {
      // this is a finished game
      title = values.construct(values.strings.display_game_number, [index + 1]);
      // for which we can show who won
      final tOnePoints = match.score.getGamePoints(TeamIndex.T_ONE, index);
      final tTwoPoints = match.score.getGamePoints(TeamIndex.T_TWO, index);
      isWinner =
          row == 0 ? (tOnePoints > tTwoPoints) : (tTwoPoints > tOnePoints);
    }
    // and return all the data collected
    return MatchScoreSummaryItem(
        score: points, title: title, isWinner: isWinner);
  }
}
