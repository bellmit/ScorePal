import 'package:flutter/material.dart';

import 'package:multiphone/helpers/setup_persistence.dart';
import 'package:multiphone/providers/active_selection.dart';
import 'package:multiphone/providers/sport.dart';
import 'package:multiphone/widgets/common/select_item_list_widget.dart';
import 'package:multiphone/widgets/common/select_item_widget.dart';
import 'package:provider/provider.dart';

import 'common/common_widgets.dart';

class SelectSportWidget extends SelectItemListWidget {
  const SelectSportWidget({Key key}) : super(key: key);

  @override
  List<SelectItemWidget> items(BuildContext context, int currentSelection) {
    final sports = Provider.of<Sports>(context, listen: false).available;
    return sports.map((e) {
      // for each sport, return a widget representing it
      return SelectItemWidget(
          icon: IconSvgWidget(
            e.icon,
            isOnBackground: currentSelection == sports.indexOf(e),
          ),
          text: e.title(context));
    }).toList();
  }

  @override
  int getInitialSelection(BuildContext context) {
    // the initial selection is handled by the active match
    Sport activeSport = Provider.of<ActiveSelection>(context).sport;
    // from the active sport - return the index of the sport for that match
    return Sports.index(activeSport);
  }

  @override
  void onSelectionChanged(BuildContext context, int newSelection) async {
    final activeSelection =
        Provider.of<ActiveSelection>(context, listen: false);
    // before we change the sport, save any old setup
    var setup = activeSelection.getSelectedSetup(false);
    if (null != setup) {
      SetupPersistence().saveAsLastSetupData(setup);
    }
    // change the sport, which will create the new setup
    activeSelection.sport = Sports.find(newSelection);
  }
}
