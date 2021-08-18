import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/providers/active_selection.dart';
import 'package:multiphone/screens/base_nav_screen.dart';
import 'package:multiphone/widgets/common/line_break_widget.dart';
import 'package:multiphone/widgets/select_sport_widget.dart';
import 'package:multiphone/widgets/setup_match_summary_widget.dart';
import 'package:multiphone/widgets/side_drawer_widget.dart';
import 'package:provider/provider.dart';

class SetupMatchScreen extends BaseNavScreen {
  static const String routeName = '/play-match';

  SetupMatchScreen({Key key})
      : super(key: key, scaffoldKey: GlobalKey(debugLabel: 'trash'));

  @override
  _SetupMatchScreenState createState() => _SetupMatchScreenState();
}

class _SetupMatchScreenState extends BaseNavScreenState<SetupMatchScreen> {
  @override
  int getMenuSelectionIndex() {
    return MenuItem.menuPlay;
  }

  @override
  String getScreenTitle(Values values) {
    return values.strings.title_play_match;
  }

  @override
  Widget buildScreenBody(BuildContext context) {
    return Column(
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
    );
  }
}
