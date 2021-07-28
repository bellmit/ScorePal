import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/screens/playing_team_widget.dart';
import 'package:provider/provider.dart';

abstract class PlayMatchScreen extends StatelessWidget {
  PlayMatchScreen();

  Widget createScoreWidget(ActiveMatch match, TeamIndex teamIndex,
      void Function(int level) onScoreClicked);

  void onScoreClicked(ActiveMatch match, TeamIndex team, int level);

  @override
  Widget build(BuildContext context) {
    // To make this screen full screen.
    // It will hide status bar and notch.
    SystemChrome.setEnabledSystemUIOverlays([]);

    return Scaffold(
      body: Consumer<ActiveMatch>(
        builder: (ctx, match, child) {
          return Column(
            children: [
              // this is the bar for team one player and partner
              PlayingTeamWidget(match: match, team: TeamIndex.T_ONE),
              Expanded(
                child: Column(
                  children: [
                    Flexible(
                      child: createScoreWidget(
                        match,
                        TeamIndex.T_ONE,
                        (level) =>
                            onScoreClicked(match, TeamIndex.T_ONE, level),
                      ),
                    ),
                    Flexible(
                      child: createScoreWidget(
                        match,
                        TeamIndex.T_TWO,
                        (level) =>
                            onScoreClicked(match, TeamIndex.T_TWO, level),
                      ),
                    ),
                  ],
                ),
              ),
              // this is the bar for team two player and partner
              PlayingTeamWidget(match: match, team: TeamIndex.T_TWO),
            ],
          );
        },
      ),
    );
  }
}
