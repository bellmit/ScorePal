import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';

enum SportType {
  TENNIS,
  BADMINTON,
  PING_PONG,
}

class Sport {
  final SportType id;
  final String Function(BuildContext context) title;
  final IconData icon;
  final String image;

  const Sport(
      {@required this.id,
      @required this.title,
      @required this.icon,
      @required this.image});

  get name {
    switch (this.id) {
      case SportType.TENNIS:
        return "Tennis";
      case SportType.BADMINTON:
        return "Badminton";
      case SportType.PING_PONG:
        return "Ping Pong";
      default:
        return "Unknown";
    }
  }
}

class Sports with ChangeNotifier {
  final List<Sport> available;

  Sports() : available = _validSports();

  static Sport sport(SportType type) {
    return _validSports().firstWhere((element) => element.id == type);
  }

  static Sport find(int index) {
    return _validSports()[index];
  }

  static int index(Sport sport) {
    List<Sport> validSports = _validSports();
    for (int i = 0; i < validSports.length; ++i) {
      if (sport == validSports[i]) {
        return i;
      }
    }
    return -1;
  }

  static List<Sport> _validSports() {
    return [
      Sport(
          id: SportType.TENNIS,
          title: (ctx) => Values(ctx).strings.sport_tennis,
          icon: Icons.ac_unit,
          image: 'tennis.jpg'),
      Sport(
          id: SportType.BADMINTON,
          title: (ctx) => Values(ctx).strings.sport_badminton,
          icon: Icons.phone,
          image: 'badminton.jpg'),
      Sport(
          id: SportType.PING_PONG,
          title: (ctx) => Values(ctx).strings.sport_ping_pong,
          icon: Icons.face,
          image: 'ping_pong.jpg'),
    ];
  }
}
