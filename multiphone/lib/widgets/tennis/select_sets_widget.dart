import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/tennis/tennis_match_setup.dart';
import 'package:multiphone/widgets/common/common_widgets.dart';
import 'package:multiphone/widgets/common/select_item_list_widget.dart';
import 'package:multiphone/widgets/common/select_item_widget.dart';

class SelectSetsWidget extends SelectItemListWidget {
  final TennisSets sets;
  final void Function(TennisSets) onSetsChanged;
  const SelectSetsWidget({
    Key key,
    @required this.sets,
    @required this.onSetsChanged,
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
          'tennis-ball-one',
        ),
        text: values.strings.tennis_one_set,
        iconSize: Values.image_medium,
      ),
      SelectItemWidget(
        icon: IconSvgWidget(
          'tennis-ball-three',
        ),
        text: values.strings.tennis_three_sets,
        iconSize: Values.image_medium,
      ),
      SelectItemWidget(
        icon: IconSvgWidget(
          'tennis-ball-five',
        ),
        text: values.strings.tennis_five_sets,
        iconSize: Values.image_medium,
      ),
    ];
  }

  @override
  int getInitialSelection(BuildContext context) {
    // the initial selection is handled by the active match's setup
    switch (sets) {
      case TennisSets.one:
        return 0;
      case TennisSets.three:
        return 1;
      case TennisSets.five:
        return 2;
      default:
        return 1;
    }
  }

  @override
  void onSelectionChanged(BuildContext context, int newSelection) {
    // the user just selected which number of sets to play in tennis
    switch (newSelection) {
      case 0:
        onSetsChanged(TennisSets.one);
        break;
      case 1:
        onSetsChanged(TennisSets.three);
        break;
      case 2:
        onSetsChanged(TennisSets.five);
        break;
      default:
        onSetsChanged(TennisSets.three);
        break;
    }
  }
}
