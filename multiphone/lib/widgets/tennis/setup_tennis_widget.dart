import 'package:flutter/material.dart';
import 'package:multiphone/helpers/setup_persistence.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/tennis/tennis_match_setup.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/providers/player.dart';
import 'package:multiphone/widgets/common/common_widgets.dart';
import 'package:multiphone/widgets/common/info_bar_widget.dart';
import 'package:multiphone/widgets/player_names_widget.dart';
import 'package:multiphone/widgets/tennis/select_games_widget.dart';
import 'package:multiphone/widgets/tennis/select_sets_widget.dart';
import 'package:multiphone/widgets/tennis/select_sudden_death_widget.dart';
import 'package:multiphone/widgets/tennis/select_tie_final_widget.dart';
import 'package:provider/provider.dart';

class SetupTennisWidget extends StatefulWidget {
  final bool isLoadSetup;
  const SetupTennisWidget({Key key, @required this.isLoadSetup})
      : super(key: key);

  @override
  _SetupTennisWidgetState createState() => _SetupTennisWidgetState();
}

class _SetupTennisWidgetState extends State<SetupTennisWidget> {
  Widget _createAdvancedTitle(String title) {
    return Expanded(
      child: Padding(
        padding: const EdgeInsets.only(right: Values.default_space),
        child: TextSubheadingWidget(title, textAlign: TextAlign.right),
      ),
    );
  }

  @override
  void initState() {
    super.initState();
    // do we need to load data into here?
    if (widget.isLoadSetup) {
      final setup = Provider.of<ActiveSetup>(context, listen: false);
      SetupPersistence().loadLastSetupData(setup);
    }
  }

  @override
  Widget build(BuildContext context) {
    // get the sport for the icon etc
    const advancedHeadingPadding = const EdgeInsets.only(
      left: Values.default_space,
      top: Values.default_space,
      right: Values.default_space,
    );
    return Container(
      width: double.infinity,
      child: Consumer<ActiveSetup>(
        builder: (ctx, setup, child) {
          if (!(setup is TennisMatchSetup)) return Text('setup is not tennis');
          final tennisSetup = setup as TennisMatchSetup;
          return Column(
            // we need a key for the data that changes when the setup changes
            key: ValueKey<String>(tennisSetup.id),
            children: [
              Padding(
                padding: EdgeInsets.all(Values.default_space),
                child: SelectSetsWidget(
                  sets: tennisSetup.sets,
                  onSetsChanged: (newSets) => tennisSetup.sets = newSets,
                ),
              ),
              PlayerNamesWidget(
                playerNames: [
                  tennisSetup.getPlayerName(PlayerIndex.P_ONE, context),
                  tennisSetup.getPlayerName(PlayerIndex.P_TWO, context),
                  tennisSetup.getPlayerName(PlayerIndex.PT_ONE, context),
                  tennisSetup.getPlayerName(PlayerIndex.PT_TWO, context),
                ],
                startingServer: tennisSetup.startingServer,
                singlesDoubles: tennisSetup.singlesDoubles,
              ),
              InfoBarWidget(title: Values(context).strings.heading_advanced),
              Padding(
                padding: advancedHeadingPadding,
                child: Row(
                  children: [
                    _createAdvancedTitle(
                      Values(context).strings.tennis_number_games_per_set,
                    ),
                    SelectGamesWidget(
                      games: tennisSetup.games,
                      onGamesChanged: (newGames) =>
                          tennisSetup.games = newGames,
                    ),
                  ],
                ),
              ),
              Padding(
                padding: advancedHeadingPadding,
                child: Row(
                  children: [
                    _createAdvancedTitle(
                      Values(context).strings.tennis_sudden_death_deuce,
                    ),
                    SelectSuddenDeathWidget(
                      isSuddenDeath: tennisSetup.isSuddenDeathOnDeuce,
                      onSuddenDeathChanged: (value) =>
                          tennisSetup.isSuddenDeathOnDeuce = value,
                    ),
                  ],
                ),
              ),
              Padding(
                padding: advancedHeadingPadding,
                child: Row(
                  children: [
                    _createAdvancedTitle(
                      Values(context).strings.tennis_final_tie,
                    ),
                    SelectTieFinalWidget(
                      isTieInFinalSet: tennisSetup.tieInFinalSet,
                      onTieChanged: (value) =>
                          tennisSetup.tieInFinalSet = value,
                    ),
                  ],
                ),
              ),
            ],
          );
        },
      ),
    );
  }
}
