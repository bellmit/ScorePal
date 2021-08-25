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
import 'package:multiphone/providers/match_persistence.dart';
import 'package:multiphone/providers/sport.dart';

class MatchInbox with ChangeNotifier {
  static const usersCollection = 'users';
  static const inboxCollection = 'inbox';

  User _user;
  StreamSubscription<User> _userSubscription;

  final Map<String, Map<String, Object>> _inbox = {};

  MatchInbox() {
    // listen to firebase in here and reveal all the matches in our inbox
    _userSubscription = FirebaseAuth.instance.authStateChanges().listen((user) {
      _user = user;
      if (_user != null) {
        // are now logged in, sync all our data from the store
        fetchDataFromFirebase();
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

  Future<void> fetchDataFromFirebase() {
    if (null == _user) {
      return Future.error('user not logged in');
    }
    // let's just get the last few (quite a few) and in descending id order
    return FirebaseFirestore.instance
        .collection(usersCollection)
        .doc(_user.uid)
        .collection(inboxCollection)
        .orderBy('id', descending: true)
        .limit(Values.firebase_fetch_limit)
        .get()
        .then((value) {
      if (null != value && null != value.docs && value.docs.length > 0) {
        // have a snapshot of everything from firebase
        value.docs.forEach((element) {
          // store the data in our member list
          final firebaseData = element.data();
          _inbox[firebaseData['id']] = firebaseData;
          // this is a change in the collection of matches we can show
          notifyListeners();
        });
        // and this is a change to this store, we have more data now
      }
    });
  }

  List<ActiveMatch> getMatches(BuildContext context) {
    // return the contents of our inbox as matches then
    return _inbox.values
        .map((e) => MatchPersistence.createMatchFromJson(e, context))
        .toList();
  }

  Future<bool> isInboxEmpty() {
    if (null == _user) {
      return Future.error('user not logged in');
    }
    return FirebaseFirestore.instance
        .collection(usersCollection)
        .doc(_user.uid)
        .collection(inboxCollection)
        .limit(1)
        .get()
        .then((value) => value.size == 0);
  }

  Future<void> deleteMatch(ActiveMatch match) {
    if (null == _user) {
      return Future.error('user not logged in');
    }
    final matchId = MatchId.create(match).toString();
    return FirebaseFirestore.instance
        .collection(usersCollection)
        .doc(_user.uid)
        .collection(inboxCollection)
        .doc(matchId)
        .delete()
        .then((value) {
      // when deleted, remove the key from the internal map
      _inbox.remove(matchId);
    });
  }

  Future<void> acceptMatch(ActiveMatch match) {
    if (null == _user) {
      return Future.error('user not logged in');
    }
    // we can transfer this match data from our inbox to our actual matches
    return MatchPersistence.putMatchInFirebase(
      match,
      _user,
      state: MatchPersistenceState.accepted,
    ).then((value) {
      // and delete this match now it is transferred
      return deleteMatch(match);
    });
  }
}
