import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/widgets/common/common_widgets.dart';
import 'package:multiphone/widgets/common/select_item_checked_widget.dart';
import 'package:multiphone/widgets/common/select_item_widget.dart';

enum FlicButton {
  one,
  two,
}

class SelectFlicWidget extends SelectItemCheckedWidget {
  final FlicButton button;
  final bool isFlicSelected;
  final void Function(bool) onFlicChanged;
  const SelectFlicWidget({
    Key key,
    @required this.button,
    @required this.isFlicSelected,
    @required this.onFlicChanged,
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
          button == FlicButton.one ? 'flic-one' : 'flic-two',
          isOnBackground: isSelected == null || isSelected.length == 0
              ? isFlicSelected
              : isSelected[0],
        ),
        text: button == FlicButton.one
            ? values.strings.control_flic
            : values.strings.control_flic2,
        iconSize: Values.image_medium,
      ),
    ];
  }

  @override
  bool getInitialSelection(BuildContext context, int index) {
    return isFlicSelected;
  }

  @override
  void onSelectionChanged(BuildContext context, int index, bool isSelected) {
    // the user just selected to do a sudden death instead of deuce
    onFlicChanged(isSelected);
  }
}
