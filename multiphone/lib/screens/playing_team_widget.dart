import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/widgets/common/common_widgets.dart';
import 'package:multiphone/widgets/server_image_widget.dart';

class PlayingTeamWidget extends StatelessWidget {
  final TeamIndex team;
  final ActiveMatch match;

  const PlayingTeamWidget({Key key, this.team, this.match}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final setup = match.getSetup();
    final theme = Theme.of(context);
    final servingPlayer = match.getServingPlayer();
    final player = setup.getTeamPlayer(team);
    final partner = setup.getTeamPartner(team);
    final playerName = setup.getPlayerName(player, context);
    final partnerName = setup.getPlayerName(partner, context);
    return Card(
      color: Theme.of(context).secondaryHeaderColor,
      child: Container(
        margin: EdgeInsets.all(Values.default_space),
        width: double.infinity,
        height: Values.team_names_widget_height,
        child: Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Flexible(
              flex: 1,
              child: Row(
                children: [
                  if (match.isTeamConceded(team))
                    Padding(
                      padding: const EdgeInsets.all(Values.default_space),
                      child: TextWidget(Values(context).strings.team_conceded),
                    ),
                  ServerImageWidget(isServing: servingPlayer == player),
                  const SizedBox(
                    width: Values.default_space,
                  ),
                  Expanded(
                    child: Text(
                      playerName,
                      overflow: TextOverflow.fade,
                      maxLines: 2,
                      style: TextStyle(
                          color: theme.accentTextTheme.button.color,
                          fontWeight: servingPlayer == player
                              ? FontWeight.bold
                              : FontWeight.normal),
                    ),
                  ),
                ],
              ),
            ),
            if (setup.singlesDoubles == MatchSinglesDoubles.doubles)
              Flexible(
                flex: 1,
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.end,
                  children: [
                    Expanded(
                      child: Text(
                        partnerName,
                        overflow: TextOverflow.fade,
                        maxLines: 2,
                        softWrap: false,
                        textAlign: TextAlign.end,
                        style: TextStyle(
                            color: theme.accentTextTheme.button.color,
                            fontWeight: servingPlayer == player
                                ? FontWeight.bold
                                : FontWeight.normal),
                      ),
                    ),
                    const SizedBox(
                      width: Values.default_space,
                    ),
                    ServerImageWidget(isServing: servingPlayer == partner),
                  ],
                ),
              ),
          ],
        ),
      ),
    );
  }
}
