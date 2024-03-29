import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/badminton/badminton_match_setup.dart';
import 'package:multiphone/widgets/common/common_widgets.dart';
import 'package:multiphone/widgets/common/select_item_list_widget.dart';
import 'package:multiphone/widgets/common/select_item_widget.dart';

class SelectPointsWidget extends SelectItemListWidget {
  final BadmintonPoints points;
  final void Function(BadmintonPoints) onPointsChanged;
  const SelectPointsWidget({
    Key key,
    @required this.points,
    @required this.onPointsChanged,
  }) : super(
          key: key,
          itemSize: Values.select_item_size_medium,
        );

  @override
  List<SelectItemWidget> items(
      BuildContext context, List<bool> currentSelection) {
    final values = Values(context);
    return [
      SelectItemWidget(
        icon: IconSvgWidget(
          'eleven_points',
          isOnBackground:
              currentSelection == null || currentSelection.length == 0
                  ? points == BadmintonPoints.eleven
                  : currentSelection[0],
        ),
        text: values.strings.badminton_eleven_points_per_game,
        iconSize: Values.image_medium,
      ),
      SelectItemWidget(
        icon: IconSvgWidget(
          'fifteen_points',
          isOnBackground: currentSelection == null || currentSelection.length == 0
                  ? points == BadmintonPoints.fifteen
                  : currentSelection[1],
        ),
        text: values.strings.badminton_fifteen_points_per_game,
        iconSize: Values.image_medium,
      ),
      SelectItemWidget(
        icon: IconSvgWidget(
          'twenty_one_points',
          isOnBackground: currentSelection == null || currentSelection.length == 0
                  ? points == BadmintonPoints.twenty_one
                  : currentSelection[2],
        ),
        text: values.strings.badminton_twenty_one_points_per_game,
        iconSize: Values.image_medium,
      ),
    ];
  }

  @override
  int getInitialSelection(BuildContext context) {
    // the initial selection is handled by the active match's setup
    switch (points) {
      case BadmintonPoints.eleven:
        return 0;
      case BadmintonPoints.fifteen:
        return 1;
      case BadmintonPoints.twenty_one:
        return 2;
      default:
        return 2;
    }
  }

  @override
  void onSelectionChanged(BuildContext context, int newSelection) {
    // the user just selected which number of sets to play in badminton
    switch (newSelection) {
      case 0:
        onPointsChanged(BadmintonPoints.eleven);
        break;
      case 1:
        onPointsChanged(BadmintonPoints.fifteen);
        break;
      case 2:
        onPointsChanged(BadmintonPoints.twenty_one);
        break;
    }
  }
}
