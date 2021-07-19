import 'package:flutter/material.dart';
import 'package:multiphone/providers/sport.dart';

class ActiveMatch with ChangeNotifier {
  Sport _sport;

  ActiveMatch(Sports sports) {
    // just use the first available valid sport as our default
    _sport = sports == null ? null : sports.available.first;
  }

  Sport get sport {
    return _sport;
  }

  set sport(Sport sport) {
    // change the member
    _sport = sport;
    // and inform listeners
    notifyListeners();
  }
}
