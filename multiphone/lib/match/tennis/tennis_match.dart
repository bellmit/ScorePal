import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/match/point.dart';
import 'package:multiphone/match/tennis/tennis_point.dart';
import 'package:multiphone/match/tennis/tennis_match_setup.dart';
import 'package:multiphone/match/tennis/tennis_score.dart';
import 'package:multiphone/match/tennis/tennis_match_speaker.dart';
import 'package:multiphone/match/tennis/tennis_match_writer.dart';

class TennisMatch extends ActiveMatch<TennisMatchSetup, TennisScore> {
  TennisMatch(TennisMatchSetup matchSetup)
      : super(matchSetup, new TennisScore(matchSetup), TennisMatchSpeaker(),
            TennisMatchWriter());

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
    score.incrementPoint(team, TennisScore.LEVEL_POINT);
    // and inform all listeners of this change now that it is complete
    notifyListeners();
  }

  void incrementGame(TeamIndex team) {
    // reset the last state
    score.resetState();
    // affect the change
    score.incrementPoint(team, TennisScore.LEVEL_GAME);
    // and inform all listeners of this change now that it is complete
    notifyListeners();
  }

  void incrementSet(TeamIndex team) {
    // reset the last state
    score.resetState();
    // affect the change
    score.incrementPoint(team, TennisScore.LEVEL_SET);
    // and inform all listeners of this change now that it is complete
    notifyListeners();
  }

  @override
  Point getDisplayPoint(int level, TeamIndex team) {
    Point displayPoint;
    if (!isInTieBreak() && level == TennisScore.LEVEL_POINT) {
      // points are special, handle them here
      int points = score.getTeamPoints(team);
      TeamIndex opposition = getSetup().getOtherTeam(team);
      int otherPoints = score.getTeamPoints(opposition);
      switch (points) {
        case 0: // love
        case 1: // 15
        case 2: // 30
          // we are less than 40, just return the string from the array
          displayPoint = TennisPoint.fromVal(points);
          break;
        case 3:
          // we have 40, if the other player has 40 too, we are at deuce
          if (otherPoints == 3) {
            // this is 40-40
            displayPoint = TennisPoint.deuce;
          } else {
            // they have fewer, or advantage, we just have 40
            displayPoint = TennisPoint.forty;
          }
          break;
        default:
          // if we are one ahead we have advantage
          int delta = points - otherPoints;
          switch (delta) {
            case 0:
              //this is deuce
              displayPoint = TennisPoint.deuce;
              break;
            case 1:
              // we have ad
              displayPoint = TennisPoint.advantage;
              break;
            case -1:
              // we are disadvantaged
              displayPoint = TennisPoint.forty;
              break;
            default:
              // we are far enough ahead to have won the game
              displayPoint = TennisPoint.game;
              break;
          }
      }
    } else {
      // do at the base does
      displayPoint = super.getDisplayPoint(level, team);
    }
    return displayPoint;
  }

  PointPair getPoints(int setIndex, int gameIndex) {
    List<int> points = score.getSetPoints(setIndex, gameIndex);
    return PointPair(SimplePoint(points[0]), SimplePoint(points[1]));
  }

  int getPointsTotal(int level, TeamIndex team) {
    return score.getPointsTotal(level, team);
  }

  Point getGames(TeamIndex team, int setIndex) {
    return new SimplePoint(score.getGames(team, setIndex));
  }

  int getPlayedSets() {
    return score.getPlayedSets();
  }

  int getPlayedGames(int setIndex) {
    return score.getPlayedGames(setIndex);
  }

  bool isSetTieBreak(int setIndex) {
    return score.isSetTieBreak(setIndex);
  }

  int getBreakPoints(TeamIndex team) {
    return score.getBreakPoints(team);
  }

  int getBreakPointsConverted(TeamIndex team) {
    return score.getBreakPointsConverted(team);
  }

  bool isInTieBreak() {
    return score.isInTieBreak();
  }
}
