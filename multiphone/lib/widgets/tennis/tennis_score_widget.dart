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
  Widget build(BuildContext context) {
    final mediaQuery = MediaQuery.of(context);
    final screenSize = mediaQuery.size;
    var isShowSetsAbove;
    if (mediaQuery.orientation == Orientation.landscape) {
      // we have half the width to draw, if the height is > half the width, draw over
      isShowSetsAbove = screenSize.height - Values.team_names_widget_height >
          0.5 * screenSize.width;
    } else {
      // we have half the height to draw, if that height > the width, draw over
      isShowSetsAbove =
          (screenSize.height * 0.5) - (Values.team_names_widget_height * 2) >
              screenSize.width;
    }
    // and return the correct widget
    return isShowSetsAbove
        ? setsOverPoints(context)
        : setsBeforePoints(context);
  }

  Widget setsOverPoints(BuildContext context) => Column(
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

  Widget setsBeforePoints(BuildContext context) => Row(
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
