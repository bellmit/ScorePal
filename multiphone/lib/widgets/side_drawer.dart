import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:multiphone/widgets/user_details_widget.dart';

class SideDrawer extends StatelessWidget {
  final User currentUser;

  const SideDrawer({
    Key key,
    @required this.currentUser,
  }) : super(key: key);

  String get userName {
    if (null == currentUser) {
      return 'not logged in';
    } else if (currentUser.displayName.isNotEmpty) {
      return currentUser.displayName;
    } else if (currentUser.email.isNotEmpty) {
      return currentUser.email;
    } else {
      return currentUser.uid;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Drawer(
      // Add a ListView to the drawer. This ensures the user can scroll
      // through the options in the drawer if there isn't enough vertical
      // space to fit everything.
      child: ListView(
        // Important: Remove any padding from the ListView.
        padding: EdgeInsets.zero,
        children: <Widget>[
          DrawerHeader(
            decoration: BoxDecoration(
              color: Theme.of(context).primaryColor,
            ),
            child: UserDetailsWidget(),
          ),
          ListTile(
            title: Text('Item 1'),
            onTap: () {
              // Update the state of the app.
              // ...
            },
          ),
          ListTile(
            title: Text('Item 2'),
            onTap: () {
              // Update the state of the app.
              // ...
            },
          ),
        ],
      ),
    );
  }
}
