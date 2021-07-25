import 'package:flutter/material.dart';
import 'package:multiphone/providers/active_selection.dart';
import 'package:multiphone/providers/sport.dart';
import 'package:multiphone/widgets/select_item_list_widget.dart';
import 'package:multiphone/widgets/select_item_widget.dart';
import 'package:provider/provider.dart';

class SelectSportWidget extends SelectItemListWidget {
  const SelectSportWidget({Key key}) : super(key: key);

  @override
  List<SelectItemWidget> items(BuildContext context) {
    return Provider.of<Sports>(context, listen: false).available.map((e) {
      // for each sport, return a widget representing it
      return SelectItemWidget(icon: e.icon, text: e.title(context));
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
  void onSelectionChanged(BuildContext context, int newSelection) {
    // the user just selected a nice sport, inform the provider of this
    final activeMatch = Provider.of<ActiveSelection>(context, listen: false);
    activeMatch.sport = Sports.find(newSelection);
  }
}
