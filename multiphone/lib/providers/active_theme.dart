import 'package:flex_color_scheme/flex_color_scheme.dart';
import 'package:flutter/material.dart';

class ActiveTheme with ChangeNotifier {
  static bool isCupertino = false;

  static FlexScheme usedFlexScheme = FlexScheme.ebonyClay;
  static ThemeMode themeMode = ThemeMode.system;

  ActiveTheme(BuildContext ctx) {
    //TODO cupertino - at the moment everything is very much Material (fine!)
    // isCupertino = Platform.isIOS || Platform.isMacOS;
  }
}
