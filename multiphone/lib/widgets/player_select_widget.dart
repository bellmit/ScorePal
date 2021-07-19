import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/match_setup.dart';
import 'package:multiphone/providers/player.dart';
import 'package:multiphone/widgets/contact_autocomplete_widget.dart';
import 'package:provider/provider.dart';

class PlayerSelectWidget extends StatefulWidget {
  final PlayerIndex playerIndex;
  const PlayerSelectWidget({Key key, @required this.playerIndex})
      : super(key: key);

  @override
  _PlayerSelectWidgetState createState() => _PlayerSelectWidgetState();
}

class _PlayerSelectWidgetState extends State<PlayerSelectWidget> {
  void _onPlayerNameChanged(String playerName) {
    // update the player name in the settings
    Provider.of<MatchSetup>(context, listen: false)
        .setPlayerName(widget.playerIndex, playerName);
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      height: Values.image_large,
      width: double.infinity,
      child: Row(
        children: [
          Container(
            height: Values.image_large,
            width: Values.image_large,
            decoration: BoxDecoration(
              color: const Color(0xff7c94b6),
              border: Border.all(
                color: Colors.black,
                width: 1.0,
              ),
              borderRadius: BorderRadius.circular(12),
            ),
          ),
          Expanded(
            child: ContactAutoCompleteWidget(
              onTextChanged: _onPlayerNameChanged,
            ),
          ),
        ],
      ),
    );
  }
}
