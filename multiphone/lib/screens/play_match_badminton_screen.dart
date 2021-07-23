import 'package:flutter/material.dart';

class PlayMatchBadmintonScreen extends StatelessWidget {
  static const String routeName = "/play-badminton";

  PlayMatchBadmintonScreen();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Theme.of(context).primaryColor,
      body: Center(
        child: Text("PLAY BADMINTON"),
      ),
    );
  }
}
