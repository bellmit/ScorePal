import 'package:flutter/material.dart';
import 'package:multiphone/helpers/setup_persistence.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/providers/active_selection.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:provider/provider.dart';

import 'common/common_widgets.dart';

class SetupMatchSummaryWidget extends StatelessWidget {
  const SetupMatchSummaryWidget({Key key}) : super(key: key);

  void _startMatch(BuildContext context) {
    // start playing the selected match then, just get the match as-is
    final selection = Provider.of<ActiveSelection>(context, listen: false);
    // but the settings might have changed, let's have a little save of them before
    // we pop out to the play match widget to play a new match
    final setup = selection.getSelectedSetup(false);
    if (null != setup) {
      SetupPersistence().saveAsLastSetupData(setup);
    }
    // and be sure to create a brand new match from the setup to clear anything
    // that might be hanging around
    selection.createMatch();
    // and navigate to the match screen
    Navigator.of(context).pushNamed(selection.sport.playNavPath);
  }

  @override
  Widget build(BuildContext context) {
    return Card(
      color: Theme.of(context).secondaryHeaderColor,
      child: Consumer<ActiveSetup>(
        builder: (ctx, matchSetup, child) {
          // this changes as the active match changes
          return Row(
            children: [
              Expanded(
                  child: Padding(
                padding: const EdgeInsets.only(left: Values.default_space),
                child: TextHeadingWidget(matchSetup.matchSummary(ctx)),
              )),
              // and the child of the consumer
              child,
            ],
          );
        },
        // the child of the consumer always is there, make it the play button
        child: Padding(
          padding: EdgeInsets.all(Values.default_space),
          child: FloatingActionButton(
            heroTag: ValueKey<String>('play_match'),
            onPressed: () => _startMatch(context),
            child: const IconWidget(Icons.play_arrow, size: null),
            backgroundColor: Theme.of(context).accentColor,
          ),
        ),
      ),
    );
  }
}
