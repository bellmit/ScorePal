// ignore: unused_import
import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/screens/home_screen.dart';
import 'package:multiphone/screens/setup_match_screen.dart';
import 'package:multiphone/widgets/user_details_widget.dart';

class MenuItem {
  final String name;
  final int index;
  final IconData icon;
  final String navPath;
  const MenuItem({this.index, this.icon, this.name, this.navPath});

  static List<MenuItem> mainMenuItems(BuildContext context) {
    var values = Values(context);
    return <MenuItem>[
      MenuItem(
        index: 0,
        icon: Icons.home,
        name: values.strings.option_matches,
        navPath: HomeScreen.routeName,
      ),
      MenuItem(
        index: 1,
        icon: Icons.play_arrow,
        name: values.strings.option_play,
        navPath: SetupMatchScreen.routeName,
      ),
      MenuItem(
        index: 2,
        icon: Icons.settings,
        name: values.strings.option_settings,
        navPath: '/settings',
      ),
    ];
  }
}

class SideDrawer extends StatelessWidget {
  final List<MenuItem> menuItems;
  final String currentPath;

  const SideDrawer({Key key, this.menuItems, this.currentPath})
      : super(key: key);

  void _onItemSelected(BuildContext context, MenuItem item) {
    Navigator.of(context).pushNamed(item.navPath);
  }

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
            var isSelected = e.navPath == currentPath;
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
              onTap: () => _onItemSelected(context, e),
            );
          }).toList(),
        ],
      ),
    );
  }
}
