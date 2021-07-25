import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/widgets/common/heading_widget.dart';

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
        Icon(
          icon,
          size: Values.image_large,
          color: Theme.of(context).primaryColorDark,
        ),
        HeadingWidget(title: title),
      ],
    );
  }
}
