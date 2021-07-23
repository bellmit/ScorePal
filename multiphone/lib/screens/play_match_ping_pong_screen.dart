import 'package:flutter/material.dart';

class PlayMatchPingPongScreen extends StatelessWidget {
  static const String routeName = "/play-ping-pong";
  PlayMatchPingPongScreen();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Theme.of(context).primaryColor,
      body: Center(
        child: Text("PLAY TABLE TENNIS"),
      ),
    );
  }
}
