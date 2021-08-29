import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/providers/active_sport.dart';
import 'package:multiphone/widgets/common/common_widgets.dart';
import 'package:multiphone/widgets/common/info_bar_widget.dart';
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
        title: TextWidget(values.strings.match_change_setup),
      ),
      body: SingleChildScrollView(
        child: Column(
          children: [
            InfoBarWidget(
              title: values.strings.warning_match_change,
              icon: Icons.warning_amber_outlined,
              isError: true,
            ),
            Consumer<ActiveSport>(
              builder: (ctx, activeSelection, child) {
                // create the correct widget to setup the sport here then
                return activeSelection.sport.createSetupWidget(ctx, false);
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
