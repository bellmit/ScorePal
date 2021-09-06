import 'package:intl/intl.dart';
import 'package:multiphone/helpers/log.dart';

class MatchStatsMonth {
  final DateTime date;
  final int losses;
  final int played;
  final int wins;
  MatchStatsMonth({this.date, this.played, this.wins, this.losses});

  get isValid => played > 0 && (wins > 0 || losses > 0);

  static DateTime dateFromFirebaseId(String firebaseId) {
    try {
      return DateFormat("yyyy-MM").parse(firebaseId);
    } catch (error) {
      Log.error(
          'failed to parse the stats month ID into a year / month $error');
      return DateTime.now();
    }
  }

  static MatchStatsMonth create(
      String firebaseId, Map<String, dynamic> firebaseData) {
    return MatchStatsMonth(
      date: dateFromFirebaseId(firebaseId),
      played: firebaseData['played'],
      wins: firebaseData['wins'],
      losses: firebaseData['losses'],
    );
  }
}
