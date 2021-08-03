import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/match_writer.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/providers/sport.dart';
import 'package:multiphone/widgets/badminton/badminton_score_summary_widget.dart';
import 'package:multiphone/widgets/pingpong/ping_pong_score_summary_widget.dart';
import 'package:multiphone/widgets/score_headline_widget.dart';
import 'package:multiphone/widgets/tennis/tennis_score_summary_widget.dart';

class PlayedMatchSummaryWidget extends StatefulWidget {
  final ActiveMatch match;
  final Widget popupMenu;
  const PlayedMatchSummaryWidget(
      {Key key, @required this.match, this.popupMenu})
      : super(key: key);

  @override
  _PlayedMatchSummaryWidgetState createState() =>
      _PlayedMatchSummaryWidgetState();
}

class _PlayedMatchSummaryWidgetState extends State<PlayedMatchSummaryWidget> {
  bool _isExpanded = false;

  _PlayedMatchSummaryWidgetState();

  void _toggleMore() {
    setState(() {
      _isExpanded = !_isExpanded;
    });
  }

  Widget _createScoreSummaryWidget(BuildContext context) {
    switch (widget.match.getSport().type) {
      case SportType.TENNIS:
        return TennisScoreSummaryWidget(
          match: widget.match,
          teamOneName:
              widget.match.getSetup().getTeamName(TeamIndex.T_ONE, context),
          isTeamOneConceded: widget.match.score.isTeamConceded(TeamIndex.T_ONE),
          teamTwoName:
              widget.match.getSetup().getTeamName(TeamIndex.T_TWO, context),
          isTeamTwoConceded: widget.match.score.isTeamConceded(TeamIndex.T_TWO),
        );
      case SportType.BADMINTON:
        return BadmintonScoreSummaryWidget(
          match: widget.match,
          teamOneName:
              widget.match.getSetup().getTeamName(TeamIndex.T_ONE, context),
          isTeamOneConceded: widget.match.score.isTeamConceded(TeamIndex.T_ONE),
          teamTwoName:
              widget.match.getSetup().getTeamName(TeamIndex.T_TWO, context),
          isTeamTwoConceded: widget.match.score.isTeamConceded(TeamIndex.T_TWO),
        );
      case SportType.PING_PONG:
        return PingPongScoreSummaryWidget(
          match: widget.match,
          teamOneName:
              widget.match.getSetup().getTeamName(TeamIndex.T_ONE, context),
          isTeamOneConceded: widget.match.score.isTeamConceded(TeamIndex.T_ONE),
          teamTwoName:
              widget.match.getSetup().getTeamName(TeamIndex.T_TWO, context),
          isTeamTwoConceded: widget.match.score.isTeamConceded(TeamIndex.T_TWO),
        );
      default:
        return Text('unsupported sport of ${widget.match.getSport().id}');
    }
  }

  @override
  Widget build(BuildContext context) {
    final values = Values(context);
    return Card(
      child: Container(
        padding: EdgeInsets.all(Values.default_space),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                SvgPicture.asset(
                  widget.match.getSport().icon,
                  height: Values.image_medium,
                  width: Values.image_medium,
                ),
                Expanded(
                  child: Padding(
                    padding: EdgeInsets.only(left: Values.default_space),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(widget.match.getSport().title(context),
                            style: TextStyle(
                              fontSize: Values.font_size_title,
                              fontWeight: FontWeight.bold,
                              color: Theme.of(context).primaryColorDark,
                            )),
                        Text(
                          widget.match
                              .getDescription(DescriptionLevel.SHORT, context),
                          style: TextStyle(
                            fontSize: Values.font_size_subtitle,
                            color: Theme.of(context).primaryColorDark,
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
                if (null != widget.popupMenu) widget.popupMenu,
              ],
            ),
            Padding(
              padding: EdgeInsets.only(
                  left: Values.default_space,
                  right: Values.default_space,
                  top: Values.default_space),
              child: ScoreHeadlineWidget(match: widget.match),
            ),
            Align(
              alignment: Alignment.topRight,
              child: TextButton.icon(
                  onPressed: _toggleMore,
                  icon:
                      Icon(_isExpanded ? Icons.expand_less : Icons.expand_more),
                  label: Text(_isExpanded
                      ? values.strings.show_less
                      : values.strings.show_more)),
            ),
            if (_isExpanded)
              Padding(
                padding: EdgeInsets.all(Values.default_space),
                child: _createScoreSummaryWidget(context),
              ),
          ],
        ),
      ),
    );
  }
}
