import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/match_setup.dart';
import 'package:multiphone/providers/sport.dart';

enum TennisSets { one, three, five }
enum TennisGames { four, six }

class TennisMatchSetup extends MatchSetup {
  TennisSets _sets = TennisSets.three;
  TennisGames _games = TennisGames.six;

  bool _isSuddenDeathOnDeuce = false;

  // this is the game at which to play a tie in the final set
  int _finalSetTieGame = 0;

  TennisMatchSetup(Sport sport) : super(sport);

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

  static int setsValue(TennisSets sets) {
    switch (sets) {
      case TennisSets.one:
        return 1;
      case TennisSets.three:
        return 3;
      case TennisSets.five:
        return 5;
      default:
        return 3;
    }
  }

  static TennisSets fromSetsValue(int value) {
    switch (value) {
      case 1:
        return TennisSets.one;
      case 3:
        return TennisSets.three;
      case 5:
        return TennisSets.five;
      default:
        return TennisSets.three;
    }
  }

  static int gamesValue(TennisGames games) {
    switch (games) {
      case TennisGames.six:
        return 6;
      case TennisGames.four:
        return 4;
      default:
        return 6;
    }
  }

  static TennisGames fromGamesValue(int value) {
    switch (value) {
      case 6:
        return TennisGames.six;
      case 4:
        return TennisGames.four;
      default:
        return TennisGames.six;
    }
  }

  @override
  Map<String, Object> getData() {
    return {
      ...super.getData(),
      ...{
        'sets': setsValue(_sets),
        'games': gamesValue(_games),
        'finalSetTie': _finalSetTieGame,
        'deuceDeath': _isSuddenDeathOnDeuce,
      },
    };
  }

  @override
  void setData(Map<String, Object> data) {
    super.setData(data);
    _sets = fromSetsValue(data['sets'] as int);
    _games = fromGamesValue(data['games'] as int);
    _finalSetTieGame = data['finalSetTie'];
    _isSuddenDeathOnDeuce = data['deuceDeath'];
  }

  get sets {
    return _sets;
  }

  set sets(TennisSets sets) {
    _sets = sets;
    // this is a change
    notifyListeners();
  }

  get games {
    return _games;
  }

  @override
  List<int> getStraightPointsToWin() {
    // points levels are 3!
    return [
      1, // 1 point to win a point
      4, // 4 points to win a game
      gamesValue(_games), // number games (4 or 6) to win a set
    ];
  }

  set games(TennisGames value) {
    if (_finalSetTieGame == gamesValue(_games)) {
      // the tie is to happen in the final game, change this too
      _finalSetTieGame = gamesValue(value);
    }
    if (_games != value) {
      // set the number games
      _games = value;
      // this changes the setup
      notifyListeners();
    }
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
    return _finalSetTieGame > 0;
  }

  set tieInFinalSet(bool value) {
    int newGame = 0;
    if (value) {
      // tie in the final set - default to the number of games
      newGame = gamesValue(_games);
    }
    if (_finalSetTieGame != newGame) {
      // not to tie at all
      _finalSetTieGame = newGame;
      // this changes the setup
      notifyListeners();
    }
  }

  get finalSetTieGame {
    return _finalSetTieGame;
  }

  set finalSetTieGame(int value) {
    if (_finalSetTieGame != value) {
      _finalSetTieGame = value;
      // this changes the setup
      notifyListeners();
    }
  }
}
