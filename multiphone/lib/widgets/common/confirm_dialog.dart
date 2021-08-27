import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';

import 'common_widgets.dart';
import 'icon_button_widget.dart';

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
          IconButtonWidget(
            () => Navigator.pop(context, false),
            null,
            textCancel != null ? textCancel : values.strings.confirm_no,
          ),
          IconButtonWidget(
            () => Navigator.pop(context, true),
            null,
            textOK != null ? textOK : values.strings.confirm_yes,
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
