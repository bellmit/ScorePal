import 'dart:io';

import 'package:flutter/cupertino.dart';
import 'package:multiphone/controllers/controller_flic.dart';
import 'package:multiphone/controllers/controller_listener.dart';
import 'package:multiphone/controllers/controller_keys.dart';
import 'package:multiphone/helpers/log.dart';
import 'package:multiphone/helpers/preferences.dart';
import 'package:multiphone/widgets/settings/select_control_widget.dart';
import 'package:permission_handler/permission_handler.dart';

enum ClickPattern {
  single,
  double,
  long,
}

enum ClickSource {
  flic2,
  mediaButton,
  volButton,
}

class Controllers {
  ControllerFlic _controllerFlic;
  ControllerKeys _controllerKeys;
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

  void informListeners(ClickPattern click, ClickSource source) {
    bool isProcessClick = true;
    if (null != _preferences) {
      // check that we want to listen to this based on the current app settings
      switch (source) {
        case ClickSource.flic2:
          isProcessClick = _preferences.isControlFlic2;
          break;
        case ClickSource.mediaButton:
          isProcessClick = _preferences.isControlMediaKeys;
          break;
        case ClickSource.volButton:
          isProcessClick = _preferences.isControlVolumeButtons;
          break;
      }
    }
    if (isProcessClick) {
      // we want to deal with this then, from a valid controller that's turned on
      final action = _clickToAction(click);
      for (ControllerListener listener in _listeners) {
        listener.onButtonPressed(action: action);
      }
    }
  }

  ClickAction _clickToAction(ClickPattern pattern) {
    if (null == _preferences ||
        _preferences.controlType == ControlType.meThem) {
      switch (pattern) {
        case ClickPattern.single:
          return ClickAction.pointTeamOne;
        case ClickPattern.double:
          return ClickAction.pointTeamTwo;
        default:
          return ClickAction.undoLast;
      }
    } else {
      // they have elected to do server / receiver
      switch (pattern) {
        case ClickPattern.single:
          return ClickAction.pointServer;
        case ClickPattern.double:
          return ClickAction.pointReceiver;
        default:
          return ClickAction.undoLast;
      }
    }
  }

  void initialiseControllers() {
    if (null == _preferences) {
      // not got the prefs yet - nothing can do
      return;
    }
    // create our controllers as required
    if (null == _controllerFlic) {
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
    }
    if (null == _controllerKeys) {
      _controllerKeys = ControllerKeys(this);
    }
  }

  void dispose() {
    // dispose all our controllers
    if (null != _controllerFlic) {
      _controllerFlic.dispose();
      _controllerFlic = null;
    }
    if (null != _controllerKeys) {
      _controllerKeys.dispose();
      _controllerKeys = null;
    }
  }
}
