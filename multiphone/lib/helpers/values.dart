import 'package:flutter/material.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';

class Values {
  static const double default_space = 8;
  static const double image_small = 32;
  static const double image_medium = 48;
  static const double image_large = 64;
  static const double font_size_title = 18;
  static const double font_size_subtitle = 12;

  static const double select_item_size_medium = 72;
  static const double select_item_size_large = 92;

  /// construct the Values object to get our localization strings object
  final BuildContext context;
  const Values(this.context);

  /// return the localisations of all our strings
  AppLocalizations get strings {
    return AppLocalizations.of(context);
  }

  String construct(String baseString, List<dynamic> args) {
    if (null == args || args.isEmpty)
      return baseString;
    else {
      for (int i = 0; i < args.length; ++i)
        baseString = baseString.replaceAll("{$i}", args[i]);
      return baseString;
    }
  }
}
