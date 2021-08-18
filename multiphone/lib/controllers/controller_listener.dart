import 'package:flutter/foundation.dart';

enum ClickPattern {
  single,
  double,
  long,
}

abstract class ControllerListener {
  void onButtonPressed({@required ClickPattern pattern});
}
