import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/tennis/tennis_match.dart';
import 'package:multiphone/match/tennis/tennis_match_setup.dart';
import 'package:multiphone/match/tennis/tennis_score.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/widgets/match_score_summary.dart';

class TennisScoreSummaryWidget extends MatchScoreSummary {
  final TennisMatch match;
  const TennisScoreSummaryWidget({
    Key key,
    @required this.match,
    @required teamOneName,
    @required teamTwoName,
  }) : super(
          key: key,
          teamOneName: teamOneName,
          teamTwoName: teamTwoName,
        );

  @override
  int getScoreCount() {
    // if we haven't finished, there are points currently in play
    return match.getPlayedSets() + (!match.score.isMatchOver() ? 1 : 0);
    return TennisMatchSetup.setsValue(match.getSetup().sets) +
        (!match.score.isMatchOver() ? 1 : 0);
  }

  @override
  String getScore(BuildContext context, int index, int row) {
    var setIndex = index;
    if (!match.score.isMatchOver()) {
      // want the points currently in play for this row
      if (index == 0) {
        // just return the points for the current match (correct team)
        return match
            .getDisplayPoint(TennisScore.LEVEL_POINT,
                row == 0 ? TeamIndex.T_ONE : TeamIndex.T_TWO)
            .displayString(context);
      }
      // the set index is one lower than the score index then
      --setIndex;
    }
    // return the games for each set
    return match.score
        .getGames(row == 0 ? TeamIndex.T_ONE : TeamIndex.T_TWO, setIndex)
        .toString();
  }

  @override
  String getScoreTitle(BuildContext context, int index, int row) {
    final values = Values(context);
    var setIndex = index;
    if (!match.score.isMatchOver()) {
      if (index == 0) {
        return row == 0 ? values.strings.points : '';
      }
      // the set index is one lower than the score index then
      --setIndex;
    }
    if (row == 0) {
      return values
          .construct(values.strings.display_set_number, [setIndex + 1]);
    } else if (match.score.isSetTieBreak(setIndex)) {
      // this was a tie set
      final setPoints = match.score
          .getSetPoints(setIndex, match.score.getPlayedGames(setIndex) - 1);
      return values.construct(values.strings.tie_display, [
        setPoints.first,
        setPoints.last,
      ]);
    }
    return '';
  }
}
