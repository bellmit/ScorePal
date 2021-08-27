import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/point.dart';
import 'package:multiphone/providers/active_match.dart';

class ScoreHeadlineWidget extends StatelessWidget {
  final ActiveMatch match;
  const ScoreHeadlineWidget({Key key, @required this.match}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    // let's calculate all the data we need from the match here then
    final setup = match.getSetup();
    final winner = match.getMatchWinner();
    final loser = setup.getOtherTeam(winner);
    final values = Values(context);
    // need to find the top level scores ATM
    Point winnerPoint, loserPoint;
    int level = 0;
    for (int i = 0; i < match.getScoreLevels(); ++i) {
      winnerPoint = match.getDisplayPoint(i, winner);
      loserPoint = match.getDisplayPoint(i, loser);
      if (null != winnerPoint &&
          null != loserPoint &&
          (winnerPoint.val() > 0 || loserPoint.val() > 0)) {
        // we have two display points and one of them isn't zero, don't go lower
        level = i;
        break;
      }
    }
    // show the results of this match currently
    final winnerTitle = setup.getTeamName(winner, context);
    final loserTitle = setup.getTeamName(loser, context);
    final playedLevel = match.getLevelTitle(level, context);
    final winnerScore = null != winnerPoint
        ? winnerPoint.displayString(context)
        : values.strings.display_zero;
    final loserScore = null != loserPoint
        ? loserPoint.displayString(context)
        : values.strings.display_zero;

    return Container(
      decoration: BoxDecoration(
        color: Theme.of(context).primaryColor,
        border: Border.all(
            color: Theme.of(context).primaryColorDark,
            width: Values.border_width),
        borderRadius: BorderRadius.all(Radius.circular(Values.default_radius)),
      ),
      child: Row(
        children: [
          Expanded(
            child: Padding(
              padding: EdgeInsets.only(
                  left: Values.default_space, right: Values.default_space),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.end,
                children: [
                  Text(
                    winnerTitle,
                    maxLines: 1,
                    overflow: TextOverflow.fade,
                    style: TextStyle(color: Theme.of(context).accentColor),
                  ),
                  Text(
                    winnerScore,
                    style: TextStyle(color: Theme.of(context).accentColor),
                  ),
                ],
              ),
            ),
          ),
          Column(
            children: [
              Text(
                match.isMatchOver()
                    ? Values(context).strings.match_beat
                    : values.strings.match_beating,
                style: TextStyle(color: Theme.of(context).accentColor),
              ),
              Text(
                playedLevel,
                style: TextStyle(color: Theme.of(context).accentColor),
              ),
            ],
          ),
          Expanded(
            child: Padding(
              padding: EdgeInsets.only(
                  left: Values.default_space, right: Values.default_space),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    loserTitle,
                    maxLines: 3,
                    overflow: TextOverflow.fade,
                    style: TextStyle(color: Theme.of(context).accentColor),
                  ),
                  Text(
                    loserScore,
                    style: TextStyle(color: Theme.of(context).accentColor),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}
