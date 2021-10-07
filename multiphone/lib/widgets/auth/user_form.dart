import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:multiphone/helpers/user_data.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/providers/match_persistence.dart';
import 'package:multiphone/widgets/common/common_widgets.dart';
import 'package:multiphone/widgets/common/icon_button_widget.dart';
import 'package:provider/provider.dart';

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
  bool _isLoggedIn = false;
  bool _isChangingPassword = false;
  bool _isDeleteRequired = false;
  bool _isChangeInPasswordRequired = false;
  bool _isSaveInProgress = false;
  bool _isDeleteInProgress = false;
  bool _isVerifySent = false;
  bool _isDirty = false;
  String _newPassword = '';

  _UserFormState(this._userData)
      : _isLoggedIn = _userData != null && _userData.currentUser != null;

  void _refreshUserData() {
    // reload the state
    if (_isLoggedIn) {
      _userData.currentUser.reload();
    }
    // and update our data
    UserData.load().then(
      (value) => setState(() {
        _userData = value;
        _isLoggedIn = _userData != null && _userData.currentUser != null;
        return _userData;
      }),
    );
  }

  void _trySubmit() {
    // validate the form
    final isValid = _formKey.currentState.validate();
    // close the soft keyboard (O:
    FocusScope.of(context).unfocus();

    if (isValid && _isLoggedIn) {
      // login here then - first save all the values to the form
      _formKey.currentState.save();
      // and change this data in the user data and send to firebase
      setState(() {
        _isSaveInProgress = true;
      });
      // update the username then
      _userData.currentUser
          .updateDisplayName(_userData.username)
          .then((value) => _onUsernameUpdated());
      // and the firebase data please
      _userData.storeData();
    }
  }

  void _changePassword() {
    // validate the form
    final isValid = _formKey.currentState.validate();
    // close the soft keyboard (O:
    FocusScope.of(context).unfocus();

    if (isValid && _isLoggedIn) {
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
      if (_isLoggedIn) {
        _userData.currentUser
            .delete()
            .then((value) => Navigator.of(context).pop())
            .onError((error, stackTrace) => _displayError(error));
      }
    } catch (error) {
      _displayError(error);
    }
  }

  void _deleteAllLocalUserData() {
    // just delete all the local data
    setState(() => _isDeleteInProgress = true);
    Provider.of<MatchPersistence>(context, listen: false)
        .deleteAllLocalData()
        .then((value) => setState(() {
              ScaffoldMessenger.of(context).showSnackBar(SnackBar(
                content: TextWidget(
                    Values(context).strings.warning_all_local_data_deleted),
              ));
              _isDeleteInProgress = false;
            }))
        .onError((error, stackTrace) => _displayError(error));
  }

  void _displayError(dynamic error) {
    var message = Values(context).strings.error_online;
    if (error is PlatformException && null != error.message) {
      message = error.message;
    } else if (error is FirebaseException && null != error.message) {
      message = error.message;
    }
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(
        content: TextWidget(message),
        backgroundColor: Theme.of(context).errorColor));
  }

  void _signOut() {
    FirebaseAuth.instance
        .signOut()
        .then((value) => Navigator.of(context).pop());
  }

  void _onUsernameUpdated() {
    if (_isLoggedIn) {
      // store the data, when done we are no longer saving data
      _userData.storeData().then((value) => setState(() {
            _isSaveInProgress = false;
            _isDirty = false;
          }));
    }
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
    if (_isLoggedIn) {
      _userData.currentUser.sendEmailVerification();
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
                crossAxisAlignment: CrossAxisAlignment.start,
                children: <Widget>[
                  Row(
                    mainAxisSize: MainAxisSize.max,
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      if (_isLoggedIn && _userData.currentUser.photoURL != null)
                        Container(
                          height: Values.image_large,
                          width: Values.image_large,
                          decoration: new BoxDecoration(
                            shape: BoxShape.circle,
                            image: new DecorationImage(
                                fit: BoxFit.contain,
                                image: new NetworkImage(
                                    _userData.currentUser.photoURL)),
                          ),
                        ),
                      if (!_isLoggedIn ||
                          _userData.currentUser.photoURL == null)
                        IconWidget(Icons.person),
                      Expanded(
                        child: Padding(
                          padding: const EdgeInsets.all(Values.default_space),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              TextWidget(
                                _isLoggedIn ? _userData.currentUser.email : '',
                              ),
                              if (_isLoggedIn &&
                                  _userData.currentUser.providerData.any(
                                      (element) =>
                                          element.providerId.toLowerCase() ==
                                          'google.com'))
                                Row(
                                  children: <Widget>[
                                    IconWidget(FontAwesomeIcons.google,
                                        size: Values.image_icon),
                                    SizedBox(width: 10),
                                    TextWidget(
                                      values.strings.provider_google,
                                    ),
                                  ],
                                ),
                              if (_isLoggedIn &&
                                  _userData.currentUser.providerData.any(
                                      (element) =>
                                          element.providerId.toLowerCase() ==
                                          'apple.com'))
                                Row(
                                  children: <Widget>[
                                    IconWidget(FontAwesomeIcons.apple,
                                        size: Values.image_icon),
                                    SizedBox(width: 10),
                                    TextWidget(
                                      values.strings.provider_apple,
                                    ),
                                  ],
                                ),
                            ],
                          ),
                        ),
                      ),
                    ],
                  ),
                  Row(
                    children: [
                      ConstrainedBox(
                        constraints: BoxConstraints.loose(Size.fromWidth(250)),
                        child: TextFormField(
                          key: ValueKey('username'),
                          initialValue: _isLoggedIn ? _userData.username : '',
                          autocorrect: true,
                          enabled: _isLoggedIn,
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
                          onChanged: (value) => _makeDirty(),
                          onSaved: (value) {
                            // store this value on the user data
                            if (_isLoggedIn) {
                              _userData.username = value ?? '';
                            }
                          },
                        ),
                      ),
                      if (_isSaveInProgress) CircularProgressIndicator(),
                      if (!_isSaveInProgress)
                        TextButton(
                            onPressed: !_isDirty ? null : _trySubmit,
                            child: TextWidget(values.strings.save)),
                    ],
                  ),
                  IconButtonWidget(_isLoggedIn ? _signOut : null, Icons.logout,
                      values.strings.sign_out),
                  if (_isLoggedIn && !_userData.currentUser.emailVerified)
                    Padding(
                      padding: const EdgeInsets.all(Values.default_space),
                      child: Column(
                        children: [
                          TextWidget(
                            values.strings.email_not_verified,
                          ),
                          Wrap(
                            spacing: Values.default_space,
                            children: [
                              IconButtonWidget(
                                _isVerifySent ? null : _verifyEmail,
                                Icons.mark_email_read,
                                values.strings.verification_email_send,
                              ),
                              IconButtonWidget(
                                _refreshUserData,
                                Icons.refresh,
                                values.strings.title_user_refresh_data,
                              ),
                            ],
                          ),
                        ],
                      ),
                    ),
                  IconButtonWidget(
                    _isLoggedIn
                        ? () => setState(() => _isChangeInPasswordRequired =
                            !_isChangeInPasswordRequired)
                        : null,
                    Icons.password,
                    _isChangeInPasswordRequired
                        ? values.strings.cancel
                        : values.strings.change_password,
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
                          enabled: _isLoggedIn,
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
                          enabled: _isLoggedIn,
                          decoration: InputDecoration(
                            labelText: values.strings.confirm_password,
                          ),
                          obscureText: true,
                          onSaved: (value) {
                            // store this value
                            _newPassword = value ?? '';
                          },
                        ),
                        IconButtonWidget(
                          !_isLoggedIn || _isChangingPassword
                              ? null
                              : _changePassword,
                          Icons.password,
                          values.strings.change_password,
                        ),
                      ],
                    ),
                  IconButtonWidget(
                    _isDeleteRequired
                        ? null
                        : () => setState(() => _isDeleteRequired = true),
                    Icons.delete,
                    values.strings.delete_user_data,
                  ),
                  if (_isDeleteRequired)
                    Padding(
                      padding: const EdgeInsets.all(Values.default_space),
                      child: Column(
                        children: [
                          TextWidget(
                            values.strings.delete_user_data_confirm,
                          ),
                          Wrap(
                            children: [
                              IconButtonWidget(
                                _isLoggedIn ? _deleteAllOnlineUserData : null,
                                Icons.delete_sweep,
                                values.strings.delete_user_data_online,
                              ),
                              SizedBox(width: Values.default_space),
                              IconButtonWidget(
                                _isDeleteInProgress
                                    ? null
                                    : _deleteAllLocalUserData,
                                Icons.delete_sweep,
                                values.strings.delete_user_data_local,
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
