import 'package:flutter/material.dart';
import 'package:multiphone/helpers/log.dart';
import 'package:multiphone/helpers/preferences.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/widgets/common/common_widgets.dart';
import 'package:multiphone/widgets/settings/settings_widget_mixin.dart';
import 'package:permission_handler/permission_handler.dart';

class SettingsPermissionsWidget extends StatefulWidget {
  final Preferences prefs;
  SettingsPermissionsWidget({Key key, @required this.prefs}) : super(key: key);

  @override
  _SettingsPermissionsWidgetState createState() =>
      _SettingsPermissionsWidgetState();
}

class _SettingsPermissionsWidgetState extends State<SettingsPermissionsWidget>
    with SettingsWidgetMixin {
  bool _isLocation = false;
  bool _isMatchLocationPermitted = true;
  bool _isContacts = false;
  bool _isBluetooth = false;

  @override
  void initState() {
    super.initState();
    // get the defaults to show
    _isMatchLocationPermitted = widget.prefs.isMatchLocationPermitted;

    // get permissions to enable the values as expected
    Permission.location.isGranted
        .then((value) => setState(() {
              _isLocation = value;
            }))
        .onError((error, stackTrace) =>
            Log.error('failed to check location permissions $error'));
    Permission.contacts.isGranted
        .then((value) => setState(() {
              _isContacts = value;
            }))
        .onError((error, stackTrace) =>
            Log.error('failed to check contact permissions $error'));
    Permission.bluetooth.isGranted
        .then((value) => setState(() {
              _isBluetooth = value;
            }))
        .onError((error, stackTrace) =>
            Log.error('failed to check bluetooth permissions $error'));

    //TODO cannot revoke permissions - just show a 'on' button somehow instead of a switch?
  }

  void _onLocationPermissionChanged(bool isLocationPermitted) {
    // push this data back out to the preferences
    setState(() {
      _isMatchLocationPermitted = isLocationPermitted;
      widget.prefs.isMatchLocationPermitted = isLocationPermitted;
    });
  }

  @override
  Widget build(BuildContext context) {
    // prepare our member data to use and reuse
    final values = Values(context);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        TextWidget(values.strings.title_permissions),
        createSwitchingRow(
          context,
          IconWidget(Icons.near_me),
          values.strings.title_permission_location,
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              TextWidget(values.strings.explain_permission_location),
              Row(
                children: [
                  IconWidget(Icons.location_history, size: Values.image_icon),
                  Expanded(
                    child: TextWidget(
                        values.strings.title_permission_match_location),
                  ),
                  Switch(
                    activeColor: Theme.of(context).primaryColor,
                    value: _isMatchLocationPermitted,
                    onChanged: _onLocationPermissionChanged,
                  ),
                ],
              ),
            ],
          ),
          (value) => Permission.location
              .request()
              .then((value) => setState(() {
                    _isLocation = value.isGranted;
                  }))
              .onError((error, stackTrace) =>
                  Log.error('failed to request location permissions $error')),
          isSelected: _isLocation,
        ),
        createSwitchingRow(
          context,
          IconWidget(Icons.contact_mail),
          values.strings.title_permission_contacts,
          TextWidget(values.strings.explain_permission_contacts),
          (value) => Permission.contacts
              .request()
              .then((value) => setState(() {
                    _isContacts = value.isGranted;
                  }))
              .onError((error, stackTrace) =>
                  Log.error('failed to request contact permissions $error')),
          isSelected: _isContacts,
        ),
        createSwitchingRow(
          context,
          IconWidget(Icons.bluetooth),
          values.strings.title_permission_bluetooth,
          TextWidget(values.strings.explain_permission_bluetooth),
          (value) => Permission.bluetooth
              .request()
              .then((value) => setState(() {
                    _isBluetooth = value.isGranted;
                  }))
              .onError((error, stackTrace) =>
                  Log.error('failed to request bluetooth permissions $error')),
          isSelected: _isBluetooth,
        ),
      ],
    );
  }
}
