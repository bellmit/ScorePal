import 'package:flutter/material.dart';
import 'package:multiphone/helpers/log.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/providers/active_selection.dart';
import 'package:multiphone/widgets/common/line_break_widget.dart';
import 'package:multiphone/widgets/select_sport_widget.dart';
import 'package:multiphone/widgets/setup_match_summary_widget.dart';
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

  bool _keyboardShowing = false;

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
          // only show the summary when the keyboard isn't up and editing something
          if (MediaQuery.of(context).viewInsets.bottom == 0)
            Column(
              children: [
                const SizedBox(
                  height: Values.default_space,
                ),
                SetupMatchSummaryWidget(),
                LineBreakWidget(),
              ],
            ),
          // listen to changes to the sports to show the currently selected sport
          Expanded(
            child: Card(
              child: SingleChildScrollView(
                child: Column(
                  children: [
                    Padding(
                      padding: EdgeInsets.all(Values.default_space),
                      child: SelectSportWidget(),
                    ),
                    Consumer<ActiveSelection>(
                      builder: (ctx, activeSelection, child) {
                        // create the correct widget to setup the sport here then
                        return activeSelection.sport.createSetupWidget(ctx);
                      },
                    ),
                    // and I much prefer scrolling when there is space at the end of the screen
                    SizedBox(height: Values.image_large),
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
