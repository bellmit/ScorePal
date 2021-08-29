import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/widgets/common/common_widgets.dart';
import 'package:multiphone/widgets/common/icon_button_widget.dart';
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
    final values = Values(context);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        TextWidget(values.strings.title_account),
        Row(
          children: <Widget>[
            if (user != null && user.photoURL != null)
              Container(
                height: Values.image_large,
                width: Values.image_large,
                decoration: new BoxDecoration(
                  shape: BoxShape.circle,
                  image: new DecorationImage(
                      fit: BoxFit.contain,
                      image: new NetworkImage(user.photoURL)),
                ),
              ),
            if (user != null && user.photoURL == null) IconWidget(Icons.person),
            Expanded(
              child: Padding(
                padding: const EdgeInsets.all(8.0),
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.start,
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    if (user != null &&
                        user.displayName != null &&
                        user.displayName.isNotEmpty)
                      TextWidget(user.displayName),
                    TextWidget(
                      user != null ? user.email : '',
                    ),
                  ],
                ),
              ),
            ),
            IconButtonWidget(
                onChangeUser,
                user != null ? Icons.logout : Icons.login,
                user != null
                    ? values.strings.sign_out
                    : values.strings.sign_in),
          ],
        ),
        TextWidget(user != null
            ? values.strings.explain_account_signedin
            : values.strings.explain_account_not_signed_in),
      ],
    );
  }
}
