import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/tennis/tennis_match.dart';
import 'package:multiphone/match/tennis/tennis_score.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/widgets/score_widget.dart';
import 'package:provider/provider.dart';

class TennisScoreWidget extends ScoreWidget {
  final void Function(int level) onScoreClicked;
  const TennisScoreWidget({
    Key key,
    @required TeamIndex team,
    this.onScoreClicked,
  }) : super(key: key, team: team);

  @override
  Widget build(BuildContext context) {
    // get the active match running
    TennisMatch match = Provider.of<ActiveMatch>(context, listen: false);
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
                match
                    .getDisplayPoint(TennisScore.LEVEL_SET, team)
                    .displayString(context),
                () => onScoreClicked(TennisScore.LEVEL_SET),
              ),
              createBox(
                context,
                Values(context).strings.title_tennis_games,
                match
                    .getDisplayPoint(TennisScore.LEVEL_GAME, team)
                    .displayString(context),
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
            match
                .getDisplayPoint(TennisScore.LEVEL_POINT, team)
                .displayString(context),
            () => onScoreClicked(TennisScore.LEVEL_POINT),
          ),
        ),
      ],
    );
  }
}
