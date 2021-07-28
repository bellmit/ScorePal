import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/providers/sport.dart';

enum BadmintonGames { one, three, five }
enum BadmintonPoints { eleven, fifteen, twenty_one }
enum BadmintonDecider { nineteen, twenty_five, twenty_nine }

class BadmintonMatchSetup extends ActiveSetup {
  BadmintonGames _games = BadmintonGames.three;
  BadmintonPoints _points = BadmintonPoints.twenty_one;
  BadmintonDecider _decidingPoint = BadmintonDecider.twenty_nine;

  BadmintonMatchSetup() : super(Sports.sport(SportType.BADMINTON));

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

  static int gamesValue(BadmintonGames games) {
    switch (games) {
      case BadmintonGames.one:
        return 1;
      case BadmintonGames.three:
        return 3;
      case BadmintonGames.five:
        return 5;
      default:
        return 3;
    }
  }

  static BadmintonGames fromGamesValue(int value) {
    switch (value) {
      case 1:
        return BadmintonGames.one;
      case 3:
        return BadmintonGames.three;
      case 5:
        return BadmintonGames.five;
      default:
        return BadmintonGames.five;
    }
  }

  static int pointsValue(BadmintonPoints points) {
    switch (points) {
      case BadmintonPoints.eleven:
        return 11;
      case BadmintonPoints.fifteen:
        return 15;
      case BadmintonPoints.twenty_one:
        return 21;
      default:
        return 21;
    }
  }

  static BadmintonPoints fromPointsValue(int value) {
    switch (value) {
      case 11:
        return BadmintonPoints.eleven;
      case 15:
        return BadmintonPoints.fifteen;
      case 21:
        return BadmintonPoints.twenty_one;
      default:
        return BadmintonPoints.twenty_one;
    }
  }

  static int deciderValue(BadmintonDecider decider) {
    switch (decider) {
      case BadmintonDecider.nineteen:
        return 19;
      case BadmintonDecider.twenty_five:
        return 25;
      case BadmintonDecider.twenty_nine:
        return 29;
      default:
        return 29;
    }
  }

  static BadmintonDecider fromDeciderValue(int value) {
    switch (value) {
      case 19:
        return BadmintonDecider.nineteen;
      case 25:
        return BadmintonDecider.twenty_five;
      case 29:
        return BadmintonDecider.twenty_nine;
      default:
        return BadmintonDecider.twenty_nine;
    }
  }

  @override
  Map<String, Object> getData() {
    final data = super.getData();
    // add ours
    data['games'] = gamesValue(_games);
    data['points'] = pointsValue(_points);
    data['decdng'] = deciderValue(_decidingPoint);
    // and return
    return data;
  }

  @override
  void setData(Map<String, Object> data) {
    super.setData(data);
    _games = fromGamesValue(data['games'] as int);
    _points = fromPointsValue(data['points'] as int);
    _decidingPoint = fromDeciderValue(data['decdng'] as int);
  }

  @override
  List<int> getStraightPointsToWin() {
    // points levels are 3!
    return [
      1, // 1 point to win a point
      pointsValue(_points), // 21 points to win a game
    ];
  }

  BadmintonDecider get decidingPoint {
    return _decidingPoint;
  }

  set decidingPoint(BadmintonDecider decider) {
    if (_decidingPoint != decider) {
      _decidingPoint = decider;
      // this is a change
      notifyListeners();
    }
  }

  BadmintonGames get games {
    return _games;
  }

  set games(BadmintonGames games) {
    if (_games != games) {
      _games = games;
      // this is a change
      notifyListeners();
    }
  }

  BadmintonPoints get points {
    return _points;
  }

  set points(BadmintonPoints points) {
    if (_points != points) {
      _points = points;
      // this is a change
      notifyListeners();
    }
  }
}
