import 'dart:async';

import 'package:flic_button/flic_button.dart';
import 'package:multiphone/helpers/log.dart';

class Flic2Wrapper with Flic2Listener {
  static Flic2Wrapper _instance;
  FlicButtonPlugin _flicPlugin;

  final List<Flic2Listener> _listeners = [];

  Flic2Wrapper._() {
    // private constructor, just create the needed plugin
    _flicPlugin = FlicButtonPlugin(flic2listener: this);
    connectToAllButtons();
  }

  static Flic2Wrapper instance() {
    if (_instance == null) {
      _instance = Flic2Wrapper._();
    }
    return _instance;
  }

  static Future<bool> dispose() {
    Completer<bool> completer = Completer();
    if (_instance != null) {
      _instance.plugin.disposeFlic2().then((value) {
        _instance = null;
        completer.complete(value);
      });
    } else {
      // fail
      completer.completeError('Flic Button Plugin Instance already disposed');
    }
    return completer.future;
  }

  void connectToAllButtons() {
    _flicPlugin.invokation.then((value) {
      // value can be true or false - depending - so just ignore it
      // have invoked the plugin here, get all the buttons to connect them up
      return _flicPlugin.getFlic2Buttons();
    }).then((value) {
      // here we have the buttons
      for (Flic2Button button in value) {
        // listen to them all!
        _flicPlugin.listenToFlic2Button(button.uuid);
      }
    }).catchError(
        (error) => Log.error('failed to initialise flic2 plugin $error'));
  }

  FlicButtonPlugin get plugin {
    return _flicPlugin;
  }

  void addListener(Flic2Listener listener) {
    _listeners.add(listener);
  }

  void removeListener(Flic2Listener listener) {
    _listeners.remove(listener);
  }

  @override
  void onButtonClicked(Flic2ButtonClick buttonClick) {
    // pass to the registered listeners
    for (Flic2Listener listener in _listeners) {
      listener.onButtonClicked(buttonClick);
    }
  }

  @override
  void onButtonFound(Flic2Button button) {
    // pass to the registered listeners
    for (Flic2Listener listener in _listeners) {
      listener.onButtonFound(button);
    }
  }

  @override
  void onButtonDiscovered(String buttonAddress) {
    // pass to the registered listeners
    for (Flic2Listener listener in _listeners) {
      listener.onButtonDiscovered(buttonAddress);
    }
  }

  @override
  void onPairedButtonDiscovered(Flic2Button button) {
    // pass to the registered listeners
    for (Flic2Listener listener in _listeners) {
      listener.onPairedButtonDiscovered(button);
    }
  }

  @override
  void onButtonConnected() {
    // pass to the registered listeners
    for (Flic2Listener listener in _listeners) {
      listener.onButtonConnected();
    }
  }

  @override
  void onScanStarted() {
    // pass to the registered listeners
    for (Flic2Listener listener in _listeners) {
      listener.onScanStarted();
    }
  }

  @override
  void onScanCompleted() {
    // pass to the registered listeners
    for (Flic2Listener listener in _listeners) {
      listener.onScanCompleted();
    }
  }

  @override
  void onFlic2Error(String error) {
    // pass to the registered listeners
    for (Flic2Listener listener in _listeners) {
      listener.onFlic2Error(error);
    }
  }
}
