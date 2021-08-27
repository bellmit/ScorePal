import 'package:flutter/material.dart';
import 'package:multiphone/helpers/preferences.dart';
import 'package:multiphone/widgets/common/common_widgets.dart';
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

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        TextWidget(values.strings.title_general),
        createSwitchingRow(
          context,
          IconWidget(Icons.chat),
          values.strings.title_use_names,
          values.strings.explain_use_names,
          (value) => setState(() {
            widget.prefs.soundUseSpeakingNames = value;
          }),
          isSelected: widget.prefs.soundUseSpeakingNames,
        ),
        /*
        //TODO show the volume control and make it do something - set the vol of the device ideally
        Padding(
          padding: const EdgeInsets.only(top: Values.default_space),
          child: Row(
            children: <Widget>[
              Padding(
                padding: const EdgeInsets.only(right: Values.default_space),
                child: IconWidget(Icons.volume_up),
              ),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text2(values.strings.title_volume),
                    TextWidget(
                      values.strings.explain_control_vol,
                    ),
                    Slider(
                      activeColor: theme.primaryColor,
                      inactiveColor: theme.primaryColorLight,
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
        ),*/
        createSwitchingRow(
          context,
          IconWidget(Icons.settings_remote),
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
