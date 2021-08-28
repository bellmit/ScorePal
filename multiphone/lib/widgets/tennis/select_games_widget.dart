import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/tennis/tennis_match_setup.dart';
import 'package:multiphone/widgets/common/common_widgets.dart';
import 'package:multiphone/widgets/common/select_item_list_widget.dart';
import 'package:multiphone/widgets/common/select_item_widget.dart';

class SelectGamesWidget extends SelectItemListWidget {
  final TennisGames games;
  final void Function(TennisGames) onGamesChanged;
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
          'tennis-ball-four',
          isOnBackground:
              currentSelection == null || currentSelection.length == 0
                  ? games == TennisGames.four
                  : currentSelection[0],
        ),
        text: values.strings.tennis_four_games_per_set,
        iconSize: Values.image_medium,
      ),
      SelectItemWidget(
        icon: IconSvgWidget(
          'tennis-ball-six',
          isOnBackground:
              currentSelection == null || currentSelection.length == 0
                  ? games == TennisGames.six
                  : currentSelection[1],
        ),
        text: values.strings.tennis_six_games_per_set,
        iconSize: Values.image_medium,
      ),
    ];
  }

  @override
  int getInitialSelection(BuildContext context) {
    // the initial selection is handled by the active match's setup
    switch (games) {
      case TennisGames.four:
        return 0;
      case TennisGames.six:
        return 1;
      default:
        return 1;
    }
  }

  @override
  void onSelectionChanged(BuildContext context, int newSelection) {
    // the user just selected which number of games to play in tennis
    switch (newSelection) {
      case 0:
        onGamesChanged(TennisGames.four);
        break;
      case 1:
        onGamesChanged(TennisGames.six);
        break;
      default:
        onGamesChanged(TennisGames.six);
        break;
    }
  }
}
