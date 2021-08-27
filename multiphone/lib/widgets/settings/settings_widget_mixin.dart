import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/widgets/common/common_widgets.dart';

class SettingsWidgetMixin {
  Values values;
  ThemeData theme;

  Widget createSwitchingRow(BuildContext context, Widget icon, String title,
      String explain, void Function(bool) onSwitch,
      {bool isSelected = false}) {
    return Padding(
      padding: const EdgeInsets.only(top: Values.default_space),
      child: Row(
        children: <Widget>[
          Padding(
            padding: const EdgeInsets.only(right: Values.default_space),
            child: icon,
          ),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Expanded(
                      child: TextWidget(title),
                    ),
                    Switch(
                      activeColor: theme.primaryColor,
                      value: isSelected,
                      onChanged: onSwitch,
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
}
