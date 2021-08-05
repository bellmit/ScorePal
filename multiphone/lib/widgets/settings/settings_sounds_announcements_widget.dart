import 'package:flutter/material.dart';
import 'package:multiphone/helpers/preferences.dart';
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
    prepareWidget(context);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        createHeading(values.strings.title_announcements),
        createSwitchingRow(
          context,
          createIcon(Icons.record_voice_over),
          values.strings.title_speak_score_changes,
          values.strings.explain_speak_score_changes,
          (value) => setState(() {
            widget.prefs.soundAnnounceChange = value;
          }),
          isSelected: widget.prefs.soundAnnounceChange,
        ),
        createSwitchingRow(
          context,
          createIconSvg('images/svg/score-points.svg'),
          values.strings.title_speak_points,
          values.strings.explain_speak_points,
          (value) => setState(() {
            widget.prefs.soundAnnounceChangePoints = value;
          }),
          isSelected: widget.prefs.soundAnnounceChangePoints,
        ),
        createSwitchingRow(
          context,
          createIcon(Icons.compare_arrows),
          values.strings.title_speak_change_ends,
          values.strings.explain_speak_change_ends,
          (value) => setState(() {
            widget.prefs.soundAnnounceChangeEnds = value;
          }),
          isSelected: widget.prefs.soundAnnounceChangeEnds,
        ),
        createSwitchingRow(
          context,
          createIconSvg('images/svg/player-serving.svg'),
          values.strings.title_speak_server,
          values.strings.explain_speak_server,
          (value) => setState(() {
            widget.prefs.soundAnnounceChangeServer = value;
          }),
          isSelected: widget.prefs.soundAnnounceChangeServer,
        ),
        createSwitchingRow(
          context,
          createIconSvg('images/svg/score-match.svg'),
          values.strings.title_speak_score,
          values.strings.explain_speak_score,
          (value) => setState(() {
            widget.prefs.soundAnnounceChangeScore = value;
          }),
          isSelected: widget.prefs.soundAnnounceChangeScore,
        ),
      ],
    );
  }
}
