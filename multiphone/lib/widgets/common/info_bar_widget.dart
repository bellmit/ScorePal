import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';

import 'common_widgets.dart';

class InfoBarWidget extends StatelessWidget {
  final String title;
  final IconData icon;
  final bool isError;
  const InfoBarWidget(
      {Key key, @required this.title, this.icon, this.isError = false})
      : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Card(
      color: Theme.of(context).secondaryHeaderColor,
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
                  child: IconWidget(icon, isOnBackground: true),
                ),
              Expanded(
                child: TextWidget(title, isOnBackground: true),
              ),
            ]),
      ),
    );
  }
}
