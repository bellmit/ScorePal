import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/match_setup.dart';
import 'package:multiphone/match/badminton_match_setup.dart';
import 'package:multiphone/widgets/select_item_list_widget.dart';
import 'package:multiphone/widgets/select_item_widget.dart';
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
        text: values.strings.badminton_one_game,
        iconSize: Values.image_medium,
      ),
      SelectItemWidget(
        icon: Icons.three_k,
        text: values.strings.badminton_three_game,
        iconSize: Values.image_medium,
      ),
      SelectItemWidget(
        icon: Icons.five_k,
        text: values.strings.badminton_five_game,
        iconSize: Values.image_medium,
      ),
    ];
  }

  @override
  int getInitialSelection(BuildContext context) {
    // the initial selection is handled by the active match's setup
    var setup = Provider.of<MatchSetup>(context, listen: false);
    if (setup is BadmintonMatchSetup) {
      // this is correct
      switch (setup.games) {
        case BadmintonGames.one:
          return 0;
        case BadmintonGames.three:
          return 1;
        case BadmintonGames.five:
          return 2;
      }
    }
    print('the badminton widget shouldn\'t show unless badminton is selected');
    return 0;
  }

  @override
  void onSelectionChanged(BuildContext context, int newSelection) {
    // the user just selected which number of games to play in badminton
    var setup = Provider.of<MatchSetup>(context, listen: false);
    if (setup is BadmintonMatchSetup) {
      switch (newSelection) {
        case 0:
          setup.games = BadmintonGames.one;
          break;
        case 1:
          setup.games = BadmintonGames.three;
          break;
        case 2:
          setup.games = BadmintonGames.five;
          break;
      }
    }
  }
}
