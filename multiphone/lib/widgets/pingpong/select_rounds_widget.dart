import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/ping_pong/ping_pong_match_setup.dart';
import 'package:multiphone/widgets/common/select_item_list_widget.dart';
import 'package:multiphone/widgets/common/select_item_widget.dart';
import 'package:flutter_svg/flutter_svg.dart';

class SelectRoundsWidget extends SelectItemListWidget {
  final PingPongRounds rounds;
  final void Function(PingPongRounds) onRoundsChanged;
  const SelectRoundsWidget({
    Key key,
    @required this.rounds,
    @required this.onRoundsChanged,
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
    switch (rounds) {
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
      default:
        return 1;
    }
  }

  @override
  void onSelectionChanged(BuildContext context, int newSelection) {
    // the user just selected which number of rounds to play in pingpong
    switch (newSelection) {
      case 0:
        onRoundsChanged(PingPongRounds.one);
        break;
      case 1:
        onRoundsChanged(PingPongRounds.three);
        break;
      case 2:
        onRoundsChanged(PingPongRounds.five);
        break;
    }
  }
}
