import 'dart:io';

import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/screens/auth_screen.dart';
import 'package:email_validator/email_validator.dart';
import 'package:multiphone/widgets/common/common_widgets.dart';
import 'package:multiphone/widgets/common/icon_button_widget.dart';
import 'package:sign_in_with_apple/sign_in_with_apple.dart';

class AuthForm extends StatefulWidget {
  final void Function(LoginType loginType, String email, String password,
      String userName, bool isLoggingIn) _onSubmit;
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
  bool _isAppleSignInAvailable = false;

  void _trySubmit(LoginType loginType) {
    var isSubmit;
    if (loginType == LoginType.emailPassword) {
      // validate the form
      final isValid = _formKey.currentState.validate();
      // close the soft keyboard (O:
      FocusScope.of(context).unfocus();

      if (isValid) {
        // login here then - first save all the values to the form
        _formKey.currentState.save();
        isSubmit = true;
      }
    } else {
      // google and apple, just go ahead
      isSubmit = true;
    }
    if (isSubmit) {
      // and use the members to auth ourselves in firebase
      widget._onSubmit(
        loginType,
        _userEmail.trim(),
        _userPassword.trim(),
        _userName.trim(),
        _isLoggingIn,
      );
    }
  }

  @override
  void initState() {
    super.initState();
    if (Platform.isIOS) {
      SignInWithApple.isAvailable()
          .then((value) => setState(() => _isAppleSignInAvailable = value));
    } else {
      // apple sign in not working )O:
      _isAppleSignInAvailable = false;
    }
  }

  @override
  Widget build(BuildContext context) {
    final values = Values(context);
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
                    enableSuggestions: true,
                    textCapitalization: TextCapitalization.none,
                    validator: (value) {
                      // check the email address
                      if (value.isEmpty ||
                          !EmailValidator.validate(value) ||
                          !value.contains('@') ||
                          value.contains('|')) {
                        return values.strings.email_valid;
                      } else {
                        return null;
                      }
                    },
                    keyboardType: TextInputType.emailAddress,
                    decoration: InputDecoration(
                      labelText: values.strings.email_address,
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
                      enableSuggestions: true,
                      validator: (value) {
                        // check the username
                        if (!Values.isUsernameValid(value)) {
                          return values.strings.username_valid;
                        } else {
                          return null;
                        }
                      },
                      decoration: InputDecoration(
                        labelText: values.strings.username,
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
                      if (value.length < 8) {
                        return values.strings.password_valid;
                      } else {
                        return null;
                      }
                    },
                    decoration: InputDecoration(
                      labelText: values.strings.password,
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
                        IconButtonWidget(
                            () => _trySubmit(LoginType.emailPassword),
                            Icons.login,
                            _isLoggingIn
                                ? values.strings.sign_in
                                : values.strings.create_account),
                        IconButtonWidget(
                            () => _trySubmit(LoginType.google),
                            FontAwesomeIcons.google,
                            values.strings.signin_google),
                        if (_isAppleSignInAvailable)
                          IconButtonWidget(
                              () => _trySubmit(LoginType.apple),
                              FontAwesomeIcons.apple,
                              values.strings.signin_apple),
                      ],
                    ),
                  TextButton(
                    child: _isLoggingIn
                        ? TextWidget(values.strings.create_account)
                        : TextWidget(values.strings.already_account),
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
