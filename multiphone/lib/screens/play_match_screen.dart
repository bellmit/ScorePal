import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:multiphone/helpers/log.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/match_play_tracker.dart';
import 'package:multiphone/match/match_writer.dart';
import 'package:multiphone/match/score_state.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/screens/playing_team_widget.dart';
import 'package:multiphone/widgets/play_match_options_widget.dart';
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

  String getEndingRoute();
}

class _PlayMatchScreenState extends State<PlayMatchScreen>
    with TickerProviderStateMixin {
  String _description = '';
  MatchPlayTracker _playTracker;

  AnimationController _messageController;
  Animation<Offset> _messageOffset;

  AnimationController _optionsController;
  Animation<Offset> _optionsOffset;

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
    _messageController = AnimationController(
      vsync: this,
      duration: Duration(milliseconds: Values.animation_duration_ms),
    );
    _optionsController = AnimationController(
      vsync: this,
      duration: Duration(milliseconds: Values.animation_duration_ms),
    );
    // and the offset to slide in the latest message
    _messageOffset = Tween<Offset>(
      begin: Offset(-1.0, 0.0),
      end: Offset.zero,
    ).animate(_messageController);
    // and the offset to slide in the options
    _optionsOffset = Tween<Offset>(
      begin: Offset(1.0, 0.0),
      end: Offset.zero,
    ).animate(_optionsController);
  }

  void _releaseLocks() {
    Log.debug("releasing locks");
    // release the lock
    Wakelock.disable();
    // and put the overlays back
    SystemChrome.setEnabledSystemUIOverlays(
        [SystemUiOverlay.bottom, SystemUiOverlay.top]);
  }

  @override
  void deactivate() {
    print("deactivate");
    _releaseLocks();
    super.deactivate();
  }

  @override
  void dispose() {
    // release all the created things
    _messageController.dispose();
    _optionsController.dispose();
    // release any locks we asked for
    _releaseLocks();
    // and dispose
    super.dispose();
  }

  void _undoLastPoint(ActiveMatch match) {
    match.undoLastPoint();
    // have the tracker process this then please
    _playTracker.processScoreChange(context);
  }

  void _showPauseOptions(bool show) {
    if (show) {
      _optionsController.forward();
    } else {
      _optionsController.reverse();
    }
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
        _messageController.forward();
        // change this state then to show the text
        setState(() {
          _description = description;
        });
        // only want to show this for a duration of time
        Future.delayed(
          Duration(milliseconds: Values.display_duration_ms),
        ).then((v) {
          // animate this out
          _messageController.reverse();
        });
      }
    }
  }

  void _onMatchOptionSelected(PlayMatchOptions option) {
    switch (option) {
      case PlayMatchOptions.resume:
        // do nothing, the options screen will always return
        break;
      case PlayMatchOptions.end_match:
        // navigate away from this screen
        Navigator.pushNamed(context, widget.getEndingRoute());
        break;
      case PlayMatchOptions.show_history:
      case PlayMatchOptions.show_settings:
      case PlayMatchOptions.show_match_settings:
        Log.error('not implemented this yet');
        break;
    }
    // and hide the options screen
    _showPauseOptions(false);
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
                  position: _messageOffset,
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
              Align(
                alignment: Alignment.bottomRight,
                child: Padding(
                  padding: EdgeInsets.only(
                      right: Values.default_space,
                      bottom: Values.team_names_widget_height),
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.end,
                    children: [
                      FloatingActionButton(
                        heroTag: ValueKey<String>('undo_point'),
                        onPressed: () => _undoLastPoint(match),
                        child: Icon(Icons.undo),
                      ),
                      SizedBox(width: Values.default_space),
                      FloatingActionButton(
                        heroTag: ValueKey<String>('pause_match'),
                        onPressed: () => _showPauseOptions(true),
                        child: Icon(Icons.more_vert),
                      ),
                    ],
                  ),
                ),
              ),
              Align(
                alignment: Alignment.bottomRight,
                child: SlideTransition(
                  position: _optionsOffset,
                  child: Padding(
                    padding: EdgeInsets.only(
                        top: Values.team_names_widget_height,
                        right: Values.default_space,
                        bottom: Values.team_names_widget_height),
                    child: PlayMatchOptionsWidget(
                      sportSvgPath: match.getSport().icon,
                      matchDescription:
                          match.getDescription(DescriptionLevel.SHORT, context),
                      teamOneName: match
                          .getSetup()
                          .getTeamName(TeamIndex.T_ONE, context),
                      teamTwoName: match
                          .getSetup()
                          .getTeamName(TeamIndex.T_TWO, context),
                      onOptionSelected: _onMatchOptionSelected,
                    ),
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
