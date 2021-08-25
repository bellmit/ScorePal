import 'dart:developer';

import 'package:flutter/material.dart';
import 'package:multiphone/helpers/speak_service.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/active_selection.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/providers/match_inbox.dart';
import 'package:multiphone/providers/match_persistence.dart';
import 'package:multiphone/providers/player.dart';
import 'package:multiphone/providers/sport.dart';
import 'package:multiphone/screens/attributions_screen.dart';
import 'package:multiphone/screens/auth_screen.dart';
import 'package:multiphone/screens/change_match_setup_screen.dart';
import 'package:multiphone/screens/home_screen.dart';
import 'package:multiphone/screens/inbox_screen.dart';
import 'package:multiphone/screens/setup_flic2_screen.dart';
import 'package:multiphone/screens/trash_screen.dart';
import 'package:multiphone/screens/user_screen.dart';
import 'package:multiphone/widgets/badminton/end_badminton_screen.dart';
import 'package:multiphone/widgets/badminton/play_badminton_screen.dart';
import 'package:multiphone/widgets/pingpong/end_ping_pong_screen.dart';
import 'package:multiphone/widgets/pingpong/play_ping_pong_screen.dart';
import 'package:multiphone/screens/setup_match_screen.dart';
import 'package:multiphone/widgets/tennis/end_tennis_screen.dart';
import 'package:multiphone/widgets/tennis/play_tennis_screen.dart';
import 'package:multiphone/screens/settings_screen.dart';
import 'package:multiphone/screens/splash_screen.dart';
import 'package:provider/provider.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_gen/gen_l10n/app_localizations.dart';
// Import the firebase_core plugin
import 'package:firebase_core/firebase_core.dart';

void main() {
  // initialise firebase
  WidgetsFlutterBinding.ensureInitialized();
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  /// The future is part of the state of our widget. We should not call `initializeApp`
  /// directly inside [build].
  final Future<FirebaseApp> _initialization = Firebase.initializeApp();
  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        // global providers because they are simple enough to setup
        // and this way we won't forget
        ChangeNotifierProvider<Players>(create: (ctx) => Players()),
        ChangeNotifierProvider<Sports>(create: (ctx) => Sports()),
        ChangeNotifierProvider<MatchPersistence>(
            create: (ctx) => MatchPersistence()),
        ChangeNotifierProvider<MatchInbox>(create: (ctx) => MatchInbox()),
        ChangeNotifierProvider<SpeakService>(create: (ctx) => SpeakService()),
        ChangeNotifierProxyProvider<Sports, ActiveSelection>(
          // this proxy is called after the specified sports object is built
          update: (ctx, sports, previousSelection) {
            return ActiveSelection(sports);
          },
          create: (ctx) {
            return ActiveSelection(null);
          },
        ),
        ChangeNotifierProxyProvider<ActiveSelection, ActiveSetup>(
          // this proxy is called after the specified active selection object is changed
          // to let us supply the setup held in the selection object
          update: (ctx, activeSelection, previousSetup) {
            final ActiveSetup selected =
                activeSelection.getSelectedSetup(false);
            if (selected != null && selected.sport == activeSelection.sport) {
              // there is an active setup that's the correct sport, use this
              return selected;
            } else {
              // there is no match, create a new setup for the sport selected
              return activeSelection.createSetup();
            }
          },
          // create an empty one initially - needs the active match setting
          create: (ctx) {
            return null;
          },
        ),
        ChangeNotifierProxyProvider<ActiveSetup, ActiveMatch>(
            update: (ctx, setup, previousMatch) {
          // this setup might change when the selection selects an active match
          final activeSelection =
              Provider.of<ActiveSelection>(ctx, listen: false);
          final selectedMatch = activeSelection.getSelectedMatch(false);
          if (null != selectedMatch && setup.sport == activeSelection.sport) {
            // and return the active selected match, first apply the settings changed
            selectedMatch.applyChangedMatchSettings();
            // and return the selected match then
            return selectedMatch;
          } else {
            // there is no selected match in the selection, create a new match
            return activeSelection.createMatch();
          }
        }, create: (ctx) {
          // this is the first match created - create it for the selected setup
          final activeSelection =
              Provider.of<ActiveSelection>(ctx, listen: false);
          final selectedMatch = activeSelection.getSelectedMatch(false);
          if (null != selectedMatch &&
              selectedMatch.getSport() == activeSelection.sport) {
            // there is a selected match of the correct sport, use this
            return selectedMatch;
          } else {
            // create a new match
            return activeSelection.createMatch();
          }
        }),
      ],
      child: MaterialApp(
        onGenerateTitle: (ctx) => Values(ctx).strings.title,
        localizationsDelegates: const [
          AppLocalizations.delegate,
          GlobalMaterialLocalizations.delegate,
          GlobalWidgetsLocalizations.delegate,
          GlobalCupertinoLocalizations.delegate,
        ],
        supportedLocales: const [
          Locale('en', ''), // English, no country code
          Locale('fr', ''), // French, no country code
        ],
        theme: ThemeData(
          // This is the theme of your application.
          scaffoldBackgroundColor: Values.primaryTextColor,
          backgroundColor: Values.primaryTextColor,
          primaryColor: Values.primaryColor,
          primaryColorLight: Values.primaryLightColor,
          primaryColorDark: Values.primaryDarkColor,
          accentColor: Values.secondaryLightColor,
          // Define the default font family.
          fontFamily: 'Georgia',

          // Define the default TextTheme. Use this to specify the default
          // text styling for headlines, titles, bodies of text, and more.
          textTheme: const TextTheme(
            headline1: TextStyle(fontSize: 72.0, fontWeight: FontWeight.bold),
            headline6: TextStyle(fontSize: 36.0, fontStyle: FontStyle.italic),
            bodyText1: TextStyle(
                fontSize: 16.0,
                fontFamily: 'Hind',
                color: Values.secondaryTextColor),
            bodyText2: TextStyle(
                fontSize: 16.0,
                fontFamily: 'Hind',
                color: Values.secondaryLightColor),
          ),
        ),
        home: FutureBuilder(
          // Initialize FlutterFire:
          future: _initialization,
          builder: (context, snapshot) {
            // firebase has initialised (or not) so we can proceedpug
            if (snapshot.hasError) {
              log('something went wrong with firebase:${snapshot.error}');
              // just send them to the home screen so they can proceed
              return HomeScreen();
            } else if (snapshot.connectionState == ConnectionState.done) {
              // connected - go on home
              return HomeScreen();
            } else {
              // show the splash screen that we are loading firebase things
              return SplashScreen(SplashScreenState.loading, '');
            }
          },
        ),
        initialRoute: HomeScreen.routeName,
        routes: {
          AuthScreen.routeName: (ctx) => AuthScreen(),
          UserScreen.routeName: (ctx) => UserScreen(),
          InboxScreen.routeName: (ctx) => InboxScreen(),
          SetupMatchScreen.routeName: (ctx) => SetupMatchScreen(),
          SettingsScreen.routeName: (ctx) => SettingsScreen(),
          ChangeMatchSetupScreen.routeName: (ctx) => ChangeMatchSetupScreen(),
          TrashScreen.routeName: (ctx) => TrashScreen(),
          SetupFlic2Screen.routeName: (ctx) => SetupFlic2Screen(),
          AttributionsScreen.routeName: (ctx) => AttributionsScreen(),
          PlayTennisScreen.routeName: (ctx) => PlayTennisScreen(),
          EndTennisScreen.routeName: (ctx) => EndTennisScreen(),
          PlayBadmintonScreen.routeName: (ctx) => PlayBadmintonScreen(),
          EndBadmintonScreen.routeName: (ctx) => EndBadmintonScreen(),
          PlayPingPongScreen.routeName: (ctx) => PlayPingPongScreen(),
          EndPingPongScreen.routeName: (ctx) => EndPingPongScreen(),
        },
      ),
    );
  }
}
