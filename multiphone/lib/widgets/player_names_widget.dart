import 'package:flutter/material.dart';
import 'package:flutter_contacts/contact.dart';
import 'package:flutter_contacts/flutter_contacts.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/match_setup.dart';
import 'package:multiphone/providers/player.dart';
import 'package:multiphone/widgets/player_name_widget.dart';
import 'package:multiphone/widgets/select_singles_doubles_widget.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:provider/provider.dart';

class PlayerNamesWidget extends StatefulWidget {
  const PlayerNamesWidget({Key key}) : super(key: key);

  @override
  _PlayerNamesWidgetState createState() => _PlayerNamesWidgetState();
}

class _PlayerNamesWidgetState extends State<PlayerNamesWidget>
    with TickerProviderStateMixin {
  bool _showPartners = false;

  AnimationController _controller;

  @override
  void initState() {
    super.initState();
    // animation things (this can have the mixin of SingleTickerProviderStateMixin)
    _controller = AnimationController(
      vsync: this,
      duration: Duration(milliseconds: Values.animation_duration_ms),
    );
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  void _onPlayerNameChanged(String playerName, PlayerIndex playerIndex) {
    // update the player name in the settings
    Provider.of<MatchSetup>(context, listen: false)
        .setPlayerName(playerIndex, playerName);
  }

  Widget _createPlayerEntries(List<Contact> contacts) {
    return Column(
      children: [
        PlayerNameWidget(
          hintText: Values(context).strings.player_one,
          onTextChanged: (newName) =>
              _onPlayerNameChanged(newName, PlayerIndex.P_ONE),
          availableOpponents: contacts,
        ),
        AnimatedContainer(
          constraints: BoxConstraints(
            maxHeight: _showPartners ? 500 : 0,
          ),
          duration: Duration(milliseconds: Values.animation_duration_ms),
          curve: Curves.easeInOut,
          child: PlayerNameWidget(
            hintText: Values(context).strings.partner_one,
            onTextChanged: (newName) =>
                _onPlayerNameChanged(newName, PlayerIndex.PT_ONE),
            availableOpponents: contacts,
          ),
        ),
        PlayerNameWidget(
          hintText: Values(context).strings.player_two,
          onTextChanged: (newName) =>
              _onPlayerNameChanged(newName, PlayerIndex.P_TWO),
          availableOpponents: contacts,
        ),
        AnimatedContainer(
          constraints: BoxConstraints(
            maxHeight: _showPartners ? 500 : 0,
          ),
          duration: Duration(milliseconds: Values.animation_duration_ms),
          curve: Curves.easeInOut,
          child: PlayerNameWidget(
            hintText: Values(context).strings.partner_two,
            onTextChanged: (newName) =>
                _onPlayerNameChanged(newName, PlayerIndex.PT_TWO),
            availableOpponents: contacts,
          ),
        ),
      ],
    );
  }

  Future<List<Contact>> _getContacts() async {
    final isGranted = await Permission.contacts.request().isGranted;
    if (!isGranted) {
      // no contacts allowed
      return [];
    } else {
      return await FlutterContacts.getContacts();
    }
  }

  void _showPartnerNames(bool isShowPartnerNames) {
    setState(() {
      _showPartners = isShowPartnerNames;
    });
  }

  @override
  Widget build(BuildContext context) {
    // get the sport for the icon etc
    return Container(
        width: double.infinity,
        child: Column(
          children: [
            Padding(
              padding: EdgeInsets.all(Values.default_space),
              child: SelectSinglesDoublesWidget(
                onSinglesDoublesChanged: (value) =>
                    _showPartnerNames(value == SINGLES_DOUBLES.DOUBLES),
              ),
            ),
            FutureBuilder(
                future: _getContacts(),
                builder: (ctx, snapshot) {
                  if (snapshot.connectionState == ConnectionState.done) {
                    // we are done getting the contacts, create the column of things with them
                    return _createPlayerEntries(snapshot.data);
                  } else {
                    // we aren't done, create the same but without autocomplete working
                    return _createPlayerEntries(snapshot.data);
                  }
                }),
          ],
        ));
  }
}
