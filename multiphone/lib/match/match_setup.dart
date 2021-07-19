import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/tennis_match_setup.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/player.dart';
import 'package:multiphone/providers/sport.dart';

enum SINGLES_DOUBLES {
  DOUBLES,
  SINGLES,
}

abstract class MatchSetup with ChangeNotifier {
  SINGLES_DOUBLES _singlesDoubles = SINGLES_DOUBLES.SINGLES;

  final List<String> _playerNames =
      List<String>.filled(PlayerIndex.values.length, '');

  MatchSetup() {}

  // need to create the proper one for the active match as it changes
  static MatchSetup create(ActiveMatch match) {
    if (match == null || match.sport == null) {
      return null;
    }
    switch (match.sport.id) {
      case SportType.TENNIS:
        return TennisMatchSetup();
        break;
      case SportType.BADMINTON:
      case SportType.PING_PONG:
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
    final String name = _playerNames[player.index];
    if (name == null || name.isEmpty) {
      switch (player) {
        case PlayerIndex.P_ONE:
          return Values(context).strings.player_one;
        case PlayerIndex.P_TWO:
          return Values(context).strings.player_two;
        case PlayerIndex.PT_ONE:
          return Values(context).strings.partner_one;
        case PlayerIndex.PT_TWO:
          return Values(context).strings.partner_two;
      }
    }
    return name;
  }

  get singlesDoubles {
    return _singlesDoubles;
  }

  set singlesDoubles(SINGLES_DOUBLES singlesDoubles) {
    _singlesDoubles = singlesDoubles;
    // this is a change
    notifyListeners();
  }

  String matchSummary(BuildContext context);
}
