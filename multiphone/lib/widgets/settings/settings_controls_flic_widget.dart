import 'package:flutter/material.dart';
import 'package:multiphone/helpers/preferences.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/match_play_tracker.dart';
import 'package:multiphone/screens/setup_flic2_screen.dart';
import 'package:multiphone/widgets/adverts/purchase_flic_widget.dart';
import 'package:multiphone/widgets/common/common_widgets.dart';
import 'package:multiphone/widgets/common/icon_button_widget.dart';
import 'package:multiphone/widgets/settings/select_flic_widget.dart';
import 'package:multiphone/widgets/settings/settings_widget_mixin.dart';

class SettingsControlsFlicWidget extends StatefulWidget {
  final Preferences prefs;
  SettingsControlsFlicWidget({Key key, @required this.prefs}) : super(key: key);

  @override
  _SettingsControlsFlicWidgetState createState() =>
      _SettingsControlsFlicWidgetState();
}

class _SettingsControlsFlicWidgetState extends State<SettingsControlsFlicWidget>
    with SettingsWidgetMixin {
  bool _isFlicOne = false;
  bool _isFlicTwo = false;

  @override
  void initState() {
    super.initState();
    // get the defaults to show
    _isFlicOne = widget.prefs.isControlFlic1;
    _isFlicTwo = widget.prefs.isControlFlic2;
  }

  void _onFlicChanged(FlicButton button, bool isSelected) {
    setState(() {
      switch (button) {
        case FlicButton.one:
          _isFlicOne = isSelected;
          widget.prefs.isControlFlic1 = _isFlicOne;
          break;
        case FlicButton.two:
          _isFlicTwo = isSelected;
          widget.prefs.isControlFlic2 = _isFlicTwo;
          break;
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    // prepare our member data to use and reuse
    final values = Values(context);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        TextWidget(values.strings.title_controlsFlic),
        Row(
          children: <Widget>[
            Padding(
              padding: const EdgeInsets.only(right: Values.default_space),
              child: IconSvgWidget('flic-two'),
            ),
            Expanded(
              child: TextWidget(values.strings.explain_flic_control),
            ),
          ],
        ),
        /*
        //TODO allow the control to work from a flic one
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
                    child: TextWidget(values.strings.option_setup_flic),
                  ),
                ),
            ],
          ),
        ),*/
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
                  child: IconButtonWidget(
                    () => MatchPlayTracker.navTo(
                        SetupFlic2Screen.routeName, context),
                    Icons.bluetooth,
                    values.strings.option_setup_flic2,
                  ),
                ),
            ],
          ),
        ),
        Padding(
          padding: const EdgeInsets.only(top: Values.default_space),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.center,
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              TextButton(
                onPressed: () =>
                    PurchaseFlicWidget.navUserToPurchaseFlic(context),
                child: TextWidget(values.strings.option_purchase),
              ),
              Padding(
                padding: const EdgeInsets.only(top: 4.0),
                child: TextWidget(
                    values.strings.explain_flicSmartButtonRecommendation),
              ),
            ],
          ),
        ),
      ],
    );
  }
}
