import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/point.dart';
import 'package:multiphone/providers/active_match.dart';

import 'common/common_widgets.dart';

class ScoreHeadlineWidget extends StatelessWidget {
  final ActiveMatch match;
  const ScoreHeadlineWidget({Key key, @required this.match}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    // let's calculate all the data we need from the match here then
    final setup = match.getSetup();
    final winner = match.getMatchWinner();
    final loser = setup.getOtherTeam(winner);
    final values = Values(context);
    final theme = Theme.of(context);
    // need to find the top level scores ATM
    Point winnerPoint, loserPoint;
    int level = 0;
    for (int i = 0; i < match.getScoreLevels(); ++i) {
      winnerPoint = match.getDisplayPoint(i, winner);
      loserPoint = match.getDisplayPoint(i, loser);
      if (null != winnerPoint &&
          null != loserPoint &&
          (winnerPoint.val() > 0 || loserPoint.val() > 0)) {
        // we have two display points and one of them isn't zero, don't go lower
        level = i;
        break;
      }
    }
    // show the results of this match currently
    final winnerTitle = setup.getTeamName(winner, context);
    final loserTitle = setup.getTeamName(loser, context);
    final playedLevel = match.getLevelTitle(level, context);
    final winnerScore = null != winnerPoint
        ? winnerPoint.displayString(context)
        : values.strings.display_zero;
    final loserScore = null != loserPoint
        ? loserPoint.displayString(context)
        : values.strings.display_zero;

    return Container(
      decoration: BoxDecoration(
        color: theme.secondaryHeaderColor,
        border: Border.all(
            color: theme.primaryColorDark, width: Values.border_width),
        borderRadius: BorderRadius.all(Radius.circular(Values.default_radius)),
      ),
      child: Row(
        children: [
          Expanded(
            child: Padding(
              padding: EdgeInsets.only(
                  left: Values.default_space, right: Values.default_space),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.end,
                children: [
                  TextWidget(winnerTitle,
                      isOnBackground: true, isLimitOverflow: true),
                  TextWidget(winnerScore, isOnBackground: true),
                ],
              ),
            ),
          ),
          Column(
            children: [
              TextWidget(
                match.isMatchOver()
                    ? Values(context).strings.match_beat
                    : values.strings.match_beating,
                isOnBackground: true,
              ),
              TextWidget(playedLevel, isOnBackground: true),
            ],
          ),
          Expanded(
            child: Padding(
              padding: EdgeInsets.only(
                  left: Values.default_space, right: Values.default_space),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  TextWidget(loserTitle,
                      isOnBackground: true, isLimitOverflow: true),
                  TextWidget(loserScore, isOnBackground: true),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}
