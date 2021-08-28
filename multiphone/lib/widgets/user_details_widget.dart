import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/match_play_tracker.dart';
import 'package:multiphone/screens/auth_screen.dart';
import 'package:multiphone/screens/user_screen.dart';
import 'package:multiphone/widgets/common/common_widgets.dart';

import 'common/icon_button_widget.dart';

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
          return TextWidget(Values(context).strings.logging_in);
        } else if (authSnapshot.hasData && authSnapshot.data != null) {
          // user is logged in
          _currentUser = authSnapshot.data;
          return UserLoggedInWidget(
              user: _currentUser, signOutFunction: _signOutFirebase);
        } else {
          // not logged in, show the login options
          return IconButtonWidget(_signInFirebase, Icons.login,
              Values(context).strings.sign_in);
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
              IconWidget(
                Icons.person,
                isOnBackground: true,
              ),
            Padding(
              padding: const EdgeInsets.all(Values.default_space),
              child: TextButton(
                child: TextWidget(userName, isOnBackground: true),
                onPressed: () =>
                    MatchPlayTracker.navTo(UserScreen.routeName, context),
              ),
            ),
          ],
        ),
        Align(
          alignment: Alignment.centerRight,
          child: IconButtonWidget(
            signOutFunction,
            Icons.logout,
            Values(context).strings.sign_out,
          ),
        ),
      ],
    );
  }
}
