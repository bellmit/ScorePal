import 'package:flutter/material.dart';
import 'package:flutter_svg/svg.dart';
import 'package:multiphone/helpers/values.dart';

class SettingsWidgetMixin {
  TextStyle contentTextStyle;
  Values values;
  ThemeData theme;

  Widget createIcon(IconData icon) {
    return Icon(
      icon,
      size: Values.image_large,
    );
  }

  Widget createIconSvg(String assetName) {
    return SvgPicture.asset(
      assetName,
      width: Values.image_large,
      height: Values.image_large,
    );
  }

  Widget createHeading(String content) {
    return Text(
      content,
      style: TextStyle(
        fontSize: Values.font_size_title,
        fontWeight: FontWeight.bold,
        color: theme.primaryColorDark,
      ),
    );
  }

  Widget createHeading2(String content) {
    return Text(
      content,
      style: TextStyle(
        fontSize: Values.font_size_title2,
        fontWeight: FontWeight.bold,
        color: Values.secondaryTextColor,
      ),
    );
  }

  void prepareWidget(BuildContext context) {
    values = Values(context);
    theme = Theme.of(context);
    contentTextStyle = TextStyle(color: Values.secondaryTextColor);
  }

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
                      child: createHeading2(title),
                    ),
                    Switch(
                      activeColor: theme.primaryColor,
                      value: isSelected,
                      onChanged: onSwitch,
                    ),
                  ],
                ),
                Text(
                  explain,
                  style: contentTextStyle,
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
