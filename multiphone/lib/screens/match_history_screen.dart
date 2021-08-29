import 'package:flutter/material.dart';
import 'package:multiphone/helpers/log.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/match_writer.dart';
import 'package:multiphone/match/score_history.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/widgets/common/common_widgets.dart';
import 'package:multiphone/widgets/common/icon_button_widget.dart';
import 'package:provider/provider.dart';

class MatchHistoryScreen extends StatefulWidget {
  static const String routeName = '/match-history';

  MatchHistoryScreen();

  @override
  _MatchHistoryScreenState createState() => _MatchHistoryScreenState();
}

class _MatchHistoryScreenState extends State<MatchHistoryScreen> {
  void _undoLastHistoryItem(ActiveMatch match) {
    // just let the match do it's thing which will cause a redraw the contents of this view
    match.undoLastPoint();
  }

  @override
  Widget build(BuildContext context) {
    // get the values to get strings from
    var values = Values(context);
    // and return the scaffold
    return Scaffold(
      appBar: AppBar(
        title: TextWidget(values.strings.match_history),
      ),
      body: Consumer<ActiveMatch>(
        builder: (ctx, activeMatch, child) {
          return Container(
            width: double.infinity,
            height: double.infinity,
            child: OrientationBuilder(builder: (ctx, orientation) {
              if (orientation == Orientation.landscape) {
                // two columns
                return Row(
                  children: [
                    // summarise the score
                    Flexible(
                      flex: 3,
                      child: _historyList(activeMatch, values),
                    ),
                    // and show the history of this match
                    Flexible(
                      flex: 5,
                      child: Card(
                        child: activeMatch
                            .getSport()
                            .createScoreSummaryWidget(ctx, activeMatch),
                      ),
                    ),
                  ],
                );
              } else {
                // one column
                return Column(
                  children: [
                    // summarise the score
                    Card(
                      child: activeMatch
                          .getSport()
                          .createScoreSummaryWidget(ctx, activeMatch),
                    ),
                    // and show the history of this match
                    Expanded(
                      child: _historyList(activeMatch, values),
                    ),
                  ],
                );
              }
            }),
          );
        },
      ),
    );
  }

  Widget _historyList(ActiveMatch activeMatch, Values values) {
    final List<HistoryValue> matchHistory =
        activeMatch == null ? [] : activeMatch.getWinnersHistory();
    final setup = activeMatch == null ? null : activeMatch.getSetup();
    final itemCount = matchHistory.length;
    return ListView.builder(
      itemCount: itemCount,
      itemBuilder: (ctx, index) {
        final historyIndex = (itemCount - 1) - index;
        final history = matchHistory[historyIndex];
        if (index == 0) {
          // the last one, let them throw them away one at a time
          return Dismissible(
            direction: DismissDirection.endToStart,
            background: Container(
              color: Values.deleteColor,
              child: const Align(
                alignment: Alignment.centerRight,
                child: const Padding(
                  padding: const EdgeInsets.only(right: Values.default_space),
                  child: const IconWidget(Icons.undo),
                ),
              ),
            ),
            // Each Dismissible must contain a Key. Keys allow Flutter to
            // uniquely identify widgets.
            key: Key('history_value_$historyIndex'),
            // Provide a function that tells the app
            // what to do after an item has been swiped away.
            onDismissed: (direction) {
              _undoLastHistoryItem(activeMatch);
            },
            child: _historyTile(
                activeMatch, setup, history, context, values, true),
          );
        } else {
          return _historyTile(
              activeMatch, setup, history, context, values, false);
        }
      },
    );
  }

  Widget _historyTile(
          ActiveMatch activeMatch,
          ActiveSetup setup,
          HistoryValue history,
          BuildContext context,
          Values values,
          bool isShowUndo) =>
      ListTile(
        leading: IconWidget(Icons.person_add),
        title: Row(
          children: [
            Expanded(
              child: TextWidget(
                values.construct(values.strings.history_point_explain,
                    [setup.getTeamName(history.team, context)]),
                isLimitOverflow: false,
              ),
            ),
            if (isShowUndo)
              IconButtonWidget(
                  () => _undoLastHistoryItem(activeMatch), Icons.undo, 'undo'),
          ],
        ),
        subtitle: Card(
          color: Theme.of(context).secondaryHeaderColor,
          margin: const EdgeInsets.all(Values.default_space),
          child: Column(
            children: [
              Row(
                children: [
                  Expanded(
                    child: Column(
                      children: [
                        teamTitle(setup.getTeamName(TeamIndex.T_ONE, context)),
                        TextWidget(
                            MatchWriter.parseScoreString(
                                history.scoreString)['team_one_score'],
                            isOnBackground: true),
                      ],
                    ),
                  ),
                  Column(
                    children: [
                      teamTitle(''),
                      TextWidget(
                          activeMatch.getLevelTitle(history.level, context),
                          isOnBackground: true),
                    ],
                  ),
                  Expanded(
                    child: Column(
                      children: [
                        teamTitle(setup.getTeamName(TeamIndex.T_TWO, context)),
                        TextWidget(
                            MatchWriter.parseScoreString(
                                history.scoreString)['team_two_score'],
                            isOnBackground: true),
                      ],
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      );

  Widget teamTitle(String title) => Padding(
        padding: const EdgeInsets.only(bottom: Values.default_space + .5),
        child: TextWidget(title),
      );
}
