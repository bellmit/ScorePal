import 'package:flutter/cupertino.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/points/point.dart';

class PingPongPoint implements Point {
  static final zero = PingPongPoint(
    0,
    (context) => Values(context).strings.display_zero,
    (context) => Values(context).strings.speak_zero,
    (context) => Values(context).strings.speak_zeros,
  );
  static final round = PingPongPoint(
    -1,
    (context) => Values(context).strings.display_round,
    (context) => Values(context).strings.speak_round,
    (context) => Values(context).strings.speak_rounds,
  );
  static final point = PingPongPoint(
    -2,
    (context) => Values(context).strings.points,
    (context) => Values(context).strings.speak_point,
    (context) => Values(context).strings.speak_points,
  );
  static final match = PingPongPoint(
    -3,
    (context) => Values(context).strings.display_match,
    (context) => Values(context).strings.speak_match,
    (context) => Values(context).strings.speak_match,
  );
  static final deuce = PingPongPoint(
    -4,
    (context) => Values(context).strings.display_deuce,
    (context) => Values(context).strings.speak_deuce,
    (context) => Values(context).strings.speak_deuce,
  );

  final int value;
  final String Function(BuildContext) displayStrId;
  final String Function(BuildContext) speakStrId;
  final String Function(BuildContext) speakStrPluralId;

  PingPongPoint(
    this.value,
    this.displayStrId,
    this.speakStrId,
    this.speakStrPluralId,
  );

  @override
  int val() {
    return this.value;
  }

  @override
  String displayString(BuildContext context) {
    if (null != context && null != this.displayStrId) {
      return this.displayStrId(context);
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
    // just say the number then 'all'
    return '${speakString(context)} ${Values(context).strings.speak_all}';
  }
}
