import 'package:flutter/material.dart';

import 'common_widgets.dart';

class HeadingIconWidget extends StatelessWidget {
  final String title;
  final IconData icon;
  const HeadingIconWidget({Key key, @required this.icon, @required this.title})
      : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        IconWidget(icon),
        TextHeadingWidget(title),
      ],
    );
  }
}
