import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/match_setup.dart';
import 'package:multiphone/providers/team.dart';

enum BadmintonGames { one, three, five }
enum BadmintonPoints { eleven, fifteen, twenty_one }

class BadmintonMatchSetup extends MatchSetup {
  BadmintonGames _games = BadmintonGames.three;
  BadmintonPoints _points = BadmintonPoints.twenty_one;

  BadmintonMatchSetup();

  @override
  String matchSummary(BuildContext context) {
    // construct the match summary accordingly
    final values = Values(context);
    return values.construct(values.strings.title_setup_badminton, [
      getTeamName(TeamIndex.T_ONE, context),
      getTeamName(TeamIndex.T_TWO, context),
      gamesValue(_games).toString(),
    ]);
  }

  int gamesValue(BadmintonGames games) {
    switch (games) {
      case BadmintonGames.one:
        return 1;
      case BadmintonGames.three:
        return 3;
      case BadmintonGames.five:
        return 5;
      default:
        return 0;
    }
  }

  get games {
    return _games;
  }

  set games(BadmintonGames games) {
    _games = games;
    // this is a change
    notifyListeners();
  }

  int pointsValue(BadmintonPoints points) {
    switch (points) {
      case BadmintonPoints.eleven:
        return 11;
      case BadmintonPoints.fifteen:
        return 15;
      case BadmintonPoints.twenty_one:
        return 21;
      default:
        return 0;
    }
  }

  get points {
    return _points;
  }

  set points(BadmintonPoints points) {
    _points = points;
    // this is a change
    notifyListeners();
  }
}
