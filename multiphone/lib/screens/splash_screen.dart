import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/providers/sport.dart';
import 'package:multiphone/widgets/common/common_widgets.dart';
import 'package:provider/provider.dart';

class SplashScreen extends StatelessWidget {
  final String _errorString;

  SplashScreen(this._errorString);

  @override
  Widget build(BuildContext context) {
    final screenSize = MediaQuery.of(context).size;
    var cols;
    if (screenSize.width > screenSize.height) {
      cols = 4;
    } else {
      cols = 2;
    }
    final theme = Theme.of(context);
    return Scaffold(
      backgroundColor: theme.primaryColor,
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
          child: FractionallySizedBox(
            widthFactor: 0.4,
            alignment: Alignment.center,
            child: Container(
              decoration: BoxDecoration(
                color: theme.secondaryHeaderColor.withOpacity(0.75),
                border: Border.all(
                    color: theme.primaryColorDark, width: Values.border_width),
                borderRadius:
                    BorderRadius.all(Radius.circular(Values.default_radius)),
              ),
              child: Padding(
                padding: const EdgeInsets.all(Values.default_space),
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Container(
                      width: double.infinity,
                      child: FittedBox(
                        fit: BoxFit.fitWidth,
                        child: Text(
                          Values(context).strings.title,
                        ),
                      ),
                    ),
                    Container(
                      width: double.infinity,
                      child: FittedBox(
                        fit: BoxFit.fitWidth,
                        child: TextWidget(
                          _errorString != null && _errorString.isNotEmpty
                              ? _errorString
                              : Values(context).strings.loading_wait,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}
