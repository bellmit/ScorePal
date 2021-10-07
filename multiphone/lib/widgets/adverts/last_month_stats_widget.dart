import 'package:fl_chart/fl_chart.dart';
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/match_stats_month.dart';
import 'package:multiphone/widgets/common/common_widgets.dart';

class LastMonthStatsWidget extends StatelessWidget {
  final MatchStatsMonth stats;
  const LastMonthStatsWidget({@required this.stats, Key key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final values = Values(context);
    final theme = Theme.of(context);
    final media = MediaQuery.of(context);
    final cardWidth = media.orientation == Orientation.landscape
        ? media.size.width * 0.4
        : media.size.width * 0.9;
    return Card(
      margin: const EdgeInsets.all(Values.default_space),
      child: ConstrainedBox(
        constraints: BoxConstraints.loose(Size.fromWidth(cardWidth)),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Flexible(
              flex: 1,
              child: AspectRatio(
                aspectRatio: 1,
                child: Padding(
                  padding: const EdgeInsets.all(Values.default_space),
                  child: PieChart(
                    PieChartData(
                        borderData: FlBorderData(
                          show: false,
                        ),
                        sectionsSpace: 0,
                        centerSpaceRadius: Values.default_space,
                        startDegreeOffset: 45,
                        sections: [
                          PieChartSectionData(
                            color: theme.primaryColorDark,
                            value: stats.losses.toDouble(),
                            title: stats.losses >= stats.wins
                                ? values.strings.title_losses
                                : values.strings.title_even,
                            //title: '${(stats.losses / stats.played * 100).round()}%',
                            titleStyle: TextStyle(
                                color: theme.accentTextTheme.button.color),
                          ),
                          PieChartSectionData(
                            color: theme.accentColor,
                            value: stats.wins.toDouble(),
                            title: stats.losses <= stats.wins
                                ? values.strings.title_wins
                                : values.strings.title_even,
                            //title: '${(stats.wins / stats.played * 100).round()}%',
                            titleStyle: TextStyle(
                                color: theme.accentTextTheme.button.color),
                          ),
                        ]),
                  ),
                ),
              ),
            ),
            Expanded(
              flex: 3,
              child: Padding(
                padding: const EdgeInsets.all(Values.default_space),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  mainAxisAlignment: MainAxisAlignment.start,
                  children: [
                    TextSubheadingWidget(
                      values.construct(values.strings.description_last_month,
                          [DateFormat.yMMM().format(stats.date)]),
                    ),
                    TextWidget(
                      values.construct(
                        values.strings.description_wins_losses,
                        [
                          stats.played,
                          stats.wins,
                          stats.losses,
                        ],
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
