import 'package:flutter/material.dart';
import 'package:multiphone/helpers/log.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/score_history.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/widgets/common/common_widgets.dart';
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
          final List<HistoryValue> matchHistory =
              activeMatch == null ? [] : activeMatch.getWinnersHistory();
          final setup = activeMatch == null ? null : activeMatch.getSetup();
          final itemCount = matchHistory.length;
          Log.info('showing ${matchHistory.length} historic items');
          return Container(
            width: double.infinity,
            height: double.infinity,
            child: Column(
              children: [
                // summarise the score
                Card(
                  child: activeMatch
                      .getSport()
                      .createScoreSummaryWidget(ctx, activeMatch),
                ),
                // and show the history of this match
                Expanded(
                  child: ListView.builder(
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
                                  padding: const EdgeInsets.only(
                                      right: Values.default_space),
                                  child: const IconWidget(Icons.delete),
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
                                activeMatch, setup, history, context, values),
                          );
                        } else {
                          return _historyTile(
                              activeMatch, setup, history, context, values);
                        }
                      }),
                ),
              ],
            ),
          );
        },
      ),
    );
  }

  Widget _historyTile(ActiveMatch activeMatch, ActiveSetup setup,
          HistoryValue history, BuildContext context, Values values) =>
      ListTile(
        leading: IconSvgWidget('player-serving'),
        title: TextWidget(values.construct(values.strings.history_point_explain,
            [setup.getTeamName(history.team, context)])),
        subtitle: TextWidget(history.scoreString),
      );
}
