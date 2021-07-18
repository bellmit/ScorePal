import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';

class SelectItemWidget extends StatelessWidget {
  final IconData icon;
  final String text;
  const SelectItemWidget({Key key, @required this.icon, @required this.text})
      : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Column(
      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
      children: [
        FittedBox(
          fit: BoxFit.contain,
          child: Icon(
            icon,
            size: Values.image_large,
          ),
        ),
        FittedBox(
          fit: BoxFit.contain,
          child: Text(text),
        ),
      ],
    );
  }
}
