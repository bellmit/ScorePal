import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/match_play_tracker.dart';
import 'package:multiphone/widgets/side_drawer_widget.dart';

class SettingsScreen extends StatefulWidget {
  static const String routeName = '/settings';

  SettingsScreen();

  @override
  _SettingsScreenState createState() => _SettingsScreenState();
}

class _SettingsScreenState extends State<SettingsScreen> {
  final GlobalKey<ScaffoldState> _scaffoldKey = GlobalKey();

  @override
  Widget build(BuildContext context) {
    // get the values to get strings from
    var values = Values(context);
    // and return the scaffold
    return Scaffold(
      key: _scaffoldKey,
      appBar: AppBar(
        title: Text(values.strings.title_settings),
        leading: IconButton(
            onPressed: () => _scaffoldKey.currentState.openDrawer(),
            icon: Icon(Icons.more_vert)),
      ),
      drawer: SideDrawer(
        menuItems: MenuItem.mainMenuItems(context),
        currentSelection: MenuItem.menuSettings,
      ),
      body: Center(
        child: Text('Change Settings'),
      ),
      floatingActionButton: FloatingActionButton(
        heroTag: ValueKey<String>('play_match'),
        onPressed: () {
          MatchPlayTracker.setupNewMatch(context);
        },
        child: const Icon(Icons.play_arrow),
        backgroundColor: Theme.of(context).accentColor,
      ),
    );
  }
}
