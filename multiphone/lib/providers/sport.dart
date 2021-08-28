import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/badminton/badminton_match.dart';
import 'package:multiphone/match/badminton/badminton_match_setup.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/match/ping_pong/ping_pong_match.dart';
import 'package:multiphone/match/ping_pong/ping_pong_match_setup.dart';
import 'package:multiphone/match/tennis/tennis_match.dart';
import 'package:multiphone/match/tennis/tennis_match_setup.dart';
import 'package:multiphone/widgets/badminton/badminton_score_summary_widget.dart';
import 'package:multiphone/widgets/badminton/play_badminton_screen.dart';
import 'package:multiphone/widgets/badminton/setup_badminton_widget.dart';
import 'package:multiphone/widgets/pingpong/ping_pong_score_summary_widget.dart';
import 'package:multiphone/widgets/pingpong/play_ping_pong_screen.dart';
import 'package:multiphone/widgets/pingpong/setup_ping_pong_widget.dart';
import 'package:multiphone/widgets/tennis/play_tennis_screen.dart';
import 'package:multiphone/widgets/tennis/setup_tennis_widget.dart';
import 'package:multiphone/widgets/tennis/tennis_score_summary_widget.dart';

enum SportType {
  TENNIS,
  BADMINTON,
  PING_PONG,
}

class Sport {
  final SportType type;
  final String id;
  final String Function(BuildContext context) title;
  final String icon;
  final String image;
  final String playNavPath;
  final Widget Function(BuildContext, bool isLoadSetupData) createSetupWidget;
  final Widget Function(BuildContext, ActiveMatch match)
      createScoreSummaryWidget;
  final ActiveSetup Function() createSetup;
  final ActiveMatch Function(ActiveSetup) createMatch;

  const Sport({
    @required this.type,
    @required this.id,
    @required this.title,
    @required this.icon,
    @required this.image,
    @required this.playNavPath,
    @required this.createSetupWidget,
    @required this.createScoreSummaryWidget,
    @required this.createSetup,
    @required this.createMatch,
  });

  @override
  bool operator ==(Object other) => other is Sport && other.id == id;

  @override
  int get hashCode => id.hashCode;
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
      if (sport.id == validSports[i].id) {
        return i;
      }
    }
    return -1;
  }

  static List<Sport> _validSports() {
    return [
      Sport(
        type: SportType.TENNIS,
        id: "tennis",
        title: (ctx) => Values(ctx).strings.sport_tennis,
        icon: 'tennis',
        image: 'images/img/tennis.jpg',
        playNavPath: PlayTennisScreen.routeName,
        createSetupWidget: (ctx, isLoadSetupData) =>
            SetupTennisWidget(isLoadSetup: isLoadSetupData),
        createScoreSummaryWidget: (ctx, match) => TennisScoreSummaryWidget(
          match: match,
          teamOneName: match.getSetup().getTeamName(TeamIndex.T_ONE, ctx),
          isTeamOneConceded: match.isTeamConceded(TeamIndex.T_ONE),
          teamTwoName: match.getSetup().getTeamName(TeamIndex.T_TWO, ctx),
          isTeamTwoConceded: match.isTeamConceded(TeamIndex.T_TWO),
        ),
        createSetup: () => TennisMatchSetup(),
        createMatch: (setup) => TennisMatch(setup),
      ),
      Sport(
        type: SportType.BADMINTON,
        id: "badminton",
        title: (ctx) => Values(ctx).strings.sport_badminton,
        icon: 'badminton',
        image: 'images/img/badminton.jpg',
        playNavPath: PlayBadmintonScreen.routeName,
        createSetupWidget: (ctx, isLoadSetupData) =>
            SetupBadmintonWidget(isLoadSetup: isLoadSetupData),
        createScoreSummaryWidget: (ctx, match) => BadmintonScoreSummaryWidget(
          match: match,
          teamOneName: match.getSetup().getTeamName(TeamIndex.T_ONE, ctx),
          isTeamOneConceded: match.isTeamConceded(TeamIndex.T_ONE),
          teamTwoName: match.getSetup().getTeamName(TeamIndex.T_TWO, ctx),
          isTeamTwoConceded: match.isTeamConceded(TeamIndex.T_TWO),
        ),
        createSetup: () => BadmintonMatchSetup(),
        createMatch: (setup) => BadmintonMatch(setup),
      ),
      Sport(
        type: SportType.PING_PONG,
        id: "pingpong",
        title: (ctx) => Values(ctx).strings.sport_ping_pong,
        icon: 'ping-pong',
        image: 'images/img/ping_pong.jpg',
        playNavPath: PlayPingPongScreen.routeName,
        createSetupWidget: (ctx, isLoadSetupData) =>
            SetupPingPongWidget(isLoadSetup: isLoadSetupData),
        createScoreSummaryWidget: (ctx, match) => PingPongScoreSummaryWidget(
          match: match,
          teamOneName: match.getSetup().getTeamName(TeamIndex.T_ONE, ctx),
          isTeamOneConceded: match.isTeamConceded(TeamIndex.T_ONE),
          teamTwoName: match.getSetup().getTeamName(TeamIndex.T_TWO, ctx),
          isTeamTwoConceded: match.isTeamConceded(TeamIndex.T_TWO),
        ),
        createSetup: () => PingPongMatchSetup(),
        createMatch: (setup) => PingPongMatch(setup),
      ),
    ];
  }
}
