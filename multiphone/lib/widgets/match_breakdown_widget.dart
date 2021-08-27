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
            padding: const EdgeInsets.only(top: Values.default_space),
            child: TextHeadingWidget(
              values.strings.results_breakdown,
              isOnBackground: true,
            ),
          ),
          Row(
            children: [
              Expanded(
                child: teamColumn(context, TeamIndex.T_ONE),
              ),
              Column(
                children: [
                  teamTitle(''),
                  TextWidget(values.strings.total_points, isOnBackground: true),
                  TextWidget(values.strings.break_points, isOnBackground: true),
                ],
              ),
              Expanded(
                child: teamColumn(context, TeamIndex.T_TWO),
              )
            ],
          ),
        ],
      ),
    );
  }

  Widget teamTitle(String title) => Padding(
        padding: const EdgeInsets.only(bottom: Values.default_space + .5),
        child: TextWidget(title),
      );

  Widget teamColumn(BuildContext context, TeamIndex team) => Padding(
        padding: const EdgeInsets.all(8.0),
        child: Column(
          crossAxisAlignment: team == TeamIndex.T_ONE
              ? CrossAxisAlignment.end
              : CrossAxisAlignment.start,
          children: [
            teamTitle(match.getSetup().getTeamName(team, context)),
            TextWidget(match.score.getPointsTotal(0, team).toString(),
                isOnBackground: true),
            if (match.score is TennisScore)
              TextWidget(breakPointsTextWidget(match.score, team),
                  isOnBackground: true),
          ],
        ),
      );

  String breakPointsTextWidget(TennisScore score, TeamIndex team) =>
      '${score.getBreakPointsConverted(team)} / ${score.getBreakPoints(team)}';
}
