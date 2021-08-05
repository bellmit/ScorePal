import 'package:flutter/material.dart';
import 'package:multiphone/helpers/log.dart';
import 'package:multiphone/widgets/settings/settings_widget_mixin.dart';
import 'package:permission_handler/permission_handler.dart';

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

    // get permissions to enable the values as expected
    Permission.location.isGranted.then((value) => setState(() {
          _isLocation = value;
        }));
    Permission.contacts.isGranted.then((value) => setState(() {
          _isContacts = value;
        }));
    Permission.bluetooth.isGranted.then((value) => setState(() {
          _isBluetooth = value;
        }));

    //TODO cannot revoke permissions - just show a 'on' button somehow instead of a switch?
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
          (value) => Permission.location.request().then((value) => setState(() {
                _isLocation = value.isGranted;
              })),
          isSelected: _isLocation,
        ),
        createSwitchingRow(
          context,
          createIcon(Icons.contact_mail),
          values.strings.title_permission_contacts,
          values.strings.explain_permission_contacts,
          (value) => Permission.contacts.request().then((value) => setState(() {
                _isContacts = value.isGranted;
              })),
          isSelected: _isContacts,
        ),
        createSwitchingRow(
          context,
          createIcon(Icons.bluetooth),
          values.strings.title_permission_bluetooth,
          values.strings.explain_permission_bluetooth,
          (value) =>
              Permission.bluetooth.request().then((value) => setState(() {
                    _isBluetooth = value.isGranted;
                  })),
          isSelected: _isBluetooth,
        ),
      ],
    );
  }
}
