import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:multiphone/helpers/log.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/widgets/settings/select_control_widget.dart';
import 'package:multiphone/widgets/settings/select_flic_widget.dart';
import 'package:multiphone/widgets/settings/settings_widget_mixin.dart';

class SettingsControlsFlicWidget extends StatefulWidget {
  SettingsControlsFlicWidget({Key key}) : super(key: key);

  @override
  _SettingsControlsFlicWidgetState createState() =>
      _SettingsControlsFlicWidgetState();
}

class _SettingsControlsFlicWidgetState extends State<SettingsControlsFlicWidget>
    with SettingsWidgetMixin {
  bool _isFlicOne = false;
  bool _isFlicTwo = false;

  void _onFlicChanged(FlicButton button, bool isSelected) {
    setState(() {
      switch (button) {
        case FlicButton.one:
          _isFlicOne = isSelected;
          break;
        case FlicButton.two:
          _isFlicTwo = isSelected;
          break;
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    // prepare our member data to use and reuse
    prepareWidget(context);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        createHeading(values.strings.title_controlsFlic),
        Row(
          children: <Widget>[
            Padding(
              padding: const EdgeInsets.only(right: Values.default_space),
              child: createIconSvg('images/svg/flic-two.svg'),
            ),
            Expanded(
              child: Text(
                values.strings.explain_flic_control,
                style: contentTextStyle,
              ),
            ),
          ],
        ),
        Padding(
          padding: const EdgeInsets.only(top: Values.default_space),
          child: Row(
            children: [
              SelectFlicWidget(
                button: FlicButton.one,
                isFlicSelected: _isFlicOne,
                onFlicChanged: (value) => _onFlicChanged(FlicButton.one, value),
              ),
              if (_isFlicOne)
                Padding(
                  padding: const EdgeInsets.only(left: Values.default_space),
                  child: ElevatedButton(
                    style: values.optionButtonStyle,
                    onPressed: () => Log.debug('setup flic one'),
                    child: Text(values.strings.option_setup_flic),
                  ),
                ),
            ],
          ),
        ),
        Padding(
          padding: const EdgeInsets.only(top: Values.default_space),
          child: Row(
            children: [
              SelectFlicWidget(
                button: FlicButton.two,
                isFlicSelected: _isFlicTwo,
                onFlicChanged: (value) => _onFlicChanged(FlicButton.two, value),
              ),
              if (_isFlicTwo)
                Padding(
                  padding: const EdgeInsets.only(left: Values.default_space),
                  child: ElevatedButton(
                    style: values.optionButtonStyle,
                    onPressed: () => Log.debug('setup flic two'),
                    child: Text(values.strings.option_setup_flic2),
                  ),
                ),
            ],
          ),
        ),
        Padding(
          padding: const EdgeInsets.only(top: Values.default_space),
          child: Center(
            child: Expanded(
              child: Text(
                values.strings.explain_flicSmartButtonRecommendation,
                style: contentTextStyle,
              ),
            ),
          ),
        ),
      ],
    );
  }
}
