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
        // call on the provider to delete the match, should update this widget list
        Provider.of<MatchPersistence>(context, listen: false)
            .deleteMatchData(match);
        break;
    }
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
            icon: Icon(Icons.more_vert)),
      ),
      drawer: SideDrawer(
          menuItems: MenuItem.mainMenuItems(context),
          currentPath: HomeScreen.routeName),
      body: Consumer<MatchPersistence>(
        // consume the persistence class to show the matches as they load
        builder: (ctx, matchPersistence, child) {
          return FutureBuilder(
            future:
                MatchPersistence().getMatches(MatchPersistenceState.accepted),
            builder: (ctx, snapshot) {
              if (snapshot.connectionState == ConnectionState.done) {
                if (snapshot.data == null) {
                  return Center(child: Text('no data'));
                }
                // return the list of data
                matches = List.of(snapshot.data.values);
                return ListView.builder(
                    itemCount: matches.length,
                    itemBuilder: (ctx, index) {
                      final match = matches.elementAt(index);
                      return Dismissible(
                        direction: DismissDirection.startToEnd,
                        background: Container(
                          color: Colors.red,
                          child: Align(
                            alignment: Alignment.centerLeft,
                            child: Padding(
                              padding:
                                  EdgeInsets.only(left: Values.default_space),
                              child: Icon(Icons.delete),
                            ),
                          ),
                        ),
                        // Each Dismissible must contain a Key. Keys allow Flutter to
                        // uniquely identify widgets.
                        key: Key(MatchId.create(match).toString()),
                        // Provide a function that tells the app
                        // what to do after an item has been swiped away.
                        onDismissed: (direction) {
                          // Remove the item from the data source.
                          setState(() {
                            matches.removeAt(index);
                          });
                          // Then show a snackbar.
                          ScaffoldMessenger.of(context).showSnackBar(
                              SnackBar(content: Text('match dismissed')));
                        },
                        child: PlayedMatchSummaryWidget(
                          match: match,
                          onMenuItemSelected: (option) =>
                              _onMatchMenuItemSelected(match, option),
                        ),
                      );
                    });
              } else {
                return Center(child: CircularProgressIndicator());
              }
            },
          );
        },
      ),
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
