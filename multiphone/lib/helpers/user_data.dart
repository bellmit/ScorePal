import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_auth/firebase_auth.dart';

class UserData {
  final User currentUser;
  String _email;
  String _username;
  bool _isDirty;

  /// private constructor
  UserData._create(
      this.currentUser, this._email, this._username, this._isDirty);

  static UserData create(
      UserCredential userCredential, String email, String username) {
    // from the current user credential, we have a base of data we can wrap
    return UserData._create(
      userCredential.user,
      email,
      username,
      true,
    );
  }

  String get email => _email;

  set email(String email) {
    _email = email;
    _isDirty = true;
  }

  String get username => _username;

  set username(String username) {
    _username = username;
    _isDirty = true;
  }

  bool get isDirty => _isDirty;

  static Future<UserData> load() async {
    // from the current user
    final currentUser = FirebaseAuth.instance.currentUser;
    if (null == currentUser) {
      return Future.error('not logged on');
    }
    // get from firebase
    final doc = await FirebaseFirestore.instance
        .collection('users')
        .doc(currentUser.uid)
        .get();
    // get the data from the document
    final docData = doc.data();
    // and return the wrapper for this data
    return UserData._create(
      currentUser,
      docData['email'],
      docData['username'],
      false,
    );
  }

  Future<void> storeData() {
    return FirebaseFirestore.instance
        .collection('users')
        .doc(currentUser.uid)
        .set({
      'username': _username ?? '',
      'email': _email ?? '',
    }, SetOptions(merge: true)).then((value) => _isDirty = false);
  }
}
