import 'dart:math';
import 'dart:convert';

import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:google_sign_in/google_sign_in.dart';
import 'package:multiphone/helpers/log.dart';
import 'package:multiphone/helpers/user_data.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/screens/user_screen.dart';
import 'package:multiphone/widgets/auth/auth_form.dart';
import 'package:multiphone/widgets/common/common_widgets.dart';
import 'package:crypto/crypto.dart';
import 'package:sign_in_with_apple/sign_in_with_apple.dart';

enum LoginType { emailPassword, google, apple, forgot }

/// Generates a cryptographically secure random nonce, to be included in a
/// credential request.
String generateNonce([int length = 32]) {
  final charset =
      '0123456789ABCDEFGHIJKLMNOPQRSTUVXYZabcdefghijklmnopqrstuvwxyz-._';
  final random = Random.secure();
  return List.generate(length, (_) => charset[random.nextInt(charset.length)])
      .join();
}

/// Returns the sha256 hash of [input] in hex notation.
String sha256ofString(String input) {
  final bytes = utf8.encode(input);
  final digest = sha256.convert(bytes);
  return digest.toString();
}

class AuthScreen extends StatefulWidget {
  static const String routeName = "/auth-screen";

  @override
  _AuthScreenState createState() => _AuthScreenState();
}

class _AuthScreenState extends State<AuthScreen> {
  final _auth = FirebaseAuth.instance;
  var _isAuthenticating = false;

  void _submitAuthForm(
    LoginType loginType,
    String email,
    String password,
    String username,
    bool isLoggingIn,
  ) async {
    // login then!
    UserCredential authResult;
    setState(() {
      _isAuthenticating = true;
    });
    try {
      switch (loginType) {
        case LoginType.emailPassword:
          authResult =
              await _loginEmailPassword(isLoggingIn, email, password, username);
          break;
        case LoginType.google:
          authResult = await _loginGoogle();
          break;
        case LoginType.apple:
          authResult = await _loginApple();
          break;
        default:
          await _forgotPassword(email);
          _displayError(Values(context).strings.warning_password_email_sent);
          // they sent themselves an email, pop back to where they were then
          Navigator.of(context).pop();
          return;
      }
      // if we are here then this worked excellently
      if (false == authResult.user.emailVerified) {
        // this isn't a verified user, ask them to very their details
        Navigator.of(context).popAndPushNamed(UserScreen.routeName);
      } else {
        // they logged in, pop back to where they were then
        Navigator.of(context).pop();
      }
    } on PlatformException catch (error) {
      _displayError(error);
    } catch (error) {
      _displayError(error);
    }
  }

  void _displayError(dynamic error) {
    var message = Values(context).strings.error_online;
    if (error is PlatformException && null != error.message) {
      message = error.message;
    } else if (error is FirebaseException && null != error.message) {
      message = error.message;
    } else if (error is String) {
      message = error;
    }
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(
        content: TextWidget(message),
        backgroundColor: Theme.of(context).errorColor));
    setState(() {
      _isAuthenticating = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Theme.of(context).primaryColor,
      body: AuthForm(_submitAuthForm, _isAuthenticating),
    );
  }

  Future<UserCredential> _loginGoogle() async {
    final GoogleSignInAccount googleUser = await GoogleSignIn(
      scopes: <String>['email', 'profile'],
    ).signIn();
    final GoogleSignInAuthentication googleAuth =
        await googleUser.authentication;
    UserCredential authResult;
    // with the google credential - we can login to firebase so get that here
    final AuthCredential credential = GoogleAuthProvider.credential(
        idToken: googleAuth.idToken, accessToken: googleAuth.accessToken);

    // and sign-in to firebase, waiting for the result
    authResult = await FirebaseAuth.instance.signInWithCredential(credential);
    // this is enough to start with
    String username = authResult.user.displayName;
    if (username == null || username.isEmpty) {
      // try the google given data
      username = googleUser.displayName.trim();
    }
    // if this is the first time, we can create this user data as here
    await _updateUserData(authResult, authResult.user.email, username);
    // and return the user credential
    return authResult;
  }

  Future<UserCredential> _loginApple() async {
    final rawNonce = generateNonce();
    final nonce = sha256ofString(rawNonce);

    // Request credential for the currently signed in Apple account.
    final appleCredential = await SignInWithApple.getAppleIDCredential(
      scopes: [
        AppleIDAuthorizationScopes.email,
        AppleIDAuthorizationScopes.fullName,
      ],
      nonce: nonce,
      /*
      //TODO apple sign in for google doesn't like the redirect URL at all
      webAuthenticationOptions: WebAuthenticationOptions(
        // Set the `clientId` and `redirectUri` arguments to the values you entered in the Apple Developer portal during the setup
        clientId: 'uk.co.darkerwaters.scorepal',
        redirectUri: Uri.parse(
          'https://regal-campus-169014.firebaseapp.com/__/auth/handler',
          'https://scorepal.darkerwaters.co.uk/callbacks/sign_in_with_apple',
        ),
      ),*/
    ).onError((error, stackTrace) {
      Log.error(error.toString());
      throw error;
    });
    // Create an `OAuthCredential` from the credential returned by Apple.
    final oauthCredential = OAuthProvider("apple.com").credential(
      idToken: appleCredential.identityToken,
      rawNonce: rawNonce,
    );
    // Sign in the user with Firebase. If the nonce we generated earlier does
    // not match the nonce in `appleCredential.identityToken`, sign in will fail.
    final authResult =
        await FirebaseAuth.instance.signInWithCredential(oauthCredential);
    // this is enough to start with
    String username = authResult.user.displayName;
    if (username == null || username.isEmpty) {
      // try the apple given data
      username =
          '${appleCredential.givenName} ${appleCredential.familyName}'.trim();
    }
    // if this is the first time, we can create this user data as here
    await _updateUserData(authResult, authResult.user.email, username);
    // and return the result
    return authResult;
  }

  Future<UserData> _updateUserData(
      UserCredential user, String email, String username) async {
    UserData userData = await UserData.loadUserData(user.user);
    if (null == userData) {
      // there isn't any doc in the datastore, create the start of this
      if (username.isEmpty) {
        // set the username from the email
        int index = email.indexOf('@');
        if (index != -1) {
          username = email.substring(0, index);
        }
      }
      // and create the data
      userData = UserData.create(user, email, username);
      // which we can pop into our database
      await userData.storeData();
    }
    // and return
    return userData;
  }

  Future<void> _forgotPassword(String email) {
    return _auth
        .sendPasswordResetEmail(email: email)
        .onError((error, stackTrace) {
      Log.error(error.toString());
      throw error;
    });
  }

  Future<UserCredential> _loginEmailPassword(
    bool isLoggingIn,
    String email,
    String password,
    String username,
  ) async {
    UserCredential authResult;
    if (isLoggingIn) {
      authResult = await _auth.signInWithEmailAndPassword(
        email: email,
        password: password,
      );
    } else {
      authResult = await _auth.createUserWithEmailAndPassword(
        email: email,
        password: password,
      );
      // this is enough to start with
      final userData = UserData.create(authResult, email, username);
      // which we can pop into our database
      await userData.storeData();
      // and update the display name of the user
      await authResult.user.updateDisplayName(username);
    }
    // and return the result
    return authResult;
  }
}
