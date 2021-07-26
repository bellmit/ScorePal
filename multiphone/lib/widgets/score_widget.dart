import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/providers/active_setup.dart';

abstract class ScoreWidget extends StatelessWidget {
  const ScoreWidget({Key key}) : super(key: key);

  Widget createBox(
    BuildContext context,
    String title,
    String points,
    void onClicked(),
  ) {
    return Padding(
      padding: const EdgeInsets.all(Values.default_space),
      child: AspectRatio(
        aspectRatio: 1,
        child: InkWell(
          onTap: onClicked,
          child: Container(
            // this is the main container for the box of points
            decoration: BoxDecoration(
              color: Theme.of(context).primaryColorDark,
              borderRadius: BorderRadius.circular(Values.default_radius),
            ),
            child: Column(
              children: [
                Text(
                  title,
                  style: TextStyle(
                      fontSize: Values.font_size_title,
                      color: Theme.of(context).accentColor),
                ),
                Expanded(
                  child: FittedBox(
                    fit: BoxFit.contain,
                    child: Text(
                      points,
                      textAlign: TextAlign.center,
                      style: TextStyle(color: Theme.of(context).accentColor),
                    ),
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
