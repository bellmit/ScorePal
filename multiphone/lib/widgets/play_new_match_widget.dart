import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/match_play_tracker.dart';

class PlayNewMatchWidget extends StatelessWidget {
  const PlayNewMatchWidget({Key key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.all(Values.default_space),
      child: Row(
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
          Expanded(
            child: Text(
              "Why not play a match to get started...",
              maxLines: 3,
              style: TextStyle(
                  fontSize: Values.font_size_title,
                  color: Theme.of(context).primaryColorDark),
            ),
          ),
          Padding(
            padding: EdgeInsets.all(Values.default_space),
            child: FloatingActionButton(
              onPressed: () => MatchPlayTracker.setupNewMatch(context),
              child: const Icon(Icons.play_arrow),
              backgroundColor: Theme.of(context).accentColor,
            ),
          ),
        ],
      ),
    );
  }
}
