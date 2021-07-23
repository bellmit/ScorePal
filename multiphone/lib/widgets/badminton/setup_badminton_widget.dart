import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/widgets/info_bar_widget.dart';
import 'package:multiphone/widgets/player_names_widget.dart';
import 'package:multiphone/widgets/badminton/select_games_widget.dart';
import 'package:multiphone/widgets/badminton/select_points_widget.dart';

class SetupBadmintonWidget extends StatefulWidget {
  const SetupBadmintonWidget({Key key}) : super(key: key);

  @override
  _SetupBadmintonWidgetState createState() => _SetupBadmintonWidgetState();
}

class _SetupBadmintonWidgetState extends State<SetupBadmintonWidget> {
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
                child: SelectGamesWidget(),
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
                  Values(context).strings.badminton_number_points_per_game,
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
