import 'package:flutter/material.dart';
import 'package:multiphone/helpers/preferences.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/widgets/settings/select_media_keys_control_widget.dart';
import 'package:multiphone/widgets/settings/select_volume_buttons_control_widget.dart';
import 'package:multiphone/widgets/settings/settings_widget_mixin.dart';
import 'package:url_launcher/url_launcher.dart';

class SettingsControlHardwareButtonsWidget extends StatefulWidget {
  final Preferences prefs;
  SettingsControlHardwareButtonsWidget({Key key, @required this.prefs})
      : super(key: key);

  @override
  _SettingsControlHardwareButtonsWidgetState createState() =>
      _SettingsControlHardwareButtonsWidgetState();
}

class _SettingsControlHardwareButtonsWidgetState
    extends State<SettingsControlHardwareButtonsWidget>
    with SettingsWidgetMixin {
  bool _isMediaKeysControl = false;
  bool _isVolumeButtonsControl = false;
  static const btRemoteUrl =
      "https://www.google.com/search?q=amazon+bluetooth+media+remote+10m";

  @override
  void initState() {
    super.initState();
    // get the defaults to show
    _isMediaKeysControl = widget.prefs.isControlMediaKeys;
    _isVolumeButtonsControl = widget.prefs.isControlVolumeButtons;
  }

  static void navUserToPurchaseRemote(BuildContext context) {
    canLaunch(btRemoteUrl).then((value) {
      return launch(btRemoteUrl);
    }).onError((error, stackTrace) {
      // failed to launch
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(
          content: Text(Values(context).strings.error_navigating_to_remote)));
      return false;
    });
  }

  @override
  Widget build(BuildContext context) {
    // prepare our member data to use and reuse
    prepareWidget(context);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        createHeading(values.strings.title_control_keys),
        Row(
          children: <Widget>[
            Padding(
              padding: const EdgeInsets.only(right: Values.default_space),
              child: SelectVolumeButtonsControlWidget(
                isKeysControlSelected: _isVolumeButtonsControl,
                onKeysControlChanged: (value) =>
                    widget.prefs.isControlVolumeButtons = value,
              ),
            ),
            Expanded(
              child: Text(
                values.strings.explain_control_volume,
                style: contentTextStyle,
              ),
            ),
          ],
        ),
        SizedBox(height: Values.default_space),
        Row(
          children: <Widget>[
            Padding(
              padding: const EdgeInsets.only(right: Values.default_space),
              child: SelectMediaKeysControlWidget(
                isKeysControlSelected: _isMediaKeysControl,
                onKeysControlChanged: (value) =>
                    widget.prefs.isControlMediaKeys = value,
              ),
            ),
            Expanded(
              child: Text(
                values.strings.explain_control_keys,
                style: contentTextStyle,
              ),
            ),
          ],
        ),
        SizedBox(height: Values.default_space),
        Row(
          children: <Widget>[
            Padding(
              padding: const EdgeInsets.only(right: Values.default_space),
              child: createIconSvg('images/svg/media-remote.svg'),
            ),
            TextButton(
              onPressed: () => navUserToPurchaseRemote(context),
              child: Text(
                values.strings.option_browse,
                style: TextStyle(fontSize: Values.font_size_title),
              ),
            ),
            Expanded(
              child: Text(
                values.strings.explain_control_keys_hack,
                style: contentTextStyle,
              ),
            ),
          ],
        ),
      ],
    );
  }
}