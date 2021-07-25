import 'dart:math';

import 'package:multiphone/providers/active_setup.dart';

class ScoreChange {
  static final none = ScoreChange(0);
  static final increment = ScoreChange(1);
  static final server = ScoreChange(2);
  static final ends = ScoreChange(4);
  static final decidingPoint = ScoreChange(8);
  static final tieBreak = ScoreChange(16);
  static final decrement = ScoreChange(32);
  static final breakPointConverted = ScoreChange(64);
  static final breakPoint = ScoreChange(128);
  static final incrementRedo = ScoreChange(256);

  final int val;
  static final List<ScoreChange> values = [
    none,
    increment,
    server,
    ends,
    decidingPoint,
    tieBreak,
    decrement,
    breakPointConverted,
    breakPoint,
    incrementRedo,
  ];

  ScoreChange(int val) : val = val;
}

class ScoreState {
  int _currentState;
  TeamIndex _teamChanged;
  int _levelChanged;

  ScoreState() {
    reset();
  }

  void reset() {
    _currentState = 0;
    _teamChanged = null;
    _levelChanged = -1;
  }

  get isEmpty {
    return _currentState == ScoreChange.none.val;
  }

  void addStateChange(ScoreChange change) {
    _currentState |= change.val;
  }

  void addChange(ScoreChange change, TeamIndex team, int level) {
    addStateChange(change);
    _teamChanged = team;
    _levelChanged = max(level, _levelChanged);
  }

  static bool changed(int state, ScoreChange change) {
    return 0 != (state & change.val);
  }

  bool isChanged(ScoreChange change) {
    return 0 != (_currentState & change.val);
  }

  List<ScoreChange> getChanges() {
    // find all the activated changes and return as a list
    List<ScoreChange> list = [];
    for (int i = 0; i < ScoreChange.values.length; ++i) {
      ScoreChange change = ScoreChange.values[i];
      if (isChanged(change)) {
        list.add(change);
      }
    }
    return list;
  }

  int getState() {
    return _currentState;
  }

  TeamIndex getTeamChanged() {
    return _teamChanged;
  }

  int getLevelChanged() {
    return _levelChanged;
  }

  void setState(int state, int levelChanged, TeamIndex teamChanged) {
    _currentState = state;
    _levelChanged = levelChanged;
    _teamChanged = teamChanged;
  }
}
