import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/widgets/common/select_item_list_widget.dart';
import 'package:multiphone/widgets/common/select_item_widget.dart';
import 'package:provider/provider.dart';
import 'package:flutter_svg/flutter_svg.dart';

class SelectSinglesDoublesWidget extends SelectItemListWidget {
  final MatchSinglesDoubles singlesDoubles;
  final Function(MatchSinglesDoubles) onSinglesDoublesChanged;
  const SelectSinglesDoublesWidget({
    Key key,
    @required this.singlesDoubles,
    @required this.onSinglesDoublesChanged,
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
          'images/svg/player-receiving-forehand.svg',
        ),
        text: values.strings.tennis_singles,
        iconSize: Values.image_medium,
      ),
      SelectItemWidget(
        icon: SvgPicture.asset(
          'images/svg/player-receiving-doubles.svg',
        ),
        text: values.strings.tennis_doubles,
        iconSize: Values.image_medium,
      ),
    ];
  }

  @override
  int getInitialSelection(BuildContext context) {
    // the initial selection is handled by the active match's setup
    switch (singlesDoubles) {
      case MatchSinglesDoubles.singles:
        return 0;
      case MatchSinglesDoubles.doubles:
        return 1;
    }
    return 0;
  }

  @override
  void onSelectionChanged(BuildContext context, int newSelection) {
    // the user just selected which number of SinglesDoubles to play
    switch (newSelection) {
      case 0:
        onSinglesDoublesChanged(MatchSinglesDoubles.singles);
        break;
      case 1:
        onSinglesDoublesChanged(MatchSinglesDoubles.doubles);
        break;
    }
  }
}
