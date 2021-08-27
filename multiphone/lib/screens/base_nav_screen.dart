import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/widgets/common/common_widgets.dart';
import 'package:multiphone/widgets/side_drawer_widget.dart';

abstract class BaseNavScreen extends StatefulWidget {
  final GlobalKey<ScaffoldState> scaffoldKey;
  const BaseNavScreen({Key key, @required this.scaffoldKey}) : super(key: key);
}

abstract class BaseNavScreenState<T extends BaseNavScreen> extends State<T> {
  int getMenuSelectionIndex();

  String getScreenTitle(Values values);

  Widget buildScreenBody(BuildContext context);

  Widget buildFloatingActionButton(BuildContext context) {
    return null;
  }

  Widget buildBottomNavigationBar(BuildContext context) {
    return null;
  }

  Widget buildIconMenu(BuildContext context) {
    return IconButton(
        onPressed: () => widget.scaffoldKey.currentState.openDrawer(),
        icon: const IconWidget(Icons.more_vert, size: null));
  }

  Widget buildSideDrawer(BuildContext context) {
    return SideDrawer(
      menuItems: MenuItem.mainMenuItems(context),
      bottomMenuItems: MenuItem.bottomMenuItems(context),
      currentSelection: getMenuSelectionIndex(),
    );
  }

  @override
  Widget build(BuildContext context) {
    // get the values to get strings from
    var values = Values(context);
    // and return the scaffold
    return Scaffold(
      key: widget.scaffoldKey,
      appBar: AppBar(
        title: TextWidget(getScreenTitle(values)),
        leading: buildIconMenu(context),
      ),
      drawer: buildSideDrawer(context),
      body: buildScreenBody(context),
      floatingActionButton: buildFloatingActionButton(context),
      bottomNavigationBar: buildBottomNavigationBar(context),
    );
  }
}
