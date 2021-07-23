import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/match_setup.dart';
import 'package:multiphone/widgets/score_widget.dart';

class BadmintonScoreWidget extends ScoreWidget {
  const BadmintonScoreWidget({Key key, @required TeamIndex team})
      : super(key: key, team: team);

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
                  context, Values(context).strings.title_badminton_games, '0'),
            ],
          ),
        ),
        Flexible(
          flex: 2,
          child: createBox(
              context, Values(context).strings.title_badminton_points, '15'),
        ),
      ],
    );
  }
}
