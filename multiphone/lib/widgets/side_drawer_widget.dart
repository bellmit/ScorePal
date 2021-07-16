// ignore: unused_import
import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:multiphone/screens/home_screen.dart';
import 'package:multiphone/widgets/user_details_widget.dart';

class SideDrawer extends StatelessWidget {
  final List<HomeScreenPanel> panels;
  final void Function(HomeScreenPanel) onPanelSelected;
  final HomeScreenPanel currentPanel;

  const SideDrawer(
      {Key key, this.panels, this.onPanelSelected, this.currentPanel})
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
          ...panels.map((e) {
            var isSelected = e == currentPanel;
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
              onTap: () => onPanelSelected(e),
            );
          }).toList(),
        ],
      ),
    );
  }
}
