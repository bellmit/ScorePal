import 'package:flutter/material.dart';
import 'package:multiphone/helpers/preferences.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/widgets/settings/settings_widget_mixin.dart';

class SettingsSoundsGeneralWidget extends StatefulWidget {
  final Preferences prefs;
  SettingsSoundsGeneralWidget({Key key, @required this.prefs})
      : super(key: key);

  @override
  _SettingsSoundsGeneralWidgetState createState() =>
      _SettingsSoundsGeneralWidgetState();
}

class _SettingsSoundsGeneralWidgetState
    extends State<SettingsSoundsGeneralWidget> with SettingsWidgetMixin {
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
          (value) => setState(() {
            widget.prefs.soundUseSpeakingNames = value;
          }),
          isSelected: widget.prefs.soundUseSpeakingNames,
        ),
        Padding(
          padding: const EdgeInsets.only(top: Values.default_space),
          child: Row(
            children: <Widget>[
              Padding(
                padding: const EdgeInsets.only(right: Values.default_space),
                child: createIcon(Icons.volume_up),
              ),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    createHeading2(values.strings.title_volume),
                    Text(
                      values.strings.explain_control_vol,
                      style: contentTextStyle,
                    ),
                    Slider(
                      min: 0,
                      max: 1,
                      divisions: 10,
                      value: widget.prefs.soundAnnounceVolume,
                      onChanged: (value) => setState(() {
                        widget.prefs.soundAnnounceVolume = value;
                      }),
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
        createSwitchingRow(
          context,
          createIcon(Icons.settings_remote),
          values.strings.title_speak_message,
          values.strings.explain_speak_message,
          (value) => setState(() {
            widget.prefs.soundActionSpeak = value;
          }),
          isSelected: widget.prefs.soundActionSpeak,
        ),
      ],
    );
  }
}
