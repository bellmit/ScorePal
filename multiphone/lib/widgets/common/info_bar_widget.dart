import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';

class InfoBarWidget extends StatelessWidget {
  final String title;
  final Icon icon;
  const InfoBarWidget({Key key, @required this.title, this.icon})
      : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Card(
      color: Theme.of(context).primaryColorDark,
      child: Container(
        margin: EdgeInsets.all(Values.default_space),
        width: double.infinity,
        child: Row(
            mainAxisAlignment: MainAxisAlignment.start,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              if (null != icon)
                Padding(
                    padding: EdgeInsets.only(right: Values.default_space),
                    child: icon),
              Expanded(
                child: Text(
                  title,
                  style: TextStyle(
                      fontSize: Values.font_size_title,
                      color: Theme.of(context).accentColor),
                ),
              ),
            ]),
      ),
    );
  }
}
