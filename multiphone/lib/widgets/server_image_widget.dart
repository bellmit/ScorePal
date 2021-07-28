import 'package:flutter/material.dart';
import 'package:flutter_svg/svg.dart';
import 'package:multiphone/helpers/values.dart';

class ServerImageWidget extends StatelessWidget {
  final bool isServing;
  const ServerImageWidget({Key key, this.isServing}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return AnimatedSwitcher(
      duration: const Duration(milliseconds: Values.animation_duration_ms),
      transitionBuilder: (Widget child, Animation<double> animation) {
        return ScaleTransition(child: child, scale: animation);
      },
      child: Container(
          key: ValueKey<bool>(isServing),
          height: isServing ? Values.image_medium : 0,
          width: isServing ? Values.image_medium : 0,
          decoration: new BoxDecoration(
            color: Theme.of(context).accentColor,
            shape: BoxShape.circle,
          ),
          child: SvgPicture.asset(
            isServing
                ? 'images/svg/player-serving.svg'
                : 'images/svg/player-receiving-backhand.svg',
            color: Theme.of(context).primaryColorDark,
          )),
    );
  }
}
