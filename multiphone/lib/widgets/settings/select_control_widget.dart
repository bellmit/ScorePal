import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/widgets/common/select_item_list_widget.dart';
import 'package:multiphone/widgets/common/select_item_widget.dart';
import 'package:flutter_svg/flutter_svg.dart';

enum ControlType {
  meThem,
  serverReceiver,
}

class SelectControlWidget extends SelectItemListWidget {
  final ControlType initialSelection;
  final void Function(ControlType) onControlTypeChanged;
  const SelectControlWidget({
    Key key,
    @required this.initialSelection,
    @required this.onControlTypeChanged,
  }) : super(
          key: key,
          itemSize: Values.select_item_size_medium,
        );

  @override
  List<SelectItemWidget> items(BuildContext context) {
    final values = Values(context);
    return [
      SelectItemWidget(
        icon: SvgPicture.asset(
          'images/svg/control-we.svg',
        ),
        text: values.strings.control_me_them,
        iconSize: Values.image_medium,
      ),
      SelectItemWidget(
        icon: SvgPicture.asset(
          'images/svg/control-them.svg',
        ),
        text: values.strings.control_server_receiver,
        iconSize: Values.image_medium,
      ),
    ];
  }

  @override
  int getInitialSelection(BuildContext context) {
    // the initial selection is handled by the active match's setup
    switch (initialSelection) {
      case ControlType.meThem:
        return 0;
      case ControlType.serverReceiver:
        return 1;
      default:
        return 0;
    }
  }

  @override
  void onSelectionChanged(BuildContext context, int newSelection) {
    // the user just selected which number of games to play in tennis
    switch (newSelection) {
      case 0:
        onControlTypeChanged(ControlType.meThem);
        break;
      case 1:
        onControlTypeChanged(ControlType.serverReceiver);
        break;
      default:
        onControlTypeChanged(ControlType.meThem);
        break;
    }
  }
}
