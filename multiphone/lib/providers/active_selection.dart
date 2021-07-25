import 'package:flutter/material.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/providers/sport.dart';

class ActiveSelection with ChangeNotifier {
  Sport _sport;
  ActiveSetup _setup;
  ActiveMatch _match;

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

      // create the setup
      _setup = _sport.createSetup();

      // and inform listeners
      notifyListeners();
    }
  }

  ActiveMatch startMatch() {
    // start our match by creating the match class
    _match = _sport.createMatch(_setup);
    return _match;
  }
}
