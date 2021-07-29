import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
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

class _PlayMatchScreenState extends State<PlayMatchScreen> {
  bool _showScoreChange = false;
  String _description = '';

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
    // change this state then
    setState(() {
      _description = description;
      _showScoreChange = _description != null && _description.isNotEmpty;
    });
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
              if (_showScoreChange)
                Center(
                  child: Container(
                    padding: EdgeInsets.all(Values.default_space),
                    width: double.infinity,
                    height: Values.image_large * 2,
                    child: Card(
                      borderOnForeground: true,
                      child: Center(
                        child: Text(_description),
                      ),
                      color: Theme.of(context).primaryColor,
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
