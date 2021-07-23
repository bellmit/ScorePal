import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:multiphone/match/match_setup.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/team.dart';
import 'package:multiphone/widgets/info_bar_widget.dart';
import 'package:multiphone/widgets/tennis/tennis_score_widget.dart';
import 'package:provider/provider.dart';

abstract class PlayMatchScreen extends StatelessWidget {
  PlayMatchScreen();

  Widget createScoreWidget(TeamIndex teamIndex);

  @override
  Widget build(BuildContext context) {
    // To make this screen full screen.
    // It will hide status bar and notch.
    SystemChrome.setEnabledSystemUIOverlays([]);

    return Scaffold(
      body: Consumer<MatchSetup>(
        builder: (ctx, setup, child) {
          // when we have the setup we can setup the static(ish) data on this screen
          return Column(
            children: [
              InfoBarWidget(title: setup.getTeamName(TeamIndex.T_ONE, context)),
              child,
              InfoBarWidget(title: setup.getTeamName(TeamIndex.T_TWO, context)),
            ],
          );
        },
        // the main screen, outside of the setup consumer, shows the match details
        child: Consumer<ActiveMatch>(builder: (ctx, match, child) {
          return Expanded(
            child: Column(
              children: [
                Flexible(
                  child: createScoreWidget(TeamIndex.T_ONE),
                ),
                Flexible(
                  child: createScoreWidget(TeamIndex.T_TWO),
                ),
              ],
            ),
          );
        }),
      ),
    );
  }
}
