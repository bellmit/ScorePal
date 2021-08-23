import 'package:flutter/material.dart';
import 'package:multiphone/helpers/preferences.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/widgets/settings/select_volume_control_widget.dart';
import 'package:multiphone/widgets/settings/settings_widget_mixin.dart';
import 'package:url_launcher/url_launcher.dart';

class SettingsControlsVolumeWidget extends StatefulWidget {
  final Preferences prefs;
  SettingsControlsVolumeWidget({Key key, @required this.prefs})
      : super(key: key);

  @override
  _SettingsControlsVolumeWidgetState createState() =>
      _SettingsControlsVolumeWidgetState();
}

class _SettingsControlsVolumeWidgetState
    extends State<SettingsControlsVolumeWidget> with SettingsWidgetMixin {
  bool _isVolControl = false;
  static const btRemoteUrl =
      "https://www.google.com/search?q=amazon+bluetooth+media+remote+10m";

  @override
  void initState() {
    super.initState();
    // get the defaults to show
    _isVolControl = widget.prefs.isControlVol;
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
        createHeading(values.strings.title_control_vol),
        Row(
          children: <Widget>[
            Padding(
              padding: const EdgeInsets.only(right: Values.default_space),
              child: SelectVolumeControlWidget(
                isVolControlSelected: _isVolControl,
                onVolControlChanged: (value) =>
                    widget.prefs.isControlVol = value,
              ),
            ),
            Expanded(
              child: Text(
                values.strings.explain_control_vol,
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
                values.strings.explain_control_vol_hack,
                style: contentTextStyle,
              ),
            ),
          ],
        ),
      ],
    );
  }
}
