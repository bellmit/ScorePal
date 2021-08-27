import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
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
    final theme = Theme.of(context);
    return Padding(
      padding: const EdgeInsets.all(Values.default_space),
      child: AspectRatio(
        aspectRatio: 1,
        child: InkWell(
          onTap: onClicked,
          child: Card(
              color: theme.primaryColor,
              shape: RoundedRectangleBorder(
                borderRadius:
                    BorderRadius.all(Radius.circular(Values.default_radius)),
              ),
              elevation: onClicked == null ? 0 : theme.cardTheme.elevation,
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
                          child: TextWidget(title, isOnBackground: true),
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
                                isOnBackground: true,
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
                              color: theme.accentColor.withOpacity(0.8),
                              shape: BoxShape.circle,
                            ),
                            child: Icon(
                              Icons.add,
                              color: theme.accentTextTheme.button.color
                                  .withOpacity(0.3),
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
                        child: SvgPicture.asset(
                          'images/svg/$serveSvg.svg',
                          width: Values.image_icon,
                          height: Values.image_icon,
                        ),
                        //child: IconSvgWidget(serveSvg, isOnBackground: true),
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
