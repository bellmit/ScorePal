import 'dart:io';

import 'package:flutter/cupertino.dart';
import 'package:multiphone/controllers/controller_flic.dart';
import 'package:multiphone/controllers/controller_listener.dart';
import 'package:multiphone/helpers/log.dart';
import 'package:multiphone/helpers/preferences.dart';
import 'package:permission_handler/permission_handler.dart';

class Controllers {
  ControllerFlic _controllerFlic;
  Preferences _preferences;

  final List<ControllerListener> _listeners = [];

  Controllers(BuildContext context) {
    Preferences.create().then((value) {
      // we have the preferences, remember them
      _preferences = value;
      // and initialise our controllers here
      initialiseControllers();
    });
  }

  void registerControllerListener(ControllerListener listener) {
    _listeners.add(listener);
  }

  void releaseControllerListener(ControllerListener listener) {
    _listeners.remove(listener);
  }

  void informListeners(ClickPattern click) {
    for (ControllerListener listener in _listeners) {
      listener.onButtonPressed(pattern: click);
    }
  }

  void initialiseControllers() {
    // create our controllers as required
    if (_preferences.isControlFlic2) {
      // we want flic 2 - check for permissions in Android as need location
      // permission to use a bluetooth device...
      if (Platform.isAndroid) {
        Permission.location.request().then((value) {
          if (value == PermissionStatus.granted) {
            // we have permission to access location (ie a bluetooth button) - create the FLIC controller
            _controllerFlic = ControllerFlic(this);
          } else {
            // this is bad?
            Log.error(
                'permission isn\'t granted so we can\'t proceed, its $value');
          }
        });
      } else {
        // in iOS just go for it
        _controllerFlic = ControllerFlic(this);
      }
    } else if (null != _controllerFlic) {
      // kill what was here
      _controllerFlic.dispose();
      _controllerFlic = null;
    }
  }

  void dispose() {
    // dispose all our controllers
    if (null != _controllerFlic) {
      _controllerFlic.dispose();
      _controllerFlic = null;
    }
  }
}
