import 'package:flutter/cupertino.dart';
import 'package:multiphone/helpers/values.dart';

class PointPair {
  final Point first;
  final Point second;

  PointPair(this.first, this.second);
}

abstract class Point {
  static const String K_SPEAKING_SPACE = " ";
  static const String K_SPEAKING_PAUSE = ". ";
  static const String K_SPEAKING_PAUSE_SLIGHT = ", ";
  static const String K_SPEAKING_PAUSE_LONG = "… "; //"... ";//""… ";

  int val();

  String displayString(BuildContext context);
  String speakString(BuildContext context);
  String speakNumberString(BuildContext context, int number);
  String speakAllString(BuildContext context);
}

class SimplePoint implements Point {
  final int value;

  SimplePoint(int value) : value = value;

  @override
  int val() {
    return this.value;
  }

  @override
  String displayString(BuildContext context) {
    return this.value.toString();
  }

  @override
  String speakString(BuildContext context) {
    if (null != context && this.value == 0) {
      return Values(context).strings.speak_zero;
    } else {
      return this.value.toString();
    }
  }

  @override
  String speakNumberString(BuildContext context, int number) {
    if (null != context && this.value == 0 && number != 1) {
      return Values(context).strings.speak_zeros;
    } else {
      return speakString(context);
    }
  }

  @override
  String speakAllString(BuildContext context) {
    return '$value ${Values(context).strings.speak_all}';
  }
}
