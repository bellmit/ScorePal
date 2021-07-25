import 'package:multiphone/match/badminton/badminton_match_setup.dart';
import 'package:multiphone/match/badminton/badminton_score.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/match/point.dart';
import 'package:multiphone/match/badminton/badminton_match_speaker.dart';
import 'package:multiphone/match/badminton/badminton_match_writer.dart';

class BadmintonMatch extends ActiveMatch<BadmintonMatchSetup, BadmintonScore> {
  BadmintonMatch(BadmintonMatchSetup matchSetup)
      : super(matchSetup, new BadmintonScore(matchSetup),
            BadmintonMatchSpeaker(), BadmintonMatchWriter());

  @override
  void storeJSONData(Map<String, Object> data) {
    super.storeJSONData(data);
    // store our data in this object too - only things that will not be recreated when
    // the score is replayed in this match - ie, very little
  }

  void restoreFromJSON(Map<String, Object> data, int version) {
    super.restoreFromJSON(data, version);
// and get our data from this object that we stored here
  }

  void incrementPoint(TeamIndex team) {
    // reset the last state
    score.resetState();
    // affect the change
    score.incrementPoint(team, BadmintonScore.LEVEL_POINT);
    // and inform all listeners of this change now that it is complete
    notifyListeners();
  }

  void incrementGame(TeamIndex team) {
    // reset the last state
    score.resetState();
    // affect the change
    score.incrementPoint(team, BadmintonScore.LEVEL_GAME);
    // and inform all listeners of this change now that it is complete
    notifyListeners();
  }

  Point getTeamDisplayPoint(TeamIndex team) {
    // just return the point as a string
    return SimplePoint(score.getPoints(team));
  }

  Point getDisplayGame(TeamIndex team) {
    // just return the point as a string
    return SimplePoint(score.getGames(team));
  }

  int getPointsTotal(int level, TeamIndex team) {
    return score.getPointsTotal(level, team);
  }
}
