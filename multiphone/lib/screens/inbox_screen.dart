import 'package:flutter/material.dart';
import 'package:flutter_staggered_grid_view/flutter_staggered_grid_view.dart';
import 'package:multiphone/helpers/log.dart';
import 'package:multiphone/helpers/preferences.dart';
import 'package:multiphone/match/match_id.dart';
import 'package:multiphone/match/match_play_tracker.dart';
import 'package:multiphone/match/score.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/providers/match_inbox.dart';
import 'package:multiphone/providers/match_persistence.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/screens/base_nav_screen.dart';
import 'package:multiphone/widgets/adverts/play_new_match_widget.dart';
import 'package:multiphone/widgets/adverts/signin_scorepal_widget.dart';
import 'package:multiphone/widgets/played_match_popup_menu.dart';
import 'package:multiphone/widgets/played_match_summary_widget.dart';
import 'package:multiphone/widgets/adverts/purchase_flic_widget.dart';
import 'package:multiphone/widgets/side_drawer_widget.dart';
import 'package:provider/provider.dart';

class InboxScreen extends BaseNavScreen {
  static const String routeName = '/match-inbox';

  InboxScreen({Key key})
      : super(key: key, scaffoldKey: GlobalKey(debugLabel: 'inbox'));

  @override
  _InboxScreenState createState() => _InboxScreenState();
}

class _InboxScreenState extends BaseNavScreenState<InboxScreen> {
  List<ActiveMatch> _matches;

  @override
  String getScreenTitle(Values values) {
    return values.strings.option_inbox;
  }

  @override
  int getMenuSelectionIndex() {
    return MenuItem.menuInbox;
  }

  @override
  void initState() {
    super.initState();
    // get our matches to show from the persistence provider
    _refreshMatches();
  }

  Future<void> _refreshMatches() async {
    // load the matches and set the state accordingly
    final inbox = Provider.of<MatchInbox>(context, listen: false);
    // get the data from firebase
    if (inbox.isUserLoggedOn) {
      await inbox.fetchDataFromFirebase();
    }
    // have the matches back here then, set them locally
    setState(() {
      _matches = inbox.getMatches(context);
    });
  }

  void _deleteMatch(ActiveMatch match, int index) {
    // remove the match from the list to remove from view
    setState(() {
      _matches.removeAt(index);
    });
    // delete the match using the provider so it's actually done
    Provider.of<MatchInbox>(context, listen: false).deleteMatch(match);
  }

  void _acceptMatch(ActiveMatch match, int index) {
    // remove the match from the list to remove from view
    setState(() {
      _matches.removeAt(index);
    });
    // accept the match using the provider so it's actually done
    Provider.of<MatchInbox>(context, listen: false).acceptMatch(match);
  }

  @override
  Widget buildFloatingActionButton(BuildContext context) {
    if (_matches != null && _matches.length > 0) {
      // only show a FAB if there are matches to show it over
      return FloatingActionButton(
        heroTag: ValueKey<String>('play_match'),
        onPressed: () {
          // make the helper call to setup a new match and navigate to the screen
          MatchPlayTracker.setupNewMatch(context);
        },
        child: const Icon(Icons.play_arrow),
        backgroundColor: Theme.of(context).accentColor,
      );
    } else {
      return super.buildFloatingActionButton(context);
    }
  }

  @override
  Widget buildScreenBody(BuildContext context) {
    return Container(
      child: Column(
        children: [
          Expanded(
            child: RefreshIndicator(
              onRefresh: _refreshMatches,
              child: OrientationBuilder(
                builder: (ctx, orientation) {
                  return StaggeredGridView.countBuilder(
                    itemCount: _matches == null ? 0 : _matches.length,
                    crossAxisCount: orientation == Orientation.portrait ? 1 : 2,
                    crossAxisSpacing: Values.default_space,
                    mainAxisSpacing: Values.default_space,
                    staggeredTileBuilder: (int index) => StaggeredTile.fit(1),
                    itemBuilder: (BuildContext context, int index) {
                      final match = _matches.elementAt(index);
                      return Dismissible(
                        background: Container(
                          color: Values.deleteColor,
                          child: const Align(
                            alignment: Alignment.centerLeft,
                            child: const Padding(
                              padding: const EdgeInsets.only(
                                  left: Values.default_space),
                              child: const Icon(
                                Icons.delete,
                                color: Values.secondaryTextColor,
                              ),
                            ),
                          ),
                        ),
                        secondaryBackground: Container(
                          color: Values.primaryLightColor,
                          child: const Align(
                            alignment: Alignment.centerRight,
                            child: const Padding(
                              padding: const EdgeInsets.only(
                                  right: Values.default_space),
                              child: const Icon(
                                Icons.done_outline,
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
                        onDismissed: (direction) {
                          if (direction == DismissDirection.startToEnd) {
                            _deleteMatch(match, index);
                          } else {
                            _acceptMatch(match, index);
                          }
                        },
                        child: PlayedMatchSummaryWidget(
                          match: match,
                          popupMenu: PlayedMatchPopupMenu(
                            onMenuItemSelected: (option) =>
                                _onMatchMenuItemSelected(match, option),
                          ),
                        ),
                      );
                    },
                  );
                },
              ),
            ),
          ),
        ],
      ),
    );
  }

  _onMatchMenuItemSelected(ActiveMatch<ActiveSetup, Score<ActiveSetup>> match,
      PlayedMatchMenuItem option) {}
}
