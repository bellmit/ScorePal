import 'package:intl/intl.dart';
import 'package:multiphone/match/active_match.dart';
import 'package:multiphone/providers/sport.dart';

class MatchId {
  static final DateFormat fileDateFormat = DateFormat("yyyyMMddHHmmss");

  final String _idString;

  static MatchId create(ActiveMatch match) =>
      MatchId(fileDateFormat.format(match.getDateMatchStarted()) +
          "_" +
          match.getSport().id);

  MatchId(String idString) : _idString = idString;

  bool equals(Object other) {
    if (this == other) return true;
    if (other == null || !other is MatchId) return false;
    return _idString == (other as MatchId)._idString;
  }

  @override
  String toString() {
    return _idString;
  }

  bool isValid() {
    return isMatchIdValid(_idString);
  }

  DateTime getDate() {
    return dateFromMatchId(_idString);
  }

  Sport getSport() {
    return sportFromMatchId(_idString);
  }

  static DateTime dateFromMatchId(String matchId) {
    DateTime played;
    if (null != matchId) {
      int sportSep = matchId.indexOf('_');
      String dateString = matchId;
      if (sportSep >= 0) {
        // there is an underscore, after this is the sport, before is the date
        dateString = matchId.substring(0, sportSep);
      }
      try {
        played = fileDateFormat.parse(dateString);
      } catch (error) {
        print("Failed to create the match date from the match id " +
            matchId +
            error.toString());
      }
    }
    return played;
  }

  static Sport sportFromMatchId(String matchId) {
    Sport sport;
    if (null != matchId) {
      int sportSep = matchId.indexOf('_');
      String sportString = matchId;
      if (sportSep >= 0) {
        // there is an underscore, after this is the sport, before is the date
        sportString = matchId.substring(sportSep + 1);
      }
      try {
        sport = Sports.fromId(sportString);
      } catch (error) {
        print("Failed to create the sport from the match id " +
            matchId +
            error.toString());
      }
    }
    return sport;
  }

  static bool isMatchIdValid(String matchId) {
    bool isValid = false;
    try {
      fileDateFormat.parse(matchId);
      isValid = true;
    } catch (error) {
      // whatever, just isn't valid is all
    }
    return isValid;
  }

  static bool isFileDatesSame(DateTime fileDate1, DateTime fileDate2) {
    // compare only up to seconds as only up to seconds stored in the filename
    // for simplicities sake we can use the same formatter we use for the filename and compare strings
    if (fileDate1 != null && fileDate2 == null) {
      return false;
    } else if (fileDate1 == null && fileDate2 != null) {
      return false;
    } else if (fileDate1 == null && fileDate2 == null) {
      return true;
    } else {
      // do the actual comparing then
      String stringDate1 = fileDateFormat.format(fileDate1);
      String stringDate2 = fileDateFormat.format(fileDate2);
      return stringDate1 == stringDate2;
    }
  }
}
