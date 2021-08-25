import 'package:flutter/foundation.dart';

enum ClickAction {
  pointTeamOne,
  pointTeamTwo,
  pointServer,
  pointReceiver,
  undoLast,
}

abstract class ControllerListener {
  void onButtonPressed({@required ClickAction action});
}
