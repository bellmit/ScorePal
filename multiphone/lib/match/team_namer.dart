import 'package:flutter/material.dart';
import 'package:multiphone/helpers/preferences.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/providers/player.dart';

enum TeamNamingMode {
  SURNAME_INITIAL,
  FIRST_NAME,
  LAST_NAME,
  FULL_NAME,
}

class TeamNamer {
  static const TEAM_SEP = ' -- ';

  final ActiveSetup _setup;
  TeamNamingMode _currentMode = TeamNamingMode.SURNAME_INITIAL;

  TeamNamer(this._setup) {
    // get the last saved naming style from preferences as the default
    Preferences.create()
        .then((value) => _currentMode = value.defaultNamingMode);
  }

  TeamNamingMode get currentMode {
    return _currentMode;
  }

  set currentMode(TeamNamingMode newMode) {
    if (newMode != this.currentMode) {
      // set it local
      _currentMode = newMode;
      // and store
      Preferences.create().then((value) => value.defaultNamingMode = newMode);
    }
  }

  TeamNamingMode nextMode(TeamNamingMode current) {
    bool found = false;
    for (int i = 0; i < TeamNamingMode.values.length; ++i) {
      if (found) {
        // this is the next one
        return TeamNamingMode.values[i];
      } else if (TeamNamingMode.values[i] == current) {
        found = true;
      }
    }
    // if here then overflowed the list
    return TeamNamingMode.SURNAME_INITIAL;
  }

  String getDefaultPlayerName(PlayerIndex playerIndex, BuildContext context) {
    switch (playerIndex) {
      case PlayerIndex.P_ONE:
        return Values(context).strings.player_one;
        break;
      case PlayerIndex.P_TWO:
        return Values(context).strings.player_two;
        break;
      case PlayerIndex.PT_ONE:
        return Values(context).strings.partner_one;
        break;
      case PlayerIndex.PT_TWO:
        return Values(context).strings.partner_two;
        break;
    }
    return '';
  }

  String getDefaultTeamName(TeamIndex teamIndex, BuildContext context) {
    switch (teamIndex) {
      case TeamIndex.T_ONE:
        return Values(context).strings.team_one;
        break;
      case TeamIndex.T_TWO:
        return Values(context).strings.team_two;
        break;
    }
    return '';
  }

  String getTeamName(BuildContext context, TeamIndex teamIndex,
      {bool isUseDefaults = false}) {
    // sort out what we are doing with our names, by default in doubles
    // we are a team, in singles we are player one
    String teamName = "";
    // combine the name in the correct chosen way
    if (!isUseDefaults) {
      switch (currentMode) {
        case TeamNamingMode.SURNAME_INITIAL:
          teamName = _createSurnameTeamName(context, teamIndex);
          break;
        case TeamNamingMode.FIRST_NAME:
          teamName = _createFirstNameTeamName(context, teamIndex);
          break;
        case TeamNamingMode.LAST_NAME:
          teamName = _createLastNameTeamName(context, teamIndex);
          break;
        case TeamNamingMode.FULL_NAME:
          teamName = _createFullNameTeamName(context, teamIndex);
          break;
      }
    } else if (_setup.singlesDoubles == MatchSinglesDoubles.doubles) {
      // we are using defaults - if we are saying team name (doubles) then just return that
      teamName = getDefaultTeamName(teamIndex, context);
    }
    if (null == teamName || teamName.isEmpty) {
      switch (teamIndex) {
        case TeamIndex.T_ONE:
          final playerOneName = !isUseDefaults
              ? _setup.getPlayerName(PlayerIndex.P_ONE, context)
              : _setup.getDefaultPlayerName(PlayerIndex.P_ONE, context);
          final partnerOneName = !isUseDefaults
              ? _setup.getPlayerName(PlayerIndex.PT_ONE, context)
              : _setup.getDefaultPlayerName(PlayerIndex.PT_ONE, context);
          teamName = playerOneName +
              (_setup.singlesDoubles == MatchSinglesDoubles.singles
                  ? ""
                  : TEAM_SEP + partnerOneName);
          break;
        case TeamIndex.T_TWO:
          final playerOneName = !isUseDefaults
              ? _setup.getPlayerName(PlayerIndex.P_TWO, context)
              : _setup.getDefaultPlayerName(PlayerIndex.P_TWO, context);
          final partnerOneName = !isUseDefaults
              ? _setup.getPlayerName(PlayerIndex.PT_TWO, context)
              : _setup.getDefaultPlayerName(PlayerIndex.PT_TWO, context);
          teamName = playerOneName +
              (_setup.singlesDoubles == MatchSinglesDoubles.singles
                  ? ""
                  : TEAM_SEP + partnerOneName);
          break;
        // no default please
      }
      // use the default
    }
    return teamName;
  }

  String _createSurnameTeamName(BuildContext context, TeamIndex team) {
    if (_setup.singlesDoubles == MatchSinglesDoubles.doubles) {
      return _combineTwoNames(
          _splitSurname(
              _setup.getPlayerName(_setup.getTeamPlayer(team), context)),
          _splitSurname(
              _setup.getPlayerName(_setup.getTeamPartner(team), context)));
    } else {
      return _splitSurname(
          _setup.getPlayerName(_setup.getTeamPlayer(team), context));
    }
  }

  String _createFirstNameTeamName(BuildContext context, TeamIndex team) {
    if (_setup.singlesDoubles == MatchSinglesDoubles.doubles) {
      return _combineTwoNames(
          _splitFirstName(
              _setup.getPlayerName(_setup.getTeamPlayer(team), context)),
          _splitFirstName(
              _setup.getPlayerName(_setup.getTeamPartner(team), context)));
    } else {
      return _splitFirstName(
          _setup.getPlayerName(_setup.getTeamPlayer(team), context));
    }
  }

  String _createLastNameTeamName(BuildContext context, TeamIndex team) {
    if (_setup.singlesDoubles == MatchSinglesDoubles.doubles) {
      return _combineTwoNames(
          _splitLastName(
              _setup.getPlayerName(_setup.getTeamPlayer(team), context)),
          _splitLastName(
              _setup.getPlayerName(_setup.getTeamPartner(team), context)));
    } else {
      return _splitLastName(
          _setup.getPlayerName(_setup.getTeamPlayer(team), context));
    }
  }

  String _createFullNameTeamName(BuildContext context, TeamIndex team) {
    if (_setup.singlesDoubles == MatchSinglesDoubles.doubles) {
      return _combineTwoNames(
          _setup.getPlayerName(_setup.getTeamPlayer(team), context),
          _setup.getPlayerName(_setup.getTeamPartner(team), context));
    } else {
      return _setup.getPlayerName(_setup.getTeamPlayer(team), context);
    }
  }

  String _splitFirstName(String fullName) {
    List<String> parts = fullName.split(" ");
    if (parts.length <= 1) {
      // no good
      return fullName;
    } else {
      // there are a number of parts, just use the first name
      return parts[0];
    }
  }

  String _splitLastName(String fullName) {
    List<String> parts = fullName.split(" ");
    if (parts.length <= 1) {
      // no good
      return fullName;
    } else {
      // there are a number of parts, just use the last name
      return parts[parts.length - 1];
    }
  }

  String _splitSurname(String fullName) {
    List<String> parts = fullName.split(" ");
    if (parts.length <= 1) {
      // no good
      return fullName;
    } else {
      // there are a number of parts, get all the initials
      String result = '';
      for (int i = 0; i < parts.length - 1; ++i) {
        if (false == parts[i].isEmpty) {
          // just append the first initial
          result += parts[i].characters.first;
          // append a dot after it
          result += '.';
        }
      }
      // after the initials, we want a space
      result += ' ';
      // and finally the surname
      result += parts[parts.length - 1];
      // and return the string
      return result;
    }
  }

  String _combineTwoNames(String name1, String name2) {
    if (null == name1 || name1.isEmpty) {
      // need to just use name 2
      return name2;
    } else if (null == name2 || name2.isEmpty) {
      // need to just use name 1
      return name1;
    } else {
      // combine the two strings with a nice separator
      String result = '';
      result += name1;
      result += TEAM_SEP;
      result += name2;
      // return this string
      return result;
    }
  }
}
