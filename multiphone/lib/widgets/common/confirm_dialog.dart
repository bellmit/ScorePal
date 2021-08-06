import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';

Future<bool> confirmDialog(
  BuildContext context, {
  String title,
  String content,
  String textOK,
  String textCancel,
  IconData icon,
}) async {
  final values = Values(context);
  final theme = Theme.of(context);
  final bool isConfirm = await showDialog<bool>(
    context: context,
    builder: (_) => WillPopScope(
      child: AlertDialog(
        title: Row(mainAxisAlignment: MainAxisAlignment.start, children: [
          Padding(
            padding: EdgeInsets.only(right: Values.default_space),
            child: Icon(
              icon == null ? Icons.warning_amber : icon,
              size: Values.image_medium,
              color: Values.deleteColor,
            ),
          ),
          Expanded(
            child: Text(
              title,
              style: theme.textTheme.bodyText1
                  .copyWith(color: theme.primaryColorDark),
            ),
          ),
        ]),
        content: Text(
          content != null ? content : values.strings.confirm_default,
          style:
              theme.textTheme.bodyText2.copyWith(color: theme.primaryColorDark),
        ),
        actions: <Widget>[
          TextButton(
            child: Text(
                textCancel != null ? textCancel : values.strings.confirm_no),
            style: values.optionButtonStyle,
            onPressed: () => Navigator.pop(context, false),
          ),
          TextButton(
            child: Text(textOK != null ? textOK : values.strings.confirm_yes),
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
