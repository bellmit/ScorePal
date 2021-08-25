import 'package:flutter/material.dart';
import 'package:multiphone/helpers/setup_persistence.dart';
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
    if (createIfNull && null == _selectedMatch) {
      createMatch();
    }
    return _selectedMatch;
  }

  ActiveSetup getSelectedSetup(bool createIfNull) {
    if (createIfNull && null == getSelectedSetup(false)) {
      // no setup, as a member or from the match, create one then
      createSetup();
    }
    // return the setup from the match if there is one, else the setup alone
    return _selectedMatch != null ? _selectedMatch.getSetup() : _selectedSetup;
  }

  ActiveSetup createSetup() {
    // create the new setup then
    _selectedSetup = _sport.createSetup();
    // but creating empty isn't great - can we try to load the data in a little
    // bit from what came before?
    SetupPersistence().loadLastSetupData(_selectedSetup);
    // and return the setup created
    return _selectedSetup;
  }

  ActiveMatch createMatch() {
    // be sure there's a setup
    if (null == _selectedSetup) {
      createSetup();
    }
    // and create the match
    _selectedMatch = _sport.createMatch(_selectedSetup);
    // this match setup is the setup to use for the next time
    // then as contains all the latest data they entered
    SetupPersistence().saveAsLastSetupData(_selectedSetup);
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
    if (_selectedMatch != null) {
      // which changes the sport
      _sport = _selectedMatch.getSport();
    }
    // and inform listeners
    notifyListeners();
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
      // this is a change in our sport, we need to create a nice setup.
      if (null != _selectedSetup) {
        // there is a setup hanging around though - let's save this
        // as the default for the next time around
        SetupPersistence().saveAsLastSetupData(_selectedSetup);
      }
      // capture all the data on this match then
      _sport = sport;
      // and inform listeners
      notifyListeners();
    }
  }
}
