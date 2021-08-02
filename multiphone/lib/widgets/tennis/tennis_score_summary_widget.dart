import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/tennis/tennis_match.dart';
import 'package:multiphone/match/tennis/tennis_score.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/widgets/match_score_summary_widget.dart';

class TennisScoreSummaryWidget extends MatchScoreSummaryWidget {
  final TennisMatch match;
  const TennisScoreSummaryWidget({
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
    var scoreCount = match.getPlayedSets();
    // so that is the sets we played and completed... are we playing one?
    if (match.getPlayedGames(scoreCount) > 0) {
      // there are some games in the latest set
      ++scoreCount;
    }
    if (!match.score.isMatchOver(isCheckConceded: false)) {
      // the match isn't over - we have some points to show too
      ++scoreCount;
    }
    return scoreCount;
  }

  @override
  MatchScoreSummaryItem getScoreItem(BuildContext context, int index, int row) {
    var setIndex = index;
    String points = '';
    bool isWinner;
    String title = '';
    final values = Values(context);
    if (!match.score.isMatchOver(isCheckConceded: false)) {
      // the match is over - we are showing the first col as points
      if (index == 0) {
        // just return the points for the current match (correct team)
        points = match
            .getDisplayPoint(TennisScore.LEVEL_POINT,
                row == 0 ? TeamIndex.T_ONE : TeamIndex.T_TWO)
            .displayString(context);
        // and show that these are points
        title = row == 0 ? values.strings.title_tennis_points : '';
      }
      // the set index is one lower than the score index then
      --setIndex;
    }
    if (points.isEmpty) {
      // return the games for each set
      points = match.score
          .getGames(row == 0 ? TeamIndex.T_ONE : TeamIndex.T_TWO, setIndex)
          .toString();
      // and we want to show the winner of this set
      if (setIndex < match.score.getPlayedSets()) {
        // only showing the winner of the sets
        final tOneGames = match.score.getGames(TeamIndex.T_ONE, setIndex);
        final tTwoGames = match.score.getGames(TeamIndex.T_TWO, setIndex);
        isWinner = row == 0 ? (tOneGames > tTwoGames) : (tTwoGames > tOneGames);
      }
    }
    if (title.isEmpty) {
      // by default the title is constructed to be the set title
      if (row == 0) {
        title =
            values.construct(values.strings.display_set_number, [setIndex + 1]);
      } else if (match.score.isSetTieBreak(setIndex)) {
        // the title is about the tie-break that occurred
        final setPoints = match.score
            .getSetPoints(setIndex, match.score.getPlayedGames(setIndex) - 1);
        title = values.construct(values.strings.tie_display, [
          setPoints.first,
          setPoints.last,
        ]);
      }
    }
    // and return all the data collected
    return MatchScoreSummaryItem(
        score: points, title: title, isWinner: isWinner);
  }
}
