import 'package:flutter/material.dart';

enum SINGLES_DOUBLES {
  DOUBLES,
  SINGLES,
}

class MatchSetup with ChangeNotifier {
  SINGLES_DOUBLES _singlesDoubles = SINGLES_DOUBLES.SINGLES;

  MatchSetup();

  get singlesDoubles {
    return _singlesDoubles;
  }

  set singlesDoubles(SINGLES_DOUBLES singlesDoubles) {
    _singlesDoubles = singlesDoubles;
    // this is a change
    notifyListeners();
  }
}
