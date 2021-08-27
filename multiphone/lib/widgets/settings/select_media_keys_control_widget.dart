import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/widgets/common/common_widgets.dart';
import 'package:multiphone/widgets/common/select_item_checked_widget.dart';
import 'package:multiphone/widgets/common/select_item_widget.dart';

class SelectMediaKeysControlWidget extends SelectItemCheckedWidget {
  final bool isKeysControlSelected;
  final void Function(bool) onKeysControlChanged;
  const SelectMediaKeysControlWidget({
    Key key,
    @required this.isKeysControlSelected,
    @required this.onKeysControlChanged,
  }) : super(
          key: key,
          itemSize: Values.select_item_size_medium,
        );

  @override
  List<SelectItemWidget> items(BuildContext context) {
    final values = Values(context);
    return [
      SelectItemWidget(
        icon: IconWidget(Icons.gamepad, size: Values.image_medium),
        text: values.strings.title_control_keys,
        iconSize: Values.image_medium,
      ),
    ];
  }

  @override
  bool getInitialSelection(BuildContext context, int index) {
    return isKeysControlSelected;
  }

  @override
  void onSelectionChanged(BuildContext context, int index, bool isSelected) {
    // the user just selected to do a sudden death instead of deuce
    onKeysControlChanged(isSelected);
  }
}
