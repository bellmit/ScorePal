import 'package:flutter/material.dart';
import 'package:multiphone/helpers/log.dart';
import 'package:multiphone/widgets/settings/settings_widget_mixin.dart';

class SettingsPermissionsWidget extends StatefulWidget {
  SettingsPermissionsWidget({Key key}) : super(key: key);

  @override
  _SettingsPermissionsWidgetState createState() =>
      _SettingsPermissionsWidgetState();
}

class _SettingsPermissionsWidgetState extends State<SettingsPermissionsWidget>
    with SettingsWidgetMixin {
  bool _isLocation = false;
  bool _isContacts = false;
  bool _isBluetooth = false;

  @override
  void initState() {
    super.initState();
    // get the defaults to show
  }

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
          (value) => Log.debug('switched'),
          isSelected: _isLocation,
        ),
        createSwitchingRow(
          context,
          createIcon(Icons.contact_mail),
          values.strings.title_permission_contacts,
          values.strings.explain_permission_contacts,
          (value) => Log.debug('switched'),
          isSelected: _isContacts,
        ),
        createSwitchingRow(
          context,
          createIcon(Icons.bluetooth),
          values.strings.title_permission_bluetooth,
          values.strings.explain_permission_bluetooth,
          (value) => Log.debug('switched'),
          isSelected: _isBluetooth,
        ),
      ],
    );
  }
}
