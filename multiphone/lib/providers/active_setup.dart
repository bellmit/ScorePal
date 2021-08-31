import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:multiphone/helpers/log.dart';
import 'package:multiphone/match/team_namer.dart';
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

class CommunicatedTo {
  final String email;
  final String username;
  final String userId;
  const CommunicatedTo(
      {@required this.email, @required this.username, @required this.userId});
}

abstract class ActiveSetup with ChangeNotifier {
  MatchSinglesDoubles _singlesDoubles = MatchSinglesDoubles.singles;

  final List<String> _playerNames =
      List<String>.filled(PlayerIndex.values.length, '');
  final List<List<String>> _playerEmailAddresses =
      List<List<String>>.filled(PlayerIndex.values.length, []);

  TeamNamer _teamNamer;
  final Sport sport;
  bool _isCreateNewMatch = false;
  ActiveMatch _matchToResume;
  String _id;
  List<CommunicatedTo> _communicatedToList = [];
  String _communicatedFrom;

  TeamIndex _firstServingTeam = TeamIndex.T_ONE;
  final List<List<PlayerIndex>> _firstServers = [
    [PlayerIndex.P_ONE],
    [PlayerIndex.P_TWO],
  ];

  ActiveSetup(this.sport)
      : _id = '${sport.id}_${DateTime.now().toIso8601String()}' {
    // the account holder is always player one
    _playerNames[PlayerIndex.P_ONE.index] = getAccountUserName();
    // set the team namer as required
    _teamNamer = TeamNamer(this);
  }

  get id {
    return _id;
  }

  List<CommunicatedTo> get communicatedTo {
    return [..._communicatedToList];
  }

  bool get isCommunicatedFrom {
    return _communicatedFrom != null && _communicatedFrom.isNotEmpty;
  }

  bool get isCreateNewMatch {
    return _isCreateNewMatch;
  }

  void newMatchCreated() {
    // called from the provider when we make a new one (to not do it again)
    _isCreateNewMatch = false;
  }

  ActiveMatch get matchToResume {
    return _matchToResume;
  }

  void createNewMatch() {
    _isCreateNewMatch = true;
    _matchToResume = null;
    // and inform listeners
    notifyListeners();
  }

  void resumeMatch(ActiveMatch match) {
    _matchToResume = match;
    _isCreateNewMatch = false;
    notifyListeners();
  }

  Map<String, Object> getData() {
    // save all our data
    final data = Map<String, Object>();
    // the ID, why not
    data['id'] = id;
    for (int i = 0; i < _playerNames.length; ++i) {
      data['player${i + 1}'] = _playerNames[i];
    }
    // also if we are singles / doubles
    data['singles'] = _singlesDoubles == MatchSinglesDoubles.singles;
    data['first_team'] = _firstServingTeam.index;
    for (int i = 0; i < _firstServers.length; ++i) {
      // there is a list in each list, first is the first server for
      // the actual match, from then on it's the set or whatever...
      for (int j = 0; j < _firstServers[i].length; ++j) {
        final indexToAdd =
            _firstServers[i][j] == null ? -1 : _firstServers[i][j].index;
        data['server${i + 1}${j == 0 ? '' : '_$j'}'] = indexToAdd;
      }
    }
    // also the email addresses of the players we are playing
    for (int i = 0; i < _playerEmailAddresses.length; ++i) {
      data['player${i + 1}_addresses'] = _playerEmailAddresses[i];
    }
    // and the 'communicated to' list
    List<String> dataList = [];
    for (CommunicatedTo communicated in _communicatedToList) {
      dataList.add(
          '${communicated.email}|${communicated.username}|${communicated.userId}');
    }
    data['communicated_to'] = dataList;
    // and from (might be null)
    data['communicated_from'] = _communicatedFrom;
    return data;
  }

  void setData(Map<String, Object> data) {
    // Id first
    _id = data['id'];
    for (int i = 0; i < _playerNames.length; ++i) {
      _playerNames[i] = data['player${i + 1}'] ?? '';
    }
    // singles / doubles
    _singlesDoubles = data['singles']
        ? MatchSinglesDoubles.singles
        : MatchSinglesDoubles.doubles;
    _firstServingTeam = TeamIndex.values[data['first_team']];
    for (int i = 0; i < _firstServers.length; ++i) {
      // there is a list in each list, first is the first server for
      // the actual match, from then on it's the set or whatever...
      int j = 0;
      int serverIndex;
      // clear the old data
      _firstServers[i] = [];
      do {
        // for each j, try to get a value for the server in i
        serverIndex = data['server${i + 1}${j == 0 ? '' : '_$j'}'];
        if (serverIndex != null) {
          _firstServers[i]
              .add(serverIndex == -1 ? null : PlayerIndex.values[serverIndex]);
        }
        // move j on
        ++j;
      } while (serverIndex != null);
    }
    // and the player's email addresses
    for (int i = 0; i < _playerEmailAddresses.length; ++i) {
      final List<dynamic> list = data['player${i + 1}_addresses'] ?? [];
      _playerEmailAddresses[i] = list.map((e) => e.toString()).toList();
    }
    // we also want the list of players this is communicated to
    final communicatedTo = data['communicated_to'] as List;
    if (communicatedTo == null || communicatedTo.isEmpty) {
      _communicatedToList = [];
    } else {
      // create the objects from the ":" separated strings
      for (String commString in communicatedTo) {
        final splitStrings = commString.split('|');
        if (splitStrings.length == 3) {
          _communicatedToList.add(CommunicatedTo(
            email: splitStrings[0],
            username: splitStrings[1],
            userId: splitStrings[2],
          ));
        } else {
          Log.error(
              'The communicated_to string of "$commString" was not as expected');
        }
      }
    }
    // and if this is from someone - to not send back
    _communicatedFrom = data['communicated_from'];
    // this, obviously, changes the data
    notifyListeners();
  }

  String getPlayerNameForEmail(String email) {
    // find the email in the list and return the player for that one
    final lcEmail = email.trim().toLowerCase();
    for (var i = 0; i < _playerEmailAddresses.length; ++i) {
      if (_playerEmailAddresses[i]
          .any((element) => element.toLowerCase().trim() == lcEmail)) {
        // this list contains the email, this is the player
        return _playerNames[i].isEmpty ? null : _playerNames[i];
      }
    }
    // if here then there's no match
    return null;
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
    // return the name
    return name;
  }

  String getDefaultPlayerName(PlayerIndex player, BuildContext context) {
    return _teamNamer.getDefaultPlayerName(player, context);
  }

  void setPlayerEmails(PlayerIndex player, List<String> emailAddresses) {
    _playerEmailAddresses[player.index] = [...emailAddresses];
  }

  List<String> getPlayerEmails(PlayerIndex player) {
    return [..._playerEmailAddresses[player.index]];
  }

  String getTeamName(TeamIndex team, BuildContext context) {
    // return the correct name for the team
    return _teamNamer.getTeamName(context, team, isUseDefaults: false);
  }

  String getDefaultTeamName(TeamIndex team, BuildContext context) {
    // return the correct name for the team
    return _teamNamer.getTeamName(context, team, isUseDefaults: true);
  }

  get firstServingTeam {
    return _firstServingTeam;
  }

  void _fillFirstServers(TeamIndex team, int entryIndex) {
    while (_firstServers[team.index].length <= entryIndex) {
      // while there are not enough, add more
      _firstServers[team.index].add(null);
    }
  }

  PlayerIndex getFirstServingPlayer(TeamIndex team, {int entryIndex = 0}) {
    // return the player that serves first for this team
    _fillFirstServers(team, entryIndex);
    return _firstServers[team.index][entryIndex];
  }

  List<PlayerIndex> getFirstServingPlayers(TeamIndex team) {
    // return the player that serves first for this team
    return [..._firstServers[team.index]];
  }

  void setFirstServingPlayer(TeamIndex team, PlayerIndex player,
      {int entryIndex = 0}) {
    // return the player that serves first for this team
    _fillFirstServers(team, entryIndex);
    _firstServers[team.index][entryIndex] = player;
    // this changes the setup
    notifyListeners();
  }

  get startingServer {
    return getFirstServingPlayer(_firstServingTeam);
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
    _fillFirstServers(playerTeam, 0);
    if (_firstServers[playerTeam.index][0] != server) {
      // get the team for the player and set accordingly
      _firstServers[playerTeam.index].clear();
      // this will wipe all subsequent changes as changes the match history
      _firstServers[playerTeam.index].add(server);
      // this changes the setup
      notifyListeners();
    }
  }

  get singlesDoubles {
    return _singlesDoubles;
  }

  set singlesDoubles(MatchSinglesDoubles newValue) {
    _singlesDoubles = newValue;
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
      _fillFirstServers(TeamIndex.T_ONE, 0);
      // this is singles, the first server has to be the player of that team
      if (_firstServers[TeamIndex.T_ONE.index][0] != PlayerIndex.P_ONE) {
        // partner can't be serving - this is a singles match
        _firstServers[TeamIndex.T_ONE.index][0] = PlayerIndex.P_ONE;
      }
      _fillFirstServers(TeamIndex.T_TWO, 0);
      if (_firstServers[TeamIndex.T_TWO.index][0] != PlayerIndex.P_TWO) {
        // partner can't be serving - this is a singles match
        _firstServers[TeamIndex.T_TWO.index][0] = PlayerIndex.P_TWO;
      }
    }
  }

  String getAccountUserName() {
    String accountUserName = '';
    final user = FirebaseAuth.instance.currentUser;
    if (null != user && user.displayName != null) {
      accountUserName = user.displayName;
    }
    return accountUserName;
  }

  bool isAccountUserInTeam(TeamIndex teamIndex) {
    final String accountUserName = getAccountUserName();
    // the account user is playing if their name is the player or the partner
    return usernameEquals(
            _playerNames[getTeamPlayer(teamIndex).index], accountUserName) ||
        usernameEquals(
            _playerNames[getTeamPartner(teamIndex).index], accountUserName);
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
