import 'dart:async';

import 'package:flic_button/flic_button.dart';
import 'package:flutter/material.dart';
import 'package:flutter_blue/flutter_blue.dart';
import 'package:flutter_staggered_grid_view/flutter_staggered_grid_view.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:multiphone/helpers/flic2_wrapper.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/screens/base_nav_screen.dart';
import 'package:multiphone/widgets/common/heading_widget.dart';
import 'package:multiphone/widgets/side_drawer_widget.dart';
import 'package:permission_handler/permission_handler.dart';

class SetupFlic2Screen extends BaseNavScreen {
  static const String routeName = '/setup-flic2';

  SetupFlic2Screen({Key key})
      : super(key: key, scaffoldKey: GlobalKey(debugLabel: 'setup_flic2'));

  @override
  _SetupFlic2ScreenState createState() => _SetupFlic2ScreenState();
}

class _SetupFlic2ScreenState extends BaseNavScreenState<SetupFlic2Screen>
    with TickerProviderStateMixin, Flic2Listener {
  // flic2 starts and isn't scanning
  bool _isScanning = false;

  // as we discover buttons, lets add them to a map of uuid/button to show
  final Map<String, Flic2Button> _buttonsFound = {};
  // the last click to show we are hearing the button click
  Flic2ButtonClick _lastClick;

  // the plugin manager to use while we are active
  Flic2Wrapper _flicPlugin;

  @override
  String getScreenTitle(Values values) {
    return values.strings.option_setup_flic2;
  }

  @override
  int getMenuSelectionIndex() {
    return MenuItem.menuSetupFlic2;
  }

  @override
  Widget buildSideDrawer(BuildContext context) {
    // no side drawer - just make them go back
    return null;
  }

  @override
  Widget buildIconMenu(BuildContext context) {
    // no icon menu please - back button will show instead
    return null;
  }

  @override
  void initState() {
    super.initState();
    // create the FLIC 2 manager and initialize it
    _startStopFlic2();
  }

  @override
  void dispose() {
    // kill everything created, first the flic plugin
    if (null != _flicPlugin) {
      _flicPlugin.removeListener(this);
      _flicPlugin = null;
    }
    // and the base class
    super.dispose();
  }

  Future<void> _startStopScanningForFlic2() async {
    // start scanning for new buttons
    if (!_isScanning) {
      // not scanning yet - start, first we need BLE permission
      final isGranted = await Permission.bluetooth.request().isGranted;
      if (!isGranted) {
        // no bluetooth allowed
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(
            content:
                Text(Values(context).strings.error_flic2_bluetooth_permission),
            backgroundColor: Theme.of(context).errorColor));
        return;
      }
      // also bluetooth needs to be on
      if (!await FlutterBlue.instance.isOn) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(
            content: Text(Values(context).strings.error_flic2_bluetooth_on),
            backgroundColor: Theme.of(context).errorColor));
        return;
      }
      _flicPlugin.plugin.scanForFlic2();
    } else {
      // are scanning - cancel that
      _flicPlugin.plugin.cancelScanForFlic2();
    }
    // update the UI
    setState(() {
      _isScanning = !_isScanning;
    });
  }

  void _startStopFlic2() {
    // start or stop the plugin (iOS doesn't stop)
    if (null == _flicPlugin) {
      // we are not started - start listening to FLIC2 buttons
      setState(() {
        _flicPlugin = Flic2Wrapper.instance();
        _flicPlugin.addListener(this);
      });
      // and get the buttons already in place
      _getButtons();
    } else {
      // started - so stop
      _flicPlugin.removeListener(this);
      _flicPlugin.plugin.cancelScanForFlic2();
      setState(() => _flicPlugin = null);
    }
  }

  Future<void> _getButtons() async {
    // get all the buttons from the plugin that were there last time
    final buttons = await _flicPlugin.plugin.getFlic2Buttons();
    // put all of these in the list to show the buttons
    buttons.forEach((button) {
      _addButtonAndListen(button);
    });
  }

  void _addButtonAndListen(Flic2Button button) {
    // as buttons are discovered via the various methods, add them
    // to the map to show them in the list on the view
    setState(() {
      // add the button to the map
      _buttonsFound[button.uuid] = button;
      // and listen to the button for clicks and things
      _flicPlugin.plugin.listenToFlic2Button(button.uuid);
    });
  }

  void _connectDisconnectButton(Flic2Button button) {
    // if disconnected, connect, else disconnect the button
    if (button.connectionState == Flic2ButtonConnectionState.disconnected) {
      _flicPlugin.plugin
          .connectButton(button.uuid)
          .then((value) => _getButtons());
    } else {
      _flicPlugin.plugin
          .disconnectButton(button.uuid)
          .then((value) => _getButtons());
    }
  }

  void _forgetButton(Flic2Button button) {
    // forget the passed button so it disapears and we can search again
    _flicPlugin.plugin.forgetButton(button.uuid).then((value) {
      if (value != null && value) {
        // button was removed
        setState(() {
          // remove from the list
          _buttonsFound.remove(button.uuid);
        });
      }
    });
  }

  @override
  void onButtonClicked(Flic2ButtonClick buttonClick) {
    // callback from the plugin that someone just clicked a button0
    setState(() {
      _lastClick = buttonClick;
      new Future.delayed(Duration(seconds: 2), () {
        setState(() {
          _lastClick = null;
        });
      });
    });
  }

  @override
  void onButtonConnected() {
    // this changes the state of our list of buttons, set state for this
    setState(() {
      print('button connected');
    });
  }

  @override
  void onButtonDiscovered(String buttonAddress) {
    // this is an address which we should be able to resolve to an actual button right away
    // but we could in theory wait for it to be connected and discovered because that will happen too
    _flicPlugin.plugin.getFlic2ButtonByAddress(buttonAddress).then((button) {
      if (button != null) {
        // which we can add to the list to show right away
        _addButtonAndListen(button);
      }
    });
  }

  @override
  void onButtonFound(Flic2Button button) {
    // we have found a new button, add to the list to show
    _addButtonAndListen(button);
  }

  @override
  void onFlic2Error(String error) {
    // something went wrong somewhere, provide feedback maybe, or did you code something in the wrong order?
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(
        content: Text(error), backgroundColor: Theme.of(context).errorColor));
  }

  @override
  void onPairedButtonDiscovered(Flic2Button button) {
    // discovered something already paired (getButtons will return these but maybe you didn't bother and
    // just went right into a scan)
    _addButtonAndListen(button);
  }

  @override
  void onScanCompleted() {
    // scan completed, update the state of our view
    setState(() {
      _isScanning = false;
    });
  }

  @override
  void onScanStarted() {
    // scan started, update the state of our view
    setState(() {
      _isScanning = true;
    });
  }

  Widget _flic2Widget(Flic2Button button) {
    final values = Values(context);
    return ListTile(
      key: ValueKey(button.uuid),
      leading: Container(
        child: Stack(
          children: [
            SvgPicture.asset(
              'images/svg/flic-two.svg',
              height: Values.image_medium,
              color: _lastClick != null && _lastClick.button.uuid == button.uuid
                  ? Theme.of(context).primaryColor
                  : Theme.of(context).primaryColorDark,
            ),
            if (_lastClick != null && _lastClick.button.uuid == button.uuid)
              Container(
                width: Values.image_large,
                height: Values.image_large + 50,
                child: FittedBox(
                  alignment: Alignment.bottomCenter,
                  fit: BoxFit.fitWidth,
                  child: Text(
                    _lastClick.isSingleClick
                        ? values.strings.flic_single_click
                        : _lastClick.isDoubleClick
                            ? values.strings.flic_double_click
                            : _lastClick.isHold
                                ? values.strings.flic_hold_click
                                : '',
                    style: TextStyle(color: Theme.of(context).primaryColorDark),
                  ),
                ),
              ),
          ],
        ),
      ),
      title: Text(
          values.construct(values.strings.flic2_title, [button.buttonAddr])),
      subtitle: Column(
        children: [
          Text(
            values.construct(
              values.strings.flic2_details,
              [
                button.name,
                button.battVoltage,
                button.battPercentage,
                button.serialNo,
                button.pressCount,
              ],
            ),
          ),
          Row(
            children: [
              ElevatedButton(
                onPressed: () => _connectDisconnectButton(button),
                child: Text(button.connectionState ==
                        Flic2ButtonConnectionState.disconnected
                    ? values.strings.button_flic_connect
                    : values.strings.button_flic_disconnect),
                style: values.optionButtonStyle,
              ),
              SizedBox(width: 20),
              ElevatedButton(
                onPressed: () => _forgetButton(button),
                child: Text(values.strings.button_flic_forget),
                style: values.optionButtonStyle,
              ),
            ],
          ),
        ],
      ),
    );
  }

  @override
  Widget buildScreenBody(BuildContext context) {
    final values = Values(context);
    return FutureBuilder(
      future: _flicPlugin != null ? _flicPlugin.plugin.invokation : null,
      builder: (ctx, snapshot) {
        if (snapshot.connectionState != ConnectionState.done) {
          // are not initialized yet, wait a sec - should be very quick!
          return Center(child: CircularProgressIndicator());
        } else {
          // we have completed the init call, we can perform scanning etc
          return Column(
            children: [
              if (_flicPlugin != null)
                Row(
                  // if we are started then show the controls to get flic2 and scan for flic2
                  mainAxisAlignment: MainAxisAlignment.start,
                  children: [
                    Padding(
                      padding: const EdgeInsets.all(Values.default_space),
                      child: ElevatedButton(
                          onPressed: () => _startStopScanningForFlic2(),
                          child: Text(_isScanning
                              ? values.strings.button_stop_scanning
                              : values.strings.button_scan),
                          style: values.optionButtonStyle),
                    ),
                    if (_isScanning)
                      HeadingWidget(title: values.strings.info_scanning_flic),
                  ],
                ),
              // and show the list of buttons we have found at this point
              Expanded(
                child: RefreshIndicator(
                  onRefresh: _getButtons,
                  child: OrientationBuilder(
                    builder: (ctx, orientation) {
                      return StaggeredGridView.countBuilder(
                        itemCount:
                            _buttonsFound == null ? 0 : _buttonsFound.length,
                        crossAxisCount:
                            orientation == Orientation.portrait ? 1 : 2,
                        crossAxisSpacing: Values.default_space,
                        mainAxisSpacing: Values.default_space,
                        staggeredTileBuilder: (int index) =>
                            StaggeredTile.fit(1),
                        itemBuilder: (BuildContext context, int index) {
                          final button = _buttonsFound.values.elementAt(index);
                          return Dismissible(
                            direction: DismissDirection.startToEnd,
                            background: Container(
                              color: Values.deleteColor,
                              child: const Align(
                                alignment: Alignment.centerLeft,
                                child: const Padding(
                                  padding: const EdgeInsets.only(
                                      left: Values.default_space),
                                  child: const Icon(
                                    Icons.delete,
                                    color: Values.secondaryTextColor,
                                  ),
                                ),
                              ),
                            ),
                            // Each Dismissible must contain a Key. Keys allow Flutter to
                            // uniquely identify widgets.
                            key: Key(button.uuid),
                            // Provide a function that tells the app
                            // what to do after an item has been swiped away.
                            onDismissed: (direction) => setState(
                                () => _buttonsFound.remove(button.uuid)),
                            child: _flic2Widget(button),
                          );
                        },
                      );
                    },
                  ),
                ),
              ),
            ],
          );
        }
      },
    );
  }
}
