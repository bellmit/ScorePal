import 'package:flutter/cupertino.dart';

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
