import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/tennis/tennis_score.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/active_setup.dart';

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
      color: theme.primaryColorDark,
      margin: const EdgeInsets.all(Values.default_space),
      child: Column(
        children: [
          Padding(
            padding: const EdgeInsets.only(top: Values.default_space),
            child: Text(
              values.strings.results_breakdown,
              style: TextStyle(
                fontSize: Values.font_size_title,
                fontWeight: FontWeight.bold,
                color: Values.primaryTextColor,
              ),
            ),
          ),
          Row(
            children: [
              Expanded(
                child: teamColumn(context, TeamIndex.T_ONE, theme.accentColor),
              ),
              Column(
                children: [
                  teamTitle(''),
                  detailsText(
                      values.strings.total_points, Values.primaryTextColor),
                  detailsText(
                      values.strings.break_points, Values.primaryTextColor),
                ],
              ),
              Expanded(
                child: teamColumn(context, TeamIndex.T_TWO, theme.accentColor),
              )
            ],
          ),
        ],
      ),
    );
  }

  Widget teamTitle(String title) => Padding(
        padding: const EdgeInsets.only(bottom: Values.default_space + .5),
        child: Text(
          title,
          style: const TextStyle(fontSize: Values.font_size_title),
        ),
      );

  Widget detailsText(String text, Color color) => Text(text,
      style: TextStyle(
        color: color,
      ));

  Widget teamColumn(BuildContext context, TeamIndex team, Color color) =>
      Padding(
        padding: const EdgeInsets.all(8.0),
        child: Column(
          crossAxisAlignment: team == TeamIndex.T_ONE
              ? CrossAxisAlignment.end
              : CrossAxisAlignment.start,
          children: [
            teamTitle(match.getSetup().getTeamName(team, context)),
            detailsText(match.score.getPointsTotal(0, team).toString(), color),
            if (match.score is TennisScore)
              detailsText(breakPointsText(match.score, team), color),
          ],
        ),
      );

  String breakPointsText(TennisScore score, TeamIndex team) =>
      '${score.getBreakPointsConverted(team)} / ${score.getBreakPoints(team)}';
}
