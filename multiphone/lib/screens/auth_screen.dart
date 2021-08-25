import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:google_sign_in/google_sign_in.dart';
import 'package:multiphone/helpers/log.dart';
import 'package:multiphone/helpers/user_data.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/screens/user_screen.dart';
import 'package:multiphone/widgets/auth/auth_form.dart';

enum LoginType { emailPassword, google, apple }

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
    BuildContext ctx,
  ) async {
    // login then!
    UserCredential authResult;
    setState(() {
      _isAuthenticating = true;
    });
    try {
      switch (loginType) {
        case LoginType.emailPassword:
          authResult = await _loginEmailPassword(
              isLoggingIn, email, password, username, ctx);
          break;
        case LoginType.google:
          authResult = await _loginGoogle();
          break;
        case LoginType.apple:
          Log.error('implement apple sign-in then please');
          throw PlatformException(
              code: 'not implemented',
              message: 'apple sign-in is not implemented yet');
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
    }
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(
        content: Text(message), backgroundColor: Theme.of(context).errorColor));
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
    final userData = UserData.create(
        authResult, authResult.user.email, authResult.user.displayName);
    // which we can pop into our database
    await userData.storeData();
    // and return the result
    return authResult;
  }

  Future<UserCredential> _loginEmailPassword(bool isLoggingIn, String email,
      String password, String username, BuildContext ctx) async {
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
