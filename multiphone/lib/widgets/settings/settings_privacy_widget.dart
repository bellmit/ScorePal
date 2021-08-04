import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:multiphone/helpers/log.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/widgets/settings/settings_widget_mixin.dart';

class SettingsPrivacyWidget extends StatefulWidget {
  SettingsPrivacyWidget({Key key}) : super(key: key);

  @override
  _SettingsPrivacyWidgetState createState() => _SettingsPrivacyWidgetState();
}

class _SettingsPrivacyWidgetState extends State<SettingsPrivacyWidget>
    with SettingsWidgetMixin {
  bool _isWipeSelected = false;

  void showWipe(bool isShow) {
    setState(() {
      _isWipeSelected = isShow;
    });
  }

  void _wipeData() {
    //TODO the wiping of all the user's data!
  }

  @override
  Widget build(BuildContext context) {
    // prepare our member data to use and reuse
    prepareWidget(context);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        createHeading(values.strings.title_privacy),
        createSwitchingRow(
          context,
          createIcon(Icons.delete_sweep),
          values.strings.title_wipe_all_data,
          values.strings.explain_wipe_all_data,
          showWipe,
          isSelected: _isWipeSelected,
        ),
        if (_isWipeSelected)
          Align(
            alignment: Alignment.topRight,
            child: ElevatedButton(
              style: ElevatedButton.styleFrom(
                  primary: Theme.of(context).primaryColor),
              onPressed: _wipeData,
              child: Text(values.strings.title_wipe_all_data),
            ),
          ),
      ],
    );
  }
}
