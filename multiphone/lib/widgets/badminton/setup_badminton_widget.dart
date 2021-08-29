import 'package:flutter/material.dart';
import 'package:multiphone/helpers/setup_persistence.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/badminton/badminton_match_setup.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/providers/player.dart';
import 'package:multiphone/widgets/common/common_widgets.dart';
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
          if (!(setup is BadmintonMatchSetup))
            return Text('setup is not badminton');
          final badmintonSetup = setup as BadmintonMatchSetup;
          return Column(
            children: [
              Padding(
                padding: EdgeInsets.all(Values.default_space),
                child: SelectGamesWidget(
                  games: badmintonSetup.games,
                  onGamesChanged: (value) => badmintonSetup.games = value,
                ),
              ),
              PlayerNamesWidget(
                playerNames: [
                  badmintonSetup.getPlayerName(PlayerIndex.P_ONE, context),
                  badmintonSetup.getPlayerName(PlayerIndex.P_TWO, context),
                  badmintonSetup.getPlayerName(PlayerIndex.PT_ONE, context),
                  badmintonSetup.getPlayerName(PlayerIndex.PT_TWO, context),
                ],
                startingServer: badmintonSetup.startingServer,
                singlesDoubles: badmintonSetup.singlesDoubles,
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
                      points: badmintonSetup.points,
                      onPointsChanged: (value) => badmintonSetup.points = value,
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
