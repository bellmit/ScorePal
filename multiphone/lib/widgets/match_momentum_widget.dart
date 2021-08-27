import 'dart:math';

import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/match_writer.dart';
import 'package:multiphone/match/score_history.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:fl_chart/fl_chart.dart';
import 'package:multiphone/widgets/common/icon_button_widget.dart';

import 'common/common_widgets.dart';

class MatchMomentumWidget extends StatefulWidget {
  final ActiveMatch match;
  const MatchMomentumWidget({
    Key key,
    @required this.match,
  }) : super(key: key);

  @override
  _MatchMomentumWidgetState createState() => _MatchMomentumWidgetState(match);
}

class _MatchMomentumWidgetState extends State<MatchMomentumWidget> {
  TeamIndex _activeTeam = TeamIndex.T_ONE;

  int noPoints = 0;
  List<int> maxValue;
  List<int> minValue;
  int noLevels = 1;
  int highestLevel = 0;

  List<Color> gradientColors() => [
        Theme.of(context).primaryColorDark,
        Theme.of(context).primaryColor,
      ];

  final List<FlSpot> teamOneGraphPoints = [];
  final List<FlSpot> teamTwoGraphPoints = [];

  final List<HistoryValue> pointHistory = [];

  _MatchMomentumWidgetState(ActiveMatch match) {
    // calculate the data from the match
    final winnersHistory = match.getWinnersHistory();
    final straightPointsToWin = match.getSetup().getStraightPointsToWin();

    // set the number of points
    noPoints = 0;
    for (HistoryValue value in winnersHistory) {
      // it is usually one point per point, but there might be more for different levels
      noPoints += straightPointsToWin[value.level];
    }
    // reset our starting data points
    maxValue = [0, 0];
    minValue = [0, 0];
    highestLevel = 1;
    noLevels = straightPointsToWin.length;
    // transfer the data to our member list to draw later
    pointHistory.clear();
    teamOneGraphPoints.clear();
    teamTwoGraphPoints.clear();
    List<int> pointSway = [0, 0];
    // add the new data while calculating the data ranges from it
    for (int i = 0; i < winnersHistory.length; ++i) {
      // it is usually one point per point, but there might be more for different levels
      int pointsToAdd = straightPointsToWin[winnersHistory[i].level];
      for (int j = 0; j < pointsToAdd; ++j) {
        // add the value to our list for each point the level represents, level being 0 (repeated) use the state only on the lat
        HistoryValue historyValue = HistoryValue(winnersHistory[i].team, 0, 0);
        if (j == pointsToAdd - 1) {
          // this is the last item of our constructed points, use the state and top level etc for this actual point
          historyValue.state = winnersHistory[i].state;
          // and copy of the top level and the string
          historyValue.topLevelChanged = winnersHistory[i].topLevelChanged;
          historyValue.scoreString = winnersHistory[i].scoreString;
        }
        // and add the copy to our list
        this.pointHistory.add(historyValue);
        // and calculate the thresholds
        if (winnersHistory[i].team == TeamIndex.T_ONE) {
          // point to team one
          ++pointSway[0];
          --pointSway[1];
        } else {
          // point to team two
          --pointSway[0];
          ++pointSway[1];
        }
        // this is a new spot of data to show on the graph
        teamOneGraphPoints.add(FlSpot(i.toDouble(), pointSway[0].toDouble()));
        teamTwoGraphPoints.add(FlSpot(i.toDouble(), pointSway[1].toDouble()));

        // also measure the min and max values we are going to show
        minValue[0] = min(pointSway[0], minValue[0]);
        maxValue[0] = max(pointSway[0], maxValue[0]);
        // and team two
        minValue[1] = min(pointSway[1], minValue[1]);
        maxValue[1] = max(pointSway[1], maxValue[1]);

        // and the highest level of points encountered
        highestLevel = max(historyValue.topLevelChanged, highestLevel);
      }
    }
  }

  LineChartData _chartData(TeamIndex team) => LineChartData(
        gridData: FlGridData(
          show: true,
          horizontalInterval: 1,
          drawHorizontalLine: true,
          checkToShowHorizontalLine: (value) => value % 10.0 == 0.0,
          getDrawingHorizontalLine: (value) {
            if (value == 0.0) {
              // this is the zero line, draw big
              return FlLine(
                color: Theme.of(context).primaryColorDark,
                strokeWidth: 4,
              );
            } else {
              return FlLine(
                color: Theme.of(context).primaryColorLight,
                strokeWidth: 1,
              );
            }
          },
          drawVerticalLine: true,
          getDrawingVerticalLine: (value) {
            return FlLine(
              color: Theme.of(context).primaryColorLight,
              strokeWidth:
                  pointHistory[value.floor()].topLevelChanged.toDouble(),
            );
          },
          checkToShowVerticalLine: (value) =>
              this.pointHistory[value.floor()].topLevelChanged >= highestLevel,
        ),
        titlesData: FlTitlesData(
          show: true,
          bottomTitles: SideTitles(
            showTitles: true,
            getTextStyles: (context, value) =>
                Theme.of(context).textTheme.caption,
            getTitles: (value) {
              final historyValue = this.pointHistory[value.floor()];
              if (historyValue.topLevelChanged >= highestLevel) {
                final dataObject =
                    MatchWriter.parseScoreString(historyValue.scoreString);
                var title;
                if (team == TeamIndex.T_ONE) {
                  title =
                      '${dataObject['level']} ${dataObject['team_one_score']} - ${dataObject['team_two_score']}';
                } else {
                  title =
                      '${dataObject['level']} ${dataObject['team_two_score']} - ${dataObject['team_one_score']}';
                }
                /*
                if (value.floor() >= noPoints - 1) {
                  // this is the last value - can't right align so add spaces instead
                  title += ' match end';
                }
                Log.info(title);*/
                return title;
              } else {
                return null;
              }
            },
            margin: Values.default_space,
          ),
          leftTitles: SideTitles(
            showTitles: true,
            interval: 1,
            getTextStyles: (context, value) =>
                Theme.of(context).textTheme.caption,
            getTitles: (value) {
              if (value.abs() % 10 == 0) {
                return '${value.floor()}';
              } else {
                return null;
              }
            },
            //reservedSize: 28,
            margin: Values.default_space,
          ),
        ),
        borderData: FlBorderData(
            show: true,
            border: Border.all(
                color: Theme.of(context).secondaryHeaderColor,
                width: Values.border_width)),
        minX: 0,
        maxX: noPoints.toDouble() - 1.0,
        minY: minValue[team.index].toDouble(),
        maxY: maxValue[team.index].toDouble(),
        lineBarsData: [
          LineChartBarData(
            spots: team == TeamIndex.T_ONE
                ? teamOneGraphPoints
                : teamTwoGraphPoints,
            isCurved: true,
            colors: gradientColors(),
            barWidth: 3,
            isStrokeCapRound: true,
            dotData: FlDotData(
                checkToShowDot: (spot, data) =>
                    pointHistory[spot.x.floor()].topLevelChanged > 0),
            belowBarData: BarAreaData(
              show: true,
              colors: gradientColors()
                  .map((color) => color.withOpacity(0.3))
                  .toList(),
            ),
          ),
        ],
      );

  void _switchActiveTeam() {
    setState(() {
      // swap to the other team
      _activeTeam = widget.match.getSetup().getOtherTeam(_activeTeam);
    });
  }

  @override
  Widget build(BuildContext context) {
    final values = Values(context);
    final setup = widget.match.getSetup();
    return AspectRatio(
      aspectRatio: 1.70,
      child: Padding(
        padding: const EdgeInsets.all(Values.default_space),
        child: Container(
          decoration: BoxDecoration(
              borderRadius: BorderRadius.all(
                Radius.circular(Values.default_radius),
              ),
              color: Theme.of(context).secondaryHeaderColor),
          child: Column(
            children: [
              Padding(
                padding: const EdgeInsets.all(Values.default_space),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    IconButtonWidget(
                      _switchActiveTeam,
                      null,
                      setup.getTeamName(_activeTeam, context),
                    ),
                    Padding(
                      padding: const EdgeInsets.all(Values.default_space),
                      child: TextWidget(values.strings.match_momentum,
                          isOnBackground: true),
                    ),
                  ],
                ),
              ),
              Expanded(
                child: Padding(
                  padding: const EdgeInsets.all(Values.default_space),
                  child: LineChart(_chartData(_activeTeam)),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
