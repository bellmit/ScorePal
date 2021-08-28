import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/badminton/badminton_match_setup.dart';
import 'package:multiphone/widgets/common/common_widgets.dart';
import 'package:multiphone/widgets/common/select_item_list_widget.dart';
import 'package:multiphone/widgets/common/select_item_widget.dart';

class SelectGamesWidget extends SelectItemListWidget {
  final BadmintonGames games;
  final void Function(BadmintonGames) onGamesChanged;
  const SelectGamesWidget({
    Key key,
    @required this.games,
    @required this.onGamesChanged,
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
          'badminton_shuttle_one',
          isOnBackground:
              currentSelection == null || currentSelection.length == 0
                  ? games == BadmintonGames.one
                  : currentSelection[0],
        ),
        text: values.strings.badminton_one_game,
        iconSize: Values.image_medium,
      ),
      SelectItemWidget(
        icon: IconSvgWidget(
          'badminton_shuttle_three',
          isOnBackground:
              currentSelection == null || currentSelection.length == 0
                  ? games == BadmintonGames.three
                  : currentSelection[1],
        ),
        text: values.strings.badminton_three_game,
        iconSize: Values.image_medium,
      ),
      SelectItemWidget(
        icon: IconSvgWidget(
          'badminton_shuttle_five',
          isOnBackground:
              currentSelection == null || currentSelection.length == 0
                  ? games == BadmintonGames.five
                  : currentSelection[2],
        ),
        text: values.strings.badminton_five_game,
        iconSize: Values.image_medium,
      ),
    ];
  }

  @override
  int getInitialSelection(BuildContext context) {
    // the initial selection is handled by the active match's setup
    switch (games) {
      case BadmintonGames.one:
        return 0;
      case BadmintonGames.three:
        return 1;
      case BadmintonGames.five:
        return 2;
      default:
        return 1;
    }
  }

  @override
  void onSelectionChanged(BuildContext context, int newSelection) {
    // the user just selected which number of games to play in badminton
    switch (newSelection) {
      case 0:
        onGamesChanged(BadmintonGames.one);
        break;
      case 1:
        onGamesChanged(BadmintonGames.three);
        break;
      case 2:
        onGamesChanged(BadmintonGames.five);
        break;
    }
  }
}
