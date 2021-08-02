import 'package:flutter/material.dart';
import 'package:multiphone/helpers/log.dart';
import 'package:multiphone/match/match_id.dart';
import 'package:multiphone/providers/match_persistence.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/screens/setup_match_screen.dart';
import 'package:multiphone/widgets/played_match_summary_widget.dart';
import 'package:multiphone/widgets/side_drawer_widget.dart';
import 'package:provider/provider.dart';

class HomeScreen extends StatefulWidget {
  static const String routeName = '/';

  HomeScreen();

  @override
  _HomeScreenState createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  final GlobalKey<ScaffoldState> _scaffoldKey = GlobalKey();
  List<ActiveMatch> matches;

  void _onMatchMenuItemSelected(
    ActiveMatch match,
    PlayedMatchSummaryMenuItem option,
  ) {
    // deal with this being selected for the match
    switch (option) {
      case PlayedMatchSummaryMenuItem.resume:
        //TODO want to resume the match
        Log.debug("need to implement this resume match");
        break;
      case PlayedMatchSummaryMenuItem.share:
        //TODO want to share the match
        Log.debug("need to implement this share match");
        break;
      case PlayedMatchSummaryMenuItem.delete:
        // this is called from a menu item, still want to remove
        // this from the list though
        if (null != matches) {
          final index = matches.indexOf(match);
          if (index >= 0) {
            _deleteMatch(match, index);
          }
        }
        break;
    }
  }

  @override
  void initState() {
    super.initState();
    // get our matches to show from the persistence provider
    Provider.of<MatchPersistence>(context, listen: false)
        .getMatches(MatchPersistenceState.accepted)
        .then((value) => {
              // have the matches back here, set them locally
              setState(() {
                matches = List.of(value.values);
              })
            });
  }

  void _deleteMatch(ActiveMatch match, int index) {
    // remove the match from the list to remove from view
    setState(() {
      matches.removeAt(index);
    });
    // delete the match using the provider so it's actually done
    Provider.of<MatchPersistence>(context, listen: false)
        .deleteMatchData(match);
    // Then show a snackbar to inform the user that this worked ok
    ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(Values(context).strings.match_deleted)));
  }

  @override
  Widget build(BuildContext context) {
    // get the values to get strings from
    var values = Values(context);
    // and return the scaffold
    return Scaffold(
      key: _scaffoldKey,
      appBar: AppBar(
        title: Text(values.strings.title),
        leading: IconButton(
            onPressed: () => _scaffoldKey.currentState.openDrawer(),
            icon: const Icon(Icons.more_vert)),
      ),
      drawer: SideDrawer(
          menuItems: MenuItem.mainMenuItems(context),
          currentPath: HomeScreen.routeName),
      body: ListView.builder(
          itemCount: matches == null ? 0 : matches.length,
          itemBuilder: (ctx, index) {
            final match = matches.elementAt(index);
            return Dismissible(
              direction: DismissDirection.startToEnd,
              background: Container(
                color: Values.deleteColor,
                child: const Align(
                  alignment: Alignment.centerLeft,
                  child: const Padding(
                    padding: const EdgeInsets.only(left: Values.default_space),
                    child: const Icon(
                      Icons.delete,
                      color: Values.secondaryTextColor,
                    ),
                  ),
                ),
              ),
              // Each Dismissible must contain a Key. Keys allow Flutter to
              // uniquely identify widgets.
              key: Key(MatchId.create(match).toString()),
              // Provide a function that tells the app
              // what to do after an item has been swiped away.
              onDismissed: (direction) => _deleteMatch(match, index),
              child: PlayedMatchSummaryWidget(
                match: match,
                onMenuItemSelected: (option) =>
                    _onMatchMenuItemSelected(match, option),
              ),
            );
          }),
      floatingActionButton: FloatingActionButton(
        heroTag: ValueKey<String>('play_match'),
        onPressed: () {
          Navigator.of(context).pushNamed(SetupMatchScreen.routeName);
        },
        child: const Icon(Icons.play_arrow),
        backgroundColor: Theme.of(context).accentColor,
      ),
    );
  }
}
