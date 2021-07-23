import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/match_setup.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/sport.dart';
import 'package:multiphone/screens/play_match_badminton_screen.dart';
import 'package:multiphone/screens/play_match_ping_pong_screen.dart';
import 'package:multiphone/screens/play_match_tennis_screen.dart';
import 'package:multiphone/widgets/heading_widget.dart';
import 'package:multiphone/widgets/select_sport_widget.dart';
import 'package:multiphone/widgets/setup_badminton_widget.dart';
import 'package:multiphone/widgets/setup_ping_pong.dart';
import 'package:multiphone/widgets/subheading_widget.dart';
import 'package:multiphone/widgets/tennis/setup_tennis_widget.dart';
import 'package:multiphone/widgets/side_drawer_widget.dart';
import 'package:provider/provider.dart';

class PlayMatchScreen extends StatefulWidget {
  static const String routeName = '/play-match';

  PlayMatchScreen();

  @override
  _PlayMatchScreenState createState() => _PlayMatchScreenState();
}

class _PlayMatchScreenState extends State<PlayMatchScreen> {
  final GlobalKey<ScaffoldState> _scaffoldKey = GlobalKey();

  Widget _createActiveMatchSetup(BuildContext context, ActiveMatch match) {
    var values = Values(context);
    switch (match.sport.id) {
      case SportType.TENNIS:
        return SetupTennisWidget();
      case SportType.BADMINTON:
        return SetupBadmintonWidget();
      case SportType.PING_PONG:
        return SetupPingPongWidget();
    }
    // if error - just return something bad!
    return Text(values.construct(
      values.strings.error_sport_not_found,
      [match.sport.title(context)],
    ));
  }

  void _startMatch() {
    // start playing the selected match then, just get the match as-is
    final match = Provider.of<ActiveMatch>(context, listen: false);
    // and navigate to the correct screen
    String navPath = '/';
    switch (match.sport.id) {
      case SportType.TENNIS:
        navPath = PlayMatchTennisScreen.routeName;
        break;
      case SportType.BADMINTON:
        navPath = PlayMatchBadmintonScreen.routeName;
        break;
      case SportType.PING_PONG:
        navPath = PlayMatchPingPongScreen.routeName;
        break;
    }
    // and go here
    Navigator.of(context).pushNamed(navPath);
  }

  @override
  Widget build(BuildContext context) {
    // get the values to get strings from
    var values = Values(context);
    // and return the scaffold
    return Scaffold(
      key: _scaffoldKey,
      appBar: AppBar(
        title: Text(values.strings.title_play_match),
        leading: IconButton(
            onPressed: () => _scaffoldKey.currentState.openDrawer(),
            icon: Icon(Icons.more_vert)),
      ),
      drawer: SideDrawer(
          menuItems: MenuItem.mainMenuItems(context),
          currentPath: PlayMatchScreen.routeName),
      body: Column(
        children: [
          const SizedBox(
            width: double.infinity,
            height: Values.default_space,
          ),
          SelectSportWidget(),
          const SizedBox(
            width: double.infinity,
            height: Values.default_space,
          ),
          Card(
            child: Consumer<MatchSetup>(
              builder: (ctx, matchSetup, child) {
                // this changes as the active match changes
                return Row(
                  children: [
                    Expanded(
                        child:
                            HeadingWidget(title: matchSetup.matchSummary(ctx))),
                    // and the child of the consumer
                    child,
                  ],
                );
              },
              // the child of the consumer always is there, make it the play button
              child: IconButton(
                onPressed: _startMatch,
                color: Theme.of(context).primaryColorDark,
                iconSize: Values.image_large,
                icon: Icon(
                  Icons.play_circle,
                ),
              ),
            ),
          ),
          // listen to changes to the sports to show the currently selected sport
          Expanded(
            child: Card(
              child: SingleChildScrollView(
                child: Stack(
                  children: [
                    Padding(
                      padding: EdgeInsets.all(Values.default_space),
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Icon(
                            Icons.settings,
                            color: Theme.of(context).primaryColorDark,
                            size: Values.image_small,
                          ),
                          SubheadingWidget(title: 'Setup'),
                        ],
                      ),
                    ),
                    Consumer<ActiveMatch>(
                      builder: (ctx, activeMatch, child) {
                        // create the correct widget to setup the sport here then
                        return _createActiveMatchSetup(ctx, activeMatch);
                      },
                    ),
                  ],
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
