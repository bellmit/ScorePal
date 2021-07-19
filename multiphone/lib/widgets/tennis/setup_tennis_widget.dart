import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/match_setup.dart';
import 'package:multiphone/providers/sport.dart';
import 'package:multiphone/widgets/heading_icon_widget.dart';
import 'package:multiphone/widgets/heading_widget.dart';
import 'package:multiphone/widgets/select_singles_doubles_widget.dart';
import 'package:multiphone/widgets/tennis/select_sets_widget.dart';
import 'package:provider/provider.dart';

class SetupTennisWidget extends StatefulWidget {
  const SetupTennisWidget({Key key}) : super(key: key);

  @override
  _SetupTennisWidgetState createState() => _SetupTennisWidgetState();
}

class _SetupTennisWidgetState extends State<SetupTennisWidget> {
  @override
  Widget build(BuildContext context) {
    // get the sport for the icon etc
    return Container(
      margin: EdgeInsets.symmetric(horizontal: Values.default_space),
      width: double.infinity,
      child: Wrap(
        alignment: WrapAlignment.center,
        children: <Widget>[
          SizedBox(
            width: Values.image_large,
          ),
          Padding(
            padding: EdgeInsets.all(Values.default_space),
            child: SelectSetsWidget(),
          ),
          Padding(
            padding: EdgeInsets.all(Values.default_space),
            child: SelectSinglesDoublesWidget(),
          ),
        ],
      ),
    );
  }
}
