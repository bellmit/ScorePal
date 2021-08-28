import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/widgets/common/common_widgets.dart';
import 'package:multiphone/widgets/common/select_item_checked_widget.dart';
import 'package:multiphone/widgets/common/select_item_widget.dart';

class SelectTieFinalWidget extends SelectItemCheckedWidget {
  final bool isTieInFinalSet;
  final void Function(bool) onTieChanged;
  const SelectTieFinalWidget({
    Key key,
    @required this.isTieInFinalSet,
    @required this.onTieChanged,
  }) : super(
          key: key,
          itemSize: Values.select_item_size_medium,
        );

  @override
  List<SelectItemWidget> items(BuildContext context, List<bool> isSelected) {
    final values = Values(context);
    return [
      SelectItemWidget(
        icon: IconSvgWidget(
          'tie-break',
          isOnBackground: isSelected == null || isSelected.length == 0
              ? isTieInFinalSet
              : isSelected[0],
        ),
        text: values.strings.tennis_final_tie_sel,
        iconSize: Values.image_medium,
      ),
    ];
  }

  @override
  bool getInitialSelection(BuildContext context, int index) {
    // the initial selection is handled by the active match's setup
    return isTieInFinalSet;
  }

  @override
  void onSelectionChanged(BuildContext context, int index, bool isSelected) {
    // the user just selected to play a tie in the final
    onTieChanged(isSelected);
  }
}
