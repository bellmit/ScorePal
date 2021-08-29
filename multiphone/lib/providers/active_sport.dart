import 'package:flutter/material.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/sport.dart';

class ActiveSport with ChangeNotifier {
  Sport _sport;
  bool _isCreateNewMatch = false;
  ActiveMatch _matchToResume;
  ActiveSport();

  void updateSportFromAvailable(Sports sports) {
    // if our sport is null (or invalid), just take the first
    if (_sport == null || sports.available.contains(_sport)) {
      sport = sports == null ? null : sports.available.first;
    }
  }

  Sport get sport {
    return _sport;
  }

  set sport(Sport sport) {
    if (_sport != sport) {
      // change the sport then
      _sport = sport;
      // and inform listeners
      notifyListeners();
    }
  }

  bool get isCreateNewMatch {
    return _isCreateNewMatch;
  }

  void newMatchCreated() {
    // called from the provider when we make a new one (to not do it again)
    _isCreateNewMatch = false;
  }

  ActiveMatch get matchToResume {
    return _matchToResume;
  }

  void createNewMatch() {
    _isCreateNewMatch = true;
    _matchToResume = null;
    // and inform listeners
    notifyListeners();
  }

  void resumeMatch(ActiveMatch match) {
    _matchToResume = match;
    _isCreateNewMatch = false;
    notifyListeners();
  }
}
