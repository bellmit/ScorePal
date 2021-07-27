import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/match/badminton/badminton_match_setup.dart';
import 'package:multiphone/widgets/common/select_item_list_widget.dart';
import 'package:multiphone/widgets/common/select_item_widget.dart';
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
        icon: SvgPicture.asset(
          'images/svg/eleven_points.svg',
        ),
        text: values.strings.badminton_eleven_points_per_game,
        iconSize: Values.image_medium,
      ),
      SelectItemWidget(
        icon: SvgPicture.asset(
          'images/svg/fifteen_points.svg',
        ),
        text: values.strings.badminton_fifteen_points_per_game,
        iconSize: Values.image_medium,
      ),
      SelectItemWidget(
        icon: SvgPicture.asset(
          'images/svg/twenty_one_points.svg',
        ),
        text: values.strings.badminton_twenty_one_points_per_game,
        iconSize: Values.image_medium,
      ),
    ];
  }

  @override
  int getInitialSelection(BuildContext context) {
    // the initial selection is handled by the active match's setup
    var setup = Provider.of<ActiveSetup>(context, listen: false);
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
    var setup = Provider.of<ActiveSetup>(context, listen: false);
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
