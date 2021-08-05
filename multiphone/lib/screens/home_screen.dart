import 'package:flutter/material.dart';
import 'package:multiphone/helpers/log.dart';
import 'package:multiphone/helpers/preferences.dart';
import 'package:multiphone/match/match_id.dart';
import 'package:multiphone/match/match_play_tracker.dart';
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

class HomeScreen extends BaseNavScreen {
  static const String routeName = '/';

  HomeScreen({Key key})
      : super(key: key, scaffoldKey: GlobalKey(debugLabel: 'home'));

  @override
  _HomeScreenState createState() => _HomeScreenState();
}

class _HomeScreenState extends BaseNavScreenState<HomeScreen> {
  List<ActiveMatch> matches;

  static const playMatchCardKey = 'advert_card_play_match';
  static const purchaseFlicCardKey = 'advert_purchase_flic';
  static const signInScorepalCardKey = 'advert_sign_in_scorepal';

  @override
  String getScreenTitle(Values values) {
    return values.strings.title;
  }

  @override
  int getMenuSelectionIndex() {
    return MenuItem.menuHome;
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

  Future<List<Widget>> _createAdvertCards() async {
    List<Widget> cards = [];
    // need the preferences
    final prefs = await Preferences.create();
    if (!Provider.of<MatchPersistence>(context, listen: false).isUserLoggedOn &&
        !prefs.isAdvertDismissed(signInScorepalCardKey)) {
      // poke them to log on / sign in
      cards.add(Dismissible(
        direction: DismissDirection.startToEnd,
        // Each Dismissible must contain a Key. Keys allow Flutter to
        // uniquely identify widgets.
        key: Key(signInScorepalCardKey),
        // Provide a function that tells the app
        // what to do after an item has been swiped away.
        onDismissed: (direction) =>
            prefs.setAdvertDismissed(signInScorepalCardKey),
        child: SignInScorepalWidget(),
      ));
    }
    if ((matches == null || matches.length <= 0) &&
        !prefs.isAdvertDismissed(playMatchCardKey)) {
      // no matches, let them play one to start up
      cards.add(Dismissible(
        direction: DismissDirection.startToEnd,
        // Each Dismissible must contain a Key. Keys allow Flutter to
        // uniquely identify widgets.
        key: Key(playMatchCardKey),
        // Provide a function that tells the app
        // what to do after an item has been swiped away.
        onDismissed: (direction) => prefs.setAdvertDismissed(playMatchCardKey),
        child: PlayNewMatchWidget(),
      ));
    }
    if (!prefs.isControlFlic1 &&
        !prefs.isControlFlic2 &&
        !prefs.isAdvertDismissed(purchaseFlicCardKey)) {
      // they are not using flic!
      cards.add(Dismissible(
        direction: DismissDirection.startToEnd,
        // Each Dismissible must contain a Key. Keys allow Flutter to
        // uniquely identify widgets.
        key: Key(purchaseFlicCardKey),
        // Provide a function that tells the app
        // what to do after an item has been swiped away.
        onDismissed: (direction) =>
            prefs.setAdvertDismissed(purchaseFlicCardKey),
        child: PurchaseFlicWidget(),
      ));
    }
    return cards;
  }

  void _onMatchMenuItemSelected(
    ActiveMatch match,
    PlayedMatchMenuItem option,
  ) {
    // deal with this being selected for the match
    switch (option) {
      case PlayedMatchMenuItem.resume:
        MatchPlayTracker.resumePreviousMatch(match, context);
        break;
      case PlayedMatchMenuItem.share:
        //TODO want to share the match
        Log.debug("need to implement this share match");
        break;
      case PlayedMatchMenuItem.delete:
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
  Widget buildFloatingActionButton(BuildContext context) {
    if (matches != null && matches.length > 0) {
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
          FutureBuilder(
            future: _createAdvertCards(),
            builder: (ctx, snapshot) {
              if (snapshot.connectionState == ConnectionState.done &&
                  snapshot.hasData) {
                // have a list of cards
                return Column(children: snapshot.data);
              } else {
                // no cards
                return Container();
              }
            },
          ),
          Expanded(
            child: ListView.builder(
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
                        padding:
                            const EdgeInsets.only(left: Values.default_space),
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
                    popupMenu: PlayedMatchPopupMenu(
                      onMenuItemSelected: (option) =>
                          _onMatchMenuItemSelected(match, option),
                    ),
                  ),
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}
