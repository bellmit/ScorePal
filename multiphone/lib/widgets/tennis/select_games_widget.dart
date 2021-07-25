import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/match/tennis/tennis_match_setup.dart';
import 'package:multiphone/widgets/common/select_item_list_widget.dart';
import 'package:multiphone/widgets/common/select_item_widget.dart';
import 'package:provider/provider.dart';

class SelectGamesWidget extends SelectItemListWidget {
  const SelectGamesWidget({Key key})
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
        text: values.strings.tennis_four_games_per_set,
        iconSize: Values.image_medium,
      ),
      SelectItemWidget(
        icon: Icons.three_k,
        text: values.strings.tennis_six_games_per_set,
        iconSize: Values.image_medium,
      ),
    ];
  }

  @override
  int getInitialSelection(BuildContext context) {
    // the initial selection is handled by the active match's setup
    var setup = Provider.of<ActiveSetup>(context, listen: false);
    if (setup is TennisMatchSetup) {
      // this is correct
      switch (setup.games) {
        case TennisGames.four:
          return 0;
        case TennisGames.six:
          return 1;
      }
    }
    print('the tennis widget shouldn\'t show unless tennis is selected');
    return 0;
  }

  @override
  void onSelectionChanged(BuildContext context, int newSelection) {
    // the user just selected which number of sets to play in tennis
    var setup = Provider.of<ActiveSetup>(context, listen: false);
    if (setup is TennisMatchSetup) {
      switch (newSelection) {
        case 0:
          setup.games = TennisGames.four;
          break;
        case 1:
          setup.games = TennisGames.six;
          break;
      }
    }
  }
}
