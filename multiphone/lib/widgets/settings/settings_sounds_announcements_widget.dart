import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:multiphone/helpers/log.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/widgets/settings/settings_widget_mixin.dart';

class SettingsSoundsAnnouncementsWidget extends StatelessWidget
    with SettingsWidgetMixin {
  SettingsSoundsAnnouncementsWidget({Key key}) : super(key: key);

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
          (bool) => Log.debug('switched'),
        ),
        createSwitchingRow(
          context,
          createIcon(Icons.volume_up),
          values.strings.title_volume,
          values.strings.explain_control_vol,
          (bool) => Log.debug('switched'),
        ),
        createSwitchingRow(
          context,
          createIconSvg('images/svg/score-points.svg'),
          values.strings.title_speak_points,
          values.strings.explain_speak_points,
          (bool) => Log.debug('switched'),
        ),
        createSwitchingRow(
          context,
          createIcon(Icons.compare_arrows),
          values.strings.title_speak_change_ends,
          values.strings.explain_speak_change_ends,
          (bool) => Log.debug('switched'),
        ),
        createSwitchingRow(
          context,
          createIconSvg('images/svg/player-serving.svg'),
          values.strings.title_speak_server,
          values.strings.explain_speak_server,
          (bool) => Log.debug('switched'),
        ),
        createSwitchingRow(
          context,
          createIconSvg('images/svg/score-match.svg'),
          values.strings.title_speak_score,
          values.strings.explain_speak_score,
          (bool) => Log.debug('switched'),
        ),
      ],
    );
  }
}
