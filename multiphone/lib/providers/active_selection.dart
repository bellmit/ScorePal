import 'package:flutter/material.dart';
import 'package:multiphone/providers/sport.dart';

class ActiveSelection with ChangeNotifier {
  Sport _sport;

  ActiveSelection(Sports sports) {
    // just use the first available valid sport as our default
    _sport = sports == null ? null : sports.available.first;
  }

  Sport get sport {
    return _sport;
  }

  set sport(Sport sport) {
    if (_sport != sport) {
      // this is a change in our sport, we need to create a nice setup to
      // capture all the data on this match then
      _sport = sport;
      // and inform listeners
      notifyListeners();
    }
  }
}
