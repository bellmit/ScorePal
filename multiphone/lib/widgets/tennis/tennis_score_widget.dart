import 'package:flutter/material.dart';
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
    return Row(
      children: [
        // split the row into the smaller controls
        Flexible(
          flex: 1,
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              createBox(
                context,
                Values(context).strings.title_tennis_sets,
                sets.displayString(context),
                false,
                null, //() => onScoreClicked(TennisScore.LEVEL_SET),
              ),
              createBox(
                context,
                Values(context).strings.title_tennis_games,
                games.displayString(context),
                false,
                () => onScoreClicked(TennisScore.LEVEL_GAME),
              ),
            ],
          ),
        ),
        Flexible(
          flex: 2,
          child: createBox(
            context,
            Values(context).strings.title_tennis_points,
            points.displayString(context),
            isServing,
            () => onScoreClicked(TennisScore.LEVEL_POINT),
          ),
        ),
      ],
    );
  }
}
