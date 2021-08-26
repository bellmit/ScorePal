import 'package:flutter/material.dart';
import 'package:multiphone/helpers/setup_persistence.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/badminton/badminton_match_setup.dart';
import 'package:multiphone/providers/active_selection.dart';
import 'package:multiphone/providers/player.dart';
import 'package:multiphone/widgets/common/info_bar_widget.dart';
import 'package:multiphone/widgets/player_names_widget.dart';
import 'package:multiphone/widgets/badminton/select_games_widget.dart';
import 'package:multiphone/widgets/badminton/select_points_widget.dart';
import 'package:provider/provider.dart';

class SetupBadmintonWidget extends StatefulWidget {
  final bool isLoadSetup;
  const SetupBadmintonWidget({Key key, @required this.isLoadSetup})
      : super(key: key);

  @override
  _SetupBadmintonWidgetState createState() => _SetupBadmintonWidgetState();
}

class _SetupBadmintonWidgetState extends State<SetupBadmintonWidget> {
  BadmintonMatchSetup _setup;

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
    _setup = Provider.of<ActiveSelection>(context, listen: false)
        .getSelectedSetup(true) as BadmintonMatchSetup;
    if (null != _setup && widget.isLoadSetup) {
      SetupPersistence().loadLastSetupData(_setup).then((value) {
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
          Padding(
            padding: EdgeInsets.all(Values.default_space),
            child: SelectGamesWidget(
              games: _setup.games,
              onGamesChanged: (value) => _setup.games = value,
            ),
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
                  Values(context).strings.badminton_number_points_per_game,
                ),
                SelectPointsWidget(
                  points: _setup.points,
                  onPointsChanged: (value) => _setup.points = value,
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
