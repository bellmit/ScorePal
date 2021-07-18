import 'package:multiphone/match/match_setup.dart';

enum TENNIS_SETS { ONE, THREE, FIVE }

class TennisMatchSetup extends MatchSetup {
  TENNIS_SETS _sets = TENNIS_SETS.THREE;

  TennisMatchSetup();

  get sets {
    return _sets;
  }

  set sets(TENNIS_SETS sets) {
    _sets = sets;
    // this is a change
    notifyListeners();
  }
}
