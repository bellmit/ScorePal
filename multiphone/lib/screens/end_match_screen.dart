import 'package:flutter/material.dart';
import 'package:multiphone/providers/match_persistence.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/match_writer.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/screens/home_screen.dart';
import 'package:multiphone/widgets/match_score_summary_widget.dart';
import 'package:multiphone/widgets/match_summary_title_widget.dart';
import 'package:provider/provider.dart';

abstract class EndMatchScreen extends StatefulWidget {
  EndMatchScreen();

  @override
  _EndMatchScreenState createState() => _EndMatchScreenState();

  MatchScoreSummaryWidget createScoreSummaryWidget(
      BuildContext context, ActiveMatch match);
}

class _EndMatchScreenState extends State<EndMatchScreen> {
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

  void _concedeMatch(ActiveMatch match, TeamIndex team) {
    // have this team concede
    match.concedeMatch(team);
    // and this is a tacit acceptance of the result
    _acceptMatch(match);
  }

  void _undoMatchConcede(ActiveMatch match) {
    TeamIndex.values.forEach((element) {
      match.concedeMatch(element, isConcede: false);
    });
    // and pop this page back to the match
    Navigator.of(context).pop();
  }

  void _deleteMatch(ActiveMatch match) {
    // discard and end this match now
    Provider.of<MatchPersistence>(context, listen: false)
        .deleteMatchData(match);
    // and send us home
    _navigateHome();
  }

  void _acceptMatch(ActiveMatch match) {
    // save and close this match now then
    Provider.of<MatchPersistence>(context, listen: false)
        .saveMatchData(match, state: MatchPersistenceState.accepted);
    // and go home
    _navigateHome();
  }

  void _navigateHome() {
    // remove all routes and replace with the home screen one
    Navigator.of(context).pushNamedAndRemoveUntil(
        HomeScreen.routeName, (Route<dynamic> route) => false);
  }

  @override
  Widget build(BuildContext context) {
    final values = Values(context);
    return Scaffold(
      appBar: AppBar(
        title: Text(values.strings.match_end),
      ),
      body: Consumer<ActiveMatch>(
        builder: (ctx, match, child) {
          return SingleChildScrollView(
            child: Column(
              children: <Widget>[
                SizedBox(height: Values.default_space),
                MatchSummaryTitleWidget(
                  svgPath: match.getSport().icon,
                  description:
                      match.getDescription(DescriptionLevel.SHORT, context),
                ),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                  children: [
                    ElevatedButton.icon(
                      style: values.optionButtonStyle,
                      onPressed: () => _deleteMatch(match),
                      icon: Icon(Icons.delete),
                      label: Text(values.strings.match_delete),
                    ),
                    ElevatedButton.icon(
                      style: values.optionButtonStyle,
                      onPressed: () => _acceptMatch(match),
                      icon: Icon(Icons.add),
                      label: Text(values.strings.match_accept),
                    ),
                  ],
                ),
                widget.createScoreSummaryWidget(ctx, match),
                if (!match.isMatchOver())
                  Padding(
                    padding: const EdgeInsets.only(
                      left: Values.default_space,
                      right: Values.default_space,
                    ),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceAround,
                      children: [
                        Flexible(
                          child: ElevatedButton.icon(
                            style: values.optionButtonStyle,
                            onPressed: () =>
                                _concedeMatch(match, TeamIndex.T_ONE),
                            icon: Icon(Icons.thumb_down_alt),
                            label: Expanded(
                              child: Text(
                                values.construct(values.strings.match_concede, [
                                  match
                                      .getSetup()
                                      .getTeamName(TeamIndex.T_ONE, ctx)
                                ]),
                                maxLines: null,
                              ),
                            ),
                          ),
                        ),
                        const SizedBox(
                          width: Values.default_space,
                        ),
                        Flexible(
                          child: ElevatedButton.icon(
                            style: values.optionButtonStyle,
                            onPressed: () =>
                                _concedeMatch(match, TeamIndex.T_TWO),
                            icon: Icon(Icons.thumb_down_alt),
                            label: Expanded(
                              child: Text(
                                values.construct(values.strings.match_concede, [
                                  match
                                      .getSetup()
                                      .getTeamName(TeamIndex.T_TWO, ctx)
                                ]),
                                maxLines: null,
                              ),
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),
                if (match.isMatchConceded)
                  Padding(
                    padding: const EdgeInsets.only(
                      left: Values.default_space,
                      right: Values.default_space,
                    ),
                    child: ElevatedButton.icon(
                      style: values.optionButtonStyle,
                      onPressed: () => _undoMatchConcede(match),
                      icon: Icon(Icons.undo),
                      label: Text(
                        values.strings.match_concede_undo,
                      ),
                    ),
                  ),
              ],
            ),
          );
        },
      ),
    );
  }
}
