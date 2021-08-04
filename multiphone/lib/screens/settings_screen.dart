import 'dart:async';

import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:multiphone/helpers/log.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/screens/base_nav_screen.dart';
import 'package:multiphone/widgets/common/heading_widget.dart';
import 'package:multiphone/widgets/common/line_break_widget.dart';
import 'package:multiphone/widgets/settings/settings_controls_flic_widget.dart';
import 'package:multiphone/widgets/settings/select_control_type_widget.dart';
import 'package:multiphone/widgets/settings/settings_controls_volume_widget.dart';
import 'package:multiphone/widgets/settings/settings_data_widget.dart';
import 'package:multiphone/widgets/settings/settings_permissions_widget.dart';
import 'package:multiphone/widgets/settings/settings_privacy_widget.dart';
import 'package:multiphone/widgets/settings/settings_sounds_announcements_widget.dart';
import 'package:multiphone/widgets/settings/settings_sounds_general_widget.dart';
import 'package:multiphone/widgets/settings/settings_user_widget.dart';
import 'package:multiphone/widgets/side_drawer_widget.dart';

class SettingsScreen extends BaseNavScreen {
  static const String routeName = '/settings';

  SettingsScreen({Key key})
      : super(key: key, scaffoldKey: GlobalKey(debugLabel: 'settings'));

  @override
  _SettingsScreenState createState() => _SettingsScreenState();
}

class _SettingsScreenState extends BaseNavScreenState<SettingsScreen> {
  int _selectedIndex = 0;
  static const TextStyle optionStyle =
      TextStyle(fontSize: 30, fontWeight: FontWeight.bold);

  StreamSubscription<User> _userSubscription;
  User _user;

  void _onItemTapped(int index) {
    setState(() {
      _selectedIndex = index;
    });
  }

  @override
  String getScreenTitle(Values values) {
    return values.strings.option_settings;
  }

  @override
  int getMenuSelectionIndex() {
    return MenuItem.menuSettings;
  }

  @override
  void initState() {
    super.initState();

    // also listen for changes to we change if they do log in again
    _userSubscription = FirebaseAuth.instance.authStateChanges().listen((user) {
      Log.debug('user login state changed');
      _changeUserLoginState(user);
    });
  }

  @override
  void dispose() {
    // kill everything created
    if (null != _userSubscription) {
      _userSubscription.cancel();
    }
    super.dispose();
  }

  void _changeUser() {
    // change the user / login or logout or whatever
  }

  void _changeUserLoginState(User user) {
    setState(() {
      _user = user;
    });
  }

  List<Widget> _getSettingsWidgets() {
    switch (_selectedIndex) {
      case 0:
        return [
          SettingsSoundsGeneralWidget(),
          const LineBreakWidget(),
          SettingsSoundsAnnouncementsWidget(),
          const LineBreakWidget(),
        ];
      case 1:
        return [
          SelectControlTypeWidget(),
          const LineBreakWidget(),
          SettingsControlsFlicWidget(),
          const LineBreakWidget(),
          SettingsControlsVolumeWidget(),
          const LineBreakWidget(),
        ];
      case 2:
        return [
          SettingsUserWidget(
            user: _user,
            onChangeUser: _changeUser,
          ),
          const LineBreakWidget(),
          SettingsPermissionsWidget(),
          const LineBreakWidget(),
          SettingsDataWidget(),
          const LineBreakWidget(),
          SettingsPrivacyWidget(),
          const LineBreakWidget(),
        ];
      default:
        return [
          Text(
            'unknown option',
            style: optionStyle,
          )
        ];
    }
  }

  @override
  Widget buildBottomNavigationBar(BuildContext context) {
    final values = Values(context);
    return BottomNavigationBar(
      items: [
        BottomNavigationBarItem(
          icon: Icon(Icons.volume_up),
          label: values.strings.option_settings_sounds,
        ),
        BottomNavigationBarItem(
          icon: Icon(Icons.settings_remote),
          label: values.strings.option_settings_control,
        ),
        BottomNavigationBarItem(
          icon: Icon(Icons.settings),
          label: values.strings.option_settings_general,
        ),
      ],
      currentIndex: _selectedIndex,
      selectedItemColor: Theme.of(context).primaryColorDark,
      onTap: _onItemTapped,
    );
  }

  @override
  Widget buildScreenBody(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(
          left: Values.default_space,
          top: Values.default_space,
          right: Values.default_space),
      child: SingleChildScrollView(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: _getSettingsWidgets(),
        ),
      ),
    );
  }
}
