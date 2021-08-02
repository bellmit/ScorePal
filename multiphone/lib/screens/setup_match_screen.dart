import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/providers/active_selection.dart';
import 'package:multiphone/widgets/common/heading_widget.dart';
import 'package:multiphone/widgets/select_sport_widget.dart';
import 'package:multiphone/widgets/common/subheading_widget.dart';
import 'package:multiphone/widgets/side_drawer_widget.dart';
import 'package:provider/provider.dart';

class SetupMatchScreen extends StatefulWidget {
  static const String routeName = '/play-match';

  SetupMatchScreen();

  @override
  _SetupMatchScreenState createState() => _SetupMatchScreenState();
}

class _SetupMatchScreenState extends State<SetupMatchScreen> {
  final GlobalKey<ScaffoldState> _scaffoldKey = GlobalKey();

  void _startMatch() {
    // start playing the selected match then, just get the match as-is
    final match = Provider.of<ActiveSelection>(context, listen: false);
    // and navigate to the match screen
    Navigator.of(context).pushNamed(match.sport.playNavPath);
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
        currentSelection: MenuItem.menuPlay,
      ),
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
            color: Values.primaryLightColor,
            child: Consumer<ActiveSetup>(
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
              child: Padding(
                padding: EdgeInsets.all(Values.default_space),
                child: FloatingActionButton(
                  heroTag: ValueKey<String>('play_match'),
                  onPressed: _startMatch,
                  child: const Icon(Icons.play_arrow),
                  backgroundColor: Theme.of(context).accentColor,
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
                    Consumer<ActiveSelection>(
                      builder: (ctx, activeSelection, child) {
                        // create the correct widget to setup the sport here then
                        return activeSelection.sport.createSetupWidget(ctx);
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
