// ignore: unused_import
import 'dart:async';

import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/match_play_tracker.dart';
import 'package:multiphone/screens/attributions_screen.dart';
import 'package:multiphone/screens/inbox_screen.dart';
import 'package:multiphone/screens/settings_screen.dart';
import 'package:multiphone/screens/trash_screen.dart';
import 'package:multiphone/widgets/user_details_widget.dart';
import 'package:provider/provider.dart';

class MenuItem {
  final String name;
  final int index;
  final IconData icon;
  final bool isForLoggedOnOnly;
  final void Function(BuildContext) onSelected;
  const MenuItem(
      {this.index,
      this.icon,
      this.name,
      this.onSelected,
      this.isForLoggedOnOnly = false});

  static const menuHome = 0;
  static const menuPlay = 1;
  static const menuSettings = 2;
  static const menuTrash = 3;
  static const menuAttributions = 4;
  static const menuSetupFlic2 = 5;
  static const menuInbox = 6;

  static List<MenuItem> mainMenuItems(BuildContext context) {
    final values = Values(context);
    return <MenuItem>[
      MenuItem(
        index: menuHome,
        icon: Icons.home,
        name: values.strings.option_matches,
        onSelected: (context) => MatchPlayTracker.navHome(context),
      ),
      MenuItem(
        index: menuInbox,
        icon: Icons.mail,
        name: values.strings.option_inbox,
        isForLoggedOnOnly: true,
        onSelected: (context) =>
            MatchPlayTracker.navTo(InboxScreen.routeName, context),
      ),
      MenuItem(
        index: menuPlay,
        icon: Icons.play_arrow,
        name: values.strings.option_play,
        onSelected: (context) => MatchPlayTracker.setupNewMatch(context),
      ),
      MenuItem(
        index: menuSettings,
        icon: Icons.settings,
        name: values.strings.option_settings,
        onSelected: (context) =>
            MatchPlayTracker.navTo(SettingsScreen.routeName, context),
      ),
      MenuItem(
        index: menuTrash,
        icon: Icons.delete_outline,
        name: values.strings.option_trash,
        onSelected: (context) =>
            MatchPlayTracker.navTo(TrashScreen.routeName, context),
      ),
      MenuItem(
        index: menuAttributions,
        icon: Icons.photo_library,
        name: values.strings.option_attributions,
        onSelected: (context) =>
            MatchPlayTracker.navTo(AttributionsScreen.routeName, context),
      ),
    ];
  }
}

class SideDrawer extends StatelessWidget {
  final List<MenuItem> menuItems;
  final int currentSelection;

  const SideDrawer({Key key, this.menuItems, this.currentSelection})
      : super(key: key);

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Drawer(
      // Add a ListView to the drawer. This ensures the user can scroll
      // through the options in the drawer if there isn't enough vertical
      // space to fit everything.
      child: StreamBuilder<User>(
        stream: FirebaseAuth.instance.authStateChanges(),
        builder: (ctx, snapshot) {
          return ListView(
            // Important: Remove any padding from the ListView.
            padding: EdgeInsets.zero,
            children: <Widget>[
              DrawerHeader(
                decoration: BoxDecoration(
                  color: theme.primaryColor,
                ),
                child: UserDetailsWidget(),
              ),
              // and the list of panels, as list tiles
              ...menuItems.map((e) {
                final isSelected = e.index == currentSelection;
                if (e.isForLoggedOnOnly && snapshot.data == null) {
                  // this item only to be shown when logged on, and we are not
                  return Container();
                }
                return ListTile(
                  selected: isSelected,
                  selectedTileColor: theme.primaryColorLight,
                  leading: Icon(
                    e.icon,
                    color: isSelected
                        ? theme.primaryColorDark
                        : theme.primaryColor,
                  ),
                  title: Text(e.name,
                      style: TextStyle(
                          color: isSelected
                              ? theme.primaryColorDark
                              : theme.primaryColor)),
                  onTap: () => e.onSelected(context),
                );
              }).toList(),
            ],
          );
        },
      ),
    );
  }
}
