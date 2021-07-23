import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';

class InfoBarWidget extends StatelessWidget {
  final String title;
  const InfoBarWidget({Key key, @required this.title}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Card(
      color: Theme.of(context).primaryColorLight,
      child: Container(
        margin: EdgeInsets.all(Values.default_space),
        width: double.infinity,
        child: Text(
          title,
          style: TextStyle(
              fontSize: Values.font_size_title,
              color: Theme.of(context).primaryColorDark),
        ),
      ),
    );
  }
}
