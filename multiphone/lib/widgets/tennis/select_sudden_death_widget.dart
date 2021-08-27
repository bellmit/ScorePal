import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/widgets/common/common_widgets.dart';
import 'package:multiphone/widgets/common/select_item_checked_widget.dart';
import 'package:multiphone/widgets/common/select_item_widget.dart';

class SelectSuddenDeathWidget extends SelectItemCheckedWidget {
  final bool isSuddenDeath;
  final void Function(bool) onSuddenDeathChanged;
  const SelectSuddenDeathWidget({
    Key key,
    @required this.isSuddenDeath,
    @required this.onSuddenDeathChanged,
  }) : super(
          key: key,
          itemSize: Values.select_item_size_medium,
        );

  @override
  List<SelectItemWidget> items(BuildContext context) {
    final values = Values(context);
    return [
      SelectItemWidget(
        icon: IconSvgWidget(
          'deuce-sudden-death',
        ),
        text: values.strings.tennis_sudden_death_deuce_sel,
        iconSize: Values.image_medium,
      ),
    ];
  }

  @override
  bool getInitialSelection(BuildContext context, int index) {
    return isSuddenDeath;
  }

  @override
  void onSelectionChanged(BuildContext context, int index, bool isSelected) {
    // the user just selected to do a sudden death instead of deuce
    onSuddenDeathChanged(isSelected);
  }
}
