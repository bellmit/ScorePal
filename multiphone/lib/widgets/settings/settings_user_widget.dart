import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/widgets/settings/settings_widget_mixin.dart';

class SettingsUserWidget extends StatelessWidget with SettingsWidgetMixin {
  final User user;
  final void Function() onChangeUser;
  SettingsUserWidget(
      {Key key, @required this.user, @required this.onChangeUser})
      : super(key: key);

  @override
  Widget build(BuildContext context) {
    // prepare our member data to use and reuse
    prepareWidget(context);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        createHeading(values.strings.title_account),
        Row(
          children: <Widget>[
            user != null && user.photoURL != null
                ? Container(
                    height: Values.image_large,
                    child: Image.network(
                      user.photoURL,
                      fit: BoxFit.fill,
                    ),
                  )
                : Icon(
                    Icons.person,
                    size: Values.image_large,
                  ),
            Expanded(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.start,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  if (user != null &&
                      user.displayName != null &&
                      user.displayName.isNotEmpty)
                    Text(
                      user.displayName,
                      style: contentTextStyle,
                    ),
                  Text(
                    user != null ? user.email : '',
                    style: contentTextStyle,
                  ),
                ],
              ),
            ),
            TextButton.icon(
              onPressed: onChangeUser,
              icon: Icon(
                Icons.exit_to_app,
                color: theme.primaryColorDark,
              ),
              label: Text(
                user != null ? values.strings.sign_out : values.strings.sign_in,
                style: contentTextStyle,
              ),
            ),
          ],
        ),
        Text(
          user != null
              ? values.strings.explain_account_signedin
              : values.strings.explain_account_not_signed_in,
          style: contentTextStyle,
        ),
      ],
    );
  }
}
