import 'package:flutter/material.dart';
import 'package:multiphone/providers/active_sport.dart';
import 'package:multiphone/providers/match_persistence.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/match_writer.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/screens/home_screen.dart';
import 'package:multiphone/widgets/common/common_widgets.dart';
import 'package:multiphone/widgets/common/icon_button_widget.dart';
import 'package:multiphone/widgets/match_breakdown_widget.dart';
import 'package:multiphone/widgets/match_momentum_widget.dart';
import 'package:multiphone/widgets/match_summary_title_widget.dart';
import 'package:provider/provider.dart';

class EndMatchScreen extends StatefulWidget {
  EndMatchScreen();

  @override
  _EndMatchScreenState createState() => _EndMatchScreenState();
}

class _EndMatchScreenState extends State<EndMatchScreen> {
  bool _isShareMatchResults = true;
  bool _isShowEmailShares = false;

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
    // suhtdown the match
    match.shutdownMatch();
    // and send us home
    _navigateHome();
  }

  void _acceptMatch(ActiveMatch match) {
    // save and close this match now then
    Provider.of<MatchPersistence>(context, listen: false)
        .saveMatchData(match, state: MatchPersistenceState.accepted);
    // suhtdown the match
    match.shutdownMatch();
    // and go home
    _navigateHome();
  }

  void _navigateHome() {
    // we are about to leave - killing this match on the way would be good
    Provider.of<ActiveSport>(context, listen: false).clearSelection();
    // remove all routes and replace with the home screen one
    Navigator.of(context).pushNamedAndRemoveUntil(
        HomeScreen.routeName, (Route<dynamic> route) => false);
  }

  Widget _matchNameInput(BuildContext context, ActiveMatch match) =>
      TextFormField(
        key: ValueKey('match_title'),
        initialValue: match.matchTitle,
        autocorrect: true,
        enableSuggestions: true,
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

  Widget _shareMatch(BuildContext context, ActiveMatch match) {
    final setup = match.getSetup();
    final values = Values(context);
    final emailsSharing = setup.getSharingEmails();
    if (emailsSharing == null || emailsSharing.isEmpty) {
      // not sharing anything
      return Container();
    } else {
      return Padding(
        padding: const EdgeInsets.all(Values.default_space),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Padding(
              padding: const EdgeInsets.all(Values.default_space),
              child: IconWidget(Icons.share, size: Values.image_icon),
            ),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Expanded(
                        child: Wrap(
                          children: [
                            TextWidget(
                                'Auto-share results to registered Scorepal users'),
                            if (_isShareMatchResults)
                              TextButton(
                                  onPressed: () => setState(() =>
                                      _isShowEmailShares = !_isShowEmailShares),
                                  child: TextSubheadingWidget(
                                      values.strings.buttons_emails))
                          ],
                        ),
                      ),
                      Switch(
                        activeColor: Theme.of(context).primaryColor,
                        value: _isShareMatchResults,
                        onChanged: (value) =>
                            setState(() => _isShareMatchResults = value),
                      ),
                    ],
                  ),
                  if (_isShareMatchResults && _isShowEmailShares)
                    Column(
                      crossAxisAlignment: CrossAxisAlignment.end,
                      children: emailsSharing
                          .map(
                            (e) => TextWidget(e),
                          )
                          .toList(),
                    ),
                ],
              ),
            ),
          ],
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final values = Values(context);
    final query = MediaQuery.of(context);
    return Scaffold(
      appBar: AppBar(
        title: TextWidget(values.strings.match_end),
      ),
      body: Consumer<ActiveMatch>(
        builder: (ctx, match, child) {
          return SingleChildScrollView(
            child: Column(
              children: <Widget>[
                SizedBox(height: Values.default_space),
                if (query.orientation == Orientation.portrait) ...[
                  _matchSummary(ctx, match),
                  _matchNameInput(ctx, match),
                ],
                if (query.orientation == Orientation.landscape)
                  Row(
                    children: [
                      Flexible(
                        child: _matchSummary(ctx, match),
                      ),
                      Flexible(
                        child: _matchNameInput(ctx, match),
                      ),
                    ],
                  ),
                Wrap(
                  alignment: WrapAlignment.spaceEvenly,
                  spacing: Values.default_space,
                  children: [
                    IconButtonWidget(() => _deleteMatch(match), Icons.delete,
                        values.strings.match_delete),
                    IconButtonWidget(() => _acceptMatch(match), Icons.add,
                        values.strings.match_accept),
                  ],
                ),
                match.sport.createScoreSummaryWidget(ctx, match),
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
                          child: IconButtonWrappedWidget(
                            () => _concedeMatch(match, TeamIndex.T_ONE),
                            Icons.thumb_down_alt,
                            values.construct(
                              values.strings.match_concede,
                              [
                                match
                                    .getSetup()
                                    .getTeamName(TeamIndex.T_ONE, ctx)
                              ],
                            ),
                          ),
                        ),
                        const SizedBox(
                          width: Values.default_space,
                        ),
                        Flexible(
                          child: IconButtonWrappedWidget(
                            () => _concedeMatch(match, TeamIndex.T_TWO),
                            Icons.thumb_down_alt,
                            values.construct(
                              values.strings.match_concede,
                              [
                                match
                                    .getSetup()
                                    .getTeamName(TeamIndex.T_TWO, ctx)
                              ],
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
                    child: IconButtonWidget(
                      () => _undoMatchConcede(match),
                      Icons.undo,
                      values.strings.match_concede_undo,
                    ),
                  ),
                if (match.isMatchOver())
                  // and the share option
                  _shareMatch(context, match),
                // and show the breakdown for the match
                MatchBreakdownWidget(match: match),
                MatchMomentumWidget(match: match),
              ],
            ),
          );
        },
      ),
    );
  }
}
