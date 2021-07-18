import 'package:flutter/material.dart';
import 'package:multiphone/match/sport.dart';
import 'package:multiphone/widgets/select_item_list_widget.dart';
import 'package:multiphone/widgets/select_item_widget.dart';
import 'package:provider/provider.dart';

class SelectSportWidget extends SelectItemListWidget {
  const SelectSportWidget({Key key}) : super(key: key);

  @override
  List<SelectItemWidget> items(BuildContext context) {
    return Provider.of<Sports>(context, listen: false).sports.map((e) {
      // for each sport, return a widget representing it
      return SelectItemWidget(icon: e.icon, text: e.title(context));
    }).toList();
  }

  @override
  int getInitialSelection(BuildContext context) {
    // the initial selection is handled by the sport provider
    Sports sports = Provider.of<Sports>(context);
    return sports.index(sports.selected);
  }

  @override
  void onSelectionChanged(BuildContext context, int newSelection) {
    // the user just selected a nice sport, inform the provider of this
    final sports = Provider.of<Sports>(context, listen: false);
    sports.selected = sports.find(newSelection);
  }
}
