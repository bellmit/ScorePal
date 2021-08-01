import 'dart:core';

import 'package:multiphone/helpers/log.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/match/score_history.dart';
import 'package:multiphone/match/score_state.dart';
import 'package:multiphone/providers/player.dart';

abstract class Score<S extends ActiveSetup> {
  static const invalidPoint = -1;
  static const clearPoint = 0;

  static final teamCount = TeamIndex.values.length;

  final List<List<int>> _points;
  final List<List<List<int>>> _pointsHistory;

  final List<bool> _playingEnd = List.filled(teamCount, null);
  final List<bool> _conceded = List.filled(teamCount, false);

  final ScoreState state;
  final S setup;
  TeamIndex servingTeam;
  final List<PlayerIndex> servingPlayers = List.filled(teamCount, null);

  final ScoreHistory _history;

  // default access, make the users go through the match class to access the score
  Score(S setup, int pointsLevels)
      : _points =
            List.generate(pointsLevels, (index) => List.filled(teamCount, 0)),
        _pointsHistory = List.filled(pointsLevels, null),
        state = ScoreState(),
        setup = setup,
        _history = ScoreHistory() {
    // make sure everything starts off the same each time
    Log.debug('new ${setup.sport.id} score created');
    resetScore();
  }

  void resetState() {
    // as we are about to affect a change, reset the change state
    this.state.reset();
  }

  Map<String, Object> getData() {
    // save all our data
    final data = Map<String, Object>();
    // we only need to store the data that cannot be re-established from the settings
    // so basically just the actual score data
    data["pts"] = this
        ._history
        .getPointHistoryAsString(this.setup.getStraightPointsToWin());

    // we don't need to store the points at all (they are re-created from the points) but
    // other apps (a website maybe) might want to skip that and just have the resuts. So, to
    // be nice, we can put them in the JSON
    List levelsArray = [];
    for (int i = 0; i < _pointsHistory.length; ++i) {
      var level = _pointsHistory[i];
      if (level == null) {
        levelsArray.add([]);
      } else {
        // do the level by putting all the scores into an array (alternating each team)
        List scoreArray = [];
        for (int j = 0; j < level.length; ++j) {
          var score = level[j];
          for (int k = 0; k < score.length; ++k) {
            var point = score[k];
            scoreArray.add(point);
          }
        }
        // and put the array of this
        levelsArray.add(scoreArray);
      }
    }
    // and put this in the JSON
    data["scr"] = levelsArray;
    // and return this
    return data;
  }

  void restoreFromData(
      Map<String, Object> data, void Function() onPointIncremented) {
    // all we did was store the raw score points, restore from this data
    String pointsString = data["pts"] as String;
    this._history.clear();
    this._history.restorePointHistoryFromString(HistoryString(pointsString));
    // now we have the history restored, we can restore the score from it
    restorePointHistory(onPointIncremented);
  }

  void resetScore() {
    servingTeam = setup.firstServingTeam;
    for (int i = 0; i < teamCount; ++i) {
      this.servingPlayers[i] = null;
    }
    // set the starting server though
    this.servingPlayers[servingTeam.index] =
        setup.getFirstServingPlayer(this.servingTeam);
    // and the ends we start at (1 and 0)
    this._playingEnd[0] = true;
    this._playingEnd[1] = false;
    // set all the points to zero
    for (int i = 0; i < _points.length; ++i) {
      List<int> teamPoints = _points[i];
      for (int j = 0; j < teamPoints.length; ++j) {
        teamPoints[j] = 0;
      }
    }
    // clear the history lists
    for (int i = 0; i < _pointsHistory.length; ++i) {
      _pointsHistory[i] = null;
    }
    state.reset();
  }

  void concedeMatch(TeamIndex team, {isConcede = true}) {
    _conceded[team.index] = isConcede;
  }

  bool isTeamConceded(TeamIndex team) {
    return _conceded[team.index];
  }

  bool get isMatchConceded {
    for (int i = 0; i < _conceded.length; ++i) {
      if (_conceded[i]) {
        // someone conceded
        return true;
      }
    }
    return false;
  }

  int getLevels() {
    return _points.length;
  }

  TeamIndex getServingTeam() {
    return servingTeam;
  }

  PlayerIndex getServingPlayer() {
    PlayerIndex server = servingPlayers[servingTeam.index];
    if (null == server) {
      // there is no server recorded, the first server of the team is serving
      server = setup.getFirstServingPlayer(servingTeam);
    }
    // return the server, correcting any errors
    return correctServerErrors(server);
  }

  TeamIndex undoLastPoint(void Function() onPointIncremented) {
    // we want to remove the last point, this can be tricky as can effect an awful lot of things
    // like are we in a tie-break, serving end, number of sets games, etc. This is hard to undo
    // so instead we are being lazy and using the power of the device you are on. ie, reset
    // the score and re-populate based on the history
    HistoryValue historyValue;
    if (false == this._history.isEmpty) {
      // pop the last point from the history
      historyValue = _history.pop();
      // and restore the history that remains
      restorePointHistory(onPointIncremented);
      // inform listeners of this
      state.addStateChange(ScoreChange.decrement);
    }
    // return the team who's point was popped
    return historyValue != null ? historyValue.team : null;
  }

  void restorePointHistory(void Function() onPointIncremented) {
    // reset the score
    resetScore();
    // and restore the rest, adding points will create a new history - so let's clear it
    List<HistoryValue> historyValues = getWinnersHistory();
    _history.clear();
    int lastState = 0;
    int lastLevel = 0;
    TeamIndex lastTeam;
    HistoryValue value;
    for (int i = 0; i < historyValues.length; ++i) {
      // for every item (save the last one) increment the point
      value = historyValues[i];
      // increment the point - not adding to this history as we will be here a while with that
      incrementPoint(value.team, value.level);
      // store this last state
      lastState = state.getState();
      lastLevel = state.getLevelChanged();
      lastTeam = state.getTeamChanged();
      // this is actually a 'redo' so be sure to add this
      state.addStateChange(ScoreChange.incrementRedo);
      // inform listener of this action
      if (null != onPointIncremented) {
        onPointIncremented();
      }
      // and reset this state
      resetState();
    }
    // restore the very last state of what we just changed
    state.setState(lastState, lastLevel, lastTeam);
  }

  List<HistoryValue> getWinnersHistory() {
    // return the history as an array of which team won each point and the score at the time
    List<HistoryValue> toReturn;
    toReturn = List.filled(_history.size, null);
    for (int i = 0; i < _history.size; ++i) {
      toReturn[i] = _history.get(i).copy();
    }
    return toReturn;
  }

  int incrementPoint(TeamIndex team, int level) {
    // just add a point to the base level
    int point = setPoint(level, team, getPoint(level, team) + 1);
    // push this to our history stack with the latest state
    _history.push(team, level, state.getState());
    // remember this change in state
    state.addChange(ScoreChange.increment, team, level);
    // and return the point
    return point;
  }

  void describeLastPoint(int newState, String historyDescription) {
    _history.describe(newState, historyDescription);
  }

  int setPoint(int level, TeamIndex team, int point) {
    _points[level][team.index] = point;
    // remember this change in state
    state.addChange(ScoreChange.increment, team, level);
    // also remember the top level that each change performed
    _history.measureLevel(level);
    return point;
  }

  void setServer(PlayerIndex server) {
    // store the serving team
    servingTeam = setup.getPlayerTeam(server);
    // and the player serving for that team
    servingPlayers[servingTeam.index] = server;
    // inform listeners of this
    state.addStateChange(ScoreChange.server);
  }

  void clearLevel(int level) {
    // we just set the points for a level that is not the bottom, we want to store
    // the points that were the level below in the history and clear them here
    _storeHistory(level, _points[level]);
    // clear this data
    for (int i = 0; i < teamCount; ++i) {
      _points[level][i] = clearPoint;
    }
  }

  int getPoint(int level, TeamIndex team) {
    return _points[level][team.index];
  }

  void _storeHistory(int level, List<int> toStore) {
    List<List<int>> points = _pointsHistory[level];
    if (points == null) {
      points = [];
      _pointsHistory[level] = points;
    }
    // create the array of points we currently have and add to the list
    points.add([]..addAll(toStore));
  }

  List<List<int>> getPointHistory(int level) {
    List<List<int>> history = _pointsHistory[level];
    if (null != history) {
      List<List<int>> toReturn = [];
      for (int i = 0; i < history.length; ++i) {
        var points = history[i];
        if (null != points) {
          toReturn.add([]..addAll(points));
        }
      }
      return toReturn;
    } else {
      // no history for this
      return null;
    }
  }

  void changeServer() {
    // just set the next server to be the new server
    setServer(getNextServer());
  }

  void changeEnds() {
    // cycle each TeamIndex's court position
    for (int i = 0; i < teamCount; ++i) {
      _playingEnd[i] = !_playingEnd[i];
    }
    // inform listeners of this
    state.addStateChange(ScoreChange.ends);
  }

  int getScoreGoal();

  bool isMatchOver();

  bool isTeamServerChangeAllowed();

  TeamIndex getWinner(int level) {
    // first check to see if anyone conceded
    if (isMatchConceded) {
      // someone quit - the winner is the one that didn't
      return TeamIndex.values
          .firstWhere((element) => _conceded[element.index] == false);
    }
    int topTeam = 0;
    List<int> finalScore;
    while (level >= 0) {
      // check the final score at the this level
      List<List<int>> pointHistory = getPointHistory(level);
      if (null != pointHistory) {
        // the final score is the last point in the list of historic values
        finalScore = pointHistory.elementAt(pointHistory.length - 1);
      } else {
        // there is no history at this level, use what is current instead
        finalScore = [
          getPoint(level, TeamIndex.T_ONE),
          getPoint(level, TeamIndex.T_TWO)
        ];
      }
      if (null != finalScore && finalScore[0] != finalScore[1]) {
        // there is a difference here, this can be used to determine the winner
        break;
      }
      // if here then at this level we are drawing, or there is no score, check lower down.
      --level;
    }
    if (null == finalScore) {
      // need to just use the points at the lowest level as there is no history
      finalScore = _points[0];
    }
    if (null != finalScore) {
      int topPoints = invalidPoint;
      for (int i = 0; i < finalScore.length; ++i) {
        if (finalScore[i] > topPoints) {
          // this is the top TeamIndex
          topPoints = finalScore[i];
          topTeam = i;
        }
      }
    }
    return TeamIndex.values[topTeam];
  }

  PlayerIndex getNextServer() {
    // the current server must yield now to the new one
    // find the team that is serving at the moment
    PlayerIndex newServer = getServingPlayer();
    if (null != servingTeam) {
      // change team, and not the player that was last serving
      TeamIndex otherTeam = setup.getOtherTeam(servingTeam);
      if (setup.singlesDoubles == MatchSinglesDoubles.doubles) {
        // we are playing doubles, cycle the server in the team
        newServer = servingPlayers[otherTeam.index];
        if (null != newServer) {
          // this new server is the current server for the team, cycle this
          newServer = setup.getOtherPlayer(newServer);
        } else {
          // there is no server who has served yet - use the first one
          newServer = setup.getFirstServingPlayer(otherTeam);
        }
      } else {
        // just use the server of the other team
        newServer = servingPlayers[otherTeam.index];
        if (null == newServer) {
          // there isn't one served yet - just use the first
          newServer = setup.getFirstServingPlayer(otherTeam);
        }
      }
    }
    // return the server - correcting any errors
    return correctServerErrors(newServer);
  }

  PlayerIndex correctServerErrors(PlayerIndex server) {
    if (setup.singlesDoubles == MatchSinglesDoubles.singles) {
      // we are playing a singles match, fiddling with the servers can see us letting
      // the partner server, correct this here
      switch (server) {
        case PlayerIndex.P_ONE:
        case PlayerIndex.PT_ONE:
          server = PlayerIndex.P_ONE;
          break;
        case PlayerIndex.P_TWO:
        case PlayerIndex.PT_TWO:
          server = PlayerIndex.P_TWO;
          break;
      }
    }
    // return this corrected server
    return server;
  }

  int getPointsTotal(int level, TeamIndex team) {
    // add all the points for this team
    int total = getPoint(level, team);
    List<List<int>> history = getPointHistory(level);
    if (null != history) {
      // add all this history to the total
      for (int i = 0; i < history.length; ++i) {
        var points = history[i];
        total += points[team.index];
      }
    }
    return total;
  }
}
