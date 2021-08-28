import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/widgets/common/select_item_list_widget.dart';
import 'package:multiphone/widgets/common/select_item_widget.dart';

import 'common/common_widgets.dart';

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
  List<SelectItemWidget> items(
      BuildContext context, List<bool> currentSelection) {
    final values = Values(context);
    return [
      SelectItemWidget(
        icon: IconSvgWidget(
          'player-receiving-forehand',
          isOnBackground:
              currentSelection == null || currentSelection.length == 0
                  ? singlesDoubles == MatchSinglesDoubles.singles
                  : currentSelection[0],
        ),
        text: values.strings.tennis_singles,
        iconSize: Values.image_medium,
      ),
      SelectItemWidget(
        icon: IconSvgWidget(
          'player-receiving-doubles',
          isOnBackground:
              currentSelection == null || currentSelection.length == 0
                  ? singlesDoubles == MatchSinglesDoubles.doubles
                  : currentSelection[1],
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
