import 'package:flutter/material.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/sport.dart';

class ActiveSelection with ChangeNotifier {
  Sport _sport;
  ActiveMatch _selectedMatch;

  ActiveSelection(Sports sports) {
    // just use the first available valid sport as our default
    _sport = sports == null ? null : sports.available.first;
  }

  ActiveMatch get selectedMatch {
    return _selectedMatch;
  }

  set selectedMatch(ActiveMatch match) {
    _selectedMatch = match;
    if (_selectedMatch != null) {
      // and inform listeners
      notifyListeners();
    }
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
