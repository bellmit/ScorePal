// ignore: unused_import
import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/screens/matches_screen.dart';
import 'package:multiphone/screens/play_match_screen.dart';
import 'package:multiphone/screens/settings_screen.dart';
import 'package:multiphone/widgets/side_drawer_widget.dart';

class HomeScreen extends StatefulWidget {
  HomeScreen();

  @override
  _HomeScreenState createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  final GlobalKey<ScaffoldState> _scaffoldKey = GlobalKey();
  int _selectedScreen = 0;

  static const List<Widget> _screens = <Widget>[
    MatchesScreen(),
    PlayMatchScreen(),
    SettingsScreen(),
  ];

  void _onItemTapped(int index) {
    setState(() {
      _selectedScreen = index;
    });
  }

  @override
  Widget build(BuildContext context) {
    // get the values to get strings from
    var values = Values(context);
    // and return the scaffold
    return Scaffold(
      key: _scaffoldKey,
      appBar: AppBar(
        title: Text('Scorepal'),
        leading: IconButton(
            onPressed: () => _scaffoldKey.currentState.openDrawer(),
            icon: Icon(Icons.more_vert)),
      ),
      backgroundColor: Theme.of(context).primaryColor,
      drawer: SideDrawer(),
      body: _screens.elementAt(_selectedScreen),
      bottomNavigationBar: BottomNavigationBar(
        items: <BottomNavigationBarItem>[
          BottomNavigationBarItem(
            icon: Icon(Icons.home),
            label: values.strings.option_matches,
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.play_arrow),
            label: values.strings.option_play,
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.settings),
            label: values.strings.option_settings,
          ),
        ],
        currentIndex: _selectedScreen,
        selectedItemColor: Theme.of(context).primaryColor,
        onTap: _onItemTapped,
      ),
    );
  }
}
