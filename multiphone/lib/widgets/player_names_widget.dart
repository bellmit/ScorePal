import 'package:flutter/material.dart';
import 'package:flutter_contacts/contact.dart';
import 'package:flutter_contacts/flutter_contacts.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/providers/player.dart';
import 'package:multiphone/widgets/common/info_bar_widget.dart';
import 'package:multiphone/widgets/player_name_widget.dart';
import 'package:multiphone/widgets/select_singles_doubles_widget.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:provider/provider.dart';

class PlayerNamesWidget extends StatefulWidget {
  final List<String> playerNames;
  final PlayerIndex startingServer;
  final MatchSinglesDoubles singlesDoubles;
  const PlayerNamesWidget({
    Key key,
    @required this.playerNames,
    @required this.startingServer,
    @required this.singlesDoubles,
  }) : super(key: key);

  @override
  _PlayerNamesWidgetState createState() => _PlayerNamesWidgetState(
      startingServer, singlesDoubles == MatchSinglesDoubles.doubles);
}

class _PlayerNamesWidgetState extends State<PlayerNamesWidget>
    with TickerProviderStateMixin {
  bool _showPartners;
  PlayerIndex _servingPlayer = PlayerIndex.P_ONE;
  PlayerIndex _playerAccountUser = null;

  AnimationController _controller;
  Animation<Offset> _slideAnimation;
  Animation<double> _opacityAnimation;

  _PlayerNamesWidgetState(this._servingPlayer, this._showPartners);

  @override
  void initState() {
    super.initState();
    // animation things (this can have the mixin of SingleTickerProviderStateMixin)
    _controller = AnimationController(
      vsync: this,
      duration: Duration(milliseconds: 500),
    );
    _slideAnimation = Tween<Offset>(
      begin: Offset(-2, 0),
      end: Offset(0, 0),
    ).animate(CurvedAnimation(
      parent: _controller,
      curve: Curves.linear,
    ));
    _opacityAnimation = Tween<double>(
      begin: 0,
      end: 1.0,
    ).animate(CurvedAnimation(
      parent: _controller,
      curve: Curves.easeInOut,
    ));
    // get who the account user is
    _playerAccountUser =
        Provider.of<ActiveSetup>(context, listen: false).getAccountUserPlayer();
    _showPartnerNames(_showPartners);
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  void _onPlayerNameChanged(String playerName, PlayerIndex playerIndex) {
    // update the player name in the settings
    final setup = Provider.of<ActiveSetup>(context, listen: false);
    setup.setPlayerName(playerIndex, playerName);
    _playerAccountUser = setup.getAccountUserPlayer();
  }

  void _onPlayerContactSet(Contact contact, PlayerIndex playerIndex) {
    List<String> emails = [];
    if (contact != null && contact.emails != null) {
      // get the contact's emails to inform about these match results when
      // we are done too
      for (Email email in contact.emails) {
        if (email.isPrimary) {
          // be sure to place the primary first
          emails.insert(0, email.address);
        } else {
          // all others below
          emails.add(email.address);
        }
      }
    }
    // set these emails on the settings for the player
    Provider.of<ActiveSetup>(context, listen: false)
        .setPlayerEmails(playerIndex, emails);
  }

  void _onSinglesDoublesChanged(MatchSinglesDoubles singlesDoubles) {
    // update the player name in the settings
    Provider.of<ActiveSetup>(context, listen: false).singlesDoubles =
        singlesDoubles;
  }

  void _onServerSelected(PlayerIndex playerIndex) {
    // update the starting server in the settings
    var setup = Provider.of<ActiveSetup>(context, listen: false);
    // set the server to start
    setup.startingServer = playerIndex;
    // but this just sets the starting server for the team they are in,
    // from the setup we also want to set their team starting
    setup.firstServingTeam = setup.getPlayerTeam(playerIndex);
    // and change our state to match this
    setState(() {
      _servingPlayer = playerIndex;
    });
  }

  Widget _createPlayerEntries(List<Contact> contacts) {
    return Column(
      children: [
        InfoBarWidget(title: Values(context).strings.team_one),
        PlayerNameWidget(
          initialText: widget.playerNames[PlayerIndex.P_ONE.index],
          hintText: Values(context).strings.player_one,
          onTextChanged: (newName) =>
              _onPlayerNameChanged(newName, PlayerIndex.P_ONE),
          onContactSelected: (contact) =>
              _onPlayerContactSet(contact, PlayerIndex.P_ONE),
          isPlayerUser: _playerAccountUser == PlayerIndex.P_ONE,
          isPlayerServer: _servingPlayer == PlayerIndex.P_ONE,
          onPlayerSelectedToServe: () => _onServerSelected(PlayerIndex.P_ONE),
          availableOpponents: contacts,
        ),
        AnimatedContainer(
          constraints: BoxConstraints(
            maxHeight: _showPartners ? 500 : 0,
          ),
          duration: Duration(milliseconds: Values.animation_duration_ms),
          curve: Curves.easeInOut,
          child: FadeTransition(
            opacity: _opacityAnimation,
            child: SlideTransition(
              position: _slideAnimation,
              child: PlayerNameWidget(
                initialText: widget.playerNames[PlayerIndex.PT_ONE.index],
                hintText: Values(context).strings.partner_one,
                onTextChanged: (newName) =>
                    _onPlayerNameChanged(newName, PlayerIndex.PT_ONE),
                onContactSelected: (contact) =>
                    _onPlayerContactSet(contact, PlayerIndex.PT_ONE),
                isPlayerUser: _playerAccountUser == PlayerIndex.PT_ONE,
                isPlayerServer: _servingPlayer == PlayerIndex.PT_ONE,
                onPlayerSelectedToServe: () =>
                    _onServerSelected(PlayerIndex.PT_ONE),
                availableOpponents: contacts,
              ),
            ),
          ),
        ),
        InfoBarWidget(title: Values(context).strings.team_two),
        PlayerNameWidget(
          initialText: widget.playerNames[PlayerIndex.P_TWO.index],
          hintText: Values(context).strings.player_two,
          onTextChanged: (newName) =>
              _onPlayerNameChanged(newName, PlayerIndex.P_TWO),
          onContactSelected: (contact) =>
              _onPlayerContactSet(contact, PlayerIndex.P_TWO),
          isPlayerUser: _playerAccountUser == PlayerIndex.P_TWO,
          isPlayerServer: _servingPlayer == PlayerIndex.P_TWO,
          onPlayerSelectedToServe: () => _onServerSelected(PlayerIndex.P_TWO),
          availableOpponents: contacts,
        ),
        AnimatedContainer(
          constraints: BoxConstraints(
            maxHeight: _showPartners ? 500 : 0,
          ),
          duration: Duration(milliseconds: Values.animation_duration_ms),
          curve: Curves.easeInOut,
          child: FadeTransition(
            opacity: _opacityAnimation,
            child: SlideTransition(
              position: _slideAnimation,
              child: PlayerNameWidget(
                initialText: widget.playerNames[PlayerIndex.PT_TWO.index],
                hintText: Values(context).strings.partner_two,
                onTextChanged: (newName) =>
                    _onPlayerNameChanged(newName, PlayerIndex.PT_TWO),
                onContactSelected: (contact) =>
                    _onPlayerContactSet(contact, PlayerIndex.PT_TWO),
                isPlayerUser: _playerAccountUser == PlayerIndex.PT_TWO,
                isPlayerServer: _servingPlayer == PlayerIndex.PT_TWO,
                onPlayerSelectedToServe: () =>
                    _onServerSelected(PlayerIndex.PT_TWO),
                availableOpponents: contacts,
              ),
            ),
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
      return await FlutterContacts.getContacts(
          withThumbnail: true, withProperties: true);
    }
  }

  void _showPartnerNames(bool isShowPartnerNames) {
    setState(() {
      _showPartners = isShowPartnerNames;
    });
    if (isShowPartnerNames) {
      // start the height animation to change
      _controller.forward();
    } else {
      _controller.reverse();
    }
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
                onSinglesDoublesChanged: (value) {
                  // put this data back on the setup
                  _onSinglesDoublesChanged(value);
                  // properly show the partner names
                  _showPartnerNames(value == MatchSinglesDoubles.doubles);
                },
                singlesDoubles: _showPartners
                    ? MatchSinglesDoubles.doubles
                    : MatchSinglesDoubles.singles,
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
