import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/screens/auth_screen.dart';

class SignInScorepalWidget extends StatelessWidget {
  const SignInScorepalWidget({Key key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final values = Values(context);
    final cardWidth = MediaQuery.of(context).size.width * 0.4;
    return Card(
      margin: const EdgeInsets.all(Values.default_space),
      child: ConstrainedBox(
        constraints: BoxConstraints.loose(Size.fromWidth(cardWidth)),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Padding(
              padding: EdgeInsets.only(
                  left: Values.default_space, right: Values.default_space),
              child: Icon(
                Icons.person,
                size: Values.image_large,
              ),
            ),
            Flexible(
              child: Column(
                children: [
                  Text(
                    values.strings.description_sign_in_scorepal,
                    style: TextStyle(
                        fontSize: Values.font_size_title,
                        color: Theme.of(context).primaryColorDark),
                  ),
                  Align(
                    alignment: Alignment.bottomRight,
                    child: TextButton(
                        onPressed: () => Navigator.of(context)
                            .pushNamed(AuthScreen.routeName),
                        child: Text(values.strings.sign_in)),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
