import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/match_setup.dart';
import 'package:multiphone/match/badminton_match_setup.dart';
import 'package:multiphone/widgets/select_item_list_widget.dart';
import 'package:multiphone/widgets/select_item_widget.dart';
import 'package:provider/provider.dart';

class SelectPointsWidget extends SelectItemListWidget {
  const SelectPointsWidget({Key key})
      : super(
          key: key,
          itemSize: Values.select_item_size_medium,
        );

  @override
  List<SelectItemWidget> items(BuildContext context) {
    final values = Values(context);
    return [
      SelectItemWidget(
        icon: Icons.one_k,
        text: values.strings.badminton_eleven_points_per_game,
        iconSize: Values.image_medium,
      ),
      SelectItemWidget(
        icon: Icons.three_k,
        text: values.strings.badminton_fifteen_points_per_game,
        iconSize: Values.image_medium,
      ),
      SelectItemWidget(
        icon: Icons.five_k,
        text: values.strings.badminton_twenty_one_points_per_game,
        iconSize: Values.image_medium,
      ),
    ];
  }

  @override
  int getInitialSelection(BuildContext context) {
    // the initial selection is handled by the active match's setup
    var setup = Provider.of<MatchSetup>(context, listen: false);
    if (setup is BadmintonMatchSetup) {
      // this is correct
      switch (setup.points) {
        case BadmintonPoints.eleven:
          return 0;
        case BadmintonPoints.fifteen:
          return 1;
        case BadmintonPoints.twenty_one:
          return 2;
      }
    }
    print('the badminton widget shouldn\'t show unless badminton is selected');
    return 0;
  }

  @override
  void onSelectionChanged(BuildContext context, int newSelection) {
    // the user just selected which number of sets to play in badminton
    var setup = Provider.of<MatchSetup>(context, listen: false);
    if (setup is BadmintonMatchSetup) {
      switch (newSelection) {
        case 0:
          setup.points = BadmintonPoints.eleven;
          break;
        case 1:
          setup.points = BadmintonPoints.fifteen;
          break;
        case 2:
          setup.points = BadmintonPoints.twenty_one;
          break;
      }
    }
  }
}
