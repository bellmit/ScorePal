import 'package:flic_button/flic_button.dart';
import 'package:multiphone/controllers/controller.dart';
import 'package:multiphone/controllers/controller_listener.dart';
import 'package:multiphone/controllers/controllers.dart';
import 'package:multiphone/helpers/log.dart';

class ControllerFlic extends Controller with Flic2Listener {
  FlicButtonPlugin _flicPlugin;

  ControllerFlic(Controllers provider) : super(provider) {
    // create the manager by creating the plugin manager
    _flicPlugin = FlicButtonPlugin(flic2listener: this);
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

  @override
  void dispose() {
    _flicPlugin.cancelScanForFlic2();
    _flicPlugin.disposeFlic2();
    _flicPlugin = null;
  }

  Future<bool> startScanning() {
    return _flicPlugin.scanForFlic2();
  }

  Future<bool> stopScanning() {
    return _flicPlugin.cancelScanForFlic2();
  }

  @override
  void onButtonClicked(Flic2ButtonClick buttonClick) {
    //TODO check the age of the click in case there's loads cached
    // a button was clicked, pass this on to the control listeners registered
    if (buttonClick.isSingleClick) {
      provider.informListeners(ClickPattern.single);
    }
    if (buttonClick.isDoubleClick) {
      provider.informListeners(ClickPattern.double);
    }
    if (buttonClick.isHold) {
      provider.informListeners(ClickPattern.long);
    }
  }

  @override
  void onButtonConnected() {
    // a button connected, this is a change
  }

  @override
  void onButtonDiscovered(String buttonAddress) {
    // we found the bluetooth address of a button (not interesting to us)
  }

  @override
  void onButtonFound(Flic2Button button) {
    // we have a new button
  }

  @override
  void onFlic2Error(String error) {
    // there is an error
    Log.error('flic error encountered: $error');
  }

  @override
  void onPairedButtonDiscovered(Flic2Button button) {
    // we have a new button
  }

  @override
  void onScanCompleted() {
    // change our flag to show we are not scanning now
  }

  @override
  void onScanStarted() {
    // change our flag to show we are scanning now
  }
}
