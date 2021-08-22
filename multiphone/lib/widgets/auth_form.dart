import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:google_sign_in/google_sign_in.dart';
import 'package:apple_sign_in/apple_sign_in.dart';

class AuthForm extends StatefulWidget {
  final void Function(
    String email,
    String password,
    String userName,
    bool isLoggingIn,
    BuildContext context,
  ) _onSubmit;
  final bool _isAuthenticating;

  AuthForm(this._onSubmit, this._isAuthenticating);

  @override
  _AuthFormState createState() => _AuthFormState();
}

class _AuthFormState extends State<AuthForm> {
  final _formKey = GlobalKey<FormState>();
  var _isLoggingIn = true;

  String _userEmail = '';
  String _userName = '';
  String _userPassword = '';

  void _trySubmit() {
    // validate the form
    final isValid = _formKey.currentState.validate();
    // close the soft keyboard (O:
    FocusScope.of(context).unfocus();

    if (isValid) {
      // login here then - first save all the values to the form
      _formKey.currentState.save();
      // and use the members to auth ourselves in firebase
      widget._onSubmit(
        _userEmail.trim(),
        _userPassword.trim(),
        _userName.trim(),
        _isLoggingIn,
        context,
      );
    }
  }

  Future<void> _signInViaGoogle() async {
    final GoogleSignInAccount googleUser = await GoogleSignIn(
      scopes: <String>['email', 'profile'],
    ).signIn();
    final GoogleSignInAuthentication googleAuth =
        await googleUser.authentication;

    final AuthCredential credential = GoogleAuthProvider.credential(
        idToken: googleAuth.idToken, accessToken: googleAuth.accessToken);

    // and sign-in to firebase
    FirebaseAuth.instance
        .signInWithCredential(credential)
        .then((value) => Navigator.of(context).pop());
  }

  Future<void> _signInViaApple() async {
    final AuthorizationResult result = await AppleSignIn.performRequests([
      AppleIdRequest(requestedScopes: [Scope.email, Scope.fullName])
    ]);
    switch (result.status) {
      case AuthorizationStatus.authorized:
        // log in to firebase here then
        final appleIdCredential = result.credential;
        final oAuthProvider = OAuthProvider('apple.com');
        final credential = oAuthProvider.credential(
          idToken: String.fromCharCodes(appleIdCredential.identityToken),
          accessToken:
              String.fromCharCodes(appleIdCredential.authorizationCode),
        );
        FirebaseAuth.instance
            .signInWithCredential(credential)
            .then((value) => Navigator.of(context).pop());
        break;

      case AuthorizationStatus.error:
        print("Sign in failed: ${result.error.localizedDescription}");
        break;
      case AuthorizationStatus.cancelled:
        print('User cancelled');
        break;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Card(
        margin: EdgeInsets.all(20),
        child: SingleChildScrollView(
          child: Padding(
            padding: EdgeInsets.all(16),
            child: Form(
              key: _formKey,
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: <Widget>[
                  TextFormField(
                    key: ValueKey('email'),
                    autocorrect: false,
                    textCapitalization: TextCapitalization.none,
                    enableSuggestions: false,
                    validator: (value) {
                      // check the email address
                      if (value.isEmpty || !value.contains('@')) {
                        return 'Please enter a valid email address.';
                      } else {
                        return null;
                      }
                    },
                    keyboardType: TextInputType.emailAddress,
                    decoration: InputDecoration(
                      labelText: 'Email address',
                    ),
                    onSaved: (value) {
                      // store this value
                      _userEmail = value ?? 'none';
                    },
                  ),
                  if (!_isLoggingIn)
                    TextFormField(
                      key: ValueKey('username'),
                      autocorrect: true,
                      textCapitalization: TextCapitalization.words,
                      enableSuggestions: false,
                      validator: (value) {
                        // check the username
                        if (value.length < 4) {
                          return 'Please enter a valid username at least 4 characters long.';
                        } else {
                          return null;
                        }
                      },
                      decoration: InputDecoration(
                        labelText: 'Username',
                      ),
                      onSaved: (value) {
                        // store this value
                        _userName = value ?? 'none';
                      },
                    ),
                  TextFormField(
                    key: ValueKey('password'),
                    validator: (value) {
                      // check the password
                      if (value.length < 7) {
                        return 'Please enter a valid password at least 7 characters long.';
                      } else {
                        return null;
                      }
                    },
                    decoration: InputDecoration(
                      labelText: 'Password',
                    ),
                    obscureText: true,
                    onSaved: (value) {
                      // store this value
                      _userPassword = value ?? 'none';
                    },
                  ),
                  SizedBox(
                    height: 12,
                  ),
                  if (widget._isAuthenticating) CircularProgressIndicator(),
                  if (!widget._isAuthenticating)
                    Wrap(
                      spacing: Values.image_medium,
                      runSpacing: Values.default_space,
                      children: [
                        MaterialButton(
                          height: Values.image_large,
                          onPressed: _trySubmit,
                          color: Theme.of(context).primaryColor,
                          child: Text(_isLoggingIn ? 'Login' : 'Create Account',
                              style:
                                  TextStyle(color: Colors.white, fontSize: 16)),
                          textColor: Colors.white,
                        ),
                        MaterialButton(
                          height: Values.image_large,
                          onPressed: _signInViaGoogle,
                          color: Colors.blue,
                          child: Wrap(
                            children: <Widget>[
                              Icon(FontAwesomeIcons.google),
                              SizedBox(width: 10),
                              Text('Sign-in using Google',
                                  style: TextStyle(
                                      color: Colors.white, fontSize: 16)),
                            ],
                          ),
                          textColor: Colors.white,
                        ),
                        MaterialButton(
                          height: Values.image_large,
                          onPressed: _signInViaApple,
                          color: Colors.blueGrey,
                          child: Wrap(
                            children: <Widget>[
                              Icon(FontAwesomeIcons.apple),
                              SizedBox(width: 10),
                              Text('Sign-in using Apple',
                                  style: TextStyle(
                                      color: Colors.white, fontSize: 16)),
                            ],
                          ),
                          textColor: Colors.white,
                        ),
                      ],
                    ),
                  TextButton(
                    child: _isLoggingIn
                        ? Text('Create new account')
                        : Text('I already have an account'),
                    onPressed: () {
                      // change our state
                      setState(() {
                        _isLoggingIn = !_isLoggingIn;
                      });
                    },
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}
