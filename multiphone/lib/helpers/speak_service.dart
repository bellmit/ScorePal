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
  String engine;

  static SpeakService _runningService;

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

  @override
  void dispose() {
    super.dispose();
    flutterTts.stop();
  }

  Future<String> _getDefaultEngine() async {
    engine = await flutterTts.getDefaultEngine;
    if (engine != null) {
      Log.debug('Flutter TTS: $engine');
    }
    return engine;
  }

  static Future<void> speakNow(FlutterTts engine, String message,
      {double volume = 1.0}) async {
    // and speak the message
    Log.debug('speaking the following (vol:$volume): "$message"');
    await engine.setVolume(volume);
    await engine.setSpeechRate(Values.speaking_rate);
    await engine.setPitch(Values.speaking_pitch);
    // and speak the new one
    await engine.speak(message ?? '');
  }

  Future<void> speak(String message) async {
    if (null == message || message.isEmpty) {
      // nothing to say, stop any previous message
      stop();
      return;
    }
    final preferences = await Preferences.create();
    final volume = preferences.soundAnnounceVolume.clamp(0.0, 1.0);
    // stop any previous message
    await stop();
    // and speak this right now at the preference's vol (always 1 for now)
    speakNow(flutterTts, message, volume: volume);
  }

  Future<void> stop() async {
    var result = await flutterTts.stop();
    if (result == 1) ttsState = TtsState.stopped;
  }

  Future<void> pause() async {
    var result = await flutterTts.pause();
    if (result == 1) ttsState = TtsState.paused;
  }
}
