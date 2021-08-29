import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/match/tennis/tennis_point.dart';
import 'package:multiphone/match/score.dart';
import 'package:multiphone/match/score_state.dart';
import 'package:multiphone/match/tennis/tennis_match_setup.dart';
import 'package:multiphone/providers/player.dart';

class TennisScore extends Score<TennisMatchSetup> {
  static const int LEVEL_POINT = 0;
  static const int LEVEL_GAME = 1;
  static const int LEVEL_SET = 2;

  static const int POINTS_TO_WIN_GAME = 4;
  static const int POINTS_AHEAD_IN_GAME = 2;

  static const int POINTS_TO_WIN_TIE = 7;
  static const int POINTS_AHEAD_IN_TIE = 2;

  static const int GAMES_AHEAD_IN_SET = 2;

  static const int K_LEVELS = 3;

  bool _isInTieBreak;
  bool _isTeamServerChangeAllowed = true;
  PlayerIndex _tieBreakServer;

  List<int> tieBreakSets;
  List<int> breakPoints;
  List<int> breakPointsConverted;

  TennisScore(TennisMatchSetup setup) : super(setup, K_LEVELS);

  @override
  Map<String, Object> getData() {
    final data = super.getData();
    // store our data in this object too - only things that will not be recreated when
    // the score is replayed in this match - ie, very little
    return data;
  }

  void restoreFromData(
      Map<String, Object> data, void Function() onPointIncremented) {
    super.restoreFromData(data, onPointIncremented);
    // and restore any of our special data in here
  }

  @override
  int getScoreGoal() {
    return TennisMatchSetup.setsValue(setup.sets);
  }

  @override
  void resetScore() {
    // let the base reset
    super.resetScore();
    // initialise all the data we are gathering
    if (null != tieBreakSets) {
      tieBreakSets.clear();
      // clear our count of breaks and breaks converted
      breakPoints = List.filled(Score.teamCount, 0);
      breakPointsConverted = List.filled(Score.teamCount, 0);
    } else {
      // create the lists
      tieBreakSets = [];
      breakPoints = List.filled(Score.teamCount, 0);
      breakPointsConverted = List.filled(Score.teamCount, 0);
    }
    // and reset our data
    _isInTieBreak = false;
    _isTeamServerChangeAllowed = true;
    _tieBreakServer = null;
  }

  @override
  bool isScoreCompleted() {
    bool isMatchOver = false;
    TennisSets setsToPlay = setup.sets;
    // return if a player has reached the number of sets required (this is just over half)
    for (int i = 0; i < Score.teamCount; ++i) {
      if (getSets(TeamIndex.values[i]) >=
          TennisMatchSetup.setsTargetValue(setsToPlay)) {
        // this team has reached the limit, match is over
        isMatchOver = true;
      }
    }
    return isMatchOver;
  }

  TennisSets getSetsToPlay() {
    // the sets to play are set from the score goal
    return TennisMatchSetup.fromSetsValue(getScoreGoal());
  }

  int getTeamPoints(TeamIndex team) {
    return super.getPoint(LEVEL_POINT, team);
  }

  List<int> getSetPoints(int setIndex, int gameIndex) {
    // get the points in the set and games index specified
    List<int> toReturn;
    // to get the points for this game, we need to find the index of that game
    // so for that we need to add up all the games for all the previous sets
    // before we get to this one
    List<List<int>> gameResults = super.getPointHistory(LEVEL_POINT);
    List<List<int>> setResults = super.getPointHistory(LEVEL_GAME);
    if (null != setResults && null != gameResults) {
      // there are results for the sets (a record of the games for each)
      // we need to add these up to find the start of the set as a number of games
      int gamesPlayed = 0;
      for (int i = 0; i < setIndex && i < setResults.length; ++i) {
        var results = setResults.elementAt(i);
        for (int j = 0; j < results.length; ++j) {
          var games = results[j];
          gamesPlayed += games;
        }
      }
      // the index into the game results is the games played in previous sets
      // plus the index we are interested in
      gameIndex += gamesPlayed;
      if (gameIndex < gameResults.length) {
        // this is ok
        toReturn = gameResults.elementAt(gameIndex);
      }
    }
    if (null == toReturn) {
      // there are no points for this game, we can return the current points
      // instead as they are probably in progress of playing it then
      toReturn = [
        getTeamPoints(TeamIndex.T_ONE),
        getTeamPoints(TeamIndex.T_TWO),
      ];
    }
    // return the points required
    return toReturn;
  }

  int getGames(TeamIndex team, int setIndex) {
    // get the games for the set index specified
    int toReturn;
    List<List<int>> gameResults = super.getPointHistory(LEVEL_GAME);
    if (null == gameResults || setIndex < 0 || setIndex >= gameResults.length) {
      // there is no history for this set, return the current games instead
      toReturn = super.getPoint(LEVEL_GAME, team);
    } else {
      List<int> setGames = gameResults.elementAt(setIndex);
      toReturn = setGames[team.index];
    }
    return toReturn;
  }

  int getSets(TeamIndex team) {
    // get the history of sets to get the last one
    List<List<int>> setResults = super.getPointHistory(LEVEL_SET);
    int toReturn;
    if (null != setResults && false == setResults.isEmpty) {
      List<int> setGames = setResults.elementAt(setResults.length - 1);
      toReturn = setGames[team.index];
    } else {
      // return the running set count
      toReturn = super.getPoint(LEVEL_SET, team);
    }
    return toReturn;
  }

  bool isSetTieBreak(int setIndex) {
    return tieBreakSets.contains(setIndex);
  }

  int getBreakPoints(TeamIndex team) {
    return breakPoints[team.index];
  }

  int getBreakPointsConverted(TeamIndex team) {
    return breakPointsConverted[team.index];
  }

  @override
  int incrementPoint(TeamIndex team, int level) {
    // do the work
    if (level == LEVEL_GAME) {
      // pre changing the point, a game has been won
      onGameWon(team);
    }
    // now we can change the point
    int point = super.incrementPoint(team, level);
    // but we have to handle things specially here
    switch (level) {
      case TennisScore.LEVEL_POINT:
        onPointIncremented(team, point);
        break;
      case TennisScore.LEVEL_GAME:
        onGameIncremented(team, point);
        break;
      case TennisScore.LEVEL_SET:
        onSetIncremented(team, point);
        break;
    }
    return point;
  }

  ///
  /// only call this privately as a game is won by winning points to prevent it going in the history
  /// as some user entry that they won the set
  /// @param team is the team that has won the game
  void incrementGame(TeamIndex team) {
    // is this a break-point converted to reality?
    onGameWon(team);
    // add one to the game already stored
    int point = super.getPoint(LEVEL_GAME, team) + 1;
    // set this back on the score
    super.setPoint(LEVEL_GAME, team, point);
    // and handle this
    onGameIncremented(team, point);
  }

  ///
  /// only called privately as a set is won by winning a set to prevent it going in the history
  /// as some user entry that they won the set
  /// @param team is the team that has won the set
  void incrementSet(TeamIndex team) {
    // add one to the set already stored
    int point = super.getPoint(LEVEL_SET, team) + 1;
    // set this back on the score
    super.setPoint(LEVEL_SET, team, point);
    // and handle this
    onSetIncremented(team, point);
  }

  void onPointIncremented(TeamIndex team, int point) {
    TeamIndex otherTeam = setup.getOtherTeam(team);
    int otherPoint = getTeamPoints(setup.getOtherTeam(team));
    int pointsAhead = point - otherPoint;
    // as soon as a point is played, you cannot change the server in the team
    _isTeamServerChangeAllowed = false;
    // has this team won the game with this point addition (can't be the other)
    if (false == _isInTieBreak) {
      if (setup.isSuddenDeathOnDeuce &&
          point == otherPoint &&
          point >= TennisPoint.forty.val()) {
        // this is a draw in points, not enough to win but we are interested
        // if this is a deciding point, in order to tell people
        state.addStateChange(ScoreChange.decidingPoint);
        // this is a break point for the receiving team
        incrementBreakPoint(setup.getOtherTeam(servingTeam));
      } else {
        // now that is out of the way we can deal with actually winning the match
        if (point >= POINTS_TO_WIN_GAME &&
            ((setup.isSuddenDeathOnDeuce && pointsAhead > 0) ||
                pointsAhead >= POINTS_AHEAD_IN_GAME)) {
          // we have enough points to win, either we are 2 ahead (won the ad)
          // or the deuce deciding point is on and we are 2 ahead
          incrementGame(team);
        } else {
          // if we just behind the points required to win the game and
          // are just one behind, this is a break-point.
          if (servingTeam == team) {
            // team two are receiving, are they about to break?
            if (otherPoint >= POINTS_TO_WIN_GAME - 1 &&
                otherPoint - point >= POINTS_AHEAD_IN_GAME - 1) {
              // they are about to win on this one
              incrementBreakPoint(otherTeam);
            }
          } else if (point >= POINTS_TO_WIN_GAME - 1 &&
              pointsAhead >= POINTS_AHEAD_IN_GAME - 1) {
            // team one are receiving and they are about to win on this one
            incrementBreakPoint(team);
          }
        }
      }
    } else {
      // are in a tie
      if (point >= POINTS_TO_WIN_TIE && pointsAhead >= POINTS_AHEAD_IN_TIE) {
        // in a tie and we have enough points (and enough ahead) to win the game
        // and move the game on
        incrementGame(team);
      } else {
        // we didn't win the tie, but are we about to?
        /*
                    *
                    * I don't think winning a tie is a break... it's a mini-break.
                    *
                    if (point >= POINTS_TO_WIN_TIE - 1 && pointsAhead >= POINTS_AHEAD_IN_TIE - 1) {
                        // one more point and we will have won this, this is a break-point
                        // if we are not serving, check this
                        incrementPotentialBreakPoint(team);
                    }*/
        // after the first, and subsequent two points, we have to change servers in a tie
        int playedPoints = getPlayedPoints();
        if ((playedPoints - 1) % 2 == 0) {
          // we are at point 1, 3, 5, 7 etc - change server
          changeServer();
        }
        // also change ends every 6 points
        if (playedPoints % 6 == 0) {
          // the set ended with
          changeEnds();
        }
      }
    }
  }

  void onGameWon(TeamIndex team) {
    if (false == _isInTieBreak &&
        false ==
            setup.isPlayerInTeam(team, servingPlayers[servingTeam.index])) {
      // the server is not in the winning team (not in a tie), this is a converted break
      ++breakPointsConverted[team.index];
      state.addStateChange(ScoreChange.breakPointConverted);
    }
  }

  void onGameIncremented(TeamIndex team, int point) {
    // clear the points
    super.clearLevel(LEVEL_POINT);
    // and handle if this won a set
    bool isSetChanged = false;
    int gamesPlayed = getPlayedGames(-1);
    if (point >= TennisMatchSetup.gamesValue(setup.games)) {
      // this team have enough games to win, as long as the other don't have too many...
      TeamIndex other = setup.getOtherTeam(team);
      int otherPoints = getGames(other, -1);
      if ((_isInTieBreak && point != otherPoints) ||
          point - otherPoints >= GAMES_AHEAD_IN_SET) {
        // they are enough games ahead (2) so they have won
        incrementSet(team);
        // won the set, this is the end of the tie break
        _isInTieBreak = false;
        isSetChanged = true;
      } else if (isTieBreak(point, otherPoints)) {
        // we are not ahead enough, we both have more than 6 and are in a tie break set
        // time to initiate a tie break
        _isInTieBreak = true;
        // inform listeners of this change
        state.addStateChange(ScoreChange.tieBreak);
        // record that this current set was settled with a tie
        tieBreakSets.add(getPlayedSets());
      }
    }
    if (false == isScoreCompleted()) {
      // every game we alternate the server
      changeServer();
      if (_isInTieBreak && null == _tieBreakServer) {
        // we just set the server to serve but we need to remember who starts
        _tieBreakServer = servingPlayers[servingTeam.index];
      }
    }
    _isTeamServerChangeAllowed = gamesPlayed == 1 &&
        setup.singlesDoubles == MatchSinglesDoubles.doubles &&
        getPlayedSets() == 0;
    if (isSetChanged) {
      // we want to change ends at the end of any set in which the score wasn't even
      if (gamesPlayed % 2 != 0) {
        // the set ended with odd number of games
        changeEnds();
      }
    } else {
      // we want to change ends at the end of the first, 3, 5 (every odd game) of each set
      if ((gamesPlayed - 1) % 2 == 0) {
        // this is an odd game, change ends
        changeEnds();
      }
    }
  }

  void onSetIncremented(TeamIndex team, int point) {
    // clear the games
    super.clearLevel(LEVEL_GAME);
  }

  void incrementBreakPoint(TeamIndex team) {
    // this is a break-point - increment the counter and inform the listeners
    ++breakPoints[team.index];
    state.addStateChange(ScoreChange.breakPoint);
  }

  int getPlayedPoints() {
    int playedPoints = 0;
    for (int i = 0; i < Score.teamCount; ++i) {
      playedPoints += getTeamPoints(TeamIndex.values[i]);
    }
    return playedPoints;
  }

  int getPlayedGames(int setIndex) {
    int playedGames = 0;
    for (int i = 0; i < Score.teamCount; ++i) {
      playedGames += getGames(TeamIndex.values[i], setIndex);
    }
    return playedGames;
  }

  int getPlayedSets() {
    int playedSets = 0;
    for (int i = 0; i < Score.teamCount; ++i) {
      playedSets += getSets(TeamIndex.values[i]);
    }
    return playedSets;
  }

  bool isInTieBreak() {
    return _isInTieBreak;
  }

  bool isTeamServerChangeAllowed() {
    return _isTeamServerChangeAllowed;
  }

  bool isTieBreak(int games1, int games2) {
    // we are in a tie break set if not the final set, or the final set is a tie-break set
    if (games1 != games2) {
      // not equal - not a tie
      return false;
    } else if (getPlayedSets() ==
        TennisMatchSetup.setsValue(getSetsToPlay()) - 1) {
      // we are playing the final set
      if (setup.finalSetTieGame <= 0) {
        // we never tie
        return false;
      } else {
        // have we played enough games to initiate a tie?
        return games1 >= setup.finalSetTieGame;
      }
    } else {
      // not the final set, this is a tie if we played enough games
      return games1 >= TennisMatchSetup.gamesValue(setup.games);
    }
  }

  @override
  void changeServer() {
    // the current server must yield now to the one
    if (!_isInTieBreak && null != _tieBreakServer) {
      // we were in a tie break, the next server should be the one after the player
      // that started the tie break, set the server back to the player that started it
      setServer(_tieBreakServer);
      _tieBreakServer = null;
    } else {
      // or let the base change the server
      super.changeServer();
    }
  }
}
