import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart';
import 'package:multiphone/helpers/user_data.dart';
import 'package:multiphone/helpers/values.dart';

class UserForm extends StatefulWidget {
  final UserData userData;

  UserForm(this.userData);

  @override
  _UserFormState createState() => _UserFormState(userData);
}

class _UserFormState extends State<UserForm> {
  final _formKey = GlobalKey<FormState>();

  UserData _userData;
  String passwordError;
  bool _isChangingPassword = false;
  bool _isDeleteRequired = false;
  bool _isChangeInPasswordRequired = false;
  bool _isSaveInProgress = false;
  bool _isVerifySent = false;
  bool _isDirty = false;
  String _newPassword = '';

  _UserFormState(this._userData);

  void _refreshUserData() {
    // reload the state
    _userData.currentUser.reload();
    // and update our data
    UserData.load().then((value) => setState(() => _userData = value));
  }

  void _trySubmit() {
    // validate the form
    final isValid = _formKey.currentState.validate();
    // close the soft keyboard (O:
    FocusScope.of(context).unfocus();

    if (isValid) {
      // login here then - first save all the values to the form
      _formKey.currentState.save();
      // and change this data in the user data and send to firebase
      setState(() {
        _isSaveInProgress = true;
      });
      // update the username them
      _userData.currentUser
          .updateDisplayName(_userData.username)
          .then((value) => _onUsernameUpdated());
    }
  }

  void _changePassword() {
    // validate the form
    final isValid = _formKey.currentState.validate();
    // close the soft keyboard (O:
    FocusScope.of(context).unfocus();

    if (isValid) {
      passwordError = null;
      setState(() {
        _isChangingPassword = true;
      });
      // so we can request a password change here then
      try {
        _userData.currentUser
            .updatePassword(_newPassword)
            .then((value) => setState(() {
                  _isChangingPassword = false;
                  _isChangeInPasswordRequired = false;
                }));
      } catch (error) {
        setState(() {
          passwordError = error.toString();
          _isChangingPassword = false;
        });
      }
    }
  }

  void _deleteAllOnlineUserData() {
    // just delete the user - cloud functions will tidy up our data
    try {
      _userData.currentUser
          .delete()
          .then((value) => Navigator.of(context).pop())
          .onError((error, stackTrace) => _displayError(error));
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
  }

  void _signOut() {
    FirebaseAuth.instance
        .signOut()
        .then((value) => Navigator.of(context).pop());
  }

  void _onUsernameUpdated() {
    // store the data, when done we are no longer saving data
    _userData.storeData().then((value) => setState(() {
          _isSaveInProgress = false;
          _isDirty = false;
        }));
  }

  void _makeDirty() {
    if (!_isDirty) {
      setState(() {
        _isDirty = true;
      });
    }
  }

  void _verifyEmail() {
    setState(() {
      _isVerifySent = true;
    });
    _userData.currentUser.sendEmailVerification();
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
                crossAxisAlignment: CrossAxisAlignment.start,
                children: <Widget>[
                  Row(
                    children: [
                      ConstrainedBox(
                        constraints: BoxConstraints.loose(Size.fromWidth(300)),
                        child: TextFormField(
                          key: ValueKey('username'),
                          initialValue: _userData.username,
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
                            labelText: values.strings.username,
                          ),
                          onChanged: (value) => _makeDirty(),
                          onSaved: (value) {
                            // store this value on the user data
                            _userData.username = value ?? '';
                          },
                        ),
                      ),
                      if (_isSaveInProgress) CircularProgressIndicator(),
                      TextButton(
                          onPressed: _isSaveInProgress || !_isDirty
                              ? null
                              : _trySubmit,
                          child: Text(values.strings.save)),
                    ],
                  ),
                  Padding(
                    padding: const EdgeInsets.all(Values.default_space),
                    child: ElevatedButton(
                        style: values.optionButtonStyle,
                        onPressed: _signOut,
                        child: Text(values.strings.sign_out)),
                  ),
                  if (!_userData.currentUser.emailVerified)
                    Padding(
                      padding: const EdgeInsets.all(Values.default_space),
                      child: Column(
                        children: [
                          Text(
                            values.strings.email_not_verified,
                            style: TextStyle(
                                color: Theme.of(context).primaryColorDark),
                          ),
                          Row(
                            children: [
                              ElevatedButton(
                                style: values.optionButtonStyle,
                                onPressed: _isVerifySent ? null : _verifyEmail,
                                child: Text(
                                    values.strings.verification_email_send),
                              ),
                              IconButton(
                                icon: Icon(Icons.refresh),
                                onPressed: _refreshUserData,
                              ),
                            ],
                          ),
                        ],
                      ),
                    ),
                  Padding(
                    padding: const EdgeInsets.all(Values.default_space),
                    child: ElevatedButton(
                        style: values.optionButtonStyle,
                        onPressed: () => setState(() =>
                            _isChangeInPasswordRequired =
                                !_isChangeInPasswordRequired),
                        child: Text(_isChangeInPasswordRequired
                            ? values.strings.cancel
                            : values.strings.change_password)),
                  ),
                  if (_isChangeInPasswordRequired)
                    Column(
                      children: [
                        TextFormField(
                          key: ValueKey('password'),
                          validator: (value) {
                            // check the password
                            if (null != passwordError) {
                              return passwordError;
                            } else if (value.length < 8) {
                              return values.strings.password_valid;
                            } else {
                              return null;
                            }
                          },
                          decoration: InputDecoration(
                            labelText: values.strings.password,
                          ),
                          obscureText: true,
                          onChanged: (value) => _newPassword = value ?? '',
                        ),
                        TextFormField(
                          key: ValueKey('confirm_password'),
                          validator: (value) {
                            // check the password
                            if (value != _newPassword) {
                              return values.strings.password_match;
                            } else {
                              return null;
                            }
                          },
                          decoration: InputDecoration(
                            labelText: values.strings.confirm_password,
                          ),
                          obscureText: true,
                          onSaved: (value) {
                            // store this value
                            _newPassword = value ?? '';
                          },
                        ),
                        ElevatedButton(
                            style: values.optionButtonStyle,
                            onPressed:
                                _isChangingPassword ? null : _changePassword,
                            child: Text(values.strings.change_password)),
                      ],
                    ),
                  ElevatedButton(
                      style: values.optionButtonStyle,
                      onPressed: _isDeleteRequired
                          ? null
                          : () => setState(() => _isDeleteRequired = true),
                      child: Text(values.strings.delete_user_data)),
                  if (_isDeleteRequired)
                    Padding(
                      padding: const EdgeInsets.all(Values.default_space),
                      child: Column(
                        children: [
                          Text(
                            values.strings.delete_user_data_confirm,
                            style: TextStyle(
                                color: Theme.of(context).primaryColorDark),
                          ),
                          Row(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: [
                              ElevatedButton(
                                style: values.optionButtonStyle,
                                onPressed: _deleteAllOnlineUserData,
                                child: Text(
                                    values.strings.delete_user_data_online),
                              ),
                              SizedBox(width: Values.default_space),
                              ElevatedButton(
                                style: values.optionButtonStyle,
                                onPressed: null,
                                child:
                                    Text(values.strings.delete_user_data_local),
                              ),
                            ],
                          ),
                        ],
                      ),
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
