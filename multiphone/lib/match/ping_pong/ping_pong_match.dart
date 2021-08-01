import 'package:async/async.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/match/ping_pong/ping_pong_match_setup.dart';
import 'package:multiphone/match/ping_pong/ping_pong_score.dart';
import 'package:multiphone/match/point.dart';
import 'package:multiphone/match/ping_pong/ping_pong_match_speaker.dart';
import 'package:multiphone/match/ping_pong/ping_pong_match_writer.dart';

class PingPongMatch extends ActiveMatch<PingPongMatchSetup, PingPongScore> {
  bool _expediteMinutesElapsed = false;
  bool _announceExpediteSystem = false;

  CancelableOperation _expediteOperation;

  PingPongMatch(PingPongMatchSetup matchSetup)
      : super(
          matchSetup,
          new PingPongScore(matchSetup),
          PingPongMatchSpeaker(),
          PingPongMatchWriter(),
        );

  Map<String, Object> getData() {
    final data = super.getData();
    // put any of our data into this map

    // and return the data
    return data;
  }

  void setData(Map<String, Object> data) {
    super.setData(data);
    //and set any from ours that we saved in here
  }

  @override
  void resetMatch() {
    super.resetMatch();

    // and we can reset our data too
    cancelExpediteSystem();
  }

  bool startExpediteSystem() {
    var isSystemStarted = false;
    // change the score to be in expedite system if we have scored enough
    if (null != score &&
        _expediteMinutesElapsed &&
        !score.isExpediteSystemInEffect()) {
      // how many points are played
      int points =
          score.getPoints(TeamIndex.T_ONE) + score.getPoints(TeamIndex.T_TWO);
      PingPongMatchSetup setup = getSetup();
      if (setup.isExpediteEnabled &&
          points >=
              PingPongMatchSetup.expeditePointsValue(
                  setup.expediteSystemPoints)) {
        // there are enough points played to start, start it now
        score.setExpediteSystemInEffect(true);
        isSystemStarted = true;
      }
    }
    return isSystemStarted;
  }

  void scheduleExpediteSystem() {
    if (null == _expediteOperation) {
      _expediteOperation = CancelableOperation.fromFuture(Future.delayed(
          Duration(
            minutes: PingPongMatchSetup.expediteMinutesValue(
                getSetup().expediteSystemMinutes),
          ), () {
        // the time has elapsed
        _expediteMinutesElapsed = true;
        // this is called as the expedite system comes into effect
        if (startExpediteSystem()) {
          // this is started right now half way through anything else
          // announce it immediately
          //TODO announce this expedite system immediately please!
          /*
          MatchService service = MatchService.GetRunningService();
          if (null != service) {
            // get the string to announce
            String messageString = Values(context).
                service.getString(R.string.speak_expedite_system);
            // send this message to announce the points with this string to say right away
            service.speakSpecialMessage(messageString);
          }
          */
        }
      }));
    }
  }

  void cancelExpediteSystem() {
    if (null != _expediteOperation) {
      // cancel this expedite operation then
      _expediteOperation.cancel();
      _expediteOperation = null;
    }
    _expediteMinutesElapsed = false;
    // cancel the system in the score
    score.setExpediteSystemInEffect(false);
  }

  bool getIsAnnounceExpediteSystem() {
    return _announceExpediteSystem;
  }

  void expediteSystemAnnounced() {
    _announceExpediteSystem = false;
  }

  void incrementPoint(TeamIndex team) {
    // reset the last state
    score.resetState();
    // affect the change
    score.incrementPoint(team, PingPongScore.LEVEL_POINT);
    PingPongMatchSetup setup = getSetup();
    // when adding points we should be sure that the expedite system is started ok
    if (null == _expediteOperation && setup.isExpediteEnabled) {
      // we are playing and want to expedite at some time but we are not
      // expecting it to happen, schedule it to happen from now
      scheduleExpediteSystem();
    }
    if (_expediteMinutesElapsed) {
      // start expedite system if we have earned enough points
      if (startExpediteSystem()) {
        // this is started as points become enough, remember to announce this
        // as we announce the change in points
        _announceExpediteSystem = true;
      }
    }
    // and inform all listeners of this change now that it is complete
    notifyListeners();
  }

  void incrementRound(TeamIndex team) {
    // reset the last state
    score.resetState();
    // affect the change
    score.incrementPoint(team, PingPongScore.LEVEL_ROUND);
    // when the round is won, we need to reset the old expedite system
    cancelExpediteSystem();
    // and inform all listeners of this change now that it is complete
    notifyListeners();
  }

  Point getTeamDisplayPoint(TeamIndex team) {
    // just return the point as a string
    return SimplePoint(score.getPoints(team));
  }

  Point getDisplayRound(TeamIndex team) {
    // just return the point as a string
    return SimplePoint(score.getRounds(team));
  }

  int getPointsTotal(int level, TeamIndex team) {
    return score.getPointsTotal(level, team);
  }
}
