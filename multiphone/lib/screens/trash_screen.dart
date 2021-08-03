import 'dart:async';

import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:multiphone/helpers/log.dart';
import 'package:multiphone/match/match_id.dart';
import 'package:multiphone/match/match_play_tracker.dart';
import 'package:multiphone/providers/match_persistence.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/screens/base_nav_screen.dart';
import 'package:multiphone/widgets/common/info_bar_widget.dart';
import 'package:multiphone/widgets/deleted_match_popup_menu.dart';
import 'package:multiphone/widgets/played_match_summary_widget.dart';
import 'package:multiphone/widgets/side_drawer_widget.dart';
import 'package:provider/provider.dart';

class TrashScreen extends BaseNavScreen {
  static const String routeName = '/trash';

  TrashScreen({Key key})
      : super(key: key, scaffoldKey: GlobalKey(debugLabel: 'trash'));

  @override
  _TrashScreenState createState() => _TrashScreenState();
}

class _TrashScreenState extends BaseNavScreenState<TrashScreen>
    with TickerProviderStateMixin {
  List<ActiveMatch> matches;
  bool _isUserLoggedOn = false;
  StreamSubscription<User> _userSubscription;

  AnimationController _controller;
  Animation<Offset> _slideAnimation;

  @override
  String getScreenTitle(Values values) {
    return values.strings.option_trash;
  }

  @override
  int getMenuSelectionIndex() {
    return MenuItem.menuTrash;
  }

  @override
  void initState() {
    super.initState();
    // animation things (this can have the mixin of SingleTickerProviderStateMixin)
    _controller = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: Values.animation_duration_ms),
    );
    _slideAnimation = Tween<Offset>(
      begin: const Offset(0, -1),
      end: const Offset(0, 0),
    ).animate(CurvedAnimation(
      parent: _controller,
      curve: Curves.linear,
    ));
    // and the data too
    final persistence = Provider.of<MatchPersistence>(context, listen: false);
    // need to know if we are logged on
    _isUserLoggedOn = persistence.isUserLoggedOn;
    _animateLoginMessage();
    // also listen for changes to we change if they do log in again
    _userSubscription = FirebaseAuth.instance.authStateChanges().listen((user) {
      _changeUserLoginState(_isUserLoggedOn = user != null);
    });
    // get our matches to show from the persistence provider
    persistence.getMatches(MatchPersistenceState.deleted).then((value) => {
          // have the matches back here, set them locally
          setState(() {
            matches = List.of(value.values);
            // which can change the display of the message
            _animateLoginMessage();
          })
        });
  }

  @override
  void dispose() {
    // kill everything created
    Log.debug(
        'disposing user subscription ${_userSubscription == null ? 'null' : 'not null'}');
    if (null != _userSubscription) {
      _userSubscription.cancel();
    }
    // and the animation controller
    _controller.dispose();
    super.dispose();
  }

  void _changeUserLoginState(bool isUserLoggedIn) {
    setState(() {
      _isUserLoggedOn = isUserLoggedIn;
      _animateLoginMessage();
    });
  }

  bool get _isShowUserWarning {
    return matches != null && matches.length > 0 && !_isUserLoggedOn;
  }

  void _animateLoginMessage() {
    _isShowUserWarning ? _controller.forward() : _controller.reverse();
  }

  void _onMatchMenuItemSelected(
    ActiveMatch match,
    DeletedMatchMenuItem option,
  ) {
    // deal with this being selected for the match
    switch (option) {
      case DeletedMatchMenuItem.resume:
        MatchPlayTracker.resumePreviousMatch(match, context);
        break;
      case DeletedMatchMenuItem.wipe:
        // this is called from a menu item, still want to remove
        // this from the list though
        if (null != matches) {
          final index = matches.indexOf(match);
          if (index >= 0) {
            _wipeMatch(match, index);
          }
        }
        break;
    }
  }

  void _wipeMatch(ActiveMatch match, int index) {
    // remove the match from the list to remove from view
    setState(() {
      matches.removeAt(index);
    });
    // delete the match using the provider so it's actually done
    Provider.of<MatchPersistence>(context, listen: false).wipeMatchData(match);
    // Then show a snackbar to inform the user that this worked ok
    ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(Values(context).strings.match_wiped)));
  }

  @override
  Widget buildScreenBody(BuildContext context) {
    final showWarning = _isShowUserWarning;
    return Container(
      child: Column(children: [
        AnimatedContainer(
          constraints: BoxConstraints(
            maxHeight: showWarning ? 500 : 0,
          ),
          duration: Duration(milliseconds: Values.animation_duration_ms),
          curve: Curves.easeInOut,
          child: SlideTransition(
            position: _slideAnimation,
            child: InfoBarWidget(
              title: Values(context).strings.warning_logon_to_delete,
              icon: Icon(
                Icons.warning_amber_outlined,
                color: Theme.of(context).errorColor,
                size: showWarning ? Values.image_medium : 0,
              ),
            ),
          ),
        ),
        if (matches == null || matches.length <= 0)
          InfoBarWidget(
            title: Values(context).strings.warning_no_matches_to_delete,
            icon: Icon(
              Icons.info_outline,
              color: Theme.of(context).accentColor,
            ),
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
                        Icons.delete_forever,
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
                onDismissed: (direction) => _wipeMatch(match, index),
                child: PlayedMatchSummaryWidget(
                  match: match,
                  popupMenu: DeletedMatchPopupMenu(
                    onMenuItemSelected: (option) =>
                        _onMatchMenuItemSelected(match, option),
                  ),
                ),
              );
            },
          ),
        ),
      ]),
    );
  }
}
