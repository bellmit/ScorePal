import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/providers/active_setup.dart';

abstract class ScoreWidget extends StatelessWidget {
  const ScoreWidget({Key key}) : super(key: key);

  Widget createBox(
    BuildContext context,
    String title,
    String points,
    bool isServer,
    void onClicked(),
  ) {
    return Padding(
      padding: const EdgeInsets.all(Values.default_space),
      child: AspectRatio(
        aspectRatio: 1,
        child: InkWell(
          onTap: onClicked,
          child: Container(
              // this is the main container for the box of points
              decoration: BoxDecoration(
                color: Theme.of(context).primaryColorDark,
                borderRadius: BorderRadius.circular(Values.default_radius),
              ),
              child: Stack(
                alignment: Alignment.topRight,
                children: [
                  Container(
                    width: double.infinity,
                    height: double.infinity,
                    child: Column(
                      children: [
                        Padding(
                          padding:
                              const EdgeInsets.only(top: Values.default_space),
                          child: Text(
                            title,
                            style: TextStyle(
                                fontSize: Values.font_size_title,
                                color: Theme.of(context).accentColor),
                          ),
                        ),
                        Expanded(
                          child: FittedBox(
                            fit: BoxFit.contain,
                            child: AnimatedSwitcher(
                              duration: const Duration(
                                  milliseconds:
                                      Values.animation_duration_rapid_ms),
                              transitionBuilder:
                                  (Widget child, Animation<double> animation) {
                                return ScaleTransition(
                                    child: child, scale: animation);
                              },
                              child: Text(
                                points,
                                key: ValueKey<String>(points),
                                textAlign: TextAlign.center,
                                style: TextStyle(
                                    color: Theme.of(context).accentColor),
                              ),
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),
                  Padding(
                    padding: const EdgeInsets.all(Values.default_space),
                    child: AnimatedSwitcher(
                      duration: const Duration(
                          milliseconds: Values.animation_duration_rapid_ms),
                      transitionBuilder:
                          (Widget child, Animation<double> animation) {
                        return ScaleTransition(child: child, scale: animation);
                      },
                      child: Container(
                        // to transition between old and new controls, you need a key
                        key: ValueKey<bool>(isServer),
                        // if not serving - making the width zero will hide it
                        width: isServer != null && isServer
                            ? Values.image_small
                            : 0,
                        height: Values.image_small,
                        decoration: new BoxDecoration(
                          color: Theme.of(context).accentColor,
                          shape: BoxShape.circle,
                        ),
                      ),
                    ),
                  ),
                ],
              )),
        ),
      ),
    );
  }
}
