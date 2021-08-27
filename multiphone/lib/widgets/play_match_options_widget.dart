import 'package:flutter/material.dart';

import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/widgets/common/info_bar_widget.dart';
import 'package:multiphone/widgets/common/line_break_widget.dart';
import 'package:multiphone/widgets/match_summary_title_widget.dart';

import 'common/common_widgets.dart';

enum PlayMatchOptions {
  clear,
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
    return Card(
      elevation: 10,
      margin: EdgeInsets.only(left: Values.image_medium),
      child: SingleChildScrollView(
        child: Padding(
          padding: const EdgeInsets.all(Values.default_space),
          child: IntrinsicWidth(
            child: Column(
              children: <Widget>[
                InfoBarWidget(title: values.strings.match_options),
                MatchSummaryTitleWidget(
                    svgPath: sportSvgPath, description: matchDescription),
                Padding(
                  padding: const EdgeInsets.all(Values.default_space),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.end,
                    children: [
                      ElevatedButton.icon(
                        style: values.optionButtonStyle,
                        onPressed: () =>
                            onOptionSelected(PlayMatchOptions.clear),
                        icon: IconWidget(Icons.clear_all),
                        label: TextWidget(values.strings.match_clear),
                      ),
                      ElevatedButton.icon(
                        style: values.optionButtonStyle,
                        onPressed: () =>
                            onOptionSelected(PlayMatchOptions.show_history),
                        icon: IconWidget(Icons.history),
                        label: TextWidget(values.strings.match_history),
                      ),
                      ElevatedButton.icon(
                        style: values.optionButtonStyle,
                        onPressed: () =>
                            onOptionSelected(PlayMatchOptions.show_settings),
                        icon: IconWidget(Icons.settings),
                        label: TextWidget(values.strings.match_app_settings),
                      ),
                      ElevatedButton.icon(
                        style: values.optionButtonStyle,
                        onPressed: () => onOptionSelected(
                            PlayMatchOptions.show_match_settings),
                        icon: IconSvgWidget(
                          'match-settings',
                          size: Values.image_icon,
                          isOnBackground: true,
                        ),
                        label: TextWidget(values.strings.match_change_setup),
                      ),
                      LineBreakWidget(),
                      Row(
                        mainAxisAlignment: MainAxisAlignment.end,
                        children: [
                          ElevatedButton.icon(
                            style: values.optionButtonStyle,
                            onPressed: () =>
                                onOptionSelected(PlayMatchOptions.end_match),
                            icon: IconWidget(Icons.stop),
                            label: TextWidget(values.strings.match_end),
                          ),
                          const SizedBox(width: Values.default_space),
                          ElevatedButton.icon(
                            style: values.optionButtonStyle,
                            onPressed: () =>
                                onOptionSelected(PlayMatchOptions.resume),
                            icon: IconWidget(Icons.play_arrow),
                            label: TextWidget(values.strings.match_resume),
                          )
                        ],
                      ),
                      const SizedBox(height: Values.default_space),
                    ],
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
