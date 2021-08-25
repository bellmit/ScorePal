import 'dart:developer';

import 'package:flutter/material.dart';
import 'package:multiphone/helpers/log.dart';
import 'package:multiphone/helpers/speak_service.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/match_id.dart';
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
          // this proxy is called after the specified sports object is build
          update: (ctx, sports, previousSelection) {
            Log.debug(
                'updating selection with ${sports == null || sports.available == null ? '0' : sports.available.length} sports');
            return ActiveSelection(sports);
          },
          create: (ctx) {
            Log.debug('creating null active selection');
            return ActiveSelection(null);
          },
        ),
        ChangeNotifierProxyProvider<ActiveSelection, ActiveSetup>(
          // this proxy is called after the specified active match object is build
          update: (ctx, activeSelection, previousSetup) {
            final ActiveMatch selectedMatch = activeSelection.selectedMatch;
            if (selectedMatch != null) {
              // there is an active match running, this is the setup to use then
              Log.debug(
                  'using the setup from the active selected match ${MatchId.create(selectedMatch).toString()}');
              return selectedMatch.getSetup();
            } else {
              // create the correct match setup from the sport, this makes the selection not have a match selected
              activeSelection.selectMatch(null, true);
              Log.debug(
                  'creating a new setup for the sport of ${activeSelection.sport == null ? 'null' : activeSelection.sport.id}');
              // and create the new setup
              return activeSelection.sport.createSetup();
            }
          },
          // create an empty one initially - needs the active match setting
          create: (ctx) {
            Log.debug('creating a null setup, no sport selected at this time');
            return null;
          },
        ),
        ChangeNotifierProxyProvider<ActiveSetup, ActiveMatch>(
            update: (ctx, setup, previousMatch) {
          // this setup might change when the selection selects an active match
          final activeSelection =
              Provider.of<ActiveSelection>(ctx, listen: false);
          final selectedMatch = activeSelection.selectedMatch;
          if (null != selectedMatch) {
            // just always use the selected match
            selectedMatch.applyChangedMatchSettings();
            Log.debug(
                'using the active selected match ${MatchId.create(selectedMatch).toString()}');
            return selectedMatch;
          } else if (previousMatch == null ||
              setup.sport.id != previousMatch.getSport().id ||
              activeSelection.isCreateNextMatchNew) {
            // this is a change in sport, create the new match needed
            Log.debug(
                'new setup as switching sport to ${setup.sport == null ? 'null' : setup.sport.id}');
            activeSelection.selectMatch(null, false);
            // and return a new match that the provider will inform people about
            return setup.sport.createMatch(setup);
          } else {
            // don't let there be a selected one, we are using the previous one
            activeSelection.selectMatch(null, false);
            // this is the same sport, just update the match running
            Log.debug('applying a setup change to the previous match');
            previousMatch.applyChangedMatchSettings();
            // and return the same
            return previousMatch;
          }
        }, create: (ctx) {
          // this is the first match created - create it for the selected setup
          var setup = Provider.of<ActiveSetup>(ctx, listen: false);
          Log.debug(
              'creating first match of ${setup.sport == null ? 'null' : setup.sport.id}');
          return setup.sport.createMatch(setup);
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
