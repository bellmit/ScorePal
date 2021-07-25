import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/badminton_match.dart';
import 'package:multiphone/match/badminton_match_setup.dart';
import 'package:multiphone/match/active_match.dart';
import 'package:multiphone/match/match_setup.dart';
import 'package:multiphone/match/ping_pong_match.dart';
import 'package:multiphone/match/ping_pong_match_setup.dart';
import 'package:multiphone/match/tennis_match.dart';
import 'package:multiphone/match/tennis_match_setup.dart';
import 'package:multiphone/widgets/badminton/play_badminton_screen.dart';
import 'package:multiphone/widgets/badminton/setup_badminton_widget.dart';
import 'package:multiphone/widgets/pingpong/play_ping_pong_screen.dart';
import 'package:multiphone/widgets/pingpong/setup_ping_pong_widget.dart';
import 'package:multiphone/widgets/tennis/play_tennis_screen.dart';
import 'package:multiphone/widgets/tennis/setup_tennis_widget.dart';

enum SportType {
  TENNIS,
  BADMINTON,
  PING_PONG,
}

class Sport {
  final SportType type;
  final String id;
  final String Function(BuildContext context) title;
  final IconData icon;
  final String image;
  final String playNavPath;
  final Widget Function(BuildContext) createSetupWidget;
  final MatchSetup Function() createSetup;
  final ActiveMatch Function(MatchSetup) createMatch;

  const Sport({
    @required this.type,
    @required this.id,
    @required this.title,
    @required this.icon,
    @required this.image,
    @required this.playNavPath,
    @required this.createSetupWidget,
    @required this.createSetup,
    @required this.createMatch,
  });
}

class Sports with ChangeNotifier {
  final List<Sport> available;

  Sports() : available = _validSports();

  static Sport sport(SportType type) {
    return _validSports().firstWhere((element) => element.type == type);
  }

  static Sport find(int index) {
    return _validSports()[index];
  }

  static Sport fromId(String sportId) {
    List<Sport> validSports = _validSports();
    for (int i = 0; i < validSports.length; ++i) {
      if (sportId == validSports[i].id) {
        return validSports[i];
      }
    }
    return null;
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
        type: SportType.TENNIS,
        id: "Tennis",
        title: (ctx) => Values(ctx).strings.sport_tennis,
        icon: Icons.ac_unit,
        image: 'tennis.jpg',
        playNavPath: PlayTennisScreen.routeName,
        createSetupWidget: (ctx) => SetupTennisWidget(),
        createSetup: () => TennisMatchSetup(),
        createMatch: (setup) => TennisMatch(setup),
      ),
      Sport(
        type: SportType.BADMINTON,
        id: "Badminton",
        title: (ctx) => Values(ctx).strings.sport_badminton,
        icon: Icons.phone,
        image: 'badminton.jpg',
        playNavPath: PlayBadmintonScreen.routeName,
        createSetupWidget: (ctx) => SetupBadmintonWidget(),
        createSetup: () => BadmintonMatchSetup(),
        createMatch: (setup) => BadmintonMatch(setup),
      ),
      Sport(
        type: SportType.PING_PONG,
        id: "Ping Pong",
        title: (ctx) => Values(ctx).strings.sport_ping_pong,
        icon: Icons.face,
        image: 'ping_pong.jpg',
        playNavPath: PlayPingPongScreen.routeName,
        createSetupWidget: (ctx) => SetupPingPongWidget(),
        createSetup: () => PingPongMatchSetup(),
        createMatch: (setup) => PingPongMatch(setup),
      ),
    ];
  }
}
