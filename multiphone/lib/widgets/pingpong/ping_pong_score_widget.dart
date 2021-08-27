import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/ping_pong/ping_pong_score.dart';
import 'package:multiphone/match/point.dart';
import 'package:multiphone/widgets/score_widget.dart';

class PingPongScoreWidget extends ScoreWidget {
  final void Function(int level) onScoreClicked;
  final Point rounds;
  final Point points;
  final bool isServing;

  const PingPongScoreWidget({
    Key key,
    @required this.rounds,
    @required this.points,
    @required this.isServing,
    this.onScoreClicked,
  }) : super(key: key);

  @override
  Widget scoreColumn(BuildContext context) => Column(
        children: [
          // split the row into the smaller controls
          Flexible(
            flex: 1,
            child: Center(child: pingPongRounds(context)),
          ),
          Flexible(
            flex: 2,
            child: Center(child: pingPongPoints(context)),
          ),
        ],
      );

  @override
  Widget scoreRow(BuildContext context) => Row(
        children: [
          // split the row into the smaller controls
          Flexible(
            flex: 1,
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                Flexible(
                  flex: 1,
                  child: pingPongRounds(context),
                ),
              ],
            ),
          ),
          Flexible(
            flex: 2,
            child: Center(
              child: pingPongPoints(context),
            ),
          ),
        ],
      );

  Widget pingPongRounds(BuildContext context) => createBox(
        context,
        Values(context).strings.title_ping_pong_rounds,
        rounds.displayString(context),
        false,
        'ping-pong-ball-large',
        () => onScoreClicked(PingPongScore.LEVEL_ROUND),
      );

  Widget pingPongPoints(BuildContext context) => createBox(
        context,
        Values(context).strings.title_ping_pong_points,
        points.displayString(context),
        isServing,
        'ping-pong-ball-large',
        () => onScoreClicked(PingPongScore.LEVEL_POINT),
      );
}
