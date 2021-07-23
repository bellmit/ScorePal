import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/match_setup.dart';
import 'package:multiphone/match/tennis_match_setup.dart';
import 'package:multiphone/widgets/select_item_checked_widget.dart';
import 'package:multiphone/widgets/select_item_list_widget.dart';
import 'package:multiphone/widgets/select_item_widget.dart';
import 'package:provider/provider.dart';

class SelectTieFinalWidget extends SelectItemCheckedWidget {
  const SelectTieFinalWidget({Key key})
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
        text: values.strings.tennis_final_tie_sel,
        iconSize: Values.image_medium,
      ),
    ];
  }

  @override
  bool getInitialSelection(BuildContext context, int index) {
    // the initial selection is handled by the active match's setup
    var setup = Provider.of<MatchSetup>(context, listen: false);
    if (setup is TennisMatchSetup) {
      // this is correct
      return setup.tieInFinalSet;
    }
    print('the tennis widget shouldn\'t show unless tennis is selected');
    return false;
  }

  @override
  void onSelectionChanged(BuildContext context, int index, bool isSelected) {
    // the user just selected to play a tie in the final
    var setup = Provider.of<MatchSetup>(context, listen: false);
    if (setup is TennisMatchSetup) {
      setup.tieInFinalSet = isSelected;
    }
  }
}