import 'package:flutter/material.dart';
import 'package:multiphone/helpers/log.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/match_play_tracker.dart';
import 'package:multiphone/screens/attributions_screen.dart';
import 'package:multiphone/screens/trash_screen.dart';
import 'package:multiphone/widgets/common/common_widgets.dart';
import 'package:multiphone/widgets/common/icon_button_widget.dart';
import 'package:multiphone/widgets/settings/settings_widget_mixin.dart';

class SettingsDataWidget extends StatelessWidget with SettingsWidgetMixin {
  SettingsDataWidget({Key key}) : super(key: key);

  Widget createDataRow(BuildContext context, IconData icon, String title,
      String explain, String buttonText, void Function() onPressed) {
    return Padding(
      padding: const EdgeInsets.only(top: Values.default_space),
      child: Row(
        children: <Widget>[
          Padding(
            padding: const EdgeInsets.only(right: Values.default_space),
            child: IconWidget(icon),
          ),
          Expanded(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.start,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Expanded(
                      child: TextWidget(title),
                    ),
                    IconButtonWidget(
                      onPressed,
                      null,
                      buttonText,
                    ),
                  ],
                ),
                TextWidget(explain),
              ],
            ),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    // prepare the mixin
    final values = Values(context);
    // prepare our member data to use and reuse
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        TextWidget(values.strings.title_manage_data),
        /*
        createDataRow(
          context,
          Icons.file_download,
          values.strings.title_import_match,
          values.strings.explain_import_match,
          values.strings.button_import,
          //TODO match data import from the settings once you can share out too
          () => Log.debug('need to do match importing I suppose'),
        ),*/
        createDataRow(
          context,
          Icons.contact_mail,
          values.strings.title_manage_deleted,
          values.strings.explain_manage_deleted,
          values.strings.button_view,
          () => MatchPlayTracker.navTo(TrashScreen.routeName, context),
        ),
        createDataRow(
          context,
          Icons.bluetooth,
          values.strings.title_attributions,
          values.strings.explain_attributions,
          values.strings.button_view,
          () => MatchPlayTracker.navTo(AttributionsScreen.routeName, context),
        ),
      ],
    );
  }
}
