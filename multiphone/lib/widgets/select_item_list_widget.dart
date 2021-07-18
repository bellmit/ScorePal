import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/widgets/select_item_widget.dart';

abstract class SelectItemListWidget extends StatefulWidget {
  const SelectItemListWidget({Key key}) : super(key: key);

  @override
  _SelectItemListWidgetState createState() => _SelectItemListWidgetState();

  List<SelectItemWidget> items(BuildContext context);

  int getInitialSelection(BuildContext context);

  void onSelectionChanged(BuildContext context, int newSelection);
}

class _SelectItemListWidgetState extends State<SelectItemListWidget> {
  int _currentSelection = 0;

  final List<bool> _isSelected = [];

  _SelectItemListWidgetState();

  @override
  Widget build(BuildContext context) {
    // get the items we will be adding to the buttons
    var selectItems = widget.items(context);
    if (_isSelected.length != selectItems.length) {
      // setup the initial selection from the widget
      _currentSelection = widget.getInitialSelection(context);
      for (int i = 0; i < selectItems.length; ++i) {
        _isSelected.add(i == _currentSelection);
      }
    }
    return Container(
      // wrapped in a container to put a nice colored background behind the buttons
      padding: EdgeInsets.zero,
      decoration: BoxDecoration(
        color: Theme.of(context).backgroundColor,
        border: Border.all(color: Colors.black, width: 1.0),
        borderRadius: BorderRadius.all(Radius.circular(20)),
      ),
      child: ToggleButtons(
        children: selectItems,
        onPressed: (int index) {
          // change our state
          setState(() {
            _currentSelection = index;
            for (int buttonIndex = 0;
                buttonIndex < _isSelected.length;
                buttonIndex++) {
              if (buttonIndex == index) {
                _isSelected[buttonIndex] = true;
              } else {
                _isSelected[buttonIndex] = false;
              }
            }
            // and on the widget that might also want to know
            widget.onSelectionChanged(context, _currentSelection);
          });
        },
        isSelected: _isSelected,
        constraints: BoxConstraints.expand(
            width: Values.select_item_size, height: Values.select_item_size),
        renderBorder: true,
        selectedColor: Theme.of(context).accentColor,
        fillColor: Theme.of(context).primaryColor,
        borderRadius: BorderRadius.circular(20),
      ),
    );
  }
}
