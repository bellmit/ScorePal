import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';

import 'common/common_widgets.dart';

abstract class ScoreWidget extends StatelessWidget {
  const ScoreWidget({Key key}) : super(key: key);

  Widget scoreColumn(BuildContext context);
  Widget scoreRow(BuildContext context);

  @override
  Widget build(BuildContext context) {
    final mediaQuery = MediaQuery.of(context);
    final screenSize = mediaQuery.size;
    var isShowDetailsAbovePoints;
    if (mediaQuery.orientation == Orientation.landscape) {
      // we have half the width to draw, if the height is > half the width, draw over
      isShowDetailsAbovePoints =
          screenSize.height - Values.team_names_widget_height >
              0.5 * screenSize.width;
    } else {
      // we have half the height to draw, if that height > the width, draw over
      isShowDetailsAbovePoints =
          (screenSize.height * 0.5) - (Values.team_names_widget_height * 2) >
              screenSize.width;
    }
    // and return the correct widget
    return isShowDetailsAbovePoints ? scoreColumn(context) : scoreRow(context);
  }

  Widget createBox(
    BuildContext context,
    String title,
    String points,
    bool isServer,
    String serveSvg,
    void onClicked(),
  ) {
    return Padding(
      padding: const EdgeInsets.all(Values.default_space),
      child: AspectRatio(
        aspectRatio: 1,
        child: InkWell(
          onTap: onClicked,
          child: Card(
              color: Theme.of(context).primaryColorDark,
              shape: RoundedRectangleBorder(
                borderRadius:
                    BorderRadius.all(Radius.circular(Values.default_radius)),
              ),
              elevation:
                  onClicked == null ? 0 : Theme.of(context).cardTheme.elevation,
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
                          child: TextWidget(title),
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
                              child: TextWidget(
                                points,
                                key: ValueKey<String>(points),
                                textAlign: TextAlign.center,
                              ),
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),
                  if (onClicked != null)
                    Align(
                      alignment: Alignment.bottomRight,
                      child: FractionallySizedBox(
                        child: Padding(
                          padding: const EdgeInsets.all(Values.default_space),
                          child: Container(
                            height: Values.image_icon,
                            width: Values.image_icon,
                            decoration: new BoxDecoration(
                              color: Theme.of(context)
                                  .accentColor
                                  .withOpacity(0.1),
                              shape: BoxShape.circle,
                            ),
                            child: Icon(
                              Icons.add,
                              color: Theme.of(context)
                                  .accentColor
                                  .withOpacity(0.2),
                            ),
                          ),
                        ),
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
                        child: IconSvgWidget(serveSvg),
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
