import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:multiphone/helpers/log.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/widgets/settings/settings_widget_mixin.dart';

class SettingsPermissionsWidget extends StatelessWidget
    with SettingsWidgetMixin {
  SettingsPermissionsWidget({Key key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    // prepare our member data to use and reuse
    prepareWidget(context);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        createHeading(values.strings.title_permissions),
        createSwitchingRow(
          context,
          createIcon(Icons.near_me),
          values.strings.title_permission_location,
          values.strings.explain_permission_location,
          (bool) => Log.debug('switched'),
        ),
        createSwitchingRow(
          context,
          createIcon(Icons.contact_mail),
          values.strings.title_permission_contacts,
          values.strings.explain_permission_contacts,
          (bool) => Log.debug('switched'),
        ),
        createSwitchingRow(
          context,
          createIcon(Icons.bluetooth),
          values.strings.title_permission_bluetooth,
          values.strings.explain_permission_bluetooth,
          (bool) => Log.debug('switched'),
        ),
      ],
    );
  }
}
