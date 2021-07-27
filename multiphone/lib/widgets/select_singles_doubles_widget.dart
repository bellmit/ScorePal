import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/widgets/common/select_item_list_widget.dart';
import 'package:multiphone/widgets/common/select_item_widget.dart';
import 'package:provider/provider.dart';
import 'package:flutter_svg/flutter_svg.dart';

class SelectSinglesDoublesWidget extends SelectItemListWidget {
  final Function(MatchSinglesDoubles) onSinglesDoublesChanged;
  const SelectSinglesDoublesWidget({Key key, this.onSinglesDoublesChanged})
      : super(
          key: key,
          itemSize: Values.select_item_size_medium,
        );

  @override
  List<SelectItemWidget> items(BuildContext context) {
    final values = Values(context);
    return [
      SelectItemWidget(
        icon: SvgPicture.asset(
          'images/svg/player-receiving-forehand.svg',
        ),
        text: values.strings.tennis_singles,
        iconSize: Values.image_medium,
      ),
      SelectItemWidget(
        icon: SvgPicture.asset(
          'images/svg/player-receiving-doubles.svg',
        ),
        text: values.strings.tennis_doubles,
        iconSize: Values.image_medium,
      ),
    ];
  }

  @override
  int getInitialSelection(BuildContext context) {
    // the initial selection is handled by the active match's setup
    var setup = Provider.of<ActiveSetup>(context, listen: false);
    switch (setup.singlesDoubles) {
      case MatchSinglesDoubles.singles:
        return 0;
      case MatchSinglesDoubles.doubles:
        return 1;
    }
    return 0;
  }

  @override
  void onSelectionChanged(BuildContext context, int newSelection) {
    // the user just selected which number of SinglesDoubles to play
    var setup = Provider.of<ActiveSetup>(context, listen: false);
    switch (newSelection) {
      case 0:
        setup.singlesDoubles = MatchSinglesDoubles.singles;
        break;
      case 1:
        setup.singlesDoubles = MatchSinglesDoubles.doubles;
        break;
    }
    if (null != onSinglesDoublesChanged) {
      onSinglesDoublesChanged(setup.singlesDoubles);
    }
  }
}
