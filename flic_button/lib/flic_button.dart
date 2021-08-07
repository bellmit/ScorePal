import 'dart:async';
import 'dart:convert';
import 'dart:ui';

import 'package:flic_button/callback_dispatcher.dart';
import 'package:flutter/services.dart';

abstract class FlicButtonEvent {
  /// there is a UUID for a Flic button that we can use to ID the button
  final String uuid;
  final int clicks;

  FlicButtonEvent(this.uuid, this.clicks);
}

typedef void MultiUseCallback(dynamic msg);
typedef void CancelListening();

class FlicButtonPlugin {
  static const MethodChannel _channel = const MethodChannel('flic_button');

  int _nextCallbackId = 0;
  final Map<int, MultiUseCallback> _callbacksById = Map();

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  Future<void> _methodCallHandler(MethodCall call) async {
    switch (call.method) {
      case 'callListener':
        final id = call.arguments["id"] ?? '';
        final callback = _callbacksById[id];
        if (null == callback) {
          print('null callback');
        } else {
          callback(call.arguments["args"]);
        }
        break;
      default:
        print(
            'TestFairy: Ignoring invoke from native. This normally shouldn\'t happen.');
    }
  }

  Future<CancelListening> startListening(MultiUseCallback callback) async {
    _channel.setMethodCallHandler(_methodCallHandler);
    int currentListenerId = _nextCallbackId++;
    _callbacksById[currentListenerId] = callback;
    await _channel.invokeMethod("initializeService", [currentListenerId]);
    return () {
      _channel.invokeMethod("cancelListening", [currentListenerId]);
      _callbacksById.remove(currentListenerId);
    };
  }

  static get getFlic2Button async {
    final result = await _channel.invokeMethod<String>('getFlic2Button');
    var json = jsonDecode(result ?? '');
    return json['UUID'] as String;
  }
}
