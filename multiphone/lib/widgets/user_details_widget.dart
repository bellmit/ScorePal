import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/screens/auth_screen.dart';

class UserDetailsWidget extends StatefulWidget {
  const UserDetailsWidget({Key key}) : super(key: key);

  @override
  _UserDetailsWidgetState createState() => _UserDetailsWidgetState();
}

class _UserDetailsWidgetState extends State<UserDetailsWidget> {
  User _currentUser;

  void _signInFirebase() {
    // need to sign into firebase then
    Navigator.of(context).pushNamed(AuthScreen.routeName);
  }

  void _signOutFirebase() {
    // need to sign into firebase then
    FirebaseAuth.instance.signOut();
  }

  @override
  Widget build(BuildContext context) {
    return StreamBuilder<User>(
      // listening to the 'auth' stream to change our screen when
      // the user logs in or out or even when they get booted
      stream: FirebaseAuth.instance.authStateChanges(),
      builder: (ctx, authSnapshot) {
        // login stream just changed
        if (authSnapshot.connectionState == ConnectionState.waiting) {
          // waiting to load the user
          return UserLoggingInWidget();
        } else if (authSnapshot.hasData && authSnapshot.data != null) {
          // user is logged in
          _currentUser = authSnapshot.data;
          return UserLoggedInWidget(
              user: _currentUser, signOutFunction: _signOutFirebase);
        } else {
          // not logged in, show the login options
          return LoginUserWidget(
            signInFunction: _signInFirebase,
          );
        }
      },
    );
  }
}

/// Widget to show the state of the user as they are logged in
class UserLoggedInWidget extends StatelessWidget {
  final User user;
  final Function signOutFunction;
  const UserLoggedInWidget({
    Key key,
    @required this.user,
    @required this.signOutFunction,
  }) : super(key: key);

  String get userName {
    if (null == user) {
      return 'not logged in';
    } else if (user.displayName != null && user.displayName.isNotEmpty) {
      return user.displayName;
    } else if (user.email != null && user.email.isNotEmpty) {
      return user.email;
    } else {
      return user.uid;
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      mainAxisSize: MainAxisSize.max,
      mainAxisAlignment: MainAxisAlignment.start,
      children: <Widget>[
        Row(
          crossAxisAlignment: CrossAxisAlignment.end,
          children: [
            if (user.photoURL != null)
              Container(
                height: Values.image_large,
                width: Values.image_large,
                decoration: new BoxDecoration(
                  shape: BoxShape.circle,
                  image: new DecorationImage(
                      fit: BoxFit.contain,
                      image: new NetworkImage(user.photoURL)),
                ),
              ),
            if (user.photoURL == null)
              Icon(
                Icons.person,
                size: Values.image_large,
              ),
            Padding(
              padding: const EdgeInsets.all(Values.default_space),
              child: Text(
                userName,
                style: TextStyle(color: Theme.of(context).accentColor),
              ),
            ),
          ],
        ),
        Align(
          alignment: Alignment.centerRight,
          child: TextButton.icon(
            onPressed: signOutFunction,
            icon: Icon(
              Icons.exit_to_app,
              color: theme.accentColor,
            ),
            label: Text(
              Values(context).strings.sign_out,
              style: TextStyle(color: theme.accentColor),
            ),
          ),
        ),
      ],
    );
  }
}

/// Widget that shows that the user is currently trying to log in
class UserLoggingInWidget extends StatelessWidget {
  const UserLoggingInWidget({
    Key key,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Text('logging in...');
  }
}

/// Widget that shows the option to log the user into firebase
class LoginUserWidget extends StatelessWidget {
  final Function signInFunction;
  const LoginUserWidget({Key key, this.signInFunction}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return TextButton.icon(
      onPressed: signInFunction,
      icon: Icon(
        Icons.exit_to_app,
        color: theme.accentColor,
      ),
      label: Text(
        Values(context).strings.sign_in,
        style: TextStyle(color: theme.accentColor),
      ),
    );
  }
}
