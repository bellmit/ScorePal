import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/screens/base_nav_screen.dart';
import 'package:multiphone/widgets/side_drawer_widget.dart';

class AttributionsScreen extends BaseNavScreen {
  static const String routeName = '/attributions';

  AttributionsScreen({Key key})
      : super(key: key, scaffoldKey: GlobalKey(debugLabel: 'attributions'));

  @override
  _AttributionsScreenState createState() => _AttributionsScreenState();
}

class _AttributionsScreenState extends BaseNavScreenState<AttributionsScreen> {
  @override
  String getScreenTitle(Values values) {
    return values.strings.option_attributions;
  }

  @override
  int getMenuSelectionIndex() {
    return MenuItem.menuAttributions;
  }

  @override
  Widget buildScreenBody(BuildContext context) {
    return Center(child: CircularProgressIndicator());
  }
}
