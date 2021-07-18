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
}

class Sports with ChangeNotifier {
  final List<Sport> available;

  Sports() : available = _validSports();

  Sport find(int index) {
    return available[index];
  }

  int index(Sport sport) {
    for (int i = 0; i < available.length; ++i) {
      if (sport == available[i]) {
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
