import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:multiphone/helpers/speak_service.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/match_play_tracker.dart';
import 'package:multiphone/match/match_writer.dart';
import 'package:multiphone/match/score_state.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/screens/playing_team_widget.dart';
import 'package:provider/provider.dart';
import 'package:wakelock/wakelock.dart';

abstract class PlayMatchScreen extends StatefulWidget {
  PlayMatchScreen();

  @override
  _PlayMatchScreenState createState() => _PlayMatchScreenState();

  Widget createScoreWidget(
    ActiveMatch match,
    TeamIndex teamIndex,
    void Function(int level) onScoreClicked,
  );

  void onScoreClicked(ActiveMatch match, TeamIndex team, int level);
}

class _PlayMatchScreenState extends State<PlayMatchScreen>
    with SingleTickerProviderStateMixin {
  String _description = '';
  MatchPlayTracker _playTracker;

  AnimationController _controller;
  Animation<Offset> _offset;

  @override
  void initState() {
    super.initState();

    // get the match as-is to track it (it will change but there will only be one)
    ActiveMatch match = Provider.of<ActiveMatch>(context, listen: false);
    // new match - new tracker
    _playTracker = MatchPlayTracker(match);

    // keep this screen on
    Wakelock.enable();
    // create the animation controlling things
    _controller = AnimationController(
      vsync: this,
      duration: Duration(milliseconds: Values.animation_duration_ms),
    );
    // and the offset to slide in the latest message
    _offset = Tween<Offset>(
      begin: Offset(-1.0, 0.0),
      end: Offset.zero,
    ).animate(_controller);
  }

  @override
  void dispose() {
    // release all the created things
    _controller.dispose();
    _playTracker.destroy(true);
    // release the lock
    Wakelock.disable();
    // and dispose
    super.dispose();
  }

  void _processScoreChange(ActiveMatch match, TeamIndex team, int level) {
    if (!match.isMatchOver()) {
      // make the derived class change our score
      widget.onScoreClicked(match, team, level);
      // have the tracker process this then please
      _playTracker.processScoreChange(context);
      // and also show any change in score we need to show
      final state = match.score.state;
      var description = '';
      if (!state.isEmpty && !state.isChanged(ScoreChange.incrementRedo)) {
        // this change is good, do we want to show this?
        description = match.getStateDescription(context, state.getState());
      }
      if (description.isNotEmpty) {
        // there is something to show, animate the control in to show it
        _controller.forward();
        // change this state then to show the text
        setState(() {
          _description = description;
        });
        // only want to show this for a duration of time
        Future.delayed(
          Duration(milliseconds: Values.display_duration_ms),
        ).then((v) {
          // animate this out
          _controller.reverse();
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    // To make this screen full screen.
    // It will hide status bar and notch.
    SystemChrome.setEnabledSystemUIOverlays([]);

    return Scaffold(
      body: Consumer<ActiveMatch>(
        builder: (ctx, match, child) {
          return Stack(
            children: [
              Column(
                children: [
                  // this is the bar for team one player and partner
                  PlayingTeamWidget(match: match, team: TeamIndex.T_ONE),
                  Expanded(
                    child: Column(
                      children: [
                        Flexible(
                          child: widget.createScoreWidget(
                            match,
                            TeamIndex.T_ONE,
                            (level) => _processScoreChange(
                                match, TeamIndex.T_ONE, level),
                          ),
                        ),
                        Flexible(
                          child: widget.createScoreWidget(
                            match,
                            TeamIndex.T_TWO,
                            (level) => _processScoreChange(
                                match, TeamIndex.T_TWO, level),
                          ),
                        ),
                      ],
                    ),
                  ),
                  // this is the bar for team two player and partner
                  PlayingTeamWidget(match: match, team: TeamIndex.T_TWO),
                ],
              ),
              Align(
                alignment: Alignment.center,
                child: SlideTransition(
                  position: _offset,
                  child: Center(
                    child: FractionallySizedBox(
                        heightFactor: 0.2,
                        widthFactor: 0.8,
                        child: Card(
                          borderOnForeground: true,
                          child: Padding(
                            padding: EdgeInsets.all(Values.default_space),
                            child: FittedBox(
                              fit: BoxFit.fitWidth,
                              child: Text(_description),
                            ),
                          ),
                          color: Theme.of(context).primaryColor,
                        )),
                  ),
                ),
              ),
            ],
          );
        },
      ),
    );
  }
}
