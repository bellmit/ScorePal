import 'package:intl/intl.dart';
import 'package:localstore/localstore.dart';
import 'package:multiphone/helpers/log.dart';
import 'package:multiphone/helpers/preferences.dart';
import 'package:multiphone/match/match_id.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/providers/sport.dart';

enum MatchPersistenceState {
  accepted,
  backup,
  deleted,
  lastActive,
}

enum MatchPersistanceSyncState {
  untried,
  stored,
  failed,
}

class MatchPersistence {
  static const matchCollection = 'matches';
  static const lastActivePrefix = 'active';

  static final DateFormat dateKey = DateFormat("yyyy-MM");

  static String stateString(MatchPersistenceState state) {
    switch (state) {
      case MatchPersistenceState.accepted:
        return 'accepted';
      case MatchPersistenceState.backup:
        return 'backup';
      case MatchPersistenceState.deleted:
        return 'deleted';
      case MatchPersistenceState.lastActive:
        return 'last_active';
      default:
        return 'backup';
    }
  }

  static MatchPersistenceState fromStateString(String state) {
    switch (state) {
      case 'accepted':
        return MatchPersistenceState.accepted;
      case 'backup':
        return MatchPersistenceState.backup;
      case 'deleted':
        return MatchPersistenceState.deleted;
      case 'last_active':
        return MatchPersistenceState.lastActive;
      default:
        return MatchPersistenceState.backup;
    }
  }

  static String syncString(MatchPersistanceSyncState state) {
    switch (state) {
      case MatchPersistanceSyncState.untried:
        return 'untried';
      case MatchPersistanceSyncState.stored:
        return 'stored';
      case MatchPersistanceSyncState.failed:
        return 'failed';
      default:
        return 'untried';
    }
  }

  static MatchPersistanceSyncState fromSyncString(String state) {
    switch (state) {
      case 'untried':
        return MatchPersistanceSyncState.untried;
      case 'stored':
        return MatchPersistanceSyncState.stored;
      case 'failed':
        return MatchPersistanceSyncState.failed;
      default:
        return MatchPersistanceSyncState.untried;
    }
  }

  Future<Sport> getLastActiveSport() async {
    final preferences = await Preferences.create();
    return preferences.lastActiveSport;
  }

  Map<String, Object> _getMatchAsJSON(
      ActiveMatch match, MatchPersistenceState state) {
    final setup = match.getSetup();
    final matchId = MatchId.create(match);
    return {
      'ver': 1,
      'state': stateString(state),
      'sync': syncString(MatchPersistanceSyncState.untried),
      'date': dateKey.format(matchId.getDate()),
      'sport': setup.sport.id,
      'setup': setup.getData(),
      'data': match.getData(),
    };
  }

  ActiveMatch _createMatchFromJson(Map<String, Object> topLevel) {
    // what is this, get the sport from the JSON object;
    Sport sport = Sports.fromId(topLevel['sport'] as String);
    // and create the setup for this
    ActiveSetup setup = sport.createSetup();
    // setup the setup from the data stored against the match
    setup.setData(topLevel['setup']);
    // and the match
    ActiveMatch match = sport.createMatch(setup);
    // set our data from this data under the top level
    match.setData(topLevel['data']);
    // and return this now it's setup properly
    return match;
  }

  Future<ActiveMatch> loadLastMatchData(ActiveMatch match) async {
    // just get the last match data and put into the already loaded match
    final setup = match.getSetup();
    final defaultData = await Localstore.instance
        .collection(matchCollection)
        .doc('${lastActivePrefix}_${setup.sport.id}')
        .get();
    if (defaultData != null && defaultData['data'] != null) {
      // have the document, load ths data from this
      setup.setData(defaultData['setup']);
      match.setData(defaultData['data']);
    } else {
      Log.error('default match setup data isn\'t valid for ${setup.sport.id}');
    }
    return match;
  }

  Future<ActiveMatch> loadLastActiveMatch({Sport sport}) async {
    if (sport == null) {
      sport = await getLastActiveSport();
    }
    // from this we can create the setup
    final setup = sport.createSetup();
    // and the match
    final match = sport.createMatch(setup);
    // and finally set the data on these
    return loadLastMatchData(match);
  }

  Future<dynamic> saveAsLastActiveMatch(ActiveMatch match) {
    final setup = match.getSetup();
    // just send this off and hope it worked
    return Localstore.instance
        .collection(matchCollection)
        .doc('${lastActivePrefix}_${setup.sport.id}')
        .set(_getMatchAsJSON(match, MatchPersistenceState.lastActive));
  }

  Future<dynamic> deleteMatchData(ActiveMatch match) {
    // we don't delete though - what we do is save as state == deleted
    return saveMatchData(match, state: MatchPersistenceState.deleted);
  }

  Future<dynamic> saveMatchData(ActiveMatch match,
      {MatchPersistenceState state = MatchPersistenceState.backup}) {
    // save the match data as specified in the correct state
    final matchId = MatchId.create(match);
    // just send this off and hope it worked
    return Localstore.instance
        .collection(matchCollection)
        .doc(matchId.toString())
        .set(_getMatchAsJSON(match, state));
  }

  Future<Map<String, dynamic>> _getMatchesForDate(DateTime date) async {
    return Localstore.instance
        .collection(matchCollection)
        .where('date', isEqualTo: dateKey.format(date))
        .get();
  }

  DateTime previousDate(DateTime date) {
    if (date.month == 1) {
      // this is jan, the date previous is dec
      return DateTime(date.year - 1, 12, date.day);
    } else {
      // just go back a month
      return DateTime(date.year, date.month - 1, date.day);
    }
  }

  Future<Map<String, ActiveMatch>> getMatches(
      MatchPersistenceState state) async {
    // get al the matches for this month and the previous month
    DateTime now = DateTime.now();
    final thisMonthMatches = await _getMatchesForDate(now);
    final lastMonthMatches = await _getMatchesForDate(previousDate(now));
    final documents = {};
    if (null != thisMonthMatches) {
      documents.addAll(thisMonthMatches);
    }
    if (null != lastMonthMatches) {
      documents.addAll(lastMonthMatches);
    }
    // this is a good map of matches, but will contain deleted, last, etc
    if (null != state) {
      final stateString = MatchPersistence.stateString(state);
      documents.removeWhere((key, value) => value['state'] != stateString);
    }
    // and convert everything that's left into actual active matches for the caller
    return documents
        .map((key, value) => MapEntry(key, _createMatchFromJson(value)));
  }
}
