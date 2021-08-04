import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:multiphone/helpers/log.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/widgets/settings/settings_widget_mixin.dart';

class SettingsDataWidget extends StatelessWidget with SettingsWidgetMixin {
  SettingsDataWidget({Key key}) : super(key: key);

  Widget createDataRow(BuildContext context, IconData icon, String title,
      String explain, String buttonText, void Function() onPressed) {
    return Padding(
      padding: const EdgeInsets.only(top: Values.default_space),
      child: Row(
        children: <Widget>[
          Padding(
            padding: const EdgeInsets.only(right: Values.default_space),
            child: Icon(
              icon,
              size: Values.image_large,
            ),
          ),
          Expanded(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.start,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Expanded(
                      child: createHeading2(title),
                    ),
                    ElevatedButton(
                      style: values.optionButtonStyle,
                      child: Text(buttonText),
                      onPressed: onPressed,
                    ),
                  ],
                ),
                Text(
                  explain,
                  style: contentTextStyle,
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    // prepare our member data to use and reuse
    prepareWidget(context);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        createHeading(values.strings.title_manage_data),
        createDataRow(
          context,
          Icons.file_download,
          values.strings.title_import_match,
          values.strings.explain_import_match,
          values.strings.button_import,
          () => Log.debug('switched'),
        ),
        createDataRow(
          context,
          Icons.contact_mail,
          values.strings.title_manage_deleted,
          values.strings.explain_manage_deleted,
          values.strings.button_view,
          () => Log.debug('switched'),
        ),
        createDataRow(
          context,
          Icons.bluetooth,
          values.strings.title_attributions,
          values.strings.explain_attributions,
          values.strings.button_view,
          () => Log.debug('switched'),
        ),
      ],
    );
  }
}
