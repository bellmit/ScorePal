import 'package:flutter/material.dart';
import 'package:multiphone/match/tennis_match_setup.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/sport.dart';

enum SINGLES_DOUBLES {
  DOUBLES,
  SINGLES,
}

abstract class MatchSetup with ChangeNotifier {
  SINGLES_DOUBLES _singlesDoubles = SINGLES_DOUBLES.SINGLES;

  MatchSetup();

  // need to create the proper one for the active match as it changes
  static MatchSetup create(ActiveMatch match) {
    if (match == null || match.sport == null) {
      return null;
    }
    switch (match.sport.id) {
      case SportType.TENNIS:
        return TennisMatchSetup();
        break;
      case SportType.BADMINTON:
      case SportType.PING_PONG:
        print('do the setup for the sport type of ${match.sport.id}');
        break;
    }
    return null;
  }

  get singlesDoubles {
    return _singlesDoubles;
  }

  set singlesDoubles(SINGLES_DOUBLES singlesDoubles) {
    _singlesDoubles = singlesDoubles;
    // this is a change
    notifyListeners();
  }

  String matchSummary(BuildContext context);
}
