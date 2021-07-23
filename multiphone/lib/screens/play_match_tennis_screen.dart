import 'package:flutter/material.dart';

class PlayMatchTennisScreen extends StatelessWidget {
  static const String routeName = "/play-tennis";
  PlayMatchTennisScreen();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Theme.of(context).primaryColor,
      body: Center(
        child: Text("PLAY TENNIS"),
      ),
    );
  }
}
