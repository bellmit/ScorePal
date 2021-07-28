import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/tennis/tennis_match_setup.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/providers/player.dart';
import 'package:multiphone/widgets/common/info_bar_widget.dart';
import 'package:multiphone/widgets/player_names_widget.dart';
import 'package:multiphone/widgets/tennis/select_games_widget.dart';
import 'package:multiphone/widgets/tennis/select_sets_widget.dart';
import 'package:multiphone/widgets/tennis/select_sudden_death_widget.dart';
import 'package:multiphone/widgets/tennis/select_tie_final_widget.dart';
import 'package:provider/provider.dart';

class SetupTennisWidget extends StatefulWidget {
  const SetupTennisWidget({Key key}) : super(key: key);

  @override
  _SetupTennisWidgetState createState() => _SetupTennisWidgetState();
}

class _SetupTennisWidgetState extends State<SetupTennisWidget> {
  TennisMatchSetup _setup;

  Widget _createAdvancedTitle(String title) {
    return Expanded(
      child: Padding(
        padding: const EdgeInsets.only(right: Values.default_space),
        child: Text(title,
            textAlign: TextAlign.right,
            style: TextStyle(
              fontSize: Values.font_size_title,
              color: Theme.of(context).primaryColorDark,
            )),
      ),
    );
  }

  @override
  void initState() {
    super.initState();
    // get our setup
    _setup =
        Provider.of<ActiveSetup>(context, listen: false) as TennisMatchSetup;
    if (null != _setup) {
      _setup.loadLastSetupData().then((value) {
        setState(() {
          _setup = value;
        });
      });
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
      child: Column(
        // we need a key for the data that changes when the setup changes
        key: ValueKey<String>(_setup.id),
        children: [
          Row(
            children: <Widget>[
              // move over a bit because the parent adds the icon and settings
              // text to explain what this card is about
              SizedBox(
                width: Values.image_large,
              ),
              Padding(
                padding: EdgeInsets.all(Values.default_space),
                child: SelectSetsWidget(
                  sets: _setup.sets,
                  onSetsChanged: (newSets) => _setup.sets = newSets,
                ),
              ),
            ],
          ),
          PlayerNamesWidget(
            playerNames: [
              _setup.getPlayerName(PlayerIndex.P_ONE, context),
              _setup.getPlayerName(PlayerIndex.P_TWO, context),
              _setup.getPlayerName(PlayerIndex.PT_ONE, context),
              _setup.getPlayerName(PlayerIndex.PT_TWO, context),
            ],
            startingServer: _setup.startingServer,
            singlesDoubles: _setup.singlesDoubles,
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
                  games: _setup.games,
                  onGamesChanged: (newGames) => _setup.games = newGames,
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
                  isSuddenDeath: _setup.isSuddenDeathOnDeuce,
                  onSuddenDeathChanged: (value) =>
                      _setup.isSuddenDeathOnDeuce = value,
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
                  isTieInFinalSet: _setup.tieInFinalSet,
                  onTieChanged: (value) => _setup.tieInFinalSet = value,
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
