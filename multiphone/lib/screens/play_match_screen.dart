import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/widgets/common/info_bar_widget.dart';
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
      body: Consumer<ActiveSetup>(
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
        child: Consumer<ActiveMatch>(
          builder: (ctx, match, child) {
            return Expanded(
              child: Column(
                children: [
                  Flexible(
                    child: createScoreWidget(
                      match,
                      TeamIndex.T_ONE,
                      (level) => onScoreClicked(match, TeamIndex.T_ONE, level),
                    ),
                  ),
                  Flexible(
                    child: createScoreWidget(
                      match,
                      TeamIndex.T_TWO,
                      (level) => onScoreClicked(match, TeamIndex.T_TWO, level),
                    ),
                  ),
                ],
              ),
            );
          },
        ),
      ),
    );
  }
}
