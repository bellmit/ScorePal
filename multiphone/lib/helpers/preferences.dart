import 'package:flutter/material.dart';
import 'package:multiphone/helpers/team_namer.dart';
import 'package:shared_preferences/shared_preferences.dart';

class Preferences with ChangeNotifier {
  final SharedPreferences prefs;

  Preferences._create(this.prefs) {
    // private constructor
  }

  //public factory method that will wait for the preferences to load up
  static Future<Preferences> create() async {
    return Preferences._create(await SharedPreferences.getInstance());
  }

  TeamNamingMode get defaultNamingMode {
    try {
      int value = prefs.getInt('default_naming_mode');
      return TeamNamingMode.values[value];
    } catch (error) {
      // fine that it doesn't exist, return the default
      return TeamNamingMode.SURNAME_INITIAL;
    }
  }

  set defaultNamingMode(TeamNamingMode value) {
    prefs.setInt('default_naming_mode', value.index);
  }

  bool get isFirebaseLoginDesired {
    // get the data direct from the preferences class
    try {
      return prefs.getBool('is_firebase_login');
    } catch (error) {
      // fine that it doesn't exist, return the default
      return false;
    }
  }

  set isFirebaseLoginDesired(bool newValue) {
    prefs.setBool('is_firebase_login', newValue);
  }
}
