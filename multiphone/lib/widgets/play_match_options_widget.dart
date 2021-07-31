import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/widgets/common/info_bar_widget.dart';
import 'package:multiphone/widgets/common/line_break_widget.dart';
import 'package:multiphone/widgets/match_summary_title_widget.dart';

enum PlayMatchOptions {
  resume,
  end_match,
  show_history,
  show_settings,
  show_match_settings
}

class PlayMatchOptionsWidget extends StatelessWidget {
  final String sportSvgPath;
  final String matchDescription;
  final String teamOneName;
  final String teamTwoName;
  final void Function(PlayMatchOptions) onOptionSelected;

  const PlayMatchOptionsWidget({
    Key key,
    @required this.sportSvgPath,
    @required this.matchDescription,
    @required this.teamOneName,
    @required this.teamTwoName,
    @required this.onOptionSelected,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final values = Values(context);
    final optionsButtonStyle = values.optionButtonStyle;
    return Card(
      elevation: 10,
      margin: EdgeInsets.only(left: Values.image_medium),
      child: SingleChildScrollView(
        child: Column(
          children: <Widget>[
            InfoBarWidget(title: values.strings.match_options),
            MatchSummaryTitleWidget(
                svgPath: sportSvgPath, description: matchDescription),
            Align(
              alignment: Alignment.bottomRight,
              child: Padding(
                padding: const EdgeInsets.only(
                  left: Values.default_space,
                  right: Values.default_space,
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    ElevatedButton.icon(
                      style: optionsButtonStyle,
                      onPressed: () =>
                          onOptionSelected(PlayMatchOptions.show_history),
                      icon: Icon(Icons.history),
                      label: Text(values.strings.match_history),
                    ),
                    ElevatedButton.icon(
                      style: optionsButtonStyle,
                      onPressed: () =>
                          onOptionSelected(PlayMatchOptions.show_settings),
                      icon: Icon(Icons.settings),
                      label: Text(values.strings.match_app_settings),
                    ),
                    ElevatedButton.icon(
                      style: optionsButtonStyle,
                      onPressed: () =>
                          onOptionSelected(PlayMatchOptions.show_settings),
                      icon: SvgPicture.asset(
                        'images/svg/match-settings.svg',
                        height: Values.image_icon,
                        width: Values.image_icon,
                        color: Theme.of(context).accentColor,
                      ),
                      label: Text(values.strings.match_change_setup),
                    ),
                    LineBreakWidget(height: Values.image_small),
                    Container(
                      width: double.infinity,
                      child: Row(
                        mainAxisAlignment: MainAxisAlignment.end,
                        children: [
                          ElevatedButton.icon(
                            style: optionsButtonStyle,
                            onPressed: () =>
                                onOptionSelected(PlayMatchOptions.end_match),
                            icon: Icon(Icons.stop),
                            label: Text(values.strings.match_end),
                          ),
                          const SizedBox(width: Values.default_space),
                          ElevatedButton.icon(
                            style: optionsButtonStyle,
                            onPressed: () =>
                                onOptionSelected(PlayMatchOptions.resume),
                            icon: Icon(Icons.play_arrow),
                            label: Text(values.strings.match_resume),
                          ),
                        ],
                      ),
                    ),
                    const SizedBox(height: Values.default_space),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
