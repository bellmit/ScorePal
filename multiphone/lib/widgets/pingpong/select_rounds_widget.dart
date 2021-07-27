import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/match/ping_pong/ping_pong_match_setup.dart';
import 'package:multiphone/widgets/common/select_item_list_widget.dart';
import 'package:multiphone/widgets/common/select_item_widget.dart';
import 'package:provider/provider.dart';
import 'package:flutter_svg/flutter_svg.dart';

class SelectRoundsWidget extends SelectItemListWidget {
  const SelectRoundsWidget({Key key})
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
          'images/svg/ping-pong-ball-one.svg',
        ),
        text: values.strings.ping_pong_one_round,
        iconSize: Values.image_medium,
      ),
      SelectItemWidget(
        icon: SvgPicture.asset(
          'images/svg/ping-pong-ball-three.svg',
        ),
        text: values.strings.ping_pong_three_round,
        iconSize: Values.image_medium,
      ),
      SelectItemWidget(
        icon: SvgPicture.asset(
          'images/svg/ping-pong-ball-five.svg',
        ),
        text: values.strings.ping_pong_five_round,
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
      switch (setup.rounds) {
        case PingPongRounds.one:
          return 0;
        case PingPongRounds.three:
          return 1;
        case PingPongRounds.five:
          return 2;
        case PingPongRounds.seven:
          return 3;
        case PingPongRounds.nine:
          return 4;
      }
    }
    print('the pingpong widget shouldn\'t show unless pingpong is selected');
    return 0;
  }

  @override
  void onSelectionChanged(BuildContext context, int newSelection) {
    // the user just selected which number of rounds to play in pingpong
    var setup = Provider.of<ActiveSetup>(context, listen: false);
    if (setup is PingPongMatchSetup) {
      switch (newSelection) {
        case 0:
          setup.rounds = PingPongRounds.one;
          break;
        case 1:
          setup.rounds = PingPongRounds.three;
          break;
        case 2:
          setup.rounds = PingPongRounds.five;
          break;
      }
    }
  }
}
