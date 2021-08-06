import 'package:flutter/material.dart';
import 'package:multiphone/helpers/log.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/providers/active_selection.dart';
import 'package:multiphone/widgets/common/heading_widget.dart';
import 'package:multiphone/widgets/common/info_bar_widget.dart';
import 'package:multiphone/widgets/common/line_break_widget.dart';
import 'package:multiphone/widgets/select_sport_widget.dart';
import 'package:multiphone/widgets/setup_match_summary_widget.dart';
import 'package:multiphone/widgets/side_drawer_widget.dart';
import 'package:provider/provider.dart';

class ChangeMatchSetupScreen extends StatefulWidget {
  static const String routeName = '/change-match';

  ChangeMatchSetupScreen();

  @override
  _ChangeMatchSetupScreenState createState() => _ChangeMatchSetupScreenState();
}

class _ChangeMatchSetupScreenState extends State<ChangeMatchSetupScreen> {
  @override
  Widget build(BuildContext context) {
    // get the values to get strings from
    var values = Values(context);
    // and return the scaffold
    return Scaffold(
      appBar: AppBar(
        title: Text(values.strings.match_change_setup),
      ),
      body: SingleChildScrollView(
        child: Column(
          children: [
            InfoBarWidget(
              title: values.strings.warning_match_change,
              icon: Icon(
                Icons.warning_amber_outlined,
                color: Theme.of(context).errorColor,
                size: Values.image_medium,
              ),
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
    );
  }
}
