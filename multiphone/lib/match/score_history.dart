import 'dart:core';
import 'dart:math';

import 'package:multiphone/helpers/class_stack.dart';
import 'package:multiphone/match/match_setup.dart';

enum Importance {
  LOW,
  MEDIUM,
  HIGH,
}

class HistoryValue {
  final TeamIndex team;
  final int level;
  int state;
  int topLevelChanged;
  String scoreString;

  HistoryValue(TeamIndex team, int level, int state)
      : team = team,
        level = level,
        state = state,
        topLevelChanged = level,
        scoreString = '';

  HistoryValue copy() {
    HistoryValue copy = HistoryValue(team, level, state);
    copy.topLevelChanged = topLevelChanged;
    copy.scoreString = scoreString;
    return copy;
  }

  void recordTopLevel(int level) {
    topLevelChanged = max(topLevelChanged, level);
  }
}

class ScoreHistory {
  static Importance fromValue(level) {
    return level <= 0
        ? Importance.LOW
        : level == 1
            ? Importance.MEDIUM
            : Importance.HIGH;
  }

  final ClassStack<HistoryValue> _levelHistory = ClassStack();

  ScoreHistory();

  void clear() {
    _levelHistory.clear();
  }

  get isEmpty {
    return _levelHistory.isEmpty;
  }

  get size {
    return _levelHistory.length;
  }

  HistoryValue get(int index) {
    return _levelHistory.get(index);
  }

  HistoryValue pop() {
    return _levelHistory.pop();
  }

  void push(TeamIndex team, int level, int state) {
    _levelHistory.push(HistoryValue(team, level, state));
  }

  void describe(int newState, String description) {
    if (!_levelHistory.isEmpty) {
      // set the last item description
      HistoryValue historyValue = _levelHistory.peek();
      if (newState >= 0) {
        // use this new state
        historyValue.state = newState;
      }
      // and the description
      historyValue.scoreString = description;
    }
  }

  void measureLevel(int level) {
    if (!_levelHistory.isEmpty) {
      // keep the highest level changed in the last history item
      _levelHistory.peek().recordTopLevel(level);
    }
  }

  List<int> getHistoryAsPointHistory(List<int> levelStraightPoints) {
    // first we need to convert the history to a more basic point only history
    List<int> pointHistory = [];
    for (int i = 0; i < _levelHistory.length; ++i) {
      HistoryValue value = _levelHistory.get(i);
      if (value.level < levelStraightPoints.length) {
        // there is a value to represent how many points constitute a change at this level
        for (int j = 0; j < levelStraightPoints[value.level]; ++j) {
          // for each number that represents a straight win, add the team that won that point
          pointHistory.add(value.team.index);
        }
      } else {
        // just one-to-one
        pointHistory.add(value.team.index);
        print(
            "the levelStraightPoints should have the points at each level that constitute a win");
      }
    }
    // and return
    return pointHistory;
  }

  String getPointHistoryAsString(List<int> levelStraightPoints) {
    var recDataString = '';
    List<int> pointHistory = getHistoryAsPointHistory(levelStraightPoints);
    int noHistoricPoints = pointHistory.length;
    // first write the number of historic points we are going to store
    recDataString += noHistoricPoints.toString();
    recDataString += ':';
    // and then all the historic points we have
    int bitCounter = 0;
    int dataPacket = 0;
    for (int i = 0; i < noHistoricPoints; ++i) {
      // get the team as a binary value
      int binaryValue = pointHistory.elementAt(i);
      // add this value to the data packet
      dataPacket |= binaryValue << bitCounter;
      // and increment the counter, sending as radix32 number means we can store 10 bits of data (up to 1023 base 10)
      if (++bitCounter >= 10) {
        // exceeded the size for next time, send this packet
        if (dataPacket < 32) {
          // this will be print as '0' up to 'F' but we need it to be '0F' as expecting a fixed length...
          // this is true for hex - who knows how a radix32 number is printed - but whatever nice that we get 10 values
          recDataString += '0';
        }
        recDataString += dataPacket.toRadixString(32);
        // and reset the counter and data
        bitCounter = 0;
        dataPacket = 0;
      }
    }
    if (bitCounter > 0) {
      // there was data we failed to send, only partially filled - send this anyway
      if (dataPacket < 32) {
        // this will be print as '0' up to 'F' but we need it to be '0F' as expecting a fixed length...
        // this is true for hex - who knows how a radix64 number is printed - but whatever nice that we get 10 values
        recDataString += '0';
      }
      recDataString += dataPacket.toRadixString(32);
    }
    return recDataString.toString();
  }

  void restorePointHistoryFromString(String pointHistoryString) {
    // the value before the colon is the number of subsequent values
    int noHistoricPoints = extractValueToColon(pointHistoryString);
    int dataCounter = 0;
    while (dataCounter < noHistoricPoints) {
      // while there are points to get, get them
      int dataReceived = extractHistoryValue(pointHistoryString);
      // this char contains somewhere between one and eight values all bit-shifted, extract them now
      int bitCounter = 0;
      while (bitCounter < 10 && dataCounter < noHistoricPoints) {
        int bitValue = 1 & (dataReceived >> bitCounter++);
        // add this to the list of value received and inc the counter of data
        _levelHistory.push(
            // the value is a point for team one if zero and team two if 1
            new HistoryValue(
                bitValue == 0 ? TeamIndex.T_ONE : TeamIndex.T_TWO,
                // this only does points and we don't store the state like this
                0,
                0));
        // increment the counter
        ++dataCounter;
      }
    }
  }

  int extractHistoryValue(String recDataString) {
    // get the string as a double char value
    String hexString = extractChars(2, recDataString);
    return int.parse(hexString, radix: 32);
  }

  int extractValueToColon(String recDataString) {
    int colonIndex = recDataString.indexOf(":");
    if (colonIndex == -1) {
      throw new Exception('String index is out of bounds, no : discovered');
    }
    // extract this data as a string
    String extracted = extractChars(colonIndex, recDataString);
    // and the colon
    recDataString = recDataString.substring(1);
    // return the data as an integer
    return int.parse(extracted);
  }

  String extractChars(int charsLength, String recDataString) {
    String extracted;
    if (recDataString.length >= charsLength) {
      extracted = recDataString.substring(0, charsLength);
    } else {
      throw new Exception('String index is out of bounds');
    }
    recDataString = recDataString.substring(charsLength);
    return extracted;
  }
}
