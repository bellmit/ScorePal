import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';

class HeadingWidget extends StatelessWidget {
  final String title;
  const HeadingWidget({Key key, @required this.title}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Text(
        title,
        textAlign: TextAlign.center,
        style: TextStyle(
          fontSize: Values.font_size_title,
          fontWeight: FontWeight.bold,
          color: Theme.of(context).primaryColorDark,
        ),
      ),
    );
  }
}
