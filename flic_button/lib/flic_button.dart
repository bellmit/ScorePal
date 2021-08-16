import 'dart:async';
import 'dart:convert';
import 'package:flutter/services.dart';

typedef void CancelListening();

enum Flic2ButtonConnectionState {
  CONNECTION_STATE_DISCONNECTED,
  CONNECTION_STATE_CONNECTING,
  CONNECTION_STATE_CONNECTED_STARTING,
  CONNECTION_STATE_CONNECTED_READY,
}

class Flic2Button {
  final String uuid;
  final String buttonAddr;
  final int readyTimestamp;
  final String name;
  final String serialNo;
  final Flic2ButtonConnectionState connectionState;
  final int firmwareVersion;
  final int battPercentage;
  final int battTimestamp;
  final double battVoltage;
  final int pressCount;

  const Flic2Button({
    required this.uuid,
    required this.buttonAddr,
    required this.readyTimestamp,
    required this.name,
    required this.serialNo,
    required this.connectionState,
    required this.firmwareVersion,
    required this.battPercentage,
    required this.battTimestamp,
    required this.battVoltage,
    required this.pressCount,
  });
}

class Flic2ButtonClick {
  final Flic2Button button;
  final bool wasQueued;
  final bool lastQueued;
  final int clickAge;
  final int timestamp;
  final bool isSingleClick;
  final bool isDoubleClick;
  final bool isHold;

  const Flic2ButtonClick({
    required this.wasQueued,
    required this.clickAge,
    required this.lastQueued,
    required this.timestamp,
    required this.isSingleClick,
    required this.isDoubleClick,
    required this.isHold,
    required this.button,
  });
}

abstract class Flic2Listener {
  void onButtonFound(String buttonAddress);
  void onButtonDiscovered(Flic2Button button);
  void onPairedButtonDiscovered(Flic2Button button);
  void onButtonClicked(Flic2ButtonClick buttonClick);
  void onButtonConnected();
  void onScanStarted();
  void onScanCompleted();
  void onFlic2Error(String error);
}

class FlicButtonPlugin {
  static const String _channelName = 'flic_button';
  static const String _methodNameInitialize = 'initializeService';
  static const String _methodNameCancel = 'cancelListening';
  static const String _methodNameCallback = 'callListener';

  static const String _methodNameStartFlic2 = 'startFlic2';
  static const String _methodNameStopFlic2 = 'stopFlic2';

  static const String _methodNameStartFlic2Scan = "startFlic2Scan";
  static const String _methodNameStopFlic2Scan = "stopFlic2Scan";
  static const String _methodNameStartListenToFlic2 = "startListenToFlic2";
  static const String _methodNameStopListenToFlic2 = "stopListenToFlic2";

  static const String _methodNameGetButtons = "getButtons";
  static const String _methodNameGetButtonsByAddr = "getButtonsByAddr";

  static const String _methodNameConnectButton = "connectButton";
  static const String _methodNameDisconnectButton = "disconnectButton";
  static const String _methodNameForgetButton = "forgetButton";

  static const String ERROR_CRITICAL = 'CRITICAL';
  static const String ERROR_NOT_STARTED = 'NOT_STARTED';
  static const String ERROR_ALREADY_STARTED = 'ALREADY_STARTED';
  static const String ERROR_INVALID_ARGUMENTS = 'INVALID_ARGUMENTS';

  static const int METHOD_FLIC2_DISCOVER_PAIRED = 100;
  static const int METHOD_FLIC2_DISCOVERED = 101;
  static const int METHOD_FLIC2_CONNECTED = 102;
  static const int METHOD_FLIC2_CLICK = 103;
  static const int METHOD_FLIC2_SCANNING = 104;
  static const int METHOD_FLIC2_SCAN_COMPLETE = 105;
  static const int METHOD_FLIC2_FOUND = 106;
  static const int METHOD_FLIC2_ERROR = 200;

  static const MethodChannel _channel = const MethodChannel(_channelName);

  int _nextCallbackId = 0;
  final Map<int, Object> _callbacksById = Map();

  Future<void> startFlic2() async {
    // this just starts the FLIC 2 manager if already started that's ok
    return _channel.invokeMethod(_methodNameStartFlic2);
  }

  Future<void> stopFlic2() async {
    // this just stops the FLIC 2 manager if not started that's ok
    return _channel.invokeMethod(_methodNameStopFlic2);
  }

  Future<CancelListening> initializeFlic2(Flic2Listener callback) async {
    // set the callback handler to ours to receive all our data back after
    // initialized
    _channel.setMethodCallHandler(_methodCallHandler);
    // this will be the ID by which we are identified
    int currentListenerId = ++_nextCallbackId;
    // and remember this
    _callbacksById[currentListenerId] = callback;
    // and wait for the initialization to be performed with an array of args
    await _channel.invokeMethod(_methodNameInitialize, [currentListenerId]);
    // returning the function to call to cancel this listener
    return () {
      // called when want to cancel so invoke this method
      _channel.invokeMethod(_methodNameCancel, [currentListenerId]);
      // and remove the callback from our list
      _callbacksById.remove(currentListenerId);
    };
  }

  Future<void> scanForFlic2() async {
    // scan for flic 2 buttons then please
    return _channel.invokeMethod(_methodNameStartFlic2Scan);
  }

  Future<void> cancelScanForFlic2() async {
    // scan for flic 2 buttons then please
    return _channel.invokeMethod(_methodNameStopFlic2Scan);
  }

  Future<void> connectButton(String buttonUuid) async {
    // connect this button then please
    return _channel.invokeMethod(_methodNameConnectButton, [buttonUuid]);
  }

  Future<void> disconnectButton(String buttonUuid) async {
    // disconnect this button then please
    return _channel.invokeMethod(_methodNameDisconnectButton, [buttonUuid]);
  }

  Future<void> forgetButton(String buttonUuid) async {
    // forget this button then please
    return _channel.invokeMethod(_methodNameForgetButton, [buttonUuid]);
  }

  Future<void> listenToFlic2Button(String buttonUuid) async {
    // scan for flic 2 buttons then please
    return _channel.invokeMethod(_methodNameStartListenToFlic2, [buttonUuid]);
  }

  Future<void> cancelListenToFlic2Button(String buttonUuid) async {
    // scan for flic 2 buttons then please
    return _channel.invokeMethod(_methodNameStopListenToFlic2, [buttonUuid]);
  }

  Future<List<Flic2Button>> getFlic2Buttons() async {
    // get the buttons
    final buttons = await _channel.invokeMethod<List?>(_methodNameGetButtons);
    if (null == buttons) {
      return [];
    } else {
      return buttons.map((e) => _createFlic2FromData(e)).toList();
    }
  }

  Future<Flic2Button> getFlic2ButtonByAddress(String buttonAddress) async {
    // scan for flic 2 buttons then please
    final buttonString = await _channel
        .invokeMethod<String?>(_methodNameGetButtonsByAddr, [buttonAddress]);
    return _createFlic2FromData(buttonString ?? '');
  }

  Flic2ButtonConnectionState _connectionStateFromChannelCode(int code) {
    switch (code) {
      case 0:
        return Flic2ButtonConnectionState.CONNECTION_STATE_DISCONNECTED;
      case 1:
        return Flic2ButtonConnectionState.CONNECTION_STATE_CONNECTING;
      case 2:
        return Flic2ButtonConnectionState.CONNECTION_STATE_CONNECTED_STARTING;
      case 3:
        return Flic2ButtonConnectionState.CONNECTION_STATE_CONNECTED_READY;
      default:
        return Flic2ButtonConnectionState.CONNECTION_STATE_DISCONNECTED;
    }
  }

  Flic2Button _createFlic2FromData(Object data) {
    try {
      // create a button from this json data
      var json;
      if (data is String) {
        // from string data, let's get the map of data
        json = jsonDecode(data);
      } else {
        // this is JSON already, so just use as-is
        json = data;
      }
      return Flic2Button(
        uuid: json['uuid'],
        buttonAddr: json['bdAddr'],
        readyTimestamp: json['readyTime'],
        name: json['name'],
        serialNo: json['serialNo'],
        connectionState: _connectionStateFromChannelCode(json['connection']),
        firmwareVersion: json['firmwareVer'],
        battPercentage: json['battPerc'],
        battTimestamp: json['battTime'],
        battVoltage: json['battVolt'],
        pressCount: json['pressCount'],
      );
    } catch (error) {
      print('data back is not a valid button: $data');
      // return an error button
      return Flic2Button(
          uuid: '',
          buttonAddr: '',
          readyTimestamp: 0,
          name: '',
          serialNo: '',
          connectionState:
              Flic2ButtonConnectionState.CONNECTION_STATE_DISCONNECTED,
          firmwareVersion: 0,
          battPercentage: 0,
          battTimestamp: 0,
          battVoltage: 0.0,
          pressCount: 0);
    }
  }

  Flic2ButtonClick _createFlic2ClickFromData(String data) {
    try {
      final json = jsonDecode(data);
      return Flic2ButtonClick(
        wasQueued: json['wasQueued'],
        clickAge: json['clickAge'],
        lastQueued: json['lastQueued'],
        timestamp: json['timestamp'],
        isSingleClick: json['isSingleClick'],
        isDoubleClick: json['isDoubleClick'],
        isHold: json['isHold'],
        button: _createFlic2FromData(json['button']),
      );
    } catch (error) {
      print('data back is not a valid click: $data');
      // return error button click data
      return Flic2ButtonClick(
        wasQueued: false,
        clickAge: 0,
        lastQueued: false,
        timestamp: 0,
        isSingleClick: false,
        isDoubleClick: false,
        isHold: false,
        button: _createFlic2FromData(''),
      );
    }
  }

  Future<void> _methodCallHandler(MethodCall call) async {
    // this is called from the other side when there's something happening in whic
    // we are interested, the ID of the method determines what is sent back
    switch (call.method) {
      case _methodNameCallback:
        final id = call.arguments['id'] ?? '';
        final methodId = call.arguments['method'] ?? '';
        final methodData = call.arguments['data'] ?? '';
        // get the callback that's registered with this ID to call it
        final callback = _callbacksById[id];
        if (null == callback) {
          print('null callback for id of $id');
        } else {
          // need to process the arguments properly for each method ID
          switch (methodId) {
            case METHOD_FLIC2_DISCOVER_PAIRED:
              // process this method - have discovered a paired flic 2 button
              (callback as Flic2Listener)
                  .onPairedButtonDiscovered(_createFlic2FromData(methodData));
              break;
            case METHOD_FLIC2_DISCOVERED:
              // process this method - have discovered a flic 2 button
              (callback as Flic2Listener)
                  .onButtonDiscovered(_createFlic2FromData(methodData));
              break;
            case METHOD_FLIC2_CONNECTED:
              // process this method - have connected a flic 2 button
              (callback as Flic2Listener).onButtonConnected();
              break;
            case METHOD_FLIC2_CONNECTED:
              // process this method - have found a flic 2 button
              (callback as Flic2Listener).onButtonFound(methodData);
              break;
            case METHOD_FLIC2_CLICK:
              // process this method - have clicked a flic 2 button
              (callback as Flic2Listener)
                  .onButtonClicked(_createFlic2ClickFromData(methodData));
              break;
            case METHOD_FLIC2_SCANNING:
              // process this method - scanning for buttons
              (callback as Flic2Listener).onScanStarted();
              break;
            case METHOD_FLIC2_SCAN_COMPLETE:
              // process this method - scanning for buttons completed
              (callback as Flic2Listener).onScanCompleted();
              break;
            case METHOD_FLIC2_ERROR:
              // process this method - scanning for buttons completed
              (callback as Flic2Listener).onFlic2Error(methodData);
              break;
            default:
              print('unrecognised method callback encountered $methodId');
              break;
          }
        }
        break;
      default:
        print('Ignoring unrecognosed invoke from native.');
        break;
    }
  }
}
