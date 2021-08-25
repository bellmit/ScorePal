import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:multiphone/controllers/controller_listener.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/match_play_tracker.dart';
import 'package:multiphone/match/match_writer.dart';
import 'package:multiphone/match/score_state.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/controllers/controllers.dart';
import 'package:multiphone/screens/change_match_setup_screen.dart';
import 'package:multiphone/screens/playing_team_widget.dart';
import 'package:multiphone/screens/settings_screen.dart';
import 'package:multiphone/widgets/common/confirm_dialog.dart';
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
    with TickerProviderStateMixin, ControllerListener {
  String _description = '';
  MatchPlayTracker _playTracker;

  AnimationController _messageController;
  Animation<Offset> _messageOffset;

  AnimationController _optionsController;
  Animation<Offset> _optionsOffset;

  Controllers _controllersProvider;

  @override
  void initState() {
    super.initState();

    // get the match as-is to track it (it will change but there will only be one)
    ActiveMatch match = Provider.of<ActiveMatch>(context, listen: false);
    // new match - new tracker
    _playTracker = MatchPlayTracker(match);
    // create the controllers we will use to track things
    _controllersProvider = new Controllers(context);
    // and listen to it
    _controllersProvider.registerControllerListener(this);

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
    // release the lock
    Wakelock.disable();
    // and put the overlays back
    SystemChrome.setEnabledSystemUIOverlays(
        [SystemUiOverlay.bottom, SystemUiOverlay.top]);
  }

  @override
  void deactivate() {
    // release the locks for the screen
    _releaseLocks();
    super.deactivate();
  }

  @override
  void dispose() {
    // release all the created things
    _messageController.dispose();
    _optionsController.dispose();
    if (null != _controllersProvider) {
      // release ourselves as a listener on this
      _controllersProvider.releaseControllerListener(this);
      _controllersProvider.dispose();
      _controllersProvider = null;
    }
    _playTracker = null;
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

  @override
  void onButtonPressed({ClickAction action}) {
    // button was pressed, from the pattern process and handle it
    switch (action) {
      case ClickAction.pointTeamOne:
        _processScoreChange(_playTracker.match, TeamIndex.T_ONE, 0);
        break;
      case ClickAction.pointTeamTwo:
        _processScoreChange(_playTracker.match, TeamIndex.T_TWO, 0);
        break;
      case ClickAction.pointServer:
        _processScoreChange(
            _playTracker.match, _playTracker.match.getServingTeam(), 0);
        break;
      case ClickAction.pointReceiver:
        _processScoreChange(
            _playTracker.match,
            _playTracker.match
                .getSetup()
                .getOtherTeam(_playTracker.match.getServingTeam()),
            0);
        break;
      case ClickAction.undoLast:
        _undoLastPoint(_playTracker.match);
        break;
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

  void _onMatchOptionSelected(PlayMatchOptions option, BuildContext context) {
    switch (option) {
      case PlayMatchOptions.clear:
        final values = Values(context);
        confirmDialog(
          context,
          title: values.strings.match_clear,
          content: values.strings.match_clear_confirm,
          textOK: values.strings.confirm_yes,
          textCancel: values.strings.confirm_no,
        ).then((value) {
          if (value) {
            // they said yes
            _playTracker.clearMatchData();
          }
        });
        break;
      case PlayMatchOptions.resume:
        // do nothing, the options screen will always return
        break;
      case PlayMatchOptions.end_match:
        // navigate away from this screen
        MatchPlayTracker.navTo(widget.getEndingRoute(), context);
        break;
      case PlayMatchOptions.show_history:
        break;
      case PlayMatchOptions.show_settings:
        // jump to the app settings without showing the side bar so they have to come back after
        MatchPlayTracker.navTo(SettingsScreen.routeName, context,
            arguments: {SettingsScreen.argShowSidebar: false});
        break;
      case PlayMatchOptions.show_match_settings:
        // show the settings without the option to change sports or play new
        MatchPlayTracker.navTo(ChangeMatchSetupScreen.routeName, context);
        break;
    }
    // and hide the options screen
    _showPauseOptions(false);
  }

  Widget _createScoreDisplay(ActiveMatch match, Orientation orientation) {
    if (orientation == Orientation.portrait) {
      return Column(
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
                    (level) =>
                        _processScoreChange(match, TeamIndex.T_ONE, level),
                  ),
                ),
                Flexible(
                  child: widget.createScoreWidget(
                    match,
                    TeamIndex.T_TWO,
                    (level) =>
                        _processScoreChange(match, TeamIndex.T_TWO, level),
                  ),
                ),
              ],
            ),
          ),
          // this is the bar for team two player and partner
          PlayingTeamWidget(match: match, team: TeamIndex.T_TWO),
        ],
      );
    } else {
      return Row(
        children: [
          Flexible(
            flex: 1,
            child: Column(
              children: [
                PlayingTeamWidget(match: match, team: TeamIndex.T_ONE),
                Expanded(
                  child: widget.createScoreWidget(
                    match,
                    TeamIndex.T_ONE,
                    (level) =>
                        _processScoreChange(match, TeamIndex.T_ONE, level),
                  ),
                ),
              ],
            ),
          ),
          Flexible(
            flex: 1,
            child: Column(
              children: [
                PlayingTeamWidget(match: match, team: TeamIndex.T_TWO),
                Expanded(
                  child: widget.createScoreWidget(
                    match,
                    TeamIndex.T_TWO,
                    (level) =>
                        _processScoreChange(match, TeamIndex.T_TWO, level),
                  ),
                ),
              ],
            ),
          ),
        ],
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    // To make this screen full screen.
    // It will hide status bar and notch.
    SystemChrome.setEnabledSystemUIOverlays([]);
    final theme = Theme.of(context);
    final values = Values(context);
    return Scaffold(
      body: OrientationBuilder(
        builder: (ctx, orientation) {
          return Consumer<ActiveMatch>(
            builder: (ctx, match, child) {
              return Stack(
                children: [
                  _createScoreDisplay(match, orientation),
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
                              color: theme.primaryColor,
                            )),
                      ),
                    ),
                  ),
                  Align(
                    alignment: Alignment.bottomRight,
                    child: Padding(
                      padding: EdgeInsets.only(
                          right: Values.default_space,
                          bottom: orientation == Orientation.portrait
                              // when we are in portrait, the buttons want to be above the bottom team's name
                              ? Values.team_names_widget_height
                              : Values.default_space),
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
                  if (match.isMatchOver())
                    Padding(
                      padding: EdgeInsets.only(
                          top: Values.team_names_widget_height,
                          right: Values.default_space,
                          bottom: Values.team_names_widget_height +
                              Values.default_space +
                              (orientation == Orientation.portrait
                                  // when we are in portrait, the buttons want to be above the bottom team's name
                                  ? Values.team_names_widget_height
                                  : Values.default_space)),
                      child: Align(
                        alignment: Alignment.bottomRight,
                        child: Card(
                          color: Theme.of(context).primaryColorLight,
                          child: Padding(
                            padding: const EdgeInsets.all(Values.default_space),
                            child: ElevatedButton.icon(
                              style: ElevatedButton.styleFrom(
                                primary: theme.accentColor,
                                onPrimary: theme.primaryColorDark,
                              ),
                              onPressed: () => _onMatchOptionSelected(
                                  PlayMatchOptions.end_match, context),
                              icon: Icon(
                                Icons.stop,
                                size: Values.image_large,
                              ),
                              label: Text(values.strings.match_end),
                            ),
                          ),
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
                          matchDescription: match.getDescription(
                              DescriptionLevel.SHORT, context),
                          teamOneName: match
                              .getSetup()
                              .getTeamName(TeamIndex.T_ONE, context),
                          teamTwoName: match
                              .getSetup()
                              .getTeamName(TeamIndex.T_TWO, context),
                          onOptionSelected: (value) =>
                              _onMatchOptionSelected(value, context),
                        ),
                      ),
                    ),
                  ),
                ],
              );
            },
          );
        },
      ),
    );
  }
}
