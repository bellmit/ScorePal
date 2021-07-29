import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/ping_pong/ping_pong_match_setup.dart';
import 'package:multiphone/widgets/common/select_item_list_widget.dart';
import 'package:multiphone/widgets/common/select_item_widget.dart';

class SelectPointsWidget extends SelectItemListWidget {
  final PingPongPoints points;
  final void Function(PingPongPoints) onPointsChanged;
  const SelectPointsWidget({
    Key key,
    @required this.points,
    @required this.onPointsChanged,
  }) : super(
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
        text: values.strings.ping_pong_eleven_points_per_round,
        iconSize: Values.image_medium,
      ),
      SelectItemWidget(
        icon: SvgPicture.asset(
          'images/svg/twenty_one_points.svg',
        ),
        text: values.strings.ping_pong_twenty_one_points_per_round,
        iconSize: Values.image_medium,
      ),
    ];
  }

  @override
  int getInitialSelection(BuildContext context) {
    // the initial selection is handled by the active match's setup
    switch (points) {
      case PingPongPoints.eleven:
        return 0;
      case PingPongPoints.twenty_one:
        return 1;
      default:
        return 0;
    }
  }

  @override
  void onSelectionChanged(BuildContext context, int newSelection) {
    // the user just selected which number of sets to play in pingpong
    switch (newSelection) {
      case 0:
        onPointsChanged(PingPongPoints.eleven);
        break;
      case 1:
        onPointsChanged(PingPongPoints.twenty_one);
        break;
    }
  }
}
