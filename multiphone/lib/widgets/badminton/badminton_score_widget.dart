import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/badminton/badminton_match.dart';
import 'package:multiphone/match/badminton/badminton_score.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/widgets/score_widget.dart';
import 'package:provider/provider.dart';

class BadmintonScoreWidget extends ScoreWidget {
  final void Function(int level) onScoreClicked;
  const BadmintonScoreWidget({
    Key key,
    @required TeamIndex team,
    this.onScoreClicked,
  }) : super(key: key, team: team);

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
                match
                    .getDisplayPoint(BadmintonScore.LEVEL_GAME, team)
                    .displayString(context),
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
            match
                .getDisplayPoint(BadmintonScore.LEVEL_POINT, team)
                .displayString(context),
            () => onScoreClicked(BadmintonScore.LEVEL_POINT),
          ),
        ),
      ],
    );
  }
}
