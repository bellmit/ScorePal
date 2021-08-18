import 'dart:async';
import 'dart:convert';
import 'package:flutter/services.dart';

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
  void onButtonFound(Flic2Button button);
  void onButtonDiscovered(String buttonAddress);
  void onPairedButtonDiscovered(Flic2Button button);
  void onButtonClicked(Flic2ButtonClick buttonClick);
  void onButtonConnected();
  void onScanStarted();
  void onScanCompleted();
  void onFlic2Error(String error);
}

class FlicButtonPlugin {
  static const String _channelName = 'flic_button';
  static const String _methodNameInitialize = 'initializeFlic2';
  static const String _methodNameDispose = 'disposeFlic2';
  static const String _methodNameCallback = 'callListener';

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

  Future<bool?>? _invokationFuture;

  final Flic2Listener flic2listener;

  FlicButtonPlugin({required this.flic2listener}) {
    // set the callback handler to ours to receive all our data back after
    // initialized
    _channel.setMethodCallHandler(_methodCallHandler);
    // an invoke the function to initialise the handling of Flic 2
    _invokationFuture = _channel.invokeMethod<bool>(_methodNameInitialize);
  }

  Future<bool?>? get invokation {
    return _invokationFuture;
  }

  Future<bool?> disposeFlic2() async {
    // this just stops the FLIC 2 manager if not started that's ok
    return _channel.invokeMethod<bool>(_methodNameDispose);
  }

  Future<bool?> scanForFlic2() async {
    // scan for flic 2 buttons then please
    return _channel.invokeMethod<bool>(_methodNameStartFlic2Scan);
  }

  Future<bool?> cancelScanForFlic2() async {
    // scan for flic 2 buttons then please
    return _channel.invokeMethod<bool>(_methodNameStopFlic2Scan);
  }

  Future<bool?> connectButton(String buttonUuid) async {
    // connect this button then please
    return _channel.invokeMethod<bool>(_methodNameConnectButton, [buttonUuid]);
  }

  Future<bool?> disconnectButton(String buttonUuid) async {
    // disconnect this button then please
    return _channel
        .invokeMethod<bool>(_methodNameDisconnectButton, [buttonUuid]);
  }

  Future<bool?> forgetButton(String buttonUuid) async {
    // forget this button then please
    return _channel.invokeMethod<bool>(_methodNameForgetButton, [buttonUuid]);
  }

  Future<bool?> listenToFlic2Button(String buttonUuid) async {
    // scan for flic 2 buttons then please
    return _channel
        .invokeMethod<bool>(_methodNameStartListenToFlic2, [buttonUuid]);
  }

  Future<bool?> cancelListenToFlic2Button(String buttonUuid) async {
    // scan for flic 2 buttons then please
    return _channel
        .invokeMethod<bool>(_methodNameStopListenToFlic2, [buttonUuid]);
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
      print('data back is not a valid button: $data $error');
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
      print('data back is not a valid click: $data $error');
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
        // this is a nice callback from the implementation - call the proper
        // function that is required then (by the passed data)
        final methodId = call.arguments['method'] ?? '';
        final methodData = call.arguments['data'] ?? '';
        // get the callback that's registered with this ID to call it
        switch (methodId) {
          case METHOD_FLIC2_DISCOVER_PAIRED:
            // process this method - have discovered a paired flic 2 button
            flic2listener
                .onPairedButtonDiscovered(_createFlic2FromData(methodData));
            break;
          case METHOD_FLIC2_DISCOVERED:
            // process this method - have discovered a flic 2 button, but just the address which isn't great
            flic2listener.onButtonDiscovered(methodData);
            break;
          case METHOD_FLIC2_CONNECTED:
            // process this method - have connected a flic 2 button
            flic2listener.onButtonConnected();
            break;
          case METHOD_FLIC2_FOUND:
            // process this method - have found a flic 2 button
            flic2listener.onButtonFound(_createFlic2FromData(methodData));
            break;
          case METHOD_FLIC2_CLICK:
            // process this method - have clicked a flic 2 button
            flic2listener
                .onButtonClicked(_createFlic2ClickFromData(methodData));
            break;
          case METHOD_FLIC2_SCANNING:
            // process this method - scanning for buttons
            flic2listener.onScanStarted();
            break;
          case METHOD_FLIC2_SCAN_COMPLETE:
            // process this method - scanning for buttons completed
            flic2listener.onScanCompleted();
            break;
          case METHOD_FLIC2_ERROR:
            // process this method - scanning for buttons completed
            flic2listener.onFlic2Error(methodData);
            break;
          default:
            print('unrecognised method callback encountered $methodId');
            break;
        }
        break;
      default:
        print('Ignoring unrecognosed invoke from native ${call.method}');
        break;
    }
  }
}
