import 'package:flutter/material.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/providers/sport.dart';

class ActiveSelection with ChangeNotifier {
  Sport _sport;
  ActiveSetup _selectedSetup;
  ActiveMatch _selectedMatch;

  ActiveSelection(Sports sports) {
    // just use the first available valid sport as our default
    _sport = sports == null ? null : sports.available.first;
  }

  ActiveMatch getSelectedMatch(bool createIfNull) {
    if (createIfNull &&
        (null == _selectedMatch || _selectedMatch.getSport() != _sport)) {
      // we want to create one and there isn't one, or it isn't the right one
      createMatch();
    }
    // and return what was created
    return _selectedMatch;
  }

  ActiveSetup getSelectedSetup(bool createIfNull) {
    if (createIfNull &&
        (null == _selectedSetup || _selectedSetup.sport != _sport)) {
      // no setup (or wrong setup), as a member or from the match, create one then
      createSetup();
    }
    // return the setup from the match if there is one, else the setup alone
    return _selectedSetup;
  }

  ActiveSetup createSetup() {
    // create the new setup then
    _selectedSetup = _sport.createSetup();
    // inform listeners
    notifyListeners();
    // and return the setup created
    return _selectedSetup;
  }

  ActiveMatch createMatch() {
    // be sure there's a setup by calling the create function
    final currentSetup = getSelectedSetup(true);
    // and create the match
    _selectedMatch = _sport.createMatch(currentSetup);
    // inform listeners
    notifyListeners();
    // and have the setup inform that is has changed
    currentSetup.notifyListeners();
    // returning what we created
    return _selectedMatch;
  }

  void clearSelection() {
    bool isInform = _selectedMatch != null || _selectedSetup != null;
    _selectedMatch = null;
    _selectedSetup = null;
    if (isInform) {
      // and inform listeners
      notifyListeners();
    }
  }

  void selectMatch(ActiveMatch match) {
    _selectedMatch = match;
    _selectedSetup = match.getSetup();
    if (_selectedMatch != null) {
      // which changes the sport
      _sport = _selectedMatch.getSport();
    }
    // and inform listeners
    notifyListeners();
    // and have the setup inform that it has changed
    _selectedSetup.notifyListeners();
  }

  void selectSetup(ActiveSetup setup) {
    _selectedMatch = null;
    _selectedSetup = setup;
    if (_selectedSetup != null) {
      // which changes the sport
      _sport = _selectedSetup.sport;
    }
    // and inform listeners
    notifyListeners();
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
}
