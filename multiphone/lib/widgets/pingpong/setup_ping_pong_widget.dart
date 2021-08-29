import 'package:flutter/material.dart';
import 'package:multiphone/helpers/setup_persistence.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/ping_pong/ping_pong_match_setup.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/providers/player.dart';
import 'package:multiphone/widgets/common/common_widgets.dart';
import 'package:multiphone/widgets/common/info_bar_widget.dart';
import 'package:multiphone/widgets/player_names_widget.dart';
import 'package:multiphone/widgets/pingpong/select_rounds_widget.dart';
import 'package:multiphone/widgets/pingpong/select_points_widget.dart';
import 'package:provider/provider.dart';

class SetupPingPongWidget extends StatefulWidget {
  final bool isLoadSetup;
  const SetupPingPongWidget({Key key, @required this.isLoadSetup})
      : super(key: key);

  @override
  _SetupPingPongWidgetState createState() => _SetupPingPongWidgetState();
}

class _SetupPingPongWidgetState extends State<SetupPingPongWidget> {
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
          if (!(setup is PingPongMatchSetup))
            return Text('setup is not ping pong');
          final pingPongSetup = setup as PingPongMatchSetup;
          return Column(
            children: [
              Padding(
                padding: EdgeInsets.all(Values.default_space),
                child: SelectRoundsWidget(
                  rounds: pingPongSetup.rounds,
                  onRoundsChanged: (value) => pingPongSetup.rounds = value,
                ),
              ),
              PlayerNamesWidget(
                playerNames: [
                  pingPongSetup.getPlayerName(PlayerIndex.P_ONE, context),
                  pingPongSetup.getPlayerName(PlayerIndex.P_TWO, context),
                  pingPongSetup.getPlayerName(PlayerIndex.PT_ONE, context),
                  pingPongSetup.getPlayerName(PlayerIndex.PT_TWO, context),
                ],
                startingServer: pingPongSetup.startingServer,
                singlesDoubles: pingPongSetup.singlesDoubles,
              ),
              InfoBarWidget(title: Values(context).strings.heading_advanced),
              Padding(
                padding: advancedHeadingPadding,
                child: Row(
                  children: [
                    _createAdvancedTitle(
                      Values(context).strings.ping_pong_number_points_per_round,
                    ),
                    SelectPointsWidget(
                      points: pingPongSetup.points,
                      onPointsChanged: (value) => pingPongSetup.points = value,
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
