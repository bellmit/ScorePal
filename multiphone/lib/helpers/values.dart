import 'package:flutter/material.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';

class Values {
  static const double image_large = 64;

  /// construct the Values object to get our localization strings object
  final BuildContext context;
  const Values(this.context);

  /// return the localisations of all our strings
  AppLocalizations get strings {
    return AppLocalizations.of(context);
  }
}
