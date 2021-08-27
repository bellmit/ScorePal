import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/providers/sport.dart';
import 'package:provider/provider.dart';

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
    final screenSize = MediaQuery.of(context).size;
    var cols;
    if (screenSize.width > screenSize.height) {
      cols = 4;
    } else {
      cols = 2;
    }
    //rows =
    return Scaffold(
      backgroundColor: Theme.of(context).primaryColor,
      body: Consumer<Sports>(
        builder: (ctx, sports, child) {
          final availableSports = sports.available;
          final images = List<String>.generate(cols * 5,
              (index) => availableSports[index % availableSports.length].image);
          return Stack(
            children: [
              GridView(
                gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
                  crossAxisCount: cols,
                ),
                children: images
                    .map((e) => Image.asset(
                          e,
                        ))
                    .toList(),
              ),
              child,
            ],
          );
        },
        child: Center(
          child: Card(
            color: Values.primaryLightColorFaded,
            child: Padding(
              padding: const EdgeInsets.all(Values.default_space),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Text(
                    Values(context).strings.title,
                    style: TextStyle(fontSize: 50),
                  ),
                  Text(
                    _errorString != null && _errorString.isNotEmpty
                        ? _errorString
                        : Values(context).strings.loading_wait,
                    style: TextStyle(fontSize: 20),
                  )
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}
