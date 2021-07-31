import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/match_writer.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/widgets/match_summary_title_widget.dart';
import 'package:multiphone/widgets/tennis/tennis_score_summary_widget.dart';
import 'package:provider/provider.dart';

abstract class EndMatchScreen extends StatefulWidget {
  EndMatchScreen();

  @override
  _EndMatchScreenState createState() => _EndMatchScreenState();
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
    match.score.concedeMatch(team);
    // and this is a tacit acceptance of the result
    _acceptMatch(match);
  }

  void _undoMatchConcede(ActiveMatch match) {
    TeamIndex.values.forEach((element) {
      match.score.concedeMatch(element, isConcede: false);
    });
  }

  void _deleteMatch(ActiveMatch match) {
    //TODO discard and end this match now
  }

  void _acceptMatch(ActiveMatch match) {
    //TODO save and close this match now then
  }

  @override
  Widget build(BuildContext context) {
    final values = Values(context);
    final buttonStyle = values.optionButtonStyle;
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
                      style: buttonStyle,
                      onPressed: () => _deleteMatch(match),
                      icon: Icon(Icons.delete),
                      label: Text(values.strings.match_delete),
                    ),
                    ElevatedButton.icon(
                      style: buttonStyle,
                      onPressed: () => _acceptMatch(match),
                      icon: Icon(Icons.add),
                      label: Text(values.strings.match_accept),
                    ),
                  ],
                ),
                TennisScoreSummaryWidget(
                  match: match,
                  teamOneName:
                      match.getSetup().getTeamName(TeamIndex.T_ONE, ctx),
                  isTeamOneConceded:
                      match.score.isTeamConceded(TeamIndex.T_ONE),
                  teamTwoName:
                      match.getSetup().getTeamName(TeamIndex.T_TWO, ctx),
                  isTeamTwoConceded:
                      match.score.isTeamConceded(TeamIndex.T_TWO),
                ),
                if (!match.score.isMatchOver())
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
                            style: buttonStyle,
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
                            style: buttonStyle,
                            onPressed: () =>
                                _concedeMatch(match, TeamIndex.T_ONE),
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
                if (match.score.isMatchConceded)
                  Padding(
                    padding: const EdgeInsets.only(
                      left: Values.default_space,
                      right: Values.default_space,
                    ),
                    child: ElevatedButton.icon(
                      style: buttonStyle,
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
