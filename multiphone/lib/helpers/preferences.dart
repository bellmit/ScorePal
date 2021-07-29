import 'package:multiphone/helpers/log.dart';
import 'package:multiphone/match/team_namer.dart';
import 'package:shared_preferences/shared_preferences.dart';

class Preferences {
  final SharedPreferences prefs;

  Preferences._create(this.prefs) {
    // private constructor
  }

  //factory method that will wait for the preferences to load up
  static Future<Preferences> create() async {
    return Preferences._create(await SharedPreferences.getInstance());
  }

  bool _getBool(String key, bool varDefault) {
    // this is wrapped to do it safely
    try {
      return prefs.getBool(key) ?? varDefault;
    } catch (error) {
      Log.error('Failed to get $key from prefs $error');
      return varDefault;
    }
  }

  int _getInt(String key, int varDefault) {
    // this is wrapped to do it safely
    try {
      return prefs.getInt(key) ?? varDefault;
    } catch (error) {
      Log.error('Failed to get $key from prefs $error');
      return varDefault;
    }
  }

  TeamNamingMode get defaultNamingMode {
    try {
      int value =
          _getInt('default_naming_mode', TeamNamingMode.SURNAME_INITIAL.index);
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
    return _getBool('is_firebase_login', false);
  }

  set isFirebaseLoginDesired(bool newValue) {
    prefs.setBool('is_firebase_login', newValue);
  }

  bool get isControlTeams {
    return _getBool('isControlTeams', false);
  }

  set isControlTeams(bool value) {
    prefs.setBool("isControlTeams", value);
  }

  bool get isControlVol {
    return _getBool('isControlVol', false);
  }

  set isControlVol(bool value) {
    prefs.setBool("isControlVol", value);
  }

  bool get isControlMedia {
    return _getBool('isControlMedia', false);
  }

  bool get isAllowMedia {
    return _getBool('isAllowMedia', false);
  }

  set isControlMedia(bool value) {
    prefs.setBool("isControlMedia", value);
  }

  bool get isControlFlic1 {
    return _getBool('isControlFlic1', false);
  }

  set isControlFlic1(bool value) {
    prefs.setBool("isControlFlic1", value);
  }

  bool get isControlFlic2 {
    return _getBool('isControlFlic2', false);
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
    return _getBool('isSoundBtnClick', false);
  }

  set soundActionSpeak(bool value) {
    prefs.setBool("isSoundActionSpeak", value);
  }

  bool get soundActionSpeak {
    return _getBool('isSoundActionSpeak', true);
  }

  set soundAnnounceChange(bool value) {
    prefs.setBool("isSoundAnncChange", value);
  }

  bool get soundAnnounceChange {
    return _getBool('isSoundAnncChange', true);
  }

  bool get soundUseSpeakingNames {
    return _getBool('isSoundUseSpeakingNames', true);
  }

  set soundUseSpeakingNames(bool value) {
    prefs.setBool("isSoundUseSpeakingNames", value);
  }

  double get soundAnnounceVolume {
    return _getInt('isSoundAnncVol', 10) / 10.0;
  }

  set soundAnnounceVolume(double value) {
    prefs.setInt("isSoundAnncVol", (value * 10).floor());
  }

  set soundAnnounceChangePoints(bool value) {
    prefs.setBool("isSoundAnncChangePt", value);
  }

  bool get soundAnnounceChangePoints {
    return _getBool('isSoundAnncChangePt', true);
  }

  set soundAnnounceChangeEnds(bool value) {
    prefs.setBool("isSoundAnncChangeEnd", value);
  }

  bool get soundAnnounceChangeEnds {
    return _getBool('isSoundAnncChangeEnd', true);
  }

  set soundAnnounceChangeServer(bool value) {
    prefs.setBool("isSoundAnncChangeSvr", value);
  }

  bool get soundAnnounceChangeServer {
    return _getBool('isSoundAnncChangeSvr', true);
  }

  set soundAnnounceChangeScore(bool value) {
    prefs.setBool("isSoundAnncChangeScore", value);
  }

  bool get soundAnnounceChangeScore {
    return _getBool('isSoundAnncChangeScore', false);
  }
}
