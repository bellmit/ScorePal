import 'package:flutter/material.dart';
import 'package:multiphone/helpers/user_data.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/widgets/auth/user_form.dart';
import 'package:multiphone/widgets/common/common_widgets.dart';

class UserScreen extends StatefulWidget {
  static const String routeName = '/user-details';
  const UserScreen({Key key}) : super(key: key);

  @override
  _UserScreenState createState() => _UserScreenState();
}

class _UserScreenState extends State<UserScreen> {
  Future<UserData> _userFuture;

  @override
  void initState() {
    super.initState();
    // start getting the data here to be super quick as we can
    _userFuture = UserData.load();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: TextWidget(Values(context).strings.option_user_details),
      ),
      body: FutureBuilder<UserData>(
        future: _userFuture,
        builder: (ctx, snapshotData) {
          // build the view for the data we are getting
          if (snapshotData.connectionState != ConnectionState.done) {
            // not done yet
            return Center(child: CircularProgressIndicator());
          } else {
            // show the details for the user to change them
            return UserForm(snapshotData.data);
          }
        },
      ),
    );
  }
}
