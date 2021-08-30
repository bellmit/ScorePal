import 'package:flutter/cupertino.dart';
import 'package:multiphone/helpers/log.dart';
import 'package:multiphone/helpers/preferences.dart';
import 'package:multiphone/helpers/speak_service.dart';
import 'package:multiphone/match/match_writer.dart';
import 'package:multiphone/match/score_state.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/active_sport.dart';
import 'package:multiphone/providers/match_persistence.dart';
import 'package:multiphone/screens/home_screen.dart';
import 'package:multiphone/screens/setup_match_screen.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:provider/provider.dart';
import 'package:location/location.dart';

class MatchPlayTracker {
  final ActiveMatch match;

  DateTime _playEnded;
  DateTime _playStarted;

  MatchPlayTracker(this.match, BuildContext context) {
    // have started then
    _playStarted = DateTime.now();
    if (match.getDateMatchStarted() == null) {
      // this goes on the match too
      match.setDateMatchStarted(_playStarted);
    }
    // because we have the context here, we can restore our history properly
    match.restorePointHistory(context);
    // check the preferences to see if we are allowed to store a location
    Preferences.create().then((prefs) {
      // have the preferences, if we want to store location, do so
      if (prefs.isMatchLocationPermitted) {
        // we can set the location of the match here too!
        Permission.location.serviceStatus
            .then((value) => value.isEnabled)
            .then((isServiceEnabled) {
          if (isServiceEnabled) {
            return Permission.location.request().isGranted;
          } else {
            return Future.error('location is not enabled');
          }
        }).then((isPermissionGranted) {
          if (isPermissionGranted) {
            Log.info('storing location of this match');
            return Location().getLocation();
          } else {
            // why not request it (will hide if annoying them already)
            return Future.error('location is not granted');
          }
        }).then((value) {
          // finally we have the location then
          match.location = value;
        }).catchError((error, stackTrace) =>
                Log.info('Location not got for match because $error'));
      }
    });
  }

  static Future<void> navTo(String route, BuildContext context,
      {Object arguments}) {
    return Navigator.of(context).pushNamed(route, arguments: arguments);
  }

  static void navHome(BuildContext context) {
    navTo(HomeScreen.routeName, context);
  }

  static void setupNewMatch(BuildContext context) {
    // clear any current selection on the selection provider (want a new one)
    Provider.of<ActiveSport>(context, listen: false).createNewMatch();
    // and show the screen to start a new one
    navTo(SetupMatchScreen.routeName, context);
  }

  static void resumePreviousMatch(ActiveMatch match, BuildContext context) {
    // select this on the selection provider
    Provider.of<ActiveSport>(context, listen: false).resumeMatch(match);
    // and show the playing screen for this
    navTo(match.sport.playNavPath, context);
  }

  void clearMatchData() {
    // reset everything
    match.resetMatch();
  }

  void processScoreChange(BuildContext context) {
    final state = match.score.state;
    if (state.isChanged(ScoreChange.increment) ||
        state.isChanged(ScoreChange.incrementRedo)) {
      // this was an item of score incremented. The undo history of this is okay but
      // we can do better - we know the match that is playing and have a context and
      // everything so can well describe the score
      String scoreString =
          match.getDescription(DescriptionLevel.SCORE, context);
      match.describeLastHistoryChange(state.getState(), scoreString);
    }
    // and do the speaking from the screen
    final speakService = Provider.of<SpeakService>(context, listen: false);
    if (!state.isChanged(ScoreChange.incrementRedo)) {
      // this is not during a 'redo' so we need to process and display this change
      speakService.speak(match.getSpokenStateMessage(context));
    }
    // and the amount of time we have played please
    if (null != _playStarted) {
      // and add the time played in this session to the active match
      int activityTime = getTimePlayedMs();
      if (activityTime > 0) {
        match.addMatchTimePlayed(activityTime);
      }
      // now we added these time, we need to not add them again, reset the
      // play started time to be now
      _playStarted = DateTime.now();
    }
    // every time the points change we want to check to see if we have ended or not
    _handlePlayEnding();
    // and finally, let's be aggressive and store this match everytime it changes
    Provider.of<MatchPersistence>(context, listen: false)
        .saveAsLastActiveMatch(match);
    // and any notification
    _handleNotificationUpdate();
  }

  void stopPlay() {
    // set the time at which play ended
    _playEnded = DateTime.now();
  }

  void clearEndedPlay() {
    if (null != _playEnded) {
      _playEnded = null;
    }
  }

  int getMatchTimePlayedMs() {
    int timePlayed = match == null ? 0 : match.getMatchTimePlayedMs();
    int activityTime = getTimePlayedMs();
    if (activityTime >= 0) {
      timePlayed += activityTime;
    }
    return timePlayed;
  }

  int getTimePlayedMs() {
    if (null == _playStarted) {
      return 0;
    } else {
      var playEndedMs;
      if (null == _playEnded) {
        // play isn't over yet, use now
        playEndedMs = DateTime.now().millisecondsSinceEpoch;
      } else {
        // use the play ended time
        playEndedMs = _playEnded.millisecondsSinceEpoch;
      }
      // Calculate difference in milliseconds
      return playEndedMs - _playStarted.millisecondsSinceEpoch;
    }
  }

  void _handlePlayEnding() {
    // is the match over
    if (null != match && match.isMatchOver()) {
      // the match is over
      if (null == _playEnded) {
        // the match is over but play hasn't ended - yes it has!
        stopPlay();
      }
    } else {
      // the match is not over, this might be from an undo, get rid of the time either way
      clearEndedPlay();
    }
  }

  ///TODO the update of any widget or whatever is being shown on Android / iOS
  void _handleNotificationUpdate() {}
}
