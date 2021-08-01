import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';

abstract class MatchScoreSummaryWidget extends StatelessWidget {
  final String teamOneName;
  final String teamTwoName;
  final bool isTeamOneConceded;
  final bool isTeamTwoConceded;
  const MatchScoreSummaryWidget({
    Key key,
    @required this.teamOneName,
    @required this.teamTwoName,
    @required this.isTeamOneConceded,
    @required this.isTeamTwoConceded,
  }) : super(key: key);

  int getScoreCount();
  String getScore(BuildContext context, int index, int row);
  String getScoreTitle(BuildContext context, int index, int row);

  Widget _createHeading(BuildContext context, String title) {
    return Center(
      child: Text(
        title,
        textAlign: TextAlign.center,
        maxLines: 1,
        overflow: TextOverflow.clip,
        style: TextStyle(
          fontWeight: FontWeight.bold,
          color: Theme.of(context).primaryColorDark,
        ),
      ),
    );
  }

  Widget _createPoint(BuildContext context, String title, String point) {
    return Flexible(
      flex: 1,
      child: Column(
        mainAxisAlignment: MainAxisAlignment.start,
        children: [
          _createHeading(context, title ?? ''),
          AspectRatio(
            aspectRatio: 1,
            child: Card(
              color: Theme.of(context).primaryColorDark,
              shape: const RoundedRectangleBorder(
                borderRadius: const BorderRadius.all(
                    Radius.circular(Values.default_radius)),
              ),
              child: Padding(
                padding: const EdgeInsets.all(Values.default_space),
                child: FittedBox(
                  fit: BoxFit.contain,
                  child: Text(point),
                ),
              ),
            ),
          )
        ],
      ),
    );
  }

  Widget _createTeamTitle(BuildContext context, String title, bool isConceded) {
    return Flexible(
      flex: 2,
      child: Column(
        mainAxisAlignment: MainAxisAlignment.start,
        children: [
          _createHeading(
              context, isConceded ? Values(context).strings.team_conceded : ''),
          Text(
            title,
            textAlign: TextAlign.start,
            style: TextStyle(
              color: Theme.of(context).primaryColorDark,
            ),
            maxLines: 3,
            overflow: TextOverflow.fade,
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final scoreCount = getScoreCount();
    return Padding(
      padding: const EdgeInsets.all(Values.default_space),
      child: Column(
        children: [
          Row(
            children: <Widget>[
              _createTeamTitle(context, teamOneName, isTeamOneConceded),
              ...List.generate(
                scoreCount,
                (index) => _createPoint(
                  context,
                  getScoreTitle(context, index, 0),
                  getScore(context, index, 0),
                ),
              ),
            ],
          ),
          Row(
            children: <Widget>[
              _createTeamTitle(context, teamTwoName, isTeamTwoConceded),
              ...List.generate(
                scoreCount,
                (index) => _createPoint(
                  context,
                  getScoreTitle(context, index, 1),
                  getScore(context, index, 1),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}
