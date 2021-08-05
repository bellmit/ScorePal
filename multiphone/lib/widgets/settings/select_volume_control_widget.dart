import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/widgets/common/select_item_checked_widget.dart';
import 'package:multiphone/widgets/common/select_item_widget.dart';

class SelectVolumeControlWidget extends SelectItemCheckedWidget {
  final bool isVolControlSelected;
  final void Function(bool) onVolControlChanged;
  const SelectVolumeControlWidget({
    Key key,
    @required this.isVolControlSelected,
    @required this.onVolControlChanged,
  }) : super(
          key: key,
          itemSize: Values.select_item_size_medium,
        );

  @override
  List<SelectItemWidget> items(BuildContext context) {
    final values = Values(context);
    return [
      SelectItemWidget(
        icon: Icon(
          Icons.volume_up,
          size: Values.image_medium,
        ),
        text: values.strings.control_volume,
        iconSize: Values.image_medium,
      ),
    ];
  }

  @override
  bool getInitialSelection(BuildContext context, int index) {
    return isVolControlSelected;
  }

  @override
  void onSelectionChanged(BuildContext context, int index, bool isSelected) {
    // the user just selected to do a sudden death instead of deuce
    onVolControlChanged(isSelected);
  }
}
