import 'package:multiphone/helpers/log.dart';
import 'package:multiphone/match/team_namer.dart';
import 'package:multiphone/providers/sport.dart';
import 'package:multiphone/widgets/settings/select_control_widget.dart';
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

  String _getString(String key, String varDefault) {
    // this is wrapped to do it safely
    try {
      return prefs.getString(key) ?? varDefault;
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

  ControlType get controlType {
    switch (_getInt('control_type', 0)) {
      case 0:
        return ControlType.meThem;
      default:
        return ControlType.serverReceiver;
    }
  }

  set controlType(ControlType type) {
    switch (type) {
      case ControlType.meThem:
        prefs.setInt('control_type', 0);
        break;
      default:
        prefs.setInt('control_type', 1);
        break;
    }
  }

  Sport get lastActiveSport {
    try {
      String value =
          _getString('default_sport', Sports.sport(SportType.TENNIS).id);
      return Sports.fromId(value);
    } catch (error) {
      // fine that it doesn't exist, return the default
      return Sports.sport(SportType.TENNIS);
    }
  }

  set lastActiveSport(Sport value) {
    prefs.setString('default_sport', value.id);
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

  bool get isControlKeys {
    return _getBool('isControlKeys', false);
  }

  set isControlKeys(bool value) {
    prefs.setBool("isControlKeys", value);
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

  DateTime getAdvertDismissedUntil(String key) {
    final result = _getString('dismiss_$key', '');
    if (result == null || result.isEmpty) {
      return null;
    } else {
      return DateTime.tryParse(result);
    }
  }

  bool isAdvertDismissed(String key) {
    DateTime dismissed = getAdvertDismissedUntil(key);
    return dismissed != null && dismissed.isAfter(DateTime.now());
  }

  Future<bool> setAdvertDismissed(String key) {
    var date = DateTime.now();
    if (date.month == 12) {
      // this is december, move to jan next year
      date = DateTime(date.year + 1, 1, date.day);
    } else {
      // just go forward a month
      date = DateTime(date.year, date.month + 1, date.day);
    }
    return prefs.setString('dismiss_$key', date.toIso8601String());
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
