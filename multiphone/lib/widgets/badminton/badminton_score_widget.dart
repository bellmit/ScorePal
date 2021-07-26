import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/badminton/badminton_match.dart';
import 'package:multiphone/match/badminton/badminton_score.dart';
import 'package:multiphone/match/point.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/widgets/score_widget.dart';
import 'package:provider/provider.dart';

class BadmintonScoreWidget extends ScoreWidget {
  final void Function(int level) onScoreClicked;
  final Point games;
  final Point points;
  final bool isServing;
  const BadmintonScoreWidget({
    Key key,
    @required this.games,
    @required this.points,
    @required this.isServing,
    this.onScoreClicked,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    BadmintonMatch match = Provider.of<ActiveMatch>(context, listen: false);
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
                Values(context).strings.title_badminton_games,
                games.displayString(context),
                false,
                () => onScoreClicked(BadmintonScore.LEVEL_GAME),
              ),
            ],
          ),
        ),
        Flexible(
          flex: 2,
          child: createBox(
            context,
            Values(context).strings.title_badminton_points,
            points.displayString(context),
            isServing,
            () => onScoreClicked(BadmintonScore.LEVEL_POINT),
          ),
        ),
      ],
    );
  }
}
