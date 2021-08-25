import 'package:flutter/material.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';

class Values {
  static const primaryColor = Color(0xFF308038);
  static const primaryLightColor = Color(0xAA62b064);
  static const primaryLightColorFaded = Color(0xFF7062b064);
  static const primaryDarkColor = Color(0xFF00530d);
  static const secondaryColor = Color(0xFFdcfd50);
  static const secondaryLightColor = Color(0xFFffff84);
  static const secondaryDarkColor = Color(0xFFa7ca0a);
  static const primaryTextColor = Color(0xFFffffff);
  static const secondaryTextColor = Color(0xFF000000);
  static const primaryBackground = Color(0xFFbbbbcc);

  static const teamOneColor = Color(0xFFdcfd50);
  static const teamTwoColor = Color(0xFFffff84);
  static const deleteColor = Color(0xFFff1010);

  static const circular_progress_default_progress = Color(0xFFffff84);
  static const circular_progress_default_background = Color(0xFF00530d);
  static const circular_progress_default_title = Color(0xFF62b064);
  static const circular_progress_default_subtitle = Color(0xFF62b064);

  static const int firebase_fetch_limit = 50;

  static const int display_duration_ms = 3000;
  static const int animation_duration_rapid_ms = 200;
  static const int animation_duration_ms = 500;
  static const double max_score_box = 150;
  static const double default_space = 8;
  static const double line_width = 1.5;
  static const double default_radius = 12;
  static const double border_width = 1;
  static const double image_icon = 24;
  static const double image_small = 32;
  static const double image_medium = 48;
  static const double image_large = 64;
  static const double font_size_title = 18;
  static const double font_size_title2 = 16;
  static const double font_size_subtitle = 12;

  static const double team_names_widget_height = 48;

  static const double speaking_pitch = 1.0;
  static const double speaking_rate = 0.5;

  static const double select_item_size_medium = 72;
  static const double select_item_size_large = 92;

  /// construct the Values object to get our localization strings object
  final BuildContext context;
  const Values(this.context);

  ButtonStyle get optionButtonStyle {
    final theme = Theme.of(context);
    return ElevatedButton.styleFrom(
      primary: theme.primaryColorDark,
      onPrimary: theme.accentColor,
    );
  }

  /// return the localisations of all our strings
  AppLocalizations get strings {
    return AppLocalizations.of(context);
  }

  String construct(String baseString, List<dynamic> args) {
    if (null == args || args.isEmpty)
      return baseString;
    else {
      for (int i = 0; i < args.length; ++i)
        baseString = baseString.replaceAll("{$i}", args[i].toString());
      return baseString;
    }
  }
}
