import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:multiphone/helpers/log.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/widgets/settings/select_control_widget.dart';
import 'package:multiphone/widgets/settings/settings_widget_mixin.dart';

class SelectControlTypeWidget extends StatefulWidget {
  SelectControlTypeWidget({Key key}) : super(key: key);

  @override
  _SelectControlTypeWidgetState createState() =>
      _SelectControlTypeWidgetState();
}

class _SelectControlTypeWidgetState extends State<SelectControlTypeWidget>
    with SettingsWidgetMixin {
  ControlType _selectedType = ControlType.meThem;

  void _onControlTypeChanged(ControlType type) {
    Log.debug('handle control input type changing');
    setState(() {
      _selectedType = type;
    });
  }

  Widget createClickExplainRow(String image, String content) {
    return Padding(
      padding: const EdgeInsets.only(top: Values.default_space),
      child: Row(
        children: [
          SvgPicture.asset(
            image,
            width: Values.image_icon,
            height: Values.image_icon,
          ),
          Expanded(
            child: Text(
              content,
              style: contentTextStyle,
            ),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    // prepare our member data to use and reuse
    prepareWidget(context);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: <Widget>[
            Padding(
              padding: const EdgeInsets.only(right: Values.default_space),
              child: createIconSvg('images/svg/click-single.svg'),
            ),
            Expanded(
              child: Text(
                values.strings.explain_controls,
                style: contentTextStyle,
              ),
            ),
          ],
        ),
        createHeading(values.strings.title_control_type),
        Center(
          child: SelectControlWidget(
            initialSelection: _selectedType,
            onControlTypeChanged: _onControlTypeChanged,
          ),
        ),
        createClickExplainRow(
          'images/svg/click-single.svg',
          _selectedType == ControlType.meThem
              ? values.strings.explain_control_click_single_team
              : values.strings.explain_control_click_single_serving,
        ),
        createClickExplainRow(
          'images/svg/click-double.svg',
          _selectedType == ControlType.meThem
              ? values.strings.explain_control_click_double_team
              : values.strings.explain_control_click_double_serving,
        ),
        createClickExplainRow(
          'images/svg/click-long.svg',
          values.strings.explain_control_click_long,
        ),
      ],
    );
  }
}
