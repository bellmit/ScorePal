import 'package:flic_button/flic_button.dart';
import 'package:multiphone/controllers/controller.dart';
import 'package:multiphone/controllers/controllers.dart';
import 'package:multiphone/helpers/flic2_wrapper.dart';
import 'package:multiphone/helpers/log.dart';

class ControllerFlic extends Controller with Flic2Listener {
  Flic2Wrapper _flicPlugin;

  ControllerFlic(Controllers provider) : super(provider, ClickSource.flic2) {
    // create the manager by creating the plugin manager
    _flicPlugin = Flic2Wrapper.instance();
    _flicPlugin.addListener(this);
    // and connect to everything right away
    _flicPlugin.connectToAllButtons();
  }

  @override
  void dispose() {
    _flicPlugin.removeListener(this);
    _flicPlugin = null;
  }

  @override
  void onButtonClicked(Flic2ButtonClick buttonClick) {
    //TODO check the age of the click in case there's loads cached
    // a button was clicked, pass this on to the control listeners registered
    if (buttonClick.isSingleClick) {
      provider.informListeners(ClickPattern.single, clickSource);
    }
    if (buttonClick.isDoubleClick) {
      provider.informListeners(ClickPattern.double, clickSource);
    }
    if (buttonClick.isHold) {
      provider.informListeners(ClickPattern.long, clickSource);
    }
  }

  @override
  void onButtonFound(Flic2Button button) {
    // we have a new button, connect to it
    if (null != _flicPlugin) {
      _flicPlugin.plugin.connectButton(button.uuid);
    }
  }

  @override
  void onFlic2Error(String error) {
    // there is an error
    Log.error('flic error encountered: $error');
  }
}
