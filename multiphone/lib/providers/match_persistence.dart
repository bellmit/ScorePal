import 'dart:async';
import 'dart:collection';

import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:localstore/localstore.dart' as LocalStore;
import 'package:multiphone/helpers/log.dart';
import 'package:multiphone/helpers/preferences.dart';
import 'package:multiphone/helpers/values.dart';
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

enum MatchPersistenceSyncState {
  dirty,
  stored,
  failed,
}

class MatchPersistence with ChangeNotifier {
  static const usersCollection = 'users';
  static const matchCollection = 'matches';
  static const lastActivePrefix = 'active';

  static final DateFormat dateKey = DateFormat("yyyy-MM");

  User _user;
  StreamSubscription<User> _userSubscription;

  MatchPersistence() {
    // listen to firebase in here and sync everything that is
    // stored locally there too
    _userSubscription = FirebaseAuth.instance.authStateChanges().listen((user) {
      _user = user;
      if (_user != null) {
        // are now logged in, sync all our data to the store
        _syncDataToFirebase();
      }
    });
  }

  bool get isUserLoggedOn {
    return null != _user;
  }

  @override
  void dispose() {
    // stop listening to firebase then
    if (null != _userSubscription) {
      _userSubscription.cancel();
    }
    // and the base class please
    super.dispose();
  }

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

  static String syncString(MatchPersistenceSyncState state) {
    switch (state) {
      case MatchPersistenceSyncState.dirty:
        return 'dirty';
      case MatchPersistenceSyncState.stored:
        return 'stored';
      case MatchPersistenceSyncState.failed:
        return 'failed';
      default:
        return 'untried';
    }
  }

  static MatchPersistenceSyncState fromSyncString(String state) {
    switch (state) {
      case 'dirty':
        return MatchPersistenceSyncState.dirty;
      case 'stored':
        return MatchPersistenceSyncState.stored;
      case 'failed':
        return MatchPersistenceSyncState.failed;
      default:
        return MatchPersistenceSyncState.dirty;
    }
  }

  void _syncDataToFirebase() {
    if (null == _user) {
      Log.error('Cannot sync data to firebase as there is no user logged in');
      return;
    }
    // get everything that's local and different and send to firebase then
    LocalStore.Localstore.instance
        .collection(matchCollection)
        .where('sync', isEqualTo: syncString(MatchPersistenceSyncState.dirty))
        .get()
        .then((matches) {
      if (null != matches) {
        // we have a map of all the data locally that has no UID in firebase
        matches.forEach((key, value) {
          // the key is the local key, the value is the map of data to send to fb
          // but the local store returns the key of the entire path!
          if (key.startsWith('/$matchCollection/')) {
            key = key.replaceFirst('/$matchCollection/', '');
          }
          if (value['state'] == stateString(MatchPersistenceState.accepted)) {
            // this is an accepted score - send this to firebase
            // we don't want to store deleted or anything else really thank you
            _storeFirebaseData(key, value);
          }
        });
      }
      // so we sent everything, why not get it all back again to this local
      // data while we are at it
      _syncDataFromFirebase();
    });
  }

  void _storeFirebaseData(String matchId, Map<String, Object> data) {
    if (null == _user) {
      Log.error('Cannot store data in firebase as there is no user logged in');
      return;
    }
    // send all our data to firebase now then
    final fbCollection = FirebaseFirestore.instance
        .collection(usersCollection)
        .doc(_user.uid)
        .collection(matchCollection);
    // if this works then the sync state on this data will be different
    data['sync'] = syncString(MatchPersistenceSyncState.stored);
    // set this data into the firebase collection then
    fbCollection.doc(matchId).set(data).then((dataSet) {
      // this worked - the data in the local store is synced now
      _changeSyncStatus(matchId, data, MatchPersistenceSyncState.stored);
    }).onError((error, stackTrace) {
      Log.error(error);
      // this failed, change this state to not try it again for now
      _changeSyncStatus(matchId, data, MatchPersistenceSyncState.failed);
    });
  }

  void _changeSyncStatus(String matchId, Map<String, Object> data,
      MatchPersistenceSyncState state) {
    // change the sync state
    data['sync'] = syncString(state);
    // and send the data out to the local score
    LocalStore.Localstore.instance
        .collection(matchCollection)
        .doc(matchId)
        .set(data, LocalStore.SetOptions(merge: true));
  }

  void _syncDataFromFirebase() {
    //TODO don't do this quite as much as sending as will always get a load of data from firebase
    // let's just get the last few (quite a few) and in descending id order
    FirebaseFirestore.instance
        .collection(usersCollection)
        .doc(_user.uid)
        .collection(matchCollection)
        .orderBy('id', descending: true)
        .limit(Values.firebase_fetch_limit)
        .get()
        .then((value) {
      if (null != value && null != value.docs && value.docs.length > 0) {
        // have a snapshot of everything from firebase
        value.docs.forEach((element) {
          // just push this to the local store if it doesn't exist
          LocalStore.Localstore.instance
              .collection(matchCollection)
              .doc(element.id)
              .get()
              .then((value) {
            // tried to get the doc of this id
            if (value == null) {
              // not got this match
              LocalStore.Localstore.instance
                  .collection(matchCollection)
                  .doc(element.id)
                  .set(element.data())
                  .then((value) {
                // this is a change in the collection of matches we can show
                notifyListeners();
              });
            }
          });
        });
        // and this is a change to this store, we have more data now
      }
    });
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
      'id': matchId.toString(),
      'state': stateString(state),
      'sync': syncString(MatchPersistenceSyncState.dirty),
      'date': dateKey.format(matchId.getDate()),
      'sport': setup.sport.id,
      'setup': setup.getData(),
      'data': match.getData(),
    };
  }

  ActiveMatch _createMatchFromJson(
      Map<String, Object> topLevel, BuildContext context) {
    // what is this, get the sport from the JSON object;
    final sport = Sports.fromId(topLevel['sport'] as String);
    final matchId = MatchId(topLevel['id']);
    // and create the setup for this
    ActiveSetup setup = sport.createSetup();
    // setup the setup from the data stored against the match
    setup.setData(topLevel['setup']);
    // and the match
    ActiveMatch match = sport.createMatch(setup);
    // set our data from this data under the top level
    match.setData(matchId, topLevel['data'], context);
    // and return this now it's setup properly
    return match;
  }

  Future<ActiveMatch> loadLastMatchData(
      ActiveMatch match, BuildContext context) async {
    // just get the last match data and put into the already loaded match
    final setup = match.getSetup();
    final defaultData = await LocalStore.Localstore.instance
        .collection(matchCollection)
        .doc('${lastActivePrefix}_${setup.sport.id}')
        .get();
    if (defaultData != null && defaultData['data'] != null) {
      // have the document, load ths data from this
      final matchId = MatchId(defaultData['id']);
      setup.setData(defaultData['setup']);
      match.setData(matchId, defaultData['data'], context);
    } else {
      Log.error('default match data isn\'t valid for ${setup.sport.id}');
    }
    return match;
  }

  Future<ActiveMatch> loadLastActiveMatch(
      Sport sport, BuildContext context) async {
    if (sport == null) {
      sport = await getLastActiveSport();
    }
    // from this we can create the setup
    final setup = sport.createSetup();
    // and the match
    final match = sport.createMatch(setup);
    // and finally set the data on these
    return loadLastMatchData(match, context);
  }

  Future<dynamic> saveAsLastActiveMatch(ActiveMatch match) {
    final setup = match.getSetup();
    // just send this off and hope it worked
    return LocalStore.Localstore.instance
        .collection(matchCollection)
        .doc('${lastActivePrefix}_${setup.sport.id}')
        .set(_getMatchAsJSON(match, MatchPersistenceState.lastActive));
  }

  void wipeMatchData(ActiveMatch match) {
    // this is the actual delete - from the local store and the remote firebase store
    final matchId = MatchId.create(match);
    // just request that it gets done
    LocalStore.Localstore.instance
        .collection(matchCollection)
        .doc(matchId.toString())
        .delete()
        .onError((error, stackTrace) =>
            Log.error('failed to delete from the local store $error'));
    // and firebase!
    if (null != _user) {
      FirebaseFirestore.instance
          .collection(usersCollection)
          .doc(_user.uid)
          .collection(matchCollection)
          .doc(matchId.toString())
          .delete()
          .onError((error, stackTrace) =>
              Log.error('failed to delete from the firebase store $error'));
    }
  }

  void deleteMatchData(ActiveMatch match) {
    // we don't delete though - what we do is save as state == deleted
    saveMatchData(match, state: MatchPersistenceState.deleted);
  }

  void saveMatchData(ActiveMatch match,
      {MatchPersistenceState state = MatchPersistenceState.backup}) {
    // save the match data as specified in the correct state
    final matchId = MatchId.create(match);
    final matchData = _getMatchAsJSON(match, state);
    // just send this off and hope it worked
    LocalStore.Localstore.instance
        .collection(matchCollection)
        .doc(matchId.toString())
        .set(matchData)
        .then((value) {
      // this was stored locally - let's take the opportunity to send to firebase
      if (null != _user) {
        _storeFirebaseData(matchId.toString(), matchData);
      }
    });
  }

  Future<Map<String, dynamic>> _getMatchesForDate(DateTime date) async {
    return LocalStore.Localstore.instance
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
      MatchPersistenceState state, BuildContext context) async {
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
      documents.removeWhere((key, value) =>
          value['state'] != stateString || value['sport'] == null);
    } else {
      // remove all those nasty ones without a sport set in the bad data
      documents.removeWhere((key, value) => value['sport'] == null);
    }
    // and convert everything that's left into actual active matches for the caller
    final matches = documents.map(
        (key, value) => MapEntry(key, _createMatchFromJson(value, context)));
    // and return this sorted nicely (with the most recent on the top please)
    return SplayTreeMap<String, ActiveMatch>.from(
        matches, (key1, key2) => key2.compareTo(key1));
  }
}
