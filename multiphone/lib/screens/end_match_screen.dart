import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/match_writer.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/widgets/match_summary_title_widget.dart';
import 'package:multiphone/widgets/tennis/tennis_score_summary.dart';
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

  @override
  Widget build(BuildContext context) {
    final buttonStyle = ElevatedButton.styleFrom(
      primary: Theme.of(context).primaryColorDark,
      onPrimary: Theme.of(context).accentColor,
    );
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
                      style: buttonStyle,
                      onPressed: () {},
                      icon: Icon(Icons.delete),
                      label: Text(values.strings.match_delete),
                    ),
                    ElevatedButton.icon(
                      style: buttonStyle,
                      onPressed: () {},
                      icon: Icon(Icons.add),
                      label: Text(values.strings.match_accept),
                    ),
                  ],
                ),
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
                          onPressed: () {},
                          icon: Icon(Icons.upgrade),
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
                          onPressed: () {},
                          icon: Icon(Icons.upgrade),
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
                TennisScoreSummary(
                  match: match,
                  teamOneName:
                      match.getSetup().getTeamName(TeamIndex.T_ONE, ctx),
                  teamTwoName:
                      match.getSetup().getTeamName(TeamIndex.T_TWO, ctx),
                ),
              ],
            ),
          );
        },
      ),
    );
  }
}
