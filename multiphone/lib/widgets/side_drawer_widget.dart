// ignore: unused_import
import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/match_play_tracker.dart';
import 'package:multiphone/widgets/user_details_widget.dart';

class MenuItem {
  final String name;
  final int index;
  final IconData icon;
  final void Function(BuildContext) onSelected;
  const MenuItem({this.index, this.icon, this.name, this.onSelected});

  static const menuHome = 0;
  static const menuPlay = 1;
  static const menuSettings = 2;

  static List<MenuItem> mainMenuItems(BuildContext context) {
    var values = Values(context);
    return <MenuItem>[
      MenuItem(
        index: menuHome,
        icon: Icons.home,
        name: values.strings.option_matches,
        onSelected: (context) => MatchPlayTracker.navHome(context),
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
        onSelected: (context) => MatchPlayTracker.navTo('settings', context),
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
    var theme = Theme.of(context);
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
              color: theme.primaryColor,
            ),
            child: UserDetailsWidget(),
          ),
          // and the list of panels, as list tiles
          ...menuItems.map((e) {
            var isSelected = e.index == currentSelection;
            return ListTile(
              selected: isSelected,
              selectedTileColor: theme.primaryColorLight,
              leading: Icon(
                e.icon,
                color: isSelected ? theme.primaryColorDark : theme.primaryColor,
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
      ),
    );
  }
}
