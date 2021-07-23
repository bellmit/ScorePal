import 'package:flutter/cupertino.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/points/point.dart';
import 'package:multiphone/match/points/simple_point.dart';

class TennisPoint implements Point {
  static final love = TennisPoint(
    0,
    (context) => Values(context).strings.display_love,
    (context) => Values(context).strings.speak_love,
    (context) => Values(context).strings.speak_love,
    (context) => Values(context).strings.speak_love_all,
  );
  static final fifteen = TennisPoint(
    1,
    (context) => Values(context).strings.display_15,
    (context) => Values(context).strings.speak_15,
    (context) => Values(context).strings.speak_15,
    (context) => Values(context).strings.speak_15_all,
  );
  static final thirty = TennisPoint(
    2,
    (context) => Values(context).strings.display_30,
    (context) => Values(context).strings.speak_30,
    (context) => Values(context).strings.speak_30,
    (context) => Values(context).strings.speak_30_all,
  );
  static final forty = TennisPoint(
    3,
    (context) => Values(context).strings.display_40,
    (context) => Values(context).strings.speak_40,
    (context) => Values(context).strings.speak_40,
    (context) => Values(context).strings.speak_deuce,
  );
  static final deuce = TennisPoint(
    4,
    (context) => Values(context).strings.display_deuce,
    (context) => Values(context).strings.speak_deuce,
    (context) => Values(context).strings.speak_deuce,
    (context) => Values(context).strings.speak_deuce,
  );
  static final advantage = TennisPoint(
    5,
    (context) => Values(context).strings.display_advantage,
    (context) => Values(context).strings.speak_advantage,
    (context) => Values(context).strings.speak_advantage,
    (context) => Values(context).strings.speak_advantage,
  );
  static final game = TennisPoint(
    6,
    (context) => Values(context).strings.display_game,
    (context) => Values(context).strings.speak_game,
    (context) => Values(context).strings.speak_games,
    null,
  );
  static final set = TennisPoint(
    7,
    (context) => Values(context).strings.display_set,
    (context) => Values(context).strings.speak_set,
    (context) => Values(context).strings.speak_sets,
    null,
  );
  static final match = TennisPoint(
    8,
    (context) => Values(context).strings.display_match,
    (context) => Values(context).strings.speak_match,
    (context) => Values(context).strings.speak_match,
    null,
  );
  static final point = TennisPoint(
    6,
    (context) => Values(context).strings.points,
    (context) => Values(context).strings.speak_point,
    (context) => Values(context).strings.speak_points,
    null,
  );

  static final List<TennisPoint> values = [
    love,
    fifteen,
    thirty,
    forty,
    deuce,
    advantage,
    game,
    set,
    match,
    point
  ];

  final int value;
  final String Function(BuildContext) displayStrId;
  final String Function(BuildContext) speakStrId;
  final String Function(BuildContext) speakStrPluralId;
  final String Function(BuildContext) speakAllStrId;

  TennisPoint(
    this.value,
    this.displayStrId,
    this.speakStrId,
    this.speakStrPluralId,
    this.speakAllStrId,
  );

  @override
  int val() {
    return this.value;
  }

  @override
  String displayString(BuildContext context) {
    if (null != context && null != this.displayStrId) {
      return displayStrId(context);
    } else {
      return this.value.toString();
    }
  }

  @override
  String speakString(BuildContext context) {
    if (null != context && null != this.speakStrId) {
      return this.speakStrId(context);
    } else {
      return this.value.toString();
    }
  }

  @override
  String speakNumberString(BuildContext context, int number) {
    if (null != context) {
      if (number == 1 && null != this.speakStrId) {
        // in the singular
        return this.speakStrId(context);
      } else if (null != this.speakStrPluralId) {
        return this.speakStrPluralId(context);
      } else {
        return this.value.toString();
      }
    } else {
      return this.value.toString();
    }
  }

  @override
  String speakAllString(BuildContext context) {
    if (null != this.speakAllStrId) {
      return this.speakAllStrId(context);
    } else {
      // just say the number then 'all'
      return '${speakString(context)} ${Values(context).strings.speak_all}';
    }
  }

  static Point fromVal(int points) {
    for (int i = 0; i < TennisPoint.values.length; ++i) {
      TennisPoint point = TennisPoint.values[i];
      if (point.val() == points) {
        return point;
      }
    }
    // if here then we don't have a tennis point, return a simple number one
    return SimplePoint(points);
  }
}
