import 'package:flutter/material.dart';
import 'package:multiphone/helpers/log.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/point.dart';
import 'package:multiphone/match/tennis/tennis_score.dart';
import 'package:multiphone/widgets/score_widget.dart';

class TennisScoreWidget extends ScoreWidget {
  final void Function(int level) onScoreClicked;
  final Point sets;
  final Point games;
  final Point points;
  final bool isServing;
  const TennisScoreWidget({
    Key key,
    @required this.sets,
    @required this.games,
    @required this.points,
    @required this.isServing,
    this.onScoreClicked,
  }) : super(key: key);

  @override
  Widget scoreColumn(BuildContext context) => Column(
        children: [
          Flexible(
            flex: 1,
            child: Row(
              children: [
                Flexible(
                  flex: 1,
                  child: Align(
                    alignment: Alignment.bottomRight,
                    child: tennisSets(context),
                  ),
                ),
                Flexible(
                  flex: 1,
                  child: Align(
                    alignment: Alignment.bottomLeft,
                    child: tennisGames(context),
                  ),
                ),
              ],
            ),
          ),
          Flexible(
            flex: 2,
            child: Center(
              child: tennisPoints(context),
            ),
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
                  child: tennisSets(context),
                ),
                Flexible(
                  flex: 1,
                  child: tennisGames(context),
                ),
              ],
            ),
          ),
          Flexible(
            flex: 2,
            child: Center(
              child: tennisPoints(context),
            ),
          ),
        ],
      );

  Widget tennisSets(BuildContext context) => createBox(
        context,
        Values(context).strings.title_tennis_sets,
        sets.displayString(context),
        false,
        'images/svg/tennis-ball-large.svg',
        null, //() => onScoreClicked(TennisScore.LEVEL_SET),
      );

  Widget tennisGames(BuildContext context) => createBox(
        context,
        Values(context).strings.title_tennis_games,
        games.displayString(context),
        false,
        'images/svg/tennis-ball-large.svg',
        () => onScoreClicked(TennisScore.LEVEL_GAME),
      );

  Widget tennisPoints(BuildContext context) => createBox(
        context,
        Values(context).strings.title_tennis_points,
        points.displayString(context),
        isServing,
        'images/svg/tennis-ball-large.svg',
        () => onScoreClicked(TennisScore.LEVEL_POINT),
      );
}
