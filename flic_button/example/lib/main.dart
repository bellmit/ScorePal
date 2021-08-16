import 'package:flutter/material.dart';

import 'package:flic_button/flic_button.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> implements Flic2Listener {
  // flic2 starts and isn't scanning
  bool _isScanning = false;

  // as we discover buttons, lets add them to a map of uuid/button to show
  final Map<String, Flic2Button> _buttonsFound = {};
  // the last click to show we are hearing the button click
  Flic2ButtonClick? _lastClick;

  // the plugin manager to use while we are active
  FlicButtonPlugin? flicButtonManager;

  @override
  void initState() {
    super.initState();
    // create the FLIC 2 manager and initialize it
    _startStopFlic2();
  }

  void _startStopScanningForFlic2() {
    // start scanning
    if (!_isScanning) {
      // not scanning yet - start
      flicButtonManager!.scanForFlic2();
    } else {
      // are scanning - cancel that
      flicButtonManager!.cancelScanForFlic2();
    }
    // update the UI
    setState(() {
      _isScanning = !_isScanning;
    });
  }

  void _startStopFlic2() {
    // start or stop it
    if (null == flicButtonManager) {
      // we are not started - start listening to FLIC2 buttons
      setState(() => flicButtonManager = FlicButtonPlugin(flic2listener: this));
    } else {
      // started - so stop
      flicButtonManager!.disposeFlic2().then((value) => setState(() {
            // as the flic manager is disposed, signal that it's gone
            flicButtonManager = null;
          }));
    }
  }

  void _getButtons() {
    // get all the buttons
    flicButtonManager!.getFlic2Buttons().then((buttons) {
      // put all of these in the list to show the buttons
      buttons.forEach((button) {
        _addButtonAndListen(button);
      });
    });
  }

  void _addButtonAndListen(Flic2Button button) {
    setState(() {
      // add the button to the map
      _buttonsFound[button.uuid] = button;
      // and listen to the button for clicks and things
      flicButtonManager!.listenToFlic2Button(button.uuid);
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
          appBar: AppBar(
            title: const Text('Flic Button Plugin Example'),
          ),
          body: FutureBuilder(
            future: flicButtonManager != null
                ? flicButtonManager!.invokation
                : null,
            builder: (ctx, snapshot) {
              if (snapshot.connectionState != ConnectionState.done) {
                // are not initialized yet, wait a sec - should be very quick!
                return Center(
                  child: ElevatedButton(
                    onPressed: () {
                      // initiate or stop scanning
                      _startStopFlic2();
                    },
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
                      onPressed: () {
                        // initiate or stop scanning
                        _startStopFlic2();
                      },
                      child: Text('Stop Flic2'),
                    ),
                    if (flicButtonManager != null)
                      Row(
                        // if we are started then show the controls to get flic2 and scan for flic2
                        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                        children: [
                          ElevatedButton(
                              onPressed: () {
                                // get all the buttons currently registered, will listen to any found
                                _getButtons();
                              },
                              child: Text('Get Buttons')),
                          ElevatedButton(
                              onPressed: () {
                                // initiate scanning
                                _startStopScanningForFlic2();
                              },
                              child: Text(_isScanning
                                  ? 'Stop Scanning'
                                  : 'Scan for buttons')),
                        ],
                      ),
                    if (null != _lastClick)
                      Padding(
                        padding: const EdgeInsets.all(20),
                        child: Text(
                          'FLIC2 @${_lastClick!.button.buttonAddr}\nclicked ${_lastClick!.timestamp - _lastClick!.button.readyTimestamp}ms from ready state\n'
                          '${_lastClick!.isSingleClick ? 'single click\n' : ''}'
                          '${_lastClick!.isDoubleClick ? 'double click\n' : ''}'
                          '${_lastClick!.isHold ? 'hold\n' : ''}',
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
                                    leading: Icon(
                                      Icons.radio_button_on,
                                      size: 48,
                                    ),
                                    title: Text('FLIC2 @${e.buttonAddr}'),
                                    subtitle: Text('${e.uuid}\n'
                                        'name: ${e.name}\n'
                                        'batt: ${e.battVoltage}V (${e.battPercentage}%)\n'
                                        'serial: ${e.serialNo}\n'
                                        'pressed: ${e.pressCount}\n'),
                                  ))
                              .toList()),
                    ),
                  ],
                );
              }
            },
          )),
    );
  }

  @override
  void onButtonClicked(Flic2ButtonClick buttonClick) {
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
  void onButtonDiscovered(Flic2Button button) {
    // we have discovered a new button, add to the list to show
    print('button ${button.uuid} discovered');
    _addButtonAndListen(button);
  }

  @override
  void onButtonFound(String buttonAddress) {
    // this is an address which we should be able to resolve to an actual button right away
    print('button @$buttonAddress found');
    // but we could in theory wait for it to be connected and discovered because that will happen too
    flicButtonManager!.getFlic2ButtonByAddress(buttonAddress).then((button) {
      print(
          'button found with address $buttonAddress resolved to actual button data ${button.uuid}');
      // which we can add to the list to show right away
      _addButtonAndListen(button);
    });
  }

  @override
  void onFlic2Error(String error) {
    // something went wrong somewhere, provide feedback maybe, or did you code something in the wrong order
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
    setState(() {
      _isScanning = false;
    });
  }

  @override
  void onScanStarted() {
    setState(() {
      _isScanning = true;
    });
  }
}
