import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/providers/active_selection.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/widgets/common/heading_widget.dart';
import 'package:provider/provider.dart';

class SetupMatchSummaryWidget extends StatelessWidget {
  const SetupMatchSummaryWidget({Key key}) : super(key: key);

  void _startMatch(BuildContext context) {
    // start playing the selected match then, just get the match as-is
    final match = Provider.of<ActiveSelection>(context, listen: false);
    // and navigate to the match screen
    Navigator.of(context).pushNamed(match.sport.playNavPath);
  }

  @override
  Widget build(BuildContext context) {
    return Card(
      color: Values.primaryLightColor,
      child: Consumer<ActiveSetup>(
        builder: (ctx, matchSetup, child) {
          // this changes as the active match changes
          return Row(
            children: [
              Expanded(
                  child: HeadingWidget(title: matchSetup.matchSummary(ctx))),
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
            child: const Icon(Icons.play_arrow),
            backgroundColor: Theme.of(context).accentColor,
          ),
        ),
      ),
    );
  }
}
