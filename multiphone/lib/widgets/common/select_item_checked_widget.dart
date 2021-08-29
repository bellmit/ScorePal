import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/widgets/common/select_item_widget.dart';

abstract class SelectItemCheckedWidget extends StatefulWidget {
  final double itemSize;

  const SelectItemCheckedWidget(
      {Key key, this.itemSize = Values.select_item_size_large})
      : super(key: key);

  @override
  _SelectItemCheckedWidgetState createState() =>
      _SelectItemCheckedWidgetState();

  List<SelectItemWidget> items(BuildContext context, List<bool> isSelected);

  bool getInitialSelection(BuildContext context, int index);

  void onSelectionChanged(BuildContext context, int index, bool newSelection);
}

class _SelectItemCheckedWidgetState extends State<SelectItemCheckedWidget> {
  final List<bool> _isSelected = [];

  _SelectItemCheckedWidgetState();

  @override
  Widget build(BuildContext context) {
    // get the items we will be adding to the buttons
    var selectItems = widget.items(context, _isSelected);
    const borderRadius =
        BorderRadius.all(Radius.circular(Values.default_radius));
    if (_isSelected.length != selectItems.length) {
      // setup the initial selection from the widget
      for (int i = 0; i < selectItems.length; ++i) {
        _isSelected.add(widget.getInitialSelection(context, i));
      }
    }
    return Container(
      // wrapped in a container to put a nice colored background behind the buttons
      padding: EdgeInsets.zero,
      decoration: BoxDecoration(
        color: Theme.of(context).backgroundColor,
        border: Border.all(
            color: Theme.of(context).secondaryHeaderColor,
            width: Values.border_width),
        borderRadius: borderRadius,
      ),
      child: ToggleButtons(
        children: selectItems,
        onPressed: (int index) {
          // change our state
          setState(() {
            // change the state
            _isSelected[index] = !_isSelected[index];
            // and on the widget that might also want to know
            widget.onSelectionChanged(context, index, _isSelected[index]);
          });
        },
        isSelected: _isSelected,
        constraints: BoxConstraints.expand(
            width: widget.itemSize, height: widget.itemSize),
        renderBorder: false,
        selectedColor: Theme.of(context).accentTextTheme.button.color,
        fillColor: Theme.of(context).primaryColor,
        borderRadius: borderRadius,
      ),
    );
  }
}
