import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/match/ping_pong/ping_pong_match_setup.dart';
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
        icon: Icons.one_k,
        text: values.strings.ping_pong_eleven_points_per_round,
        iconSize: Values.image_medium,
      ),
      SelectItemWidget(
        icon: Icons.five_k,
        text: values.strings.ping_pong_twenty_one_points_per_round,
        iconSize: Values.image_medium,
      ),
    ];
  }

  @override
  int getInitialSelection(BuildContext context) {
    // the initial selection is handled by the active match's setup
    var setup = Provider.of<ActiveSetup>(context, listen: false);
    if (setup is PingPongMatchSetup) {
      // this is correct
      switch (setup.points) {
        case PingPongPoints.eleven:
          return 0;
        case PingPongPoints.twenty_one:
          return 1;
      }
    }
    print('the pingpong widget shouldn\'t show unless pingpong is selected');
    return 0;
  }

  @override
  void onSelectionChanged(BuildContext context, int newSelection) {
    // the user just selected which number of sets to play in pingpong
    var setup = Provider.of<ActiveSetup>(context, listen: false);
    if (setup is PingPongMatchSetup) {
      switch (newSelection) {
        case 0:
          setup.points = PingPongPoints.eleven;
          break;
        case 1:
          setup.points = PingPongPoints.twenty_one;
          break;
      }
    }
  }
}
