import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:multiphone/helpers/speak_service.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/score_state.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/screens/playing_team_widget.dart';
import 'package:provider/provider.dart';

abstract class PlayMatchScreen extends StatefulWidget {
  PlayMatchScreen();

  @override
  _PlayMatchScreenState createState() => _PlayMatchScreenState();

  Widget createScoreWidget(ActiveMatch match, TeamIndex teamIndex,
      void Function(int level) onScoreClicked);

  void onScoreClicked(ActiveMatch match, TeamIndex team, int level);
}

class _PlayMatchScreenState extends State<PlayMatchScreen>
    with SingleTickerProviderStateMixin {
  String _description = '';

  AnimationController controller;
  Animation<Offset> offset;

  @override
  void initState() {
    super.initState();

    controller = AnimationController(
        vsync: this,
        duration: Duration(milliseconds: Values.animation_duration_ms));

    offset = Tween<Offset>(begin: Offset(-1.0, 0.0), end: Offset.zero)
        .animate(controller);
  }

  void _processScoreChange(ActiveMatch match, TeamIndex team, int level) {
    // make the derived class change our score
    widget.onScoreClicked(match, team, level);
    // and also show any change in score we need to show
    final state = match.score.state;
    var description = '';
    if (!state.isEmpty && !state.isChanged(ScoreChange.incrementRedo)) {
      // this change is good, do we want to show this?
      description = match.getStateDescription(context, state.getState());
    }
    // change this state then to show the text (or cleared text)
    setState(() {
      _description = description;
    });
    if (description.isNotEmpty) {
      // there is something to show, animate the control in to show it
      controller.forward();
      // only want to show this for a duration of time
      Future.delayed(
        Duration(milliseconds: Values.display_duration_ms),
      ).then((v) {
        // animate this out
        controller.reverse();
      });
    }
    _speakMatchChange(context, match);
  }

  void _speakMatchChange(BuildContext context, ActiveMatch match) {
    final state = match.score.state;
    final speakService = Provider.of<SpeakService>(context, listen: false);
    if (!state.isChanged(ScoreChange.incrementRedo)) {
      // this is not during a 'redo' so we need to process and display this change
      if (match != null) {
        // so speak this state
        speakService.speak(match.getSpokenStateMessage(context));
      }
      //TODO handle play tracking
      /*
      if (null != playTracker) {
        // every time the points change we want to check to see if we have ended or not
        playTracker.handlePlayEnding();
      }
      // update any open notification with this new match data
      if (null != matchNotification) {
        matchNotification.updateNotification(activeMatch);
      }
       */
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
                  position: offset,
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
