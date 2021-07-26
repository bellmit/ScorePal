import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/ping_pong/ping_pong_match.dart';
import 'package:multiphone/match/ping_pong/ping_pong_score.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/widgets/score_widget.dart';
import 'package:provider/provider.dart';

class PingPongScoreWidget extends ScoreWidget {
  final void Function(int level) onScoreClicked;
  const PingPongScoreWidget({
    Key key,
    @required TeamIndex team,
    this.onScoreClicked,
  }) : super(key: key, team: team);

  @override
  Widget build(BuildContext context) {
    PingPongMatch match = Provider.of<ActiveMatch>(context, listen: false);
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
                Values(context).strings.title_ping_pong_rounds,
                match
                    .getDisplayPoint(PingPongScore.LEVEL_ROUND, team)
                    .displayString(context),
                () => onScoreClicked(PingPongScore.LEVEL_ROUND),
              ),
            ],
          ),
        ),
        Flexible(
          flex: 2,
          child: createBox(
            context,
            Values(context).strings.title_ping_pong_points,
            match
                .getDisplayPoint(PingPongScore.LEVEL_POINT, team)
                .displayString(context),
            () => onScoreClicked(PingPongScore.LEVEL_POINT),
          ),
        ),
      ],
    );
  }
}
