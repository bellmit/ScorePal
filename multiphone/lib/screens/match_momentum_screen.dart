import 'package:flutter/material.dart';
import 'package:multiphone/helpers/log.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/widgets/common/common_widgets.dart';
import 'package:multiphone/widgets/match_momentum_widget.dart';

class MatchMomentumScreen extends StatefulWidget {
  static const String routeName = '/match-momentum';

  MatchMomentumScreen();

  @override
  _MatchMomentumScreenState createState() => _MatchMomentumScreenState();
}

class _MatchMomentumScreenState extends State<MatchMomentumScreen> {
  @override
  Widget build(BuildContext context) {
    // get the values to get strings from
    var values = Values(context);
    // and the match that was sent to be summarised
    ActiveMatch match =
        ModalRoute.of(context).settings.arguments as ActiveMatch;
    if (match == null) {
      Log.error('there was no match sent to the momentum screeen');
    }
    // and return the scaffold
    return Scaffold(
      appBar: AppBar(
        title: TextWidget(values.strings.match_momentum),
      ),
      body: Container(
        width: double.infinity,
        height: double.infinity,
        child: MatchMomentumWidget(
          match: match,
          isAllowFullscreenButton: false,
        ),
      ),
    );
  }
}
