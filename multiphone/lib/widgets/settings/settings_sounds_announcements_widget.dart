import 'package:flutter/material.dart';
import 'package:multiphone/helpers/preferences.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/widgets/common/common_widgets.dart';
import 'package:multiphone/widgets/settings/settings_widget_mixin.dart';

class SettingsSoundsAnnouncementsWidget extends StatefulWidget {
  final Preferences prefs;
  SettingsSoundsAnnouncementsWidget({Key key, @required this.prefs})
      : super(key: key);

  @override
  _SettingsSoundsAnnouncementsWidgetState createState() =>
      _SettingsSoundsAnnouncementsWidgetState();
}

class _SettingsSoundsAnnouncementsWidgetState
    extends State<SettingsSoundsAnnouncementsWidget> with SettingsWidgetMixin {
  @override
  Widget build(BuildContext context) {
    // prepare our member data to use and reuse
    final values = Values(context);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        TextWidget(values.strings.title_announcements),
        createSwitchingRow(
          context,
          IconWidget(Icons.record_voice_over),
          values.strings.title_speak_score_changes,
          TextWidget(values.strings.explain_speak_score_changes),
          (value) => setState(() {
            widget.prefs.soundAnnounceChange = value;
          }),
          isSelected: widget.prefs.soundAnnounceChange,
        ),
        createSwitchingRow(
          context,
          IconSvgWidget('score-points'),
          values.strings.title_speak_points,
          TextWidget(values.strings.explain_speak_points),
          (value) => setState(() {
            widget.prefs.soundAnnounceChangePoints = value;
          }),
          isSelected: widget.prefs.soundAnnounceChangePoints,
        ),
        createSwitchingRow(
          context,
          IconWidget(Icons.compare_arrows),
          values.strings.title_speak_change_ends,
          TextWidget(values.strings.explain_speak_change_ends),
          (value) => setState(() {
            widget.prefs.soundAnnounceChangeEnds = value;
          }),
          isSelected: widget.prefs.soundAnnounceChangeEnds,
        ),
        createSwitchingRow(
          context,
          IconSvgWidget('player-serving'),
          values.strings.title_speak_server,
          TextWidget(values.strings.explain_speak_server),
          (value) => setState(() {
            widget.prefs.soundAnnounceChangeServer = value;
          }),
          isSelected: widget.prefs.soundAnnounceChangeServer,
        ),
        createSwitchingRow(
          context,
          IconSvgWidget('score-match'),
          values.strings.title_speak_score,
          TextWidget(values.strings.explain_speak_score),
          (value) => setState(() {
            widget.prefs.soundAnnounceChangeScore = value;
          }),
          isSelected: widget.prefs.soundAnnounceChangeScore,
        ),
      ],
    );
  }
}
