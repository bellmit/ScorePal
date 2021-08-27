import 'dart:async';

import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:multiphone/helpers/values.dart';

class CurrentTimeWidget extends StatefulWidget {
  const CurrentTimeWidget({Key key}) : super(key: key);

  @override
  _CurrentTimeWidgetState createState() => _CurrentTimeWidgetState();
}

class _CurrentTimeWidgetState extends State<CurrentTimeWidget> {
  String _timeString;

  @override
  void initState() {
    _timeString = _formatDateTime(DateTime.now());
    Timer.periodic(Duration(seconds: 1), (Timer t) => _getTime());
    super.initState();
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
            Text(_timeString),
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
