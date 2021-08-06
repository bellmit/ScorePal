import 'package:flutter/material.dart';
import 'package:multiphone/match/match_id.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/match/point.dart';
import 'package:multiphone/match/score.dart';
import 'package:multiphone/match/score_history.dart';
import 'package:multiphone/match/match_speaker.dart';
import 'package:multiphone/match/match_writer.dart';
import 'package:multiphone/providers/player.dart';
import 'package:multiphone/providers/sport.dart';

abstract class ActiveMatch<TSetup extends ActiveSetup, TScore extends Score>
    with ChangeNotifier {
  // we have the score and the setup to track what we are doing / played
  TScore _score;
  TSetup _setup;
  // but someone might have conceded the match
  final List<bool> _conceded = List.filled(TeamIndex.values.length, false);

  DateTime _dateMatchStarted;
  int _matchTimePlayedMs;

  MatchSpeaker _speaker;
  MatchWriter _writer;

  ActiveMatch(
      TSetup matchSetup, TScore score, MatchSpeaker speaker, MatchWriter writer)
      : _setup = matchSetup,
        _score = score,
        _speaker = speaker,
        _writer = writer {
    // and the time played
    _matchTimePlayedMs = 0;
    _dateMatchStarted = DateTime.now();
  }

  TScore get score {
    return _score;
  }

  void resetMatch() {
    // reset the last state
    _score.resetState();
    // the position and server and stuff will have changed on the teams, reset them here
    _score.resetScore();
    // and the time played
    _matchTimePlayedMs = 0;
    // inform listeners so they can set the player who is starting serve, location etc.
    notifyListeners();
  }

  void applyChangedMatchSettings() {
    // this is a little different to the reset as we want to keep the score
    // so we can just restore the point history, which has the side-effect of doing just this
    _score.restorePointHistory(() {
      // every time a point is incremented inform listeners
      notifyListeners();
    });
    // reset any state as nothing actually changed
    _score.resetState();
    // inform listeners of this change to the score
    notifyListeners();
  }

  void describeLastHistoryChange(int state, String description) {
    _score.describeLastPoint(state, description);
  }

  void addMatchTimePlayed(int msPlayed) {
    _matchTimePlayedMs += msPlayed;
  }

  Map<String, Object> getData() {
    // save all our data
    final data = Map<String, Object>();
    data["secs"] = (_matchTimePlayedMs / 1000.0).floor();
    // and if anyone conceded
    for (int i = 0; i < _conceded.length; ++i) {
      data['conceded_${i + 1}'] = _conceded[i] ?? false;
    }
    // most importantly store the score so we can re-establish the state of this match
    // when we reload it
    data["score"] = _score.getData();
    // and return the data
    return data;
  }

  void setData(MatchId matchId, Map<String, Object> data) {
    // Id first
    _dateMatchStarted = matchId.getDate();
    _matchTimePlayedMs = (data["secs"] as int) * 1000;
    for (int i = 0; i < _conceded.length; ++i) {
      _conceded[i] = data['conceded_${i + 1}'] ?? false;
    }
    //this.playedLocation = LocationWrapper().deserialiseFromString(data.getString("locn")).content;
    // most importantly we want to put the score in. Then we can replay the score to
    // put the state of this match back to how it was when we saved it
    _score.restoreFromData(data["score"], () {
      // every time a point is incremented inform listeners
      notifyListeners();
    });
    // this, obviously, changes the data
    notifyListeners();
  }

  bool isMatchPlayStarted() {
    // we are started if there are points at the top level for either team
    for (int i = 0; i < getScoreLevels(); ++i) {
      if (_score.getPointsTotal(i, TeamIndex.T_ONE) > 0 ||
          _score.getPointsTotal(i, TeamIndex.T_TWO) > 0) {
        // someone has scored something
        return true;
      }
    }
    // if here, there is no score on any level
    return false;
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

  bool isMatchOver({bool isCheckConceded = true}) {
    if (isCheckConceded && isMatchConceded) {
      return true;
    } else {
      // just return if the score is over
      return _score.isScoreCompleted();
    }
  }

  int getMatchTimePlayedMs() {
    return _matchTimePlayedMs;
  }

  TSetup getSetup() {
    return _setup;
  }

  Sport getSport() {
    return _setup.sport;
  }

  DateTime getDateMatchStarted() {
    return _dateMatchStarted;
  }

  void setDateMatchStarted(DateTime value) {
    _dateMatchStarted = value == null ? _dateMatchStarted : value;
  }

  int getPoint(int level, TeamIndex team) {
    return _score.getPoint(level, team);
  }

  Point getDisplayPoint(int level, TeamIndex team) {
    return SimplePoint(getPoint(level, team));
  }

  String getLevelTitle(int level, BuildContext context) {
    return _writer.getLevelTitle(level, context);
  }

  int getScoreLevels() {
    return _score.getLevels();
  }

  TeamIndex getMatchWinner() {
    // first check to see if anyone conceded
    if (isMatchConceded) {
      // someone quit - the winner is the one that didn't
      return TeamIndex.values
          .firstWhere((element) => _conceded[element.index] == false);
    } else {
      // return whoever is winning in the score
      return _score.getScoreWinner(_score.getLevels() - 1);
    }
  }

  List<HistoryValue> getWinnersHistory() {
    return _score.getWinnersHistory();
  }

  /*
  //TODO set the location of the match played
  Location getPlayedLocation() {
    return this.playedLocation;
  }

  void setPlayedLocation(Location currentLocation) {
    this.playedLocation = currentLocation;
  }*/

  void undoLastPoint() {
    // reset the last state
    _score.resetState();
    // affect the change
    TeamIndex undoTeam = _score.undoLastPoint(() {
      // inform listeners of this reconstruction of the undo stack
      notifyListeners();
    });
    if (null != undoTeam) {
      // and inform all listeners of this change now that it is complete
      notifyListeners();
    }
  }

  /*
    this is a private function so that it is done from the input which we trust,
    incrementing requires care and usually a derived class to do it
     */
  void incrementPoint(TeamIndex team) {
    // reset the last state
    _score.resetState();
    // affect the change
    _score.incrementPoint(team, 0);
    // and inform all listeners of this change now that it is complete
    notifyListeners();
  }

  /*
  void onControllerInput(Controller.ControllerAction selectedAction) {
    // reset any state built up at this point
    _score.resetState();
    //TODO now do the action that we received from the controller
    switch (selectedAction) {
      case PointServer:
        if (!isMatchOver()) {
          incrementPoint(_score.getServingTeam());
        }
        break;
      case PointReceiver:
        if (!isMatchOver()) {
          incrementPoint(_setup.getOtherTeam(_score.getServingTeam()));
        }
        break;
      case PointTeamOne:
        if (!isMatchOver()) {
          incrementPoint(TeamIndex.T_ONE);
        }
        break;
      case PointTeamTwo:
        if (!isMatchOver()) {
          incrementPoint(TeamIndex.T_TWO);
        }
        break;
      case UndoLastPoint:
        undoLastPoint();
        break;
      case AnnouncePoints:
        // announce the current score then
        MatchService service = MatchService.GetRunningService();
        if (null != service) {
          service.speakSpecialMessage(createPointsAnnouncement(service));
        }
        break;
    }
  }
   */

  void endMatch() {
    // end this match here
  }

  TeamIndex getServingTeam() {
    return _score.getServingTeam();
  }

  PlayerIndex getServingPlayer() {
    return _score.getServingPlayer();
  }

  String getDescription(DescriptionLevel level, BuildContext context) {
    return _writer.getDescription(this, level, context);
  }

  String getStateCurrentDescription(BuildContext context) {
    return getStateDescription(context, _score.state.getState());
  }

  String getStateDescription(BuildContext context, int state) {
    return _writer.getStateDescription(context, state);
  }

  String getSpokenStateMessage(BuildContext context) {
    return _speaker.getSpeakingStateMessage(context, this, _score.state);
  }

  String createPointsPhrase(BuildContext context, TeamIndex team, int level) {
    return _speaker.createPointsPhrase(this, context, team, level);
  }

  String createScorePhrase(BuildContext context, TeamIndex team, int level) {
    return _speaker.createScorePhrase(this, context, team, level);
  }

  String createPointsAnnouncement(BuildContext context) {
    return _speaker.createPointsAnnouncement(this, context);
  }
}
