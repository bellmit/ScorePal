import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/match_writer.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/providers/sport.dart';
import 'package:multiphone/widgets/badminton/badminton_score_summary_widget.dart';
import 'package:multiphone/widgets/common/icon_button_widget.dart';
import 'package:multiphone/widgets/pingpong/ping_pong_score_summary_widget.dart';
import 'package:multiphone/widgets/score_headline_widget.dart';
import 'package:multiphone/widgets/tennis/tennis_score_summary_widget.dart';

import 'common/common_widgets.dart';

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
    switch (widget.match.sport.type) {
      case SportType.TENNIS:
        return TennisScoreSummaryWidget(
          match: widget.match,
          teamOneName:
              widget.match.getSetup().getTeamName(TeamIndex.T_ONE, context),
          isTeamOneConceded: widget.match.isTeamConceded(TeamIndex.T_ONE),
          teamTwoName:
              widget.match.getSetup().getTeamName(TeamIndex.T_TWO, context),
          isTeamTwoConceded: widget.match.isTeamConceded(TeamIndex.T_TWO),
        );
      case SportType.BADMINTON:
        return BadmintonScoreSummaryWidget(
          match: widget.match,
          teamOneName:
              widget.match.getSetup().getTeamName(TeamIndex.T_ONE, context),
          isTeamOneConceded: widget.match.isTeamConceded(TeamIndex.T_ONE),
          teamTwoName:
              widget.match.getSetup().getTeamName(TeamIndex.T_TWO, context),
          isTeamTwoConceded: widget.match.isTeamConceded(TeamIndex.T_TWO),
        );
      case SportType.PING_PONG:
        return PingPongScoreSummaryWidget(
          match: widget.match,
          teamOneName:
              widget.match.getSetup().getTeamName(TeamIndex.T_ONE, context),
          isTeamOneConceded: widget.match.isTeamConceded(TeamIndex.T_ONE),
          teamTwoName:
              widget.match.getSetup().getTeamName(TeamIndex.T_TWO, context),
          isTeamTwoConceded: widget.match.isTeamConceded(TeamIndex.T_TWO),
        );
      default:
        return TextWidget('unsupported sport of ${widget.match.sport.id}');
    }
  }

  @override
  Widget build(BuildContext context) {
    final values = Values(context);
    final setup = widget.match.getSetup();
    final sport = widget.match.sport;
    return Card(
      child: Container(
        padding: EdgeInsets.all(Values.default_space),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                ClipRRect(
                  borderRadius: BorderRadius.circular(Values.default_radius),
                  child: Image.asset(
                    sport.image,
                    height: Values.image_large,
                    width: Values.image_large,
                  ),
                ),
                /*
                IconSvgWidget(
                  widget.match.sport.icon,
                  height: Values.image_medium,
                  width: Values.image_medium,
                ),*/
                Expanded(
                  child: Padding(
                    padding: EdgeInsets.only(left: Values.default_space),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        TextSubheadingWidget(
                            widget.match.getMatchTitle(context)),
                        TextWidget(
                          widget.match
                              .getDescription(DescriptionLevel.SHORT, context),
                        ),
                      ],
                    ),
                  ),
                ),
                if (null != widget.popupMenu) widget.popupMenu,
              ],
            ),
            Padding(
              padding: EdgeInsets.only(top: Values.default_space),
              child: ScoreHeadlineWidget(match: widget.match),
            ),
            Align(
              alignment: Alignment.topRight,
              child: IconButtonWidget(
                  _toggleMore,
                  _isExpanded ? Icons.expand_less : Icons.expand_more,
                  _isExpanded
                      ? values.strings.show_less
                      : values.strings.show_more),
            ),
            if (_isExpanded)
              Column(
                children: [
                  Padding(
                    padding: EdgeInsets.all(Values.default_space),
                    child: _createScoreSummaryWidget(context),
                  ),
                  if (setup.isCommunicatedFrom)
                    ListTile(
                      leading: IconWidget(Icons.hail),
                      title: TextWidget(values.strings.auto_send_rx_summary),
                    ),
                  if (!setup.isCommunicatedFrom &&
                      setup.communicatedTo.isNotEmpty)
                    Column(
                      children: setup.communicatedTo
                          .map(
                            (e) => ListTile(
                              leading: IconWidget(Icons.person_add),
                              title: TextWidget(values.construct(
                                  values.strings.auto_send_summary, [
                                setup.getPlayerNameForEmail(e.email) ??
                                    e.username
                              ])),
                            ),
                          )
                          .toList(),
                    ),
                ],
              ),
          ],
        ),
      ),
    );
  }
}
