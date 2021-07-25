import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/match_setup.dart';
import 'package:multiphone/providers/sport.dart';

enum PingPongRounds { one, three, five, seven, nine }
enum PingPongPoints { eleven, twenty_one }
enum PingPongExpeditePoints { eighteen }
enum PingPongExpediteMinutes { ten }

class PingPongMatchSetup extends MatchSetup {
  PingPongRounds _rounds = PingPongRounds.three;
  PingPongPoints _points = PingPongPoints.eleven;
  PingPongExpeditePoints _expeditePoints = PingPongExpeditePoints.eighteen;
  PingPongExpediteMinutes _expediteMinutes = PingPongExpediteMinutes.ten;
  bool _isExpediteEnabled = true;

  PingPongMatchSetup() : super(Sports.sport(SportType.PING_PONG));

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

  static int roundsValue(PingPongRounds rounds) {
    switch (rounds) {
      case PingPongRounds.one:
        return 1;
      case PingPongRounds.three:
        return 3;
      case PingPongRounds.five:
        return 5;
      case PingPongRounds.seven:
        return 7;
      case PingPongRounds.nine:
        return 9;
      default:
        return 3;
    }
  }

  static PingPongRounds fromRoundsValue(int value) {
    switch (value) {
      case 1:
        return PingPongRounds.one;
      case 3:
        return PingPongRounds.three;
      case 5:
        return PingPongRounds.five;
      case 7:
        return PingPongRounds.seven;
      case 9:
        return PingPongRounds.nine;
      default:
        return PingPongRounds.three;
    }
  }

  static int roundsTarget(PingPongRounds rounds) {
    return ((roundsValue(rounds) + 1.0) / 2.0).floor();
  }

  static int pointsValue(PingPongPoints points) {
    switch (points) {
      case PingPongPoints.eleven:
        return 11;
      case PingPongPoints.twenty_one:
        return 21;
      default:
        return 21;
    }
  }

  static PingPongPoints fromPointsValue(int value) {
    switch (value) {
      case 11:
        return PingPongPoints.eleven;
      case 21:
        return PingPongPoints.twenty_one;
      default:
        return PingPongPoints.twenty_one;
    }
  }

  static int expeditePointsValue(PingPongExpeditePoints points) {
    switch (points) {
      case PingPongExpeditePoints.eighteen:
        return 18;
      default:
        return 18;
    }
  }

  static PingPongExpeditePoints fromExpeditePointsValue(int value) {
    switch (value) {
      case 18:
        return PingPongExpeditePoints.eighteen;
      default:
        return PingPongExpeditePoints.eighteen;
    }
  }

  static int expediteMinutesValue(PingPongExpediteMinutes minutes) {
    switch (minutes) {
      case PingPongExpediteMinutes.ten:
        return 10;
      default:
        return 10;
    }
  }

  static PingPongExpediteMinutes fromExpediteMinutesValue(int value) {
    switch (value) {
      case 10:
        return PingPongExpediteMinutes.ten;
      default:
        return PingPongExpediteMinutes.ten;
    }
  }

  @override
  Map<String, Object> getData() {
    return {
      ...super.getData(),
      ...{
        'rounds': roundsValue(_rounds),
        'points': pointsValue(_points),
        'expMins': expediteMinutesValue(_expediteMinutes),
        'expPts': expeditePointsValue(_expeditePoints),
        'expOn': _isExpediteEnabled,
      }
    };
  }

  @override
  void setData(Map<String, Object> data) {
    super.setData(data);
    _rounds = fromRoundsValue(data['rounds'] as int);
    _points = fromPointsValue(data['points'] as int);
    _expediteMinutes = fromExpediteMinutesValue(data['expMins']);
    _expeditePoints = fromExpeditePointsValue(data['expPts']);
    _isExpediteEnabled = data['expOn'];
  }

  @override
  List<int> getStraightPointsToWin() {
    // points levels are 3!
    return [
      1, // 1 point to win a point
      pointsValue(_points), // 11 points to win a game
    ];
  }

  PingPongRounds get rounds {
    return _rounds;
  }

  set rounds(PingPongRounds rounds) {
    if (_rounds != rounds) {
      _rounds = rounds;
      // this is a change
      notifyListeners();
    }
  }

  PingPongPoints get points {
    return _points;
  }

  set points(PingPongPoints points) {
    if (_points != points) {
      _points = points;
      // this is a change
      notifyListeners();
    }
  }

  int get decidingPoint {
    return pointsValue(_points) - 1;
  }

  bool get isExpediteEnabled {
    return _isExpediteEnabled;
  }

  set isExpediteEnabled(bool isEnabled) {
    if (_isExpediteEnabled != isEnabled) {
      _isExpediteEnabled = isEnabled;
      // this is a change
      notifyListeners();
    }
  }

  PingPongExpeditePoints get expediteSystemPoints {
    return _expeditePoints;
  }

  set expediteSystemPoints(PingPongExpeditePoints points) {
    if (_expeditePoints != points) {
      _expeditePoints = points;
      // this is a change
      notifyListeners();
    }
  }

  PingPongExpediteMinutes get expediteSystemMinutes {
    return _expediteMinutes;
  }

  set expediteSystemMinutes(PingPongExpediteMinutes minutes) {
    if (_expediteMinutes != minutes) {
      _expediteMinutes = minutes;
      // this is a change
      notifyListeners();
    }
  }
}
