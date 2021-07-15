import 'package:flutter/material.dart';

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
                      _userEmail = value;
                    },
                  ),
                  if (!_isLoggingIn)
                    TextFormField(
                      key: ValueKey('username'),
                      autocorrect: true,
                      textCapitalization: TextCapitalization.words,
                      enableSuggestions: false,
                      validator: (value) {
                        // check the userame
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
                        _userName = value;
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
                      _userPassword = value;
                    },
                  ),
                  SizedBox(
                    height: 12,
                  ),
                  if (widget._isAuthenticating) CircularProgressIndicator(),
                  if (!widget._isAuthenticating)
                    RaisedButton(
                      child:
                      _isLoggingIn ? Text('Login') : Text('Create Account'),
                      onPressed: _trySubmit,
                    ),
                  FlatButton(
                    textColor: Theme.of(context).primaryColor,
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
