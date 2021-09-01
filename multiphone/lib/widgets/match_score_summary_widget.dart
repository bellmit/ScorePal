import 'package:flutter/material.dart';
import 'package:flutter_svg/svg.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/providers/active_setup.dart';

import 'common/common_widgets.dart';

class MatchScoreSummaryItem {
  final String score;
  final String title;
  final bool isWinner;
  MatchScoreSummaryItem({this.score, this.title, this.isWinner});
}

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
  MatchScoreSummaryItem getScoreItem(BuildContext context, int index, int row);

  TeamIndex getServingTeam();
  String getServingSvgIcon();

  Widget headingTextWidget(BuildContext context, String title) {
    return Center(
      child: TextSubheadingWidget(
        title,
        textAlign: TextAlign.center,
        isLimitOverflow: true,
      ),
    );
  }

  Widget _createServingWidget(BuildContext context, bool isServing) {
    if (isServing) {
      return Center(
          child: SvgPicture.asset('images/svg/${getServingSvgIcon()}.svg',
              width: Values.image_icon, height: Values.image_icon));
    } else {
      return Center(
          child:
              Container(width: Values.image_icon, height: Values.image_icon));
    }
  }

  Widget _createPoint(BuildContext context, MatchScoreSummaryItem item) {
    return Flexible(
      flex: 1,
      child: Column(
        mainAxisAlignment: MainAxisAlignment.start,
        children: [
          headingTextWidget(context, item.title ?? ''),
          ConstrainedBox(
            constraints: BoxConstraints.loose(
              Size(Values.max_score_box, Values.max_score_box),
            ),
            child: AspectRatio(
              aspectRatio: 1,
              child: Card(
                color: Theme.of(context).secondaryHeaderColor,
                shape: const RoundedRectangleBorder(
                  borderRadius: const BorderRadius.all(
                      Radius.circular(Values.default_radius)),
                ),
                child: Padding(
                  padding: const EdgeInsets.all(Values.default_space),
                  child: FittedBox(
                    fit: BoxFit.contain,
                    child: TextWidget(
                      item.score,
                      isOnBackground: true,
                      isBold: true,
                    ),
                  ),
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
          headingTextWidget(
              context, isConceded ? Values(context).strings.team_conceded : ''),
          Text(
            title,
            textAlign: TextAlign.start,
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
              if (getServingTeam() != null)
                _createServingWidget(
                    context, getServingTeam() == TeamIndex.T_ONE),
              ...List.generate(
                scoreCount,
                (index) => _createPoint(
                  context,
                  getScoreItem(context, index, 0),
                ),
              ),
            ],
          ),
          Row(
            children: <Widget>[
              _createTeamTitle(context, teamTwoName, isTeamTwoConceded),
              if (getServingTeam() != null)
                _createServingWidget(
                    context, getServingTeam() == TeamIndex.T_TWO),
              ...List.generate(
                scoreCount,
                (index) => _createPoint(
                  context,
                  getScoreItem(context, index, 1),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}
