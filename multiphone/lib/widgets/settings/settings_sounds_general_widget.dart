import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:multiphone/helpers/log.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/widgets/settings/settings_widget_mixin.dart';

class SettingsSoundsGeneralWidget extends StatelessWidget
    with SettingsWidgetMixin {
  SettingsSoundsGeneralWidget({Key key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    // prepare our member data to use and reuse
    prepareWidget(context);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        createHeading(values.strings.title_general),
        createSwitchingRow(
          context,
          createIcon(Icons.chat),
          values.strings.title_use_names,
          values.strings.explain_use_names,
          (bool) => Log.debug('switched'),
        ),
        createSwitchingRow(
          context,
          createIcon(Icons.volume_up),
          values.strings.title_volume,
          values.strings.explain_control_vol,
          (bool) => Log.debug('switched'),
        ),
        createSwitchingRow(
          context,
          createIcon(Icons.settings_remote),
          values.strings.title_speak_message,
          values.strings.explain_speak_message,
          (bool) => Log.debug('switched'),
        ),
      ],
    );
  }
}
