import 'package:multiphone/match/badminton/badminton_match_setup.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/match/score.dart';
import 'package:multiphone/match/score_state.dart';

class BadmintonScore extends Score<BadmintonMatchSetup> {
  static const LEVEL_POINT = 0;
  static const LEVEL_GAME = 1;

  static const POINTS_AHEAD_IN_GAME = 2;

  static const K_LEVELS = 2;
  bool _isTeamServerChangeAllowed = true;

  BadmintonScore(BadmintonMatchSetup setup) : super(setup, K_LEVELS);

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
  void resetScore() {
    // let the base reset
    super.resetScore();
    // and reset any stats we have here
    _isTeamServerChangeAllowed = true;
  }

  @override
  int getScoreGoal() {
    return BadmintonMatchSetup.gamesValue(setup.games);
  }

  @override
  bool isScoreCompleted() {
    bool isMatchOver = false;
    int targetGames = ((getScoreGoal() + 1.0) / 2.0).floor();
    // return if a player has reached the number of games required (this is just over half)
    for (int i = 0; i < Score.teamCount; ++i) {
      if (getGames(TeamIndex.values[i]) >= targetGames) {
        // this team has reached the limit, match is over
        isMatchOver = true;
      }
    }
    return isMatchOver;
  }

  int getPoints(TeamIndex team) {
    return super.getPoint(LEVEL_POINT, team);
  }

  int getGames(TeamIndex team) {
    return super.getPoint(LEVEL_GAME, team);
  }

  int getPlayedGames() {
    int playedGames = 0;
    for (int i = 0; i < Score.teamCount; ++i) {
      playedGames += getGames(TeamIndex.values[i]);
    }
    return playedGames;
  }

  int getPlayedPoints(int gameIndex) {
    int playedPoints = 0;
    for (int i = 0; i < Score.teamCount; ++i) {
      playedPoints += getGamePoints(TeamIndex.values[i], gameIndex);
    }
    return playedPoints;
  }

  int getGamePoints(TeamIndex team, int gameIndex) {
    // get the games for the set index specified
    int toReturn;
    List<List<int>> pointResults = super.getPointHistory(LEVEL_POINT);
    if (null == pointResults ||
        gameIndex < 0 ||
        gameIndex >= pointResults.length) {
      // there is no history for this game, return the current games instead
      toReturn = super.getPoint(LEVEL_POINT, team);
    } else {
      List<int> gamePoints = pointResults.elementAt(gameIndex);
      toReturn = gamePoints[team.index];
    }
    return toReturn;
  }

  bool isTeamServerChangeAllowed() {
    return _isTeamServerChangeAllowed;
  }

  @override
  int incrementPoint(TeamIndex team, int level) {
    // let the base do it's thing
    int point = super.incrementPoint(team, level);
    // but we have to handle things specially here
    switch (level) {
      case LEVEL_POINT:
        onPointIncremented(team, point);
        break;
      case LEVEL_GAME:
        onGameIncremented(team, point);
        break;
    }
    return point;
  }

  int incrementGame(TeamIndex team) {
    // add one to the game already stored
    int games = super.getPoint(LEVEL_GAME, team) + 1;
    // set this back on the score
    super.setPoint(LEVEL_GAME, team, games);
    // and perform actions on this
    onGameIncremented(team, games);
    // and return the games
    return games;
  }

  void onPointIncremented(TeamIndex team, int point) {
    TeamIndex otherTeam = setup.getOtherTeam(team);
    int otherPoint = getPoints(otherTeam);
    int pointsAhead = point - otherPoint;
    int totalGames = getGames(team) + getGames(setup.getOtherTeam(team));
    // as soon as a point is played, you cannot change the server in the team
    _isTeamServerChangeAllowed = false;

    int gameDecider = BadmintonMatchSetup.deciderValue(setup.decidingPoint);
    if (gameDecider > 0 && point == gameDecider && otherPoint == gameDecider) {
      // we are at the deciding point
      state.addStateChange(ScoreChange.decidingPoint);
    }
    if (team != getServingTeam()) {
      // the server just lost their point, change the server away from them
      changeServer();
    }
    // has this team won the game with this new point addition (can't be the other)
    if ((gameDecider > 0 && point > gameDecider) ||
        (point >= BadmintonMatchSetup.pointsValue(setup.points) &&
            pointsAhead >= POINTS_AHEAD_IN_GAME)) {
      // we have enough points to win
      incrementGame(team);
      // do we change ends?
      if (totalGames != getScoreGoal() - 1) {
        // we are not in the final game, change ends when we win a game
        changeEnds();
      }
    } else if (totalGames == getScoreGoal() - 1) {
      // game not won, but we are in the last game
      // in the last game we change ends at half way
      if (otherPoint < point &&
          point == (BadmintonMatchSetup.pointsValue(setup.points) + 1) / 2) {
        // if 11 points to win then change when either player hits 6 ((11 + 1) / 2 == 6)
        changeEnds();
      }
    }
  }

  void onGameIncremented(TeamIndex team, int games) {
    // clear the points
    super.clearLevel(LEVEL_POINT);
  }
}
