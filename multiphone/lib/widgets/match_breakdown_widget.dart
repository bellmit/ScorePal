import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/tennis/tennis_score.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/active_setup.dart';

import 'common/common_widgets.dart';

class MatchBreakdownWidget extends StatelessWidget {
  final ActiveMatch match;
  const MatchBreakdownWidget({
    Key key,
    @required this.match,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final values = Values(context);
    return Card(
      color: theme.secondaryHeaderColor,
      margin: const EdgeInsets.all(Values.default_space),
      child: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(Values.default_space),
            child: TextHeadingWidget(
              values.strings.results_breakdown,
              isOnBackground: true,
            ),
          ),
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Flexible(
                  child: teamTitle(
                      match.getSetup().getTeamName(TeamIndex.T_ONE, context))),
              Padding(
                padding: const EdgeInsets.only(
                    left: Values.default_space, right: Values.default_space),
                child: teamTitle(values.strings.playing, isOnBackground: false),
              ),
              Flexible(
                  child: teamTitle(
                      match.getSetup().getTeamName(TeamIndex.T_TWO, context))),
            ],
          ),
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Flexible(
                  child: teamTitle(match.score
                      .getPointsTotal(0, TeamIndex.T_ONE)
                      .toString())),
              Padding(
                padding: const EdgeInsets.only(
                    left: Values.default_space, right: Values.default_space),
                child: teamTitle(values.strings.total_points,
                    isOnBackground: false),
              ),
              Flexible(
                  child: teamTitle(match.score
                      .getPointsTotal(0, TeamIndex.T_TWO)
                      .toString())),
            ],
          ),
          if (match.score is TennisScore)
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Flexible(
                    child: teamTitle(
                        breakPointsText(match.score, TeamIndex.T_ONE))),
                Padding(
                  padding: const EdgeInsets.only(
                      left: Values.default_space, right: Values.default_space),
                  child: teamTitle(values.strings.break_points,
                      isOnBackground: false),
                ),
                Flexible(
                    child: teamTitle(
                        breakPointsText(match.score, TeamIndex.T_TWO))),
              ],
            ),
          if (match.score is TennisScore)
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Flexible(
                    child: teamTitle(
                        deucePointsText(match.score, TeamIndex.T_ONE))),
                Padding(
                  padding: const EdgeInsets.only(
                      left: Values.default_space, right: Values.default_space),
                  child: teamTitle(values.strings.receiver_deuce_points,
                      isOnBackground: false),
                ),
                Flexible(
                    child: teamTitle(
                        deucePointsText(match.score, TeamIndex.T_TWO))),
              ],
            ),
        ],
      ),
    );
  }

  Widget teamTitle(String title, {isOnBackground = true}) => Padding(
        padding: const EdgeInsets.only(bottom: Values.default_space + .5),
        child: TextWidget(title, isOnBackground: isOnBackground),
      );

  Widget teamColumn(BuildContext context, TeamIndex team) => Padding(
        padding: const EdgeInsets.all(8.0),
        child: Column(
          crossAxisAlignment: team == TeamIndex.T_ONE
              ? CrossAxisAlignment.end
              : CrossAxisAlignment.start,
          children: [
            teamTitle(match.getSetup().getTeamName(team, context)),
            teamTitle(match.score.getPointsTotal(0, team).toString()),
            if (match.score is TennisScore) ...[
              teamTitle(breakPointsText(match.score, team)),
              teamTitle(deucePointsText(match.score, team)),
            ],
          ],
        ),
      );

  String breakPointsText(TennisScore score, TeamIndex team) =>
      '${score.getBreakPointsConverted(team)} / ${score.getBreakPoints(team)}';

  String deucePointsText(TennisScore score, TeamIndex team) =>
      '${score.getReceivingDeucePoints(team)}';
}
