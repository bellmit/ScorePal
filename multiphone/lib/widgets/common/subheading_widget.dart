import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';

class SubheadingWidget extends StatelessWidget {
  final String title;
  const SubheadingWidget({Key key, @required this.title}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Text(
      'Setup',
      style: TextStyle(
        color: Theme.of(context).primaryColorDark,
        fontSize: Values.font_size_subtitle,
        fontWeight: FontWeight.bold,
      ),
    );
  }
}
