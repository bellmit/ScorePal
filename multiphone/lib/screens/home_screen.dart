import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/screens/panels/settings_panel.dart';
import 'package:multiphone/screens/panels/matches_panel.dart';
import 'package:multiphone/screens/panels/play_match_panel.dart';
import 'package:multiphone/widgets/side_drawer_widget.dart';

class HomeScreen extends StatefulWidget {
  HomeScreen();

  @override
  _HomeScreenState createState() => _HomeScreenState();
}

class HomeScreenPanel {
  final String name;
  final int index;
  final IconData icon;
  final Widget Function() panel;
  const HomeScreenPanel({this.index, this.icon, this.name, this.panel});
}

class _HomeScreenState extends State<HomeScreen> {
  final GlobalKey<ScaffoldState> _scaffoldKey = GlobalKey();
  int _selectedPanel = 0;

  List<HomeScreenPanel> createPanels(BuildContext context) {
    var values = Values(context);
    return <HomeScreenPanel>[
      HomeScreenPanel(
        index: 0,
        icon: Icons.home,
        name: values.strings.option_matches,
        panel: () => MatchesPanel(),
      ),
      HomeScreenPanel(
        index: 1,
        icon: Icons.play_arrow,
        name: values.strings.option_play,
        panel: () => PlayMatchPanel(),
      ),
      HomeScreenPanel(
        index: 2,
        icon: Icons.settings,
        name: values.strings.option_settings,
        panel: () => SettingsPanel(),
      ),
    ];
  }

  void _onPanelSelected(HomeScreenPanel panel) {
    // pass to the same tapped function - this is from the side panel
    _onItemTapped(panel.index);
    // and close the drawer
    _scaffoldKey.currentState.openEndDrawer();
  }

  void _onItemTapped(int index) {
    setState(() {
      _selectedPanel = index;
    });
  }

  @override
  Widget build(BuildContext context) {
    // get the values to get strings from
    var values = Values(context);
    // and the list of panels to use
    final panels = createPanels(context);
    // and return the scaffold
    return Scaffold(
      key: _scaffoldKey,
      appBar: AppBar(
        title: Text(values.strings.title),
        leading: IconButton(
            onPressed: () => _scaffoldKey.currentState.openDrawer(),
            icon: Icon(Icons.more_vert)),
      ),
      backgroundColor: Theme.of(context).primaryColor,
      drawer: SideDrawer(
        panels: panels,
        onPanelSelected: _onPanelSelected,
        currentPanel: panels[_selectedPanel],
      ),
      body: panels.elementAt(_selectedPanel).panel(),
      bottomNavigationBar: BottomNavigationBar(
        // convert the list of panels to bottom navigation bar items to show
        items: panels
            .map((e) => BottomNavigationBarItem(
                  icon: Icon(e.icon),
                  label: e.name,
                ))
            .toList(),
        currentIndex: _selectedPanel,
        selectedItemColor: Theme.of(context).primaryColor,
        onTap: _onItemTapped,
      ),
    );
  }
}
