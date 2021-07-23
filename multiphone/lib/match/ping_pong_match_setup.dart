import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/match_setup.dart';
import 'package:multiphone/providers/team.dart';

enum PingPongRounds { one, three, five }
enum PingPongPoints { eleven, twenty_one }

class PingPongMatchSetup extends MatchSetup {
  PingPongRounds _rounds = PingPongRounds.three;
  PingPongPoints _points = PingPongPoints.twenty_one;

  PingPongMatchSetup();

  @override
  String matchSummary(BuildContext context) {
    // construct the match summary accordingly
    final values = Values(context);
    return values.construct(values.strings.title_setup_ping_pong, [
      getTeamName(TeamIndex.T_ONE, context),
      getTeamName(TeamIndex.T_TWO, context),
      roundsValue(_rounds).toString(),
    ]);
  }

  int roundsValue(PingPongRounds rounds) {
    switch (rounds) {
      case PingPongRounds.one:
        return 1;
      case PingPongRounds.three:
        return 3;
      case PingPongRounds.five:
        return 5;
      default:
        return 0;
    }
  }

  get rounds {
    return _rounds;
  }

  set rounds(PingPongRounds rounds) {
    _rounds = rounds;
    // this is a change
    notifyListeners();
  }

  int pointsValue(PingPongPoints points) {
    switch (points) {
      case PingPongPoints.eleven:
        return 11;
      case PingPongPoints.twenty_one:
        return 21;
      default:
        return 0;
    }
  }

  get points {
    return _points;
  }

  set points(PingPongPoints points) {
    _points = points;
    // this is a change
    notifyListeners();
  }
}
