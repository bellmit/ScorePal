import 'package:flic_button/flic_button.dart';
import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/screens/base_nav_screen.dart';
import 'package:multiphone/widgets/side_drawer_widget.dart';

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
  FlicButtonPlugin flicButtonManager;

  AnimationController _controller;
  Animation<Offset> _slideAnimation;

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
    // animation things (this can have the mixin of SingleTickerProviderStateMixin)
    _controller = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: Values.animation_duration_ms),
    );
    _slideAnimation = Tween<Offset>(
      begin: const Offset(0, -1),
      end: const Offset(0, 0),
    ).animate(CurvedAnimation(
      parent: _controller,
      curve: Curves.linear,
    ));
  }

  @override
  void dispose() {
    // kill everything created, first the flic plugin
    if (null != flicButtonManager) {
      flicButtonManager.cancelScanForFlic2();
      flicButtonManager.disposeFlic2();
      flicButtonManager = null;
    }
    // and the animation thing
    _controller.dispose();
    // and the base class
    super.dispose();
  }

  void _startStopScanningForFlic2() {
    // start scanning for new buttons
    if (!_isScanning) {
      // not scanning yet - start
      flicButtonManager.scanForFlic2();
    } else {
      // are scanning - cancel that
      flicButtonManager.cancelScanForFlic2();
    }
    // update the UI
    setState(() {
      _isScanning = !_isScanning;
    });
  }

  void _startStopFlic2() {
    // start or stop the plugin (iOS doesn't stop)
    if (null == flicButtonManager) {
      // we are not started - start listening to FLIC2 buttons
      setState(() => flicButtonManager = FlicButtonPlugin(flic2listener: this));
    } else {
      // started - so stop
      flicButtonManager.disposeFlic2().then((value) => setState(() {
            // as the flic manager is disposed, signal that it's gone
            flicButtonManager = null;
          }));
    }
  }

  void _getButtons() {
    // get all the buttons from the plugin that were there last time
    flicButtonManager.getFlic2Buttons().then((buttons) {
      // put all of these in the list to show the buttons
      buttons.forEach((button) {
        _addButtonAndListen(button);
      });
    });
  }

  void _addButtonAndListen(Flic2Button button) {
    // as buttons are discovered via the various methods, add them
    // to the map to show them in the list on the view
    setState(() {
      // add the button to the map
      _buttonsFound[button.uuid] = button;
      // and listen to the button for clicks and things
      flicButtonManager.listenToFlic2Button(button.uuid);
    });
  }

  void _connectDisconnectButton(Flic2Button button) {
    // if disconnected, connect, else disconnect the button
    if (button.connectionState == Flic2ButtonConnectionState.disconnected) {
      flicButtonManager.connectButton(button.uuid);
    } else {
      flicButtonManager.disconnectButton(button.uuid);
    }
  }

  void _forgetButton(Flic2Button button) {
    // forget the passed button so it disapears and we can search again
    flicButtonManager.forgetButton(button.uuid).then((value) {
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
    // callback from the plugin that someone just clicked a button
    print('button ${buttonClick.button.uuid} clicked');
    setState(() {
      _lastClick = buttonClick;
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
    print('button @$buttonAddress discovered');
    // but we could in theory wait for it to be connected and discovered because that will happen too
    flicButtonManager.getFlic2ButtonByAddress(buttonAddress).then((button) {
      if (button != null) {
        print(
            'button found with address $buttonAddress resolved to actual button data ${button.uuid}');
        // which we can add to the list to show right away
        _addButtonAndListen(button);
      }
    });
  }

  @override
  void onButtonFound(Flic2Button button) {
    // we have found a new button, add to the list to show
    print('button ${button.uuid} found');
    // and add to the list to show
    _addButtonAndListen(button);
  }

  @override
  void onFlic2Error(String error) {
    // something went wrong somewhere, provide feedback maybe, or did you code something in the wrong order?
    print('ERROR: $error');
  }

  @override
  void onPairedButtonDiscovered(Flic2Button button) {
    print('paired button ${button.uuid} discovered');
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

  @override
  Widget buildScreenBody(BuildContext context) {
    return FutureBuilder(
      future: flicButtonManager != null ? flicButtonManager.invokation : null,
      builder: (ctx, snapshot) {
        if (snapshot.connectionState != ConnectionState.done) {
          // are not initialized yet, wait a sec - should be very quick!
          return Center(
            child: ElevatedButton(
              onPressed: () => _startStopFlic2(),
              child: Text('Start and initialize Flic2'),
            ),
          );
        } else {
          // we have completed the init call, we can perform scanning etc
          return Column(
            children: [
              SizedBox(
                height: 10,
              ),
              Text(
                'Flic2 is initialized',
                style: TextStyle(fontSize: 20),
              ),
              ElevatedButton(
                onPressed: () => _startStopFlic2(),
                child: Text('Stop Flic2'),
              ),
              if (flicButtonManager != null)
                Row(
                  // if we are started then show the controls to get flic2 and scan for flic2
                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                  children: [
                    ElevatedButton(
                        onPressed: () => _getButtons(),
                        child: Text('Get Buttons')),
                    ElevatedButton(
                        onPressed: () => _startStopScanningForFlic2(),
                        child: Text(_isScanning
                            ? 'Stop Scanning'
                            : 'Scan for buttons')),
                  ],
                ),
              if (null != _lastClick)
                Padding(
                  padding: const EdgeInsets.all(20),
                  child: Text(
                    'FLIC2 @${_lastClick.button.buttonAddr}\nclicked ${_lastClick.timestamp - _lastClick.button.readyTimestamp}ms from ready state\n'
                    '${_lastClick.isSingleClick ? 'single click\n' : ''}'
                    '${_lastClick.isDoubleClick ? 'double click\n' : ''}'
                    '${_lastClick.isHold ? 'hold\n' : ''}',
                  ),
                ),
              if (_isScanning)
                Text(
                    'Hold down your flic2 button so we can find it now we are scanning...'),
              // and show the list of buttons we have found at this point
              Expanded(
                child: ListView(
                    children: _buttonsFound.values
                        .map((e) => ListTile(
                              key: ValueKey(e.uuid),
                              leading: Icon(Icons.radio_button_on, size: 48),
                              title: Text('FLIC2 @${e.buttonAddr}'),
                              subtitle: Column(
                                children: [
                                  Text('${e.uuid}\n'
                                      'name: ${e.name}\n'
                                      'batt: ${e.battVoltage}V (${e.battPercentage}%)\n'
                                      'serial: ${e.serialNo}\n'
                                      'pressed: ${e.pressCount}\n'),
                                  Row(
                                    children: [
                                      ElevatedButton(
                                        onPressed: () =>
                                            _connectDisconnectButton(e),
                                        child: Text(e.connectionState ==
                                                Flic2ButtonConnectionState
                                                    .disconnected
                                            ? 'connect'
                                            : 'disconnect'),
                                      ),
                                      SizedBox(width: 20),
                                      ElevatedButton(
                                        onPressed: () => _forgetButton(e),
                                        child: Text('forget'),
                                      ),
                                    ],
                                  ),
                                ],
                              ),
                            ))
                        .toList()),
              ),
            ],
          );
        }
      },
    );
  }
}
