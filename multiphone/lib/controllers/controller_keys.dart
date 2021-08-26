import 'package:flutter/services.dart';
import 'package:multiphone/controllers/controller.dart';
import 'package:multiphone/controllers/controllers.dart';
import 'package:multiphone/helpers/log.dart';
import 'package:multiphone/helpers/values.dart';

class Click {
  final Duration duration;
  final DateTime time;
  final ClickSource source;
  const Click(this.time, this.duration, this.source);
}

class ControllerKeys extends Controller {
  DateTime _buttonDown;
  final List<Click> _buttonClicks = [];

  ControllerKeys(Controllers provider) : super(provider, null) {
    // listen for key presses then please
    RawKeyboard.instance.addListener(_onKeyboardPressed);
  }

  void _onKeyboardPressed(RawKeyEvent event) {
    if (event != null &&
        (event.logicalKey == LogicalKeyboardKey.browserBack ||
            event.logicalKey == LogicalKeyboardKey.home)) {
      // the event is back or home, ignore this bad-boy
      return;
    }
    if (event is RawKeyDownEvent) {
      // handle key down
      if (null == _buttonDown) {
        _buttonDown = DateTime.now();
      }
    } else if (event is RawKeyUpEvent) {
      // handle key up
      if (_buttonDown != null) {
        final source = event.logicalKey == LogicalKeyboardKey.audioVolumeDown ||
                event.logicalKey == LogicalKeyboardKey.audioVolumeUp
            ? ClickSource.volButton
            : ClickSource.mediaButton;
        final click =
            Click(_buttonDown, DateTime.now().difference(_buttonDown), source);
        Log.info('clicked ${click.duration.inMilliseconds}ms');
        if (click.duration.inMilliseconds > Values.click_hold_ms) {
          // this is a long click
          provider.informListeners(ClickPattern.long, clickSource);
        } else {
          // add to the list in case there are more
          _buttonClicks.add(click);
          // check in a second to see how many
          Future.delayed(Duration(milliseconds: Values.click_capture_period_ms),
              () {
            // handle the number of clicks gathered in this short period of time
            switch (_buttonClicks.length) {
              case 1:
                provider.informListeners(
                    ClickPattern.single, _buttonClicks.last.source);
                break;
              case 2:
                provider.informListeners(
                    ClickPattern.double, _buttonClicks.last.source);
                break;
            }
            // clear the one we added just above then, timed-out
            //_buttonClicks.removeAt(0);
            // feel like it's more robust just to clear everything within
            // this time elapsed
            _buttonClicks.clear();
          });
        }
        _buttonDown = null;
      }
    }
  }

  @override
  void dispose() {
    RawKeyboard.instance.removeListener(_onKeyboardPressed);
  }
}
