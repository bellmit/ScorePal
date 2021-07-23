import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/match_setup.dart';
import 'package:multiphone/providers/team.dart';

enum TennisSets { one, three, five }
enum TennisGames { four, six }

class TennisMatchSetup extends MatchSetup {
  TennisSets _sets = TennisSets.three;
  TennisGames _games = TennisGames.six;

  bool _isSuddenDeathOnDeuce = false;
  bool _tieInFinalSet = false;

  TennisMatchSetup();

  @override
  String matchSummary(BuildContext context) {
    // construct the match summary accordingly
    final values = Values(context);
    return values.construct(values.strings.title_setup_tennis, [
      getTeamName(TeamIndex.T_ONE, context),
      getTeamName(TeamIndex.T_TWO, context),
      setsValue(_sets).toString(),
    ]);
  }

  int setsValue(TennisSets sets) {
    switch (sets) {
      case TennisSets.one:
        return 1;
      case TennisSets.three:
        return 3;
      case TennisSets.five:
        return 5;
      default:
        return 0;
    }
  }

  get sets {
    return _sets;
  }

  set sets(TennisSets sets) {
    _sets = sets;
    // this is a change
    notifyListeners();
  }

  int gamesValue(TennisGames games) {
    switch (games) {
      case TennisGames.six:
        return 6;
      case TennisGames.four:
        return 4;
      default:
        return 0;
    }
  }

  get games {
    return _games;
  }

  set games(TennisGames games) {
    _games = games;
    // this is a change
    notifyListeners();
  }

  bool get isSuddenDeathOnDeuce {
    return _isSuddenDeathOnDeuce;
  }

  set isSuddenDeathOnDeuce(bool value) {
    _isSuddenDeathOnDeuce = value;
    // this is a change
    notifyListeners();
  }

  bool get tieInFinalSet {
    return _tieInFinalSet;
  }

  set tieInFinalSet(bool value) {
    _tieInFinalSet = value;
    // this is a change
    notifyListeners();
  }
}
