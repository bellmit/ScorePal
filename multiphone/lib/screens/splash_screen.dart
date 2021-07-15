import 'package:flutter/material.dart';

enum SplashScreenState {
  error,
  loading,
}

class SplashScreen extends StatelessWidget {
  final String _errorString;
  final SplashScreenState _state;

  SplashScreen(this._state, this._errorString);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Theme.of(context).primaryColor,
      body: Center(
        child: Text(
            _state == SplashScreenState.loading ? "Loading..." : _errorString),
      ),
    );
  }
}
