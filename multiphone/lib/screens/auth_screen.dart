import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:multiphone/widgets/auth/auth_form.dart';

class AuthScreen extends StatefulWidget {
  static const String routeName = "/auth-screen";

  @override
  _AuthScreenState createState() => _AuthScreenState();
}

class _AuthScreenState extends State<AuthScreen> {
  final _auth = FirebaseAuth.instance;
  var _isAuthenticating = false;

  void _submitAuthForm(
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
        // let's create the user details then
        await FirebaseFirestore.instance
            .collection('users')
            .doc(authResult.user.uid)
            .set({
          'username': username,
          'email': email,
        });
      }
      // if we are here then this worked excellently
      Navigator.of(context).pop();
    } on PlatformException catch (error) {
      var message = 'An error occurred, please check your credentials';

      if (error.message != null) {
        message = error.message;
      }
      Scaffold.of(ctx).showSnackBar(
        SnackBar(
          content: Text(message),
          backgroundColor: Theme.of(context).errorColor,
        ),
      );
      setState(() {
        _isAuthenticating = false;
      });
    } catch (error) {
      // shouldn't be many
      print(error);
      setState(() {
        _isAuthenticating = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Theme.of(context).primaryColor,
      body: AuthForm(_submitAuthForm, _isAuthenticating),
    );
  }
}
