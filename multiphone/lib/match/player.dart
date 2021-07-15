import 'package:flutter/foundation.dart';
import 'package:multiphone/match/team.dart';
import 'package:shared_preferences/shared_preferences.dart';

enum PlayerIndex {
  P_ONE,
  P_TWO,
  PT_ONE,
  PT_TWO,
}

class Players with ChangeNotifier {
  static final Player playerOne = Player(PlayerIndex.P_ONE, Teams.teamOne, "Player One",);
  static final Player playerTwo = Player(PlayerIndex.P_TWO, Teams.teamOne, "Player Two",);
  static final Player partnerOne = Player(PlayerIndex.PT_ONE, Teams.teamTwo, "Partner One",);
  static final Player partnerTwo = Player(PlayerIndex.PT_TWO, Teams.teamTwo, "Partner Two",);
}

class Player with ChangeNotifier {
  final PlayerIndex index;
  final Team team;
  String _name;

  Player(this.index, this.team, this._name) {
    // get the last stored instead of the default if there is one
    SharedPreferences.getInstance().then((prefs) {
      // get the name from the preferences to use rather than the default name
      String playerName = (prefs.getString('player_name_$index') ?? '');
      if (playerName.isNotEmpty) {
        // use the name from the preferences instead
        name = playerName;
      }
    });
  }

  Player get partner {
    switch(index) {
      case PlayerIndex.P_ONE:
        return Players.partnerOne;
      case PlayerIndex.PT_ONE:
        return Players.playerOne;
      case PlayerIndex.P_TWO:
        return Players.partnerTwo;
      case PlayerIndex.PT_TWO:
        return Players.playerTwo;
      default:
        return Players.partnerOne;
    }
  }

  set name(String newName) {
    // put this in the preferences
    SharedPreferences.getInstance().then((prefs) {
      prefs.setString('player_name_$index', newName);
    });
    // and set our member accordingly
    _name = newName;
    // this changes the state of the player - notify listeners of this
    notifyListeners();
  }

  String get name {
    return _name;
  }

  bool isInTeam(Team team) {
    switch(team.index) {
      case TeamIndex.T_ONE:
        return index == PlayerIndex.P_ONE || index == PlayerIndex.PT_ONE;
      case TeamIndex.T_TWO:
        return index == PlayerIndex.P_TWO || index == PlayerIndex.PT_TWO;
      default:
        return false;
    }
  }
}