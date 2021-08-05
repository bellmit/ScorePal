import 'package:flutter/material.dart';
import 'package:multiphone/helpers/preferences.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/widgets/settings/select_volume_control_widget.dart';
import 'package:multiphone/widgets/settings/settings_widget_mixin.dart';

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

  @override
  void initState() {
    super.initState();
    // get the defaults to show
    _isVolControl = widget.prefs.isControlVol;
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
