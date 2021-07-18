import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/tennis_match_setup.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/sport.dart';
import 'package:multiphone/widgets/select_item_list_widget.dart';
import 'package:multiphone/widgets/select_item_widget.dart';
import 'package:provider/provider.dart';

class SelectSetsWidget extends SelectItemListWidget {
  const SelectSetsWidget({Key key})
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
        text: values.strings.tennis_one_set,
        iconSize: Values.image_medium,
      ),
      SelectItemWidget(
        icon: Icons.three_k,
        text: values.strings.tennis_three_sets,
        iconSize: Values.image_medium,
      ),
      SelectItemWidget(
        icon: Icons.five_k,
        text: values.strings.tennis_five_sets,
        iconSize: Values.image_medium,
      ),
    ];
  }

  @override
  int getInitialSelection(BuildContext context) {
    // the initial selection is handled by the active match's setup
    var setup = Provider.of<ActiveMatch>(context, listen: false).setup;
    if (setup is TennisMatchSetup) {
      // this is correct
      switch (setup.sets) {
        case TENNIS_SETS.ONE:
          return 0;
        case TENNIS_SETS.THREE:
          return 1;
        case TENNIS_SETS.FIVE:
          return 2;
      }
    }
    print('the tennis widget shouldn\'t show unless tennis is selected');
    return 0;
  }

  @override
  void onSelectionChanged(BuildContext context, int newSelection) {
    // the user just selected which number of sets to play in tennis
    var setup = Provider.of<ActiveMatch>(context, listen: false).setup;
    if (setup is TennisMatchSetup) {
      switch (newSelection) {
        case 0:
          setup.sets = TENNIS_SETS.ONE;
          break;
        case 1:
          setup.sets = TENNIS_SETS.THREE;
          break;
        case 2:
          setup.sets = TENNIS_SETS.FIVE;
          break;
      }
    }
  }
}
