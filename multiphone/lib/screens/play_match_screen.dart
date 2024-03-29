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
import 'package:multiphone/screens/match_history_screen.dart';
import 'package:multiphone/screens/playing_team_widget.dart';
import 'package:multiphone/screens/settings_screen.dart';
import 'package:multiphone/widgets/common/common_widgets.dart';
import 'package:multiphone/widgets/common/confirm_dialog.dart';
import 'package:multiphone/widgets/common/icon_button_widget.dart';
import 'package:multiphone/widgets/current_time_widget.dart';
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

  void _onTeamClicked(ActiveMatch match, TeamIndex team) {
    final setup = match.getSetup();
    if (setup.singlesDoubles == MatchSinglesDoubles.singles) {
      if (match.isMatchPlayStarted()) {
        // can't change servers once started
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(
          content: TextWidget(Values(context).strings.error_change_server),
        ));
      } else if (match.getServingTeam() != team) {
        // they clicked the other team, change the server for singles then
        setup.firstServingTeam = team;
      }
    } else if (match.score.isTeamServerChangeAllowed(team)) {
      // we are in doubles and this team server change is allowed
      if (!match.isMatchPlayStarted() && match.getServingTeam() != team) {
        // we want to change the serving team
        setup.firstServingTeam = team;
      } else {
        // we want to change the first server in the current team
        final currentServer = match.getServingPlayer();
        // so use the other
        match.setFirstServingPlayer(team, setup.getOtherPlayer(currentServer));
      }
    } else {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(
        content: TextWidget(Values(context).strings.error_change_server),
      ));
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

  void _onMatchOptionSelected(
      ActiveSetup setup, PlayMatchOptions option, BuildContext context) {
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
        // navigate away from this screen, first pause our key controller
        _activateKeyController(isActivate: false);
        // then nav away to the page and re-activate when we come back
        MatchPlayTracker.navTo(widget.getEndingRoute(), context)
            .then((value) => _activateKeyController());
        break;
      case PlayMatchOptions.show_history:
        // navigate away from this screen, first pause our key controller
        _activateKeyController(isActivate: false);
        // then nav away to the page and re-activate when we come back
        MatchPlayTracker.navTo(MatchHistoryScreen.routeName, context)
            .then((value) => _activateKeyController());
        break;
      case PlayMatchOptions.show_settings:
        // jump to the app settings without showing the side bar so they have to come back after
        // first pause our key controller
        _activateKeyController(isActivate: false);
        // then nav away to the page and re-activate when we come back
        MatchPlayTracker.navTo(SettingsScreen.routeName, context,
            arguments: {SettingsScreen.argShowSidebar: false}).then((value) {
          // activate the key controller
          _activateKeyController();
          // and update the setup as things might have changed
          setup.refreshSettings();
        });
        break;
      case PlayMatchOptions.show_match_settings:
        // show the settings without the option to change sports or play new
        // first pause our key controller
        _activateKeyController(isActivate: false);
        // then nav away to the page and re-activate when we come back
        MatchPlayTracker.navTo(ChangeMatchSetupScreen.routeName, context)
            .then((value) {
          // activate the key controller
          _activateKeyController();
          // and update the setup as things might have changed
          setup.refreshSettings();
        });
        break;
    }
    // and hide the options screen
    _showPauseOptions(false);
  }

  void _activateKeyController({bool isActivate = true}) {
    if (null != _controllersProvider) {
      _controllersProvider.activateSource(ClickSource.mediaButton,
          isActive: isActivate);
    }
  }

  Widget _createScoreDisplay(ActiveMatch match, Orientation orientation) {
    if (orientation == Orientation.portrait) {
      return Column(
        children: [
          // this is the bar for team one player and partner
          PlayingTeamWidget(
            match: match,
            team: TeamIndex.T_ONE,
            onTeamNameClicked: () => _onTeamClicked(match, TeamIndex.T_ONE),
          ),
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
          PlayingTeamWidget(
            match: match,
            team: TeamIndex.T_TWO,
            onTeamNameClicked: () => _onTeamClicked(match, TeamIndex.T_TWO),
          ),
        ],
      );
    } else {
      return Row(
        children: [
          Flexible(
            flex: 1,
            child: Column(
              children: [
                PlayingTeamWidget(
                  match: match,
                  team: TeamIndex.T_ONE,
                  onTeamNameClicked: () =>
                      _onTeamClicked(match, TeamIndex.T_ONE),
                ),
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
                PlayingTeamWidget(
                  match: match,
                  team: TeamIndex.T_TWO,
                  onTeamNameClicked: () =>
                      _onTeamClicked(match, TeamIndex.T_TWO),
                ),
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
    final values = Values(context);
    return Scaffold(
      body: OrientationBuilder(
        builder: (ctx, orientation) {
          return Consumer<ActiveMatch>(
            builder: (ctx, match, child) {
              // new match - new tracker
              if (null == _playTracker || _playTracker.match != match) {
                _playTracker = MatchPlayTracker(match, ctx);
              }
              return Stack(
                children: [
                  _createScoreDisplay(match, orientation),
                  if (_description != null && _description.isNotEmpty)
                    Align(
                      alignment: Alignment.center,
                      child: SlideTransition(
                        position: _messageOffset,
                        child: Center(
                          child: FractionallySizedBox(
                              widthFactor: 0.7,
                              child: Card(
                                color: Theme.of(context).accentColor,
                                child: Padding(
                                  padding: EdgeInsets.all(Values.default_space),
                                  child: FittedBox(
                                    fit: BoxFit.contain,
                                    child: TextWidget(_description,
                                        isOnBackground: true),
                                  ),
                                ),
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
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          CurrentTimeWidget(),
                          SizedBox(width: Values.default_space),
                          FloatingActionButton(
                            heroTag: ValueKey<String>('undo_point'),
                            onPressed: () => _undoLastPoint(match),
                            child: IconWidget(Icons.undo, size: null),
                          ),
                          SizedBox(width: Values.default_space),
                          FloatingActionButton(
                            heroTag: ValueKey<String>('pause_match'),
                            onPressed: () => _showPauseOptions(true),
                            child: IconWidget(Icons.more_vert, size: null),
                          ),
                        ],
                      ),
                    ),
                  ),
                  /*
                  _placeOverButtons(
                    context,
                    orientation,
                    CurrentTimeWidget(),
                  ),*/
                  if (match.isMatchOver())
                    _placeOverButtons(
                      context,
                      orientation,
                      Card(
                        color: Theme.of(context).secondaryHeaderColor,
                        child: Padding(
                          padding: const EdgeInsets.all(Values.default_space),
                          child: IconButtonWidget(
                            () => _onMatchOptionSelected(match.getSetup(),
                                PlayMatchOptions.end_match, context),
                            Icons.stop,
                            values.strings.match_end,
                          ),
                        ),
                      ),
                    ),
                  Align(
                    alignment: Alignment.bottomRight,
                    child: GestureDetector(
                      onPanUpdate: (details) {
                        // Swiping in right direction.
                        if (details.delta.dx > 0) {
                          _showPauseOptions(false);
                        }
                      },
                      child: SlideTransition(
                        position: _optionsOffset,
                        child: Padding(
                          padding: EdgeInsets.only(
                              top: Values.team_names_widget_height,
                              right: Values.default_space,
                              bottom: Values.team_names_widget_height),
                          child: PlayMatchOptionsWidget(
                            sportSvgPath: match.sport.icon,
                            matchDescription: match.getDescription(
                                DescriptionLevel.SHORT, context),
                            teamOneName: match
                                .getSetup()
                                .getTeamName(TeamIndex.T_ONE, context),
                            teamTwoName: match
                                .getSetup()
                                .getTeamName(TeamIndex.T_TWO, context),
                            onOptionSelected: (value) => _onMatchOptionSelected(
                                match.getSetup(), value, context),
                          ),
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

  Widget _placeOverButtons(
          BuildContext context, Orientation orientation, Widget child) =>
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
          child: child,
        ),
      );
}
