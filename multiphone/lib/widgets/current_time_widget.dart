import 'dart:async';

import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:multiphone/helpers/values.dart';

import 'common/common_widgets.dart';

class CurrentTimeWidget extends StatefulWidget {
  const CurrentTimeWidget({Key key}) : super(key: key);

  @override
  _CurrentTimeWidgetState createState() => _CurrentTimeWidgetState();
}

class _CurrentTimeWidgetState extends State<CurrentTimeWidget> {
  String _timeString;
  Timer _timer;

  @override
  void initState() {
    super.initState();
    // and setup our time and string
    _timeString = _formatDateTime(DateTime.now());
    _timer = Timer.periodic(Duration(seconds: 1), (Timer t) => _getTime());
  }

  @override
  void dispose() {
    if (_timer != null) {
      _timer.cancel();
      _timer = null;
    }
    // and dispose
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Card(
      color: Values.primaryLightColorFaded,
      child: Padding(
        padding: const EdgeInsets.all(Values.default_space),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextWidget(_timeString),
          ],
        ),
      ),
    );
  }

  void _getTime() {
    final DateTime now = DateTime.now();
    final String formattedDateTime = _formatDateTime(now);
    setState(() {
      _timeString = formattedDateTime;
    });
  }

  String _formatDateTime(DateTime dateTime) {
    return DateFormat(DateFormat.HOUR_MINUTE).format(dateTime);
  }
}
