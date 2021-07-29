import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/match/ping_pong/ping_pong_match_setup.dart';
import 'package:multiphone/match/score.dart';
import 'package:multiphone/providers/player.dart';

class PingPongScore extends Score<PingPongMatchSetup> {
  static const LEVEL_POINT = 0;
  static const LEVEL_ROUND = 1;

  static const POINTS_AHEAD_IN_ROUND = 2;

  static const K_LEVELS = 2;

  bool _isExpediteSystemInEffect = false;
  bool _isTeamServerChangeAllowed;
  PlayerIndex _nextRoundServer;

  PingPongScore(PingPongMatchSetup setup) : super(setup, K_LEVELS);

  @override
  void storeJSONData(Map<String, Object> data) {
    super.storeJSONData(data);
// store our data in this object too - only things that will not be recreated when
// the score is replayed in this match - ie, very little
  }

  void restoreFromJSON(Map<String, Object> data, int version,
      void Function() onPointIncremented) {
    super.restoreFromJSON(data, version, onPointIncremented);
    // and get our data from this object that we stored here
  }

  @override
  int getScoreGoal() {
    return PingPongMatchSetup.roundsValue(setup.rounds);
  }

  @override
  void resetScore() {
    // let the base reset
    super.resetScore();
    // and reset our data
    _isExpediteSystemInEffect = false;
    _isTeamServerChangeAllowed = false;
    _nextRoundServer = getNextServer();
  }

  void setExpediteSystemInEffect(bool isInEffect) {
    _isExpediteSystemInEffect = isInEffect;
  }

  bool isExpediteSystemInEffect() {
    return _isExpediteSystemInEffect;
  }

  @override
  bool isMatchOver() {
    bool isMatchOver = false;
    int targetRounds = ((getScoreGoal() + 1.0) / 2.0).floor();
    // return if a player has reached the number of rounds required (this is just over half)
    for (int i = 0; i < Score.teamCount; ++i) {
      if (getRounds(TeamIndex.values[i]) >= targetRounds) {
        // this team has reached the limit, match is over
        isMatchOver = true;
      }
    }
    return isMatchOver;
  }

  int getPoints(TeamIndex team) {
    return super.getPoint(LEVEL_POINT, team);
  }

  int getRounds(TeamIndex team) {
    return super.getPoint(LEVEL_ROUND, team);
  }

  bool isTeamServerChangeAllowed() {
    return _isTeamServerChangeAllowed;
  }

  @override
  int incrementPoint(TeamIndex team, int level) {
    // do the work
    int point = super.incrementPoint(team, level);
    // but we have to handle things specially here
    switch (level) {
      case LEVEL_POINT:
        onPointIncremented(team, point);
        break;
      case LEVEL_ROUND:
        onRoundIncremented(team, point);
        break;
    }
    return point;
  }

/// only call this privately as a game is won by winning points to prevent it going in the history
/// as some user entry that they won the set
/// @param team is the team that has won the game
  void incrementRound(TeamIndex team) {
    // add one to the rounds already stored
    int rounds = super.getPoint(LEVEL_ROUND, team) + 1;
    // set this back on the score
    super.setPoint(LEVEL_ROUND, team, rounds);
    // and handle this
    onRoundIncremented(team, rounds);
  }

  int onPointIncremented(TeamIndex team, int point) {
    TeamIndex otherTeam = setup.getOtherTeam(team);
    int otherPoint = getPoints(otherTeam);
    int pointsAhead = point - otherPoint;
    // as soon as a point is played, you cannot change the server in the team
    _isTeamServerChangeAllowed = false;
    // started playing, remember who should start the next round
    if (null == _nextRoundServer) {
      _nextRoundServer = getNextServer();
    }
    // has this team won the round with this new point addition (can't be the other)
    if (point >= PingPongMatchSetup.pointsValue(setup.points) &&
        pointsAhead >= POINTS_AHEAD_IN_ROUND) {
      // we have enough points to win
      incrementRound(team);
    } else {
      int roundsPlayed = getRounds(getServingTeam()) +
          getRounds(setup.getOtherTeam(getServingTeam()));
      int totalPoints = point + otherPoint;
      if (roundsPlayed == getScoreGoal() - 1 // last game
          &&
          point ==
              (PingPongMatchSetup.pointsValue(setup.points) - 1) /
                  2 // reached 5 points
          &&
          otherPoint < point) {
        // the other player didn't already
        // this is the last round, we want to change ends when someone makes 5 points
        changeEnds();
      }
      int decidingPoint = setup.decidingPoint;
      // every two points we change server, or every point if expediting
      if (_isExpediteSystemInEffect // expedite system
          ||
          (point >= decidingPoint &&
              otherPoint >= decidingPoint) // both have at least 10 points
          ||
          totalPoints % 2 == 0) {
        // served 2 already
        // change server
        changeServer();
      }
    }
    return point;
  }

  void onRoundIncremented(TeamIndex team, int rounds) {
    // clear the points
    super.clearLevel(LEVEL_POINT);
    // no longer expediting, new round
    _isExpediteSystemInEffect = false;

    if (false == isMatchOver()) {
      // every game we change ends
      changeEnds();
    }
    if (_nextRoundServer != getServingPlayer()) {
      // the sever is not correct, change the server
      setServer(_nextRoundServer);
    }
    // remember the next one from here now we have changed
    _nextRoundServer = getNextServer();
  }
}
