import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/providers/team.dart';
import 'package:multiphone/widgets/score_widget.dart';

class PingPongScoreWidget extends ScoreWidget {
  const PingPongScoreWidget({Key key, @required TeamIndex team})
      : super(key: key, team: team);

  @override
  Widget build(BuildContext context) {
    final boxColor = Theme.of(context).primaryColorDark;
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
                  context, Values(context).strings.title_ping_pong_rounds, '0'),
            ],
          ),
        ),
        Flexible(
          flex: 2,
          child: createBox(
              context, Values(context).strings.title_ping_pong_points, '15'),
        ),
      ],
    );
  }
}
