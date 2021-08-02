import 'package:flutter/cupertino.dart';
import 'package:multiphone/helpers/speak_service.dart';
import 'package:multiphone/match/match_writer.dart';
import 'package:multiphone/match/score_state.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/match_persistence.dart';
import 'package:provider/provider.dart';

class MatchPlayTracker {
  final ActiveMatch match;

  DateTime _playEnded;
  DateTime _playStarted;

  MatchPlayTracker(this.match) {
    // have started then
    _playStarted = DateTime.now();
    if (match.getDateMatchStarted() == null) {
      // this goes on the match too
      match.setDateMatchStarted(_playStarted);
    }
    //TODO we can set the location of the match here too!
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
