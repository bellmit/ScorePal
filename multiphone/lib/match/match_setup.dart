import 'package:flutter/material.dart';
import 'package:multiphone/helpers/team_namer.dart';
import 'package:multiphone/match/badminton_match_setup.dart';
import 'package:multiphone/match/ping_pong_match_setup.dart';
import 'package:multiphone/match/tennis_match_setup.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/player.dart';
import 'package:multiphone/providers/sport.dart';

enum MatchSinglesDoubles {
  doubles,
  singles,
}

enum TeamIndex {
  T_ONE,
  T_TWO,
}

abstract class MatchSetup with ChangeNotifier {
  MatchSinglesDoubles _singlesDoubles = MatchSinglesDoubles.singles;

  final List<String> _playerNames =
      List<String>.filled(PlayerIndex.values.length, '');

  TeamNamer _teamNamer;
  final Sport sport;

  TeamIndex _firstServingTeam = TeamIndex.T_ONE;
  final List<PlayerIndex> _firstServers = [
    PlayerIndex.P_ONE,
    PlayerIndex.P_TWO
  ];

  MatchSetup(this.sport) {
    _teamNamer = TeamNamer(this);
  }

  // need to create the proper one for the active match as it changes
  static MatchSetup create(ActiveMatch match) {
    if (match == null || match.sport == null) {
      return null;
    } else {
      return _createSetup(match.sport);
    }
  }

  static MatchSetup _createSetup(Sport sport) {
    switch (sport.id) {
      case SportType.TENNIS:
        return TennisMatchSetup(sport);
      case SportType.BADMINTON:
        return BadmintonMatchSetup(sport);
      case SportType.PING_PONG:
        return PingPongMatchSetup(sport);
      default:
        print('do the setup for the sport type of ${sport.id}');
        return null;
    }
  }

  Map<String, Object> getAsJSON() {
    return {
      'ver': 1,
      'sport': sport.id.index,
      'data': getData(),
    };
  }

  static MatchSetup createFromJson(Map<String, Object> topLevel) {
    // what is this?
    MatchSetup setup = _createSetup(Sports.find(topLevel['sport'] as int));
    // set our data from this data under the top level
    setup.setData(topLevel['data']);
    // and return this now it's setup properly
    return setup;
  }

  Map<String, Object> getData() {
    final data = {};
    for (int i = 0; i < _playerNames.length; ++i) {
      data['player${i + 1}'] = _playerNames[i];
    }
    return data;
  }

  void setData(Map<String, Object> data) {
    for (int i = 0; i < _playerNames.length; ++i) {
      _playerNames[i] = data['player${i + 1}'];
    }
  }

  List<int> getStraightPointsToWin();

  String matchSummary(BuildContext context);

  void setPlayerName(PlayerIndex player, String name) {
    bool isInformChange = !usernameEquals(name, _playerNames[player.index]);
    _playerNames[player.index] = name.trim();
    if (isInformChange) {
      // this is a change
      notifyListeners();
    }
  }

  String getPlayerName(PlayerIndex player, BuildContext context) {
    String name = _playerNames[player.index];
    if (name == null || name.isEmpty) {
      name = _teamNamer.getDefaultPlayerName(player, context);
    }
    if (startingServer == player) {
      name += ' (*)';
    }
    return name;
  }

  String getTeamName(TeamIndex team, BuildContext context) {
    // return the correct name for the team
    return _teamNamer.getTeamName(context, team);
  }

  get firstServingTeam {
    return _firstServingTeam;
  }

  get startingServer {
    return _firstServers[_firstServingTeam.index];
  }

  set firstServingTeam(TeamIndex team) {
    if (_firstServingTeam != team) {
      _firstServingTeam = team;
      // this is a change
      notifyListeners();
    }
  }

  set startingServer(PlayerIndex server) {
    TeamIndex playerTeam = getPlayerTeam(server);
    if (_firstServers[playerTeam.index] != server) {
      // get the team for the player and set accordingly
      _firstServers[playerTeam.index] = server;
      // this changes the setup
      notifyListeners();
    }
  }

  get singlesDoubles {
    return _singlesDoubles;
  }

  set singlesDoubles(MatchSinglesDoubles singlesDoubles) {
    _singlesDoubles = singlesDoubles;
    // this is a change
    notifyListeners();
  }

  TeamIndex getPlayerTeam(PlayerIndex player) {
    switch (player) {
      case PlayerIndex.P_ONE:
      case PlayerIndex.PT_ONE:
        return TeamIndex.T_ONE;
      case PlayerIndex.P_TWO:
      case PlayerIndex.PT_TWO:
        return TeamIndex.T_TWO;
    }
    return null;
  }

  TeamIndex getOtherTeam(TeamIndex team) {
    if (team == TeamIndex.T_ONE) {
      return TeamIndex.T_TWO;
    } else {
      return TeamIndex.T_ONE;
    }
  }

  PlayerIndex getOtherPlayer(PlayerIndex player) {
    switch (player) {
      case PlayerIndex.P_ONE:
        return PlayerIndex.PT_ONE;
      case PlayerIndex.PT_ONE:
        return PlayerIndex.P_ONE;
      case PlayerIndex.P_TWO:
        return PlayerIndex.PT_TWO;
      case PlayerIndex.PT_TWO:
        return PlayerIndex.P_TWO;
    }
    return null;
  }

  bool isPlayerInTeam(TeamIndex team, PlayerIndex player) {
    switch (team) {
      case TeamIndex.T_ONE:
        return player == PlayerIndex.P_ONE || player == PlayerIndex.PT_ONE;
      case TeamIndex.T_TWO:
        return player == PlayerIndex.P_TWO || player == PlayerIndex.PT_TWO;
    }
    return false;
  }

  PlayerIndex getTeamPlayer(TeamIndex team) {
    if (team == TeamIndex.T_ONE) {
      return PlayerIndex.P_ONE;
    } else {
      return PlayerIndex.P_TWO;
    }
  }

  PlayerIndex getTeamPartner(TeamIndex team) {
    if (team == TeamIndex.T_ONE) {
      return PlayerIndex.PT_ONE;
    } else {
      return PlayerIndex.PT_TWO;
    }
  }

  void correctPlayerErrors() {
    // changing the type from doubles to singles etc, can effect some settings
    // a player no longer playing could be serving - correct those here
    if (singlesDoubles == MatchSinglesDoubles.singles) {
      // this is singles, the first server has to be the player of that team
      if (_firstServers[TeamIndex.T_ONE.index] != PlayerIndex.P_ONE) {
        // partner can't be serving - this is a singles match
        _firstServers[TeamIndex.T_ONE.index] = PlayerIndex.P_ONE;
      }
      if (_firstServers[TeamIndex.T_TWO.index] != PlayerIndex.P_TWO) {
        // partner can't be serving - this is a singles match
        _firstServers[TeamIndex.T_TWO.index] = PlayerIndex.P_TWO;
      }
    }
  }

  static bool usernameEquals(String username, String compare) {
    if (username == null && compare == null) {
      return true;
    } else if (username != null && compare == null) {
      // not the same
      return false;
    } else if (username == null && compare != null) {
      // not the same
      return false;
    } else {
      return username.trim().toLowerCase() == compare.trim().toLowerCase();
    }
  }

  void setUsernameInTeamOne(String userName) {
    // first move if they are playing in team two
    bool isUserFound = false;
    if (usernameEquals(userName, _playerNames[PlayerIndex.P_TWO.index]) ||
        usernameEquals(userName, _playerNames[PlayerIndex.PT_TWO.index])) {
      // the user is playing in team two - boo
      String player = _playerNames[PlayerIndex.P_TWO.index];
      String partner = _playerNames[PlayerIndex.PT_TWO.index];
      // swap the names
      _playerNames[PlayerIndex.P_TWO.index] =
          _playerNames[PlayerIndex.P_ONE.index];
      _playerNames[PlayerIndex.PT_TWO.index] =
          _playerNames[PlayerIndex.PT_ONE.index];
      _playerNames[PlayerIndex.P_ONE.index] = player;
      _playerNames[PlayerIndex.PT_ONE.index] = partner;
      isUserFound = true;
    }
    // then move if they are the partner
    if (usernameEquals(userName, _playerNames[PlayerIndex.PT_ONE.index])) {
      // the user is the partner - less boo but still boo
      String player = _playerNames[PlayerIndex.P_ONE.index];
      _playerNames[PlayerIndex.P_ONE.index] =
          _playerNames[PlayerIndex.PT_ONE.index];
      _playerNames[PlayerIndex.PT_ONE.index] = player;
      isUserFound = true;
    }
    if (!isUserFound) {
      // the user isn't anywhere (or in P_ONE co-incidentally) set P_ONE to be perfect anyway
      _playerNames[PlayerIndex.P_ONE.index] = userName;
    }
  }
}
