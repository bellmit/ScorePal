import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flic_button/flic_button.dart';
import 'package:permission_handler/permission_handler.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  String _flic2Uuid = 'unknown';

  Future<CancelListening>? listener;
  var flicButtonManager;

  @override
  void initState() {
    super.initState();

    flicButtonManager = FlicButtonPlugin();
    // init background calls from the android service
    listener = flicButtonManager.startListening((msg) {
      setState(() {
        _flic2Uuid = msg;
      });
    });

    initPlatformState();
  }

  Future<void> scanForFlic2() async {
    await Permission.location.request();

    String flic2Uuid;
    try {
      flic2Uuid = await FlicButtonPlugin.getFlic2Button ?? 'no flic2';
    } on PlatformException {
      flic2Uuid = 'Failed to get flic 2.';
    }
    if (!mounted) return;

    setState(() {
      _flic2Uuid = flic2Uuid;
    });
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      platformVersion =
          await FlicButtonPlugin.platformVersion ?? 'Unknown platform version';
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            children: [
              Text('Running on: $_platformVersion and found $_flic2Uuid'),
              TextButton(
                  onPressed: () {
                    listener!.then((value) => value());
                  },
                  child: Text('stop listening'))
            ],
          ),
        ),
      ),
    );
  }
}
