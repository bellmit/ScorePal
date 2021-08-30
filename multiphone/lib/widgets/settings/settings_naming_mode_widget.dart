import 'package:flutter/material.dart';
import 'package:multiphone/helpers/preferences.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/team_namer.dart';
import 'package:multiphone/widgets/common/common_widgets.dart';
import 'package:multiphone/widgets/settings/settings_widget_mixin.dart';
import 'package:material_segmented_control/material_segmented_control.dart';

class SettingsNamingModeWidget extends StatefulWidget {
  final Preferences prefs;
  SettingsNamingModeWidget({Key key, @required this.prefs}) : super(key: key);

  @override
  _SettingsNamingModeWidgetState createState() =>
      _SettingsNamingModeWidgetState();
}

class _SettingsNamingModeWidgetState extends State<SettingsNamingModeWidget>
    with SettingsWidgetMixin {
  TeamNamingMode _selectedMode;

  Map<int, Widget> _availableOptions(Values values) => {
        TeamNamingMode.SURNAME_INITIAL.index: Padding(
          padding: const EdgeInsets.all(Values.default_space),
          child: TextWidget(values.strings.naming_mode_surname_initial),
        ),
        TeamNamingMode.FIRST_NAME.index: Padding(
          padding: const EdgeInsets.all(Values.default_space),
          child: TextWidget(values.strings.naming_mode_first_name),
        ),
        TeamNamingMode.LAST_NAME.index: Padding(
          padding: const EdgeInsets.all(Values.default_space),
          child: TextWidget(values.strings.naming_mode_surname),
        ),
        TeamNamingMode.FULL_NAME.index: Padding(
          padding: const EdgeInsets.all(Values.default_space),
          child: TextWidget(values.strings.naming_mode_full_name),
        ),
      };

  String _example(Values values) {
    switch (_selectedMode) {
      case TeamNamingMode.SURNAME_INITIAL:
        return values.strings.example_naming_mode_surname_initial;
      case TeamNamingMode.FIRST_NAME:
        return values.strings.example_naming_mode_first_name;
      case TeamNamingMode.LAST_NAME:
        return values.strings.example_naming_mode_surname;
      case TeamNamingMode.FULL_NAME:
        return values.strings.example_naming_mode_full_name;
      default:
        return '';
    }
  }

  @override
  void initState() {
    super.initState();
    // get the defaults to show
    _selectedMode = widget.prefs.defaultNamingMode;
  }

  void _onNamingModeChanged(TeamNamingMode mode) {
    // push this data back out to the preferences
    setState(() {
      _selectedMode = mode;
      widget.prefs.defaultNamingMode = mode;
    });
  }

  @override
  Widget build(BuildContext context) {
    // prepare our member data to use and reuse
    final values = Values(context);
    final theme = Theme.of(context);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.only(top: Values.default_space),
          child: Row(
            children: <Widget>[
              Padding(
                padding: const EdgeInsets.only(right: Values.default_space),
                child: IconWidget(Icons.speaker_notes),
              ),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    MaterialSegmentedControl(
                      children: _availableOptions(values),
                      selectionIndex: _selectedMode.index,
                      borderColor: theme.primaryColorDark,
                      selectedColor: theme.primaryColor,
                      unselectedColor: theme.backgroundColor,
                      borderRadius: Values.default_radius,
                      onSegmentChosen: (index) =>
                          _onNamingModeChanged(TeamNamingMode.values[index]),
                    ),
                    Padding(
                      padding: const EdgeInsets.all(Values.default_space),
                      child: TextSubheadingWidget(_example(values)),
                    ),
                    Padding(
                      padding: const EdgeInsets.all(Values.default_space),
                      child: TextWidget(values.strings.explain_name_mode),
                    ),
                  ],
                ),
              ),
            ],
          ),
        )
      ],
    );
  }
}
