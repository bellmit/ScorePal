import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/providers/player.dart';
import 'package:multiphone/widgets/player_select_widget.dart';
import 'package:multiphone/widgets/select_singles_doubles_widget.dart';
import 'package:multiphone/widgets/tennis/select_sets_widget.dart';

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
      width: double.infinity,
      child: Column(
        children: [
          Wrap(
            alignment: WrapAlignment.center,
            children: <Widget>[
              // move over a bit because the parent adds the icon and settings
              // text to explain what this card is about
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
          Padding(
            padding: EdgeInsets.all(Values.default_space),
            child: PlayerSelectWidget(
              playerIndex: PlayerIndex.P_ONE,
            ),
          ),
          Padding(
            padding: EdgeInsets.all(Values.default_space),
            child: PlayerSelectWidget(
              playerIndex: PlayerIndex.P_TWO,
            ),
          ),
        ],
      ),
    );
  }
}
