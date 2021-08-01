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
