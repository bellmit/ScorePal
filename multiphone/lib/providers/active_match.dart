import 'package:flutter/material.dart';
import 'package:multiphone/match/match_setup.dart';
import 'package:multiphone/match/tennis_match_setup.dart';
import 'package:multiphone/providers/sport.dart';

class ActiveMatch with ChangeNotifier {
  Sport _sport;
  MatchSetup _setup;

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

  MatchSetup get setup {
    if (null == _setup) {
      // we need a setup for the sport then
      switch (sport.id) {
        case SportType.TENNIS:
          _setup = TennisMatchSetup();
          break;
        case SportType.BADMINTON:
        case SportType.PING_PONG:
          print('do the setup for the sport type of ${sport.id}');
          break;
      }
    }
    return _setup;
  }

  set setup(MatchSetup setup) {
    _setup = setup;
    notifyListeners();
  }
}
