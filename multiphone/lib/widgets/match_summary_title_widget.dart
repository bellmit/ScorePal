import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:multiphone/helpers/values.dart';

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
    return Hero(
      tag: ValueKey<String>('match_summary'),
      child: Padding(
        padding: const EdgeInsets.only(
          left: Values.default_space,
          right: Values.default_space,
        ),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            SvgPicture.asset(
              svgPath,
              height: Values.image_medium,
              width: Values.image_medium,
            ),
            Expanded(
              child: Text(
                description,
                style: TextStyle(color: Theme.of(context).primaryColorDark),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
