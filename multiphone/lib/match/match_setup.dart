import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/badminton_match_setup.dart';
import 'package:multiphone/match/ping_pong_match_setup.dart';
import 'package:multiphone/match/tennis_match_setup.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/player.dart';
import 'package:multiphone/providers/sport.dart';
import 'package:multiphone/providers/team.dart';

enum MatchSinglesDoubles {
  doubles,
  singles,
}

abstract class MatchSetup with ChangeNotifier {
  MatchSinglesDoubles _singlesDoubles = MatchSinglesDoubles.singles;

  final List<String> _playerNames =
      List<String>.filled(PlayerIndex.values.length, '');

  PlayerIndex _startingServer = PlayerIndex.P_ONE;

  MatchSetup();

  // need to create the proper one for the active match as it changes
  static MatchSetup create(ActiveMatch match) {
    if (match == null || match.sport == null) {
      return null;
    }
    switch (match.sport.id) {
      case SportType.TENNIS:
        return TennisMatchSetup();
      case SportType.BADMINTON:
        return BadmintonMatchSetup();
      case SportType.PING_PONG:
        return PingPongMatchSetup();
      default:
        print('do the setup for the sport type of ${match.sport.id}');
        break;
    }
    return null;
  }

  void setPlayerName(PlayerIndex player, String name) {
    _playerNames[player.index] = name.trim();
    // this is a change
    notifyListeners();
  }

  String getPlayerName(PlayerIndex player, BuildContext context) {
    String name = _playerNames[player.index];
    if (name == null || name.isEmpty) {
      switch (player) {
        case PlayerIndex.P_ONE:
          name = Values(context).strings.player_one;
          break;
        case PlayerIndex.P_TWO:
          name = Values(context).strings.player_two;
          break;
        case PlayerIndex.PT_ONE:
          name = Values(context).strings.partner_one;
          break;
        case PlayerIndex.PT_TWO:
          name = Values(context).strings.partner_two;
          break;
      }
    }
    if (_startingServer == player) {
      name += ' (*)';
    }
    return name;
  }

  String getTeamName(TeamIndex team, BuildContext context) {
    if (singlesDoubles == MatchSinglesDoubles.singles) {
      // just do the player name
      return getPlayerName(
          team == TeamIndex.T_ONE ? PlayerIndex.P_ONE : PlayerIndex.P_TWO,
          context);
    } else if (team == TeamIndex.T_ONE) {
      // do doubles names for team one
      return '${getPlayerName(PlayerIndex.P_ONE, context)} / ${getPlayerName(PlayerIndex.PT_ONE, context)}';
    } else {
      return '${getPlayerName(PlayerIndex.P_TWO, context)} / ${getPlayerName(PlayerIndex.PT_TWO, context)}';
    }
  }

  get startingServer {
    return _startingServer;
  }

  set startingServer(PlayerIndex server) {
    _startingServer = server;
    // this is a change
    notifyListeners();
  }

  get singlesDoubles {
    return _singlesDoubles;
  }

  set singlesDoubles(MatchSinglesDoubles singlesDoubles) {
    _singlesDoubles = singlesDoubles;
    // this is a change
    notifyListeners();
  }

  String matchSummary(BuildContext context);
}
