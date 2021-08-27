import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';

import 'common_widgets.dart';

class IconButtonWidget extends StatelessWidget {
  final void Function() callback;
  final IconData icon;
  final String text;
  const IconButtonWidget(this.callback, this.icon, this.text);
  @override
  Widget build(BuildContext context) {
    return MaterialButton(
      onPressed: callback,
      color: Theme.of(context).primaryColor,
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          IconWidget(icon, size: null),
          Padding(
            padding: const EdgeInsets.all(Values.default_space),
            child: Text(text),
          ),
        ],
      ),
    );
  }
}
