import 'package:flutter/material.dart';
import 'package:multiphone/helpers/setup_persistence.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/ping_pong/ping_pong_match_setup.dart';
import 'package:multiphone/providers/active_selection.dart';
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
  PingPongMatchSetup _setup;

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
    // get our setup
    _setup = Provider.of<ActiveSelection>(context, listen: false)
        .getSelectedSetup(true) as PingPongMatchSetup;
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
            child: SelectRoundsWidget(
              rounds: _setup.rounds,
              onRoundsChanged: (value) => _setup.rounds = value,
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
                  Values(context).strings.ping_pong_number_points_per_round,
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
