import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/match_setup.dart';
import 'package:multiphone/providers/team.dart';

enum TENNIS_SETS { ONE, THREE, FIVE }

class TennisMatchSetup extends MatchSetup {
  TENNIS_SETS _sets = TENNIS_SETS.THREE;

  TennisMatchSetup();

  @override
  String matchSummary(BuildContext context) {
    // construct the match summary accordingly
    final values = Values(context);
    return values.construct(values.strings.title_setup_tennis, [
      getTeamName(TeamIndex.T_ONE, context),
      getTeamName(TeamIndex.T_TWO, context),
      setsValue(_sets).toString(),
    ]);
  }

  int setsValue(TENNIS_SETS sets) {
    switch (sets) {
      case TENNIS_SETS.ONE:
        return 1;
      case TENNIS_SETS.THREE:
        return 3;
      case TENNIS_SETS.FIVE:
        return 5;
      default:
        return 0;
    }
  }

  get sets {
    return _sets;
  }

  set sets(TENNIS_SETS sets) {
    _sets = sets;
    // this is a change
    notifyListeners();
  }
}
