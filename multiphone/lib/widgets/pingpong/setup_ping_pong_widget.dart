import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/widgets/common/info_bar_widget.dart';
import 'package:multiphone/widgets/player_names_widget.dart';
import 'package:multiphone/widgets/pingpong/select_rounds_widget.dart';
import 'package:multiphone/widgets/pingpong/select_points_widget.dart';

class SetupPingPongWidget extends StatefulWidget {
  const SetupPingPongWidget({Key key}) : super(key: key);

  @override
  _SetupPingPongWidgetState createState() => _SetupPingPongWidgetState();
}

class _SetupPingPongWidgetState extends State<SetupPingPongWidget> {
  Widget _createAdvancedTitle(String title) {
    return Expanded(
      child: Padding(
        padding: const EdgeInsets.only(right: Values.default_space),
        child: Text(title,
            textAlign: TextAlign.right,
            style: TextStyle(
              fontSize: Values.font_size_title,
              color: Theme.of(context).primaryColorDark,
            )),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    // get the sport for the icon etc
    const advancedHeadingPadding = const EdgeInsets.only(
      left: Values.default_space,
      top: Values.default_space,
      right: Values.default_space,
    );
    return Container(
      width: double.infinity,
      child: Column(
        children: [
          Row(
            children: <Widget>[
              // move over a bit because the parent adds the icon and settings
              // text to explain what this card is about
              SizedBox(
                width: Values.image_large,
              ),
              Padding(
                padding: EdgeInsets.all(Values.default_space),
                child: SelectRoundsWidget(),
              ),
            ],
          ),
          PlayerNamesWidget(),
          InfoBarWidget(title: Values(context).strings.heading_advanced),
          Padding(
            padding: advancedHeadingPadding,
            child: Row(
              children: [
                _createAdvancedTitle(
                  Values(context).strings.ping_pong_number_points_per_round,
                ),
                SelectPointsWidget(),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
