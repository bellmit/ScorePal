import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';

import 'common_widgets.dart';

Future<bool> confirmDialog(
  BuildContext context, {
  String title,
  String content,
  String textOK,
  String textCancel,
  IconData icon,
}) async {
  final values = Values(context);
  final bool isConfirm = await showDialog<bool>(
    context: context,
    builder: (_) => WillPopScope(
      child: AlertDialog(
        title: Row(mainAxisAlignment: MainAxisAlignment.start, children: [
          Padding(
            padding: EdgeInsets.only(right: Values.default_space),
            child: IconWidget(icon == null ? Icons.warning_amber : icon),
          ),
          Expanded(
            child: TextWidget(
              title,
            ),
          ),
        ]),
        content: TextWidget(
          content != null ? content : values.strings.confirm_default,
        ),
        actions: <Widget>[
          TextButton(
            child: TextWidget(
                textCancel != null ? textCancel : values.strings.confirm_no),
            style: values.optionButtonStyle,
            onPressed: () => Navigator.pop(context, false),
          ),
          TextButton(
            child: TextWidget(
                textOK != null ? textOK : values.strings.confirm_yes),
            style: values.optionButtonStyle,
            onPressed: () => Navigator.pop(context, true),
          ),
        ],
      ),
      onWillPop: () async {
        Navigator.pop(context, false);
        return true;
      },
    ),
  );

  return (isConfirm != null) ? isConfirm : false;
}
