import 'package:flutter/material.dart';
import 'package:multiphone/helpers/log.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/match_writer.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/widgets/common/common_widgets.dart';
import 'package:multiphone/widgets/match_breakdown_widget.dart';
import 'package:multiphone/widgets/match_communicated_widget.dart';
import 'package:multiphone/widgets/match_momentum_widget.dart';
import 'package:multiphone/widgets/match_summary_title_widget.dart';

class MatchBreakdownScreen extends StatefulWidget {
  static const String routeName = '/match-breakdown';
  MatchBreakdownScreen();

  @override
  _MatchBreakdownScreenState createState() => _MatchBreakdownScreenState();
}

class _MatchBreakdownScreenState extends State<MatchBreakdownScreen> {
  @override
  void initState() {
    super.initState();
  }

  @override
  void dispose() {
    // release all the created things

    // and dispose
    super.dispose();
  }

  Widget _matchNameInput(BuildContext context, ActiveMatch match) =>
      TextFormField(
        key: ValueKey('match_title'),
        initialValue: match.matchTitle,
        autocorrect: true,
        enableSuggestions: true,
        readOnly: true,
        decoration: InputDecoration(
          labelText: Values(context).strings.match_title_entry,
        ),
        onChanged: (value) => match.setMatchTitle(value, false),
      );

  Widget _matchSummary(BuildContext context, ActiveMatch match) =>
      MatchSummaryTitleWidget(
        svgPath: match.sport.icon,
        description: match.getDescription(DescriptionLevel.SHORT, context),
      );

  @override
  Widget build(BuildContext context) {
    final values = Values(context);
    final query = MediaQuery.of(context);
    // and the match that was sent to be summarised
    final args = ModalRoute.of(context).settings.arguments as List;
    ActiveMatch match;
    if (args != null && args.length > 0) {
      match = args[0] as ActiveMatch;
    } else {
      Log.error('there was no match sent to the breakdown screeen');
    }
    return Scaffold(
      appBar: AppBar(
        title: TextWidget(values.strings.match_breakdown),
      ),
      body: match == null
          ? Text('no match')
          : SingleChildScrollView(
              child: Column(
                children: <Widget>[
                  SizedBox(height: Values.default_space),
                  if (query.orientation == Orientation.portrait) ...[
                    _matchSummary(context, match),
                    _matchNameInput(context, match),
                  ],
                  if (query.orientation == Orientation.landscape)
                    Row(
                      children: [
                        Flexible(
                          child: _matchSummary(context, match),
                        ),
                        Flexible(
                          child: _matchNameInput(context, match),
                        ),
                      ],
                    ),
                  match.sport.createScoreSummaryWidget(context, match),
                  // and show the breakdown for the match
                  MatchBreakdownWidget(match: match),
                  MatchCommunicatedWidget(setup: match.getSetup()),
                  MatchMomentumWidget(match: match),
                ],
              ),
            ),
    );
  }
}
