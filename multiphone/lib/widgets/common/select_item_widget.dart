import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';

import 'common_widgets.dart';

class SelectItemWidget extends StatelessWidget {
  final Widget icon;
  final String text;
  final double iconSize;
  const SelectItemWidget(
      {Key key,
      @required this.icon,
      @required this.text,
      this.iconSize = Values.image_large})
      : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Column(
      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
      children: [
        FittedBox(
          fit: BoxFit.contain,
          child: Container(
            height: iconSize,
            width: iconSize,
            child: icon,
          ),
        ),
        FittedBox(
          fit: BoxFit.contain,
          child: TextWidget(text),
        ),
      ],
    );
  }
}
