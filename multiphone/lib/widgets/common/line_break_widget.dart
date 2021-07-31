import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';

class LineBreakWidget extends StatelessWidget {
  final double height;
  const LineBreakWidget({
    Key key,
    this.height = Values.image_small,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Container(
      height: height,
      width: double.infinity,
      child: Center(
        child: Container(
          height: Values.line_width,
          width: double.infinity,
          color: Theme.of(context).primaryColor,
        ),
      ),
    );
  }
}
