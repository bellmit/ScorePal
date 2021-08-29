import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';

import 'common/common_widgets.dart';

class MatchSummaryTitleWidget extends StatelessWidget {
  final String svgPath;
  final String description;
  const MatchSummaryTitleWidget({
    Key key,
    @required this.svgPath,
    @required this.description,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(
        left: Values.default_space,
        right: Values.default_space,
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisSize: MainAxisSize.min,
        children: <Widget>[
          IconSvgWidget(
            svgPath,
            size: Values.image_medium,
          ),
          Expanded(
            child: TextWidget(description),
          ),
        ],
      ),
    );
  }
}
