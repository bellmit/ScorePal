import 'dart:async';
import 'dart:io' show Platform;
import 'package:flutter/foundation.dart' show kIsWeb;
import 'package:flutter/material.dart';
import 'package:flutter_tts/flutter_tts.dart';
import 'package:multiphone/helpers/log.dart';
import 'package:multiphone/helpers/preferences.dart';
import 'package:multiphone/helpers/values.dart';

enum TtsState { playing, stopped, paused, continued }

class SpeakService with ChangeNotifier {
  final FlutterTts flutterTts;
  String language;
  String engine;
  bool isCurrentLanguageInstalled = false;

  TtsState _ttsState = TtsState.stopped;

  bool get isPlaying => _ttsState == TtsState.playing;
  bool get isStopped => _ttsState == TtsState.stopped;
  bool get isPaused => _ttsState == TtsState.paused;
  bool get isContinued => _ttsState == TtsState.continued;

  TtsState get ttsState => _ttsState;
  set ttsState(TtsState state) {
    _ttsState = state;
    notifyListeners();
  }

  bool get isIOS => !kIsWeb && Platform.isIOS;
  bool get isAndroid => !kIsWeb && Platform.isAndroid;
  bool get isWeb => kIsWeb;

  SpeakService() : flutterTts = FlutterTts() {
    if (isAndroid) {
      _getDefaultEngine();
    }

    flutterTts.setStartHandler(() {
      ttsState = TtsState.playing;
    });

    flutterTts.setCompletionHandler(() {
      ttsState = TtsState.stopped;
    });

    flutterTts.setCancelHandler(() {
      ttsState = TtsState.stopped;
    });

    if (isWeb || isIOS) {
      flutterTts.setPauseHandler(() {
        ttsState = TtsState.paused;
      });

      flutterTts.setContinueHandler(() {
        ttsState = TtsState.continued;
      });
    }

    flutterTts.setErrorHandler((msg) {
      Log.error(msg);
      ttsState = TtsState.stopped;
    });
  }

  Future _getDefaultEngine() async {
    var engine = await flutterTts.getDefaultEngine;
    if (engine != null) {
      Log.debug(engine);
    }
  }

  Future speak(String message) async {
    final preferences = await Preferences.create();
    final volume = preferences.soundAnnounceVolume.clamp(0.0, 1.0);
    Log.debug('speaking the following (vol:$volume): "$message"');
    await flutterTts.setVolume(volume);
    await flutterTts.setSpeechRate(Values.speaking_rate);
    await flutterTts.setPitch(Values.speaking_pitch);

    // stop any previous message
    await stop();
    // and speak the new one
    return flutterTts.speak(message ?? '');
  }

  Future stop() async {
    var result = await flutterTts.stop();
    if (result == 1) ttsState = TtsState.stopped;
  }

  Future pause() async {
    var result = await flutterTts.pause();
    if (result == 1) ttsState = TtsState.paused;
  }

  @override
  void dispose() {
    super.dispose();
    flutterTts.stop();
  }

  /*
  Widget _volume() {
    return Slider(
        value: volume,
        onChanged: (newVolume) {
          setState(() => volume = newVolume);
        },
        min: 0.0,
        max: 1.0,
        divisions: 10,
        label: "Volume: $volume");
  }

  Widget _pitch() {
    return Slider(
      value: pitch,
      onChanged: (newPitch) {
        setState(() => pitch = newPitch);
      },
      min: 0.5,
      max: 2.0,
      divisions: 15,
      label: "Pitch: $pitch",
      activeColor: Colors.red,
    );
  }

  Widget _rate() {
    return Slider(
      value: rate,
      onChanged: (newRate) {
        setState(() => rate = newRate);
      },
      min: 0.0,
      max: 1.0,
      divisions: 10,
      label: "Rate: $rate",
      activeColor: Colors.green,
    );
  }

  */
}
