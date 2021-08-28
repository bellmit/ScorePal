import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/match_play_tracker.dart';
import 'package:multiphone/widgets/common/common_widgets.dart';

class PlayNewMatchWidget extends StatelessWidget {
  const PlayNewMatchWidget({Key key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final media = MediaQuery.of(context);
    final cardWidth = media.orientation == Orientation.landscape
        ? media.size.width * 0.4
        : media.size.width * 0.9;
    return Card(
      margin: const EdgeInsets.all(Values.default_space),
      child: ConstrainedBox(
        constraints: BoxConstraints.loose(Size.fromWidth(cardWidth)),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Padding(
              padding: EdgeInsets.only(
                  left: Values.default_space, right: Values.default_space),
              child: ClipRRect(
                borderRadius: BorderRadius.circular(Values.default_radius),
                child: Image.asset(
                  'images/img/tennis.jpg',
                  height: Values.image_large,
                  width: Values.image_large,
                ),
              ),
            ),
            Flexible(
              child: TextWidget(
                  Values(context).strings.description_play_new_match),
            ),
            Padding(
              padding: EdgeInsets.all(Values.default_space),
              child: FloatingActionButton(
                onPressed: () => MatchPlayTracker.setupNewMatch(context),
                child: const IconWidget(Icons.play_arrow, size: null),
                backgroundColor: Theme.of(context).accentColor,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
