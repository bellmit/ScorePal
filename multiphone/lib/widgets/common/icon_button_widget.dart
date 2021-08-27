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
          if (icon != null) IconWidget(icon, size: null),
          Padding(
            padding: EdgeInsets.all(icon != null ? Values.default_space : 0),
            child: Text(text),
          ),
        ],
      ),
    );
  }
}

class SvgIconButtonWidget extends StatelessWidget {
  final void Function() callback;
  final String icon;
  final String text;
  const SvgIconButtonWidget(this.callback, this.icon, this.text);
  @override
  Widget build(BuildContext context) {
    return MaterialButton(
      onPressed: callback,
      color: Theme.of(context).primaryColor,
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          if (icon != null) IconSvgWidget(icon, size: null),
          Padding(
            padding: EdgeInsets.all(icon != null ? Values.default_space : 0),
            child: Text(text),
          ),
        ],
      ),
    );
  }
}

class IconButtonWrappedWidget extends StatelessWidget {
  final void Function() callback;
  final IconData icon;
  final String text;
  const IconButtonWrappedWidget(this.callback, this.icon, this.text);
  @override
  Widget build(BuildContext context) {
    return MaterialButton(
      onPressed: callback,
      color: Theme.of(context).primaryColor,
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          IconWidget(icon, size: null),
          Expanded(
            child: Padding(
              padding: const EdgeInsets.all(8.0),
              child: Text(text, maxLines: 5, overflow: TextOverflow.clip),
            ),
          ),
        ],
      ),
    );
  }
}
