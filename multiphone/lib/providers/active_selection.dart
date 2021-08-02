import 'package:flutter/material.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/sport.dart';

class ActiveSelection with ChangeNotifier {
  Sport _sport;
  ActiveMatch _selectedMatch;
  bool _isCreateNextMatchNew = false;

  ActiveSelection(Sports sports) {
    // just use the first available valid sport as our default
    _sport = sports == null ? null : sports.available.first;
  }

  ActiveMatch get selectedMatch {
    return _selectedMatch;
  }

  void selectMatch(ActiveMatch match, bool isCreateNextNew) {
    _selectedMatch = match;
    _isCreateNextMatchNew = isCreateNextNew;
    if (_selectedMatch != null) {
      // which changes the sport
      _sport = _selectedMatch.getSport();
      // and inform listeners
      notifyListeners();
    }
  }

  bool get isCreateNextMatchNew {
    return _isCreateNextMatchNew;
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
