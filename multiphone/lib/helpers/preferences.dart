import 'package:flutter/material.dart';
import 'package:multiphone/helpers/team_namer.dart';
import 'package:shared_preferences/shared_preferences.dart';

class Preferences with ChangeNotifier {
  final SharedPreferences prefs;

  Preferences._create(this.prefs) {
    // private constructor
  }

  //factory method that will wait for the preferences to load up
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

  bool get isControlTeams {
    try {
      return prefs.getBool("isControlTeams");
    } catch (error) {
      // fine that it doesn't exist, return the default
      return false;
    }
  }

  set isControlTeams(bool value) {
    prefs.setBool("isControlTeams", value);
  }

  bool get isControlVol {
    try {
      return prefs.getBool("isControlVol");
    } catch (error) {
      // fine that it doesn't exist, return the default
      return false;
    }
  }

  set isControlVol(bool value) {
    prefs.setBool("isControlVol", value);
  }

  bool get isControlMedia {
    try {
      return prefs.getBool("isControlMedia");
    } catch (error) {
      // fine that it doesn't exist, return the default
      return false;
    }
  }

  bool get isAllowMedia {
    try {
      return prefs.getBool("isAllowMedia");
    } catch (error) {
      // fine that it doesn't exist, return the default
      return false;
    }
  }

  set isControlMedia(bool value) {
    prefs.setBool("isControlMedia", value);
  }

  bool get isControlFlic1 {
    try {
      return prefs.getBool("isControlFlic1");
    } catch (error) {
      // fine that it doesn't exist, return the default
      return false;
    }
  }

  set isControlFlic1(bool value) {
    prefs.setBool("isControlFlic1", value);
  }

  bool get isControlFlic2 {
    try {
      return prefs.getBool("isControlFlic2");
    } catch (error) {
      // fine that it doesn't exist, return the default
      return false;
    }
  }

  set isControlFlic2(bool value) {
    prefs.setBool("isControlFlic2", value);
  }

  bool isLogging() {
    return true;
  }

  set soundButtonClick(bool value) {
    prefs.setBool("isSoundBtnClick", value);
  }

  bool get soundButtonClick {
    try {
      return prefs.getBool("isSoundBtnClick");
    } catch (error) {
      // fine that it doesn't exist, return the default
      return false;
    }
  }

  set soundActionSpeak(bool value) {
    prefs.setBool("isSoundActionSpeak", value);
  }

  bool get soundActionSpeak {
    try {
      return prefs.getBool("isSoundActionSpeak");
    } catch (error) {
      // fine that it doesn't exist, return the default
      return true;
    }
  }

  set soundAnnounceChange(bool value) {
    prefs.setBool("isSoundAnncChange", value);
  }

  bool get soundAnnounceChange {
    try {
      return prefs.getBool("isSoundAnncChange");
    } catch (error) {
      // fine that it doesn't exist, return the default
      return true;
    }
  }

  set soundAnnounceVolume(int value) {
    prefs.setInt("isSoundAnncVol", value);
  }

  bool get soundUseSpeakingNames {
    try {
      return prefs.getBool("isSoundUseSpeakingNames");
    } catch (error) {
      // fine that it doesn't exist, return the default
      return true;
    }
  }

  set soundUseSpeakingNames(bool value) {
    prefs.setBool("isSoundUseSpeakingNames", value);
  }

  int get soundAnnounceVolume {
    try {
      return prefs.getInt("isSoundAnncVol");
    } catch (error) {
      // fine that it doesn't exist, return the default
      return -1;
    }
  }

  set soundAnnounceChangePoints(bool value) {
    prefs.setBool("isSoundAnncChangePt", value);
  }

  bool get soundAnnounceChangePoints {
    try {
      return prefs.getBool("isSoundAnncChangePt");
    } catch (error) {
      // fine that it doesn't exist, return the default
      return true;
    }
  }

  set soundAnnounceChangeEnds(bool value) {
    prefs.setBool("isSoundAnncChangeEnd", value);
  }

  bool get soundAnnounceChangeEnds {
    try {
      return prefs.getBool("isSoundAnncChangeEnd");
    } catch (error) {
      // fine that it doesn't exist, return the default
      return true;
    }
  }

  set soundAnnounceChangeServer(bool value) {
    prefs.setBool("isSoundAnncChangeSvr", value);
  }

  bool get soundAnnounceChangeServer {
    try {
      return prefs.getBool("isSoundAnncChangeSvr");
    } catch (error) {
      // fine that it doesn't exist, return the default
      return true;
    }
  }

  set soundAnnounceChangeScore(bool value) {
    prefs.setBool("isSoundAnncChangeScore", value);
  }

  bool get soundAnnounceChangeScore {
    try {
      return prefs.getBool("isSoundAnncChangeScore");
    } catch (error) {
      // fine that it doesn't exist, return the default
      return false;
    }
  }
}
