import 'dart:developer';

import 'package:flutter/material.dart';
import 'package:multiphone/helpers/log.dart';
import 'package:multiphone/helpers/speak_service.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/active_sport.dart';
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
import 'package:multiphone/screens/match_history_screen.dart';
import 'package:multiphone/screens/match_momentum_screen.dart';
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
import 'package:flex_color_scheme/flex_color_scheme.dart';
// Import the firebase_core plugin
import 'package:firebase_core/firebase_core.dart';
import 'package:provider/single_child_widget.dart';

void main() {
  // initialise firebase
  WidgetsFlutterBinding.ensureInitialized();
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  /// The future is part of the state of our widget. We should not call `initializeApp`
  /// directly inside [build].
  final Future<FirebaseApp> _initialization = Firebase.initializeApp();
  // Define which predefined FlexScheme to use.
  final FlexScheme usedFlexScheme = FlexScheme.ebonyClay;
  // Used to select if we use the dark or light theme.
  final ThemeMode themeMode = ThemeMode.system;
  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: _initProviders(),
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
        // A light scheme, passed to FlexColorScheme.light factory, then use
        // toTheme to return the resulting theme to the MaterialApp theme.
        theme: FlexColorScheme.light(
          scheme: usedFlexScheme,
          // Use comfortable on desktops instead of compact, devices use default.
          visualDensity: FlexColorScheme.comfortablePlatformDensity,
        ).toTheme,
        // Same thing for the dark theme, but using FlexColorScheme.dark factory.
        darkTheme: FlexColorScheme.dark(
          scheme: usedFlexScheme,
          visualDensity: FlexColorScheme.comfortablePlatformDensity,
        ).toTheme,
        // Use the above dark or light theme, based on active themeMode
        // value light/dark/system.
        themeMode: themeMode,
        home: FutureBuilder(
          // Initialize FlutterFire:
          future: _initialization,
          builder: (context, snapshot) {
            // firebase has initialised (or not) so we can proceed
            if (snapshot.hasError) {
              log('something went wrong with firebase:${snapshot.error}');
              // just send them to the home screen so they can proceed
              return HomeScreen();
            } else if (snapshot.connectionState == ConnectionState.done) {
              // connected - go on home
              return HomeScreen();
            } else {
              // show the splash screen that we are loading firebase things
              return SplashScreen('');
            }
          },
        ),
        initialRoute: HomeScreen.routeName,
        routes: _initRoutes(),
      ),
    );
  }

  _initRoutes() => {
        AuthScreen.routeName: (ctx) => AuthScreen(),
        UserScreen.routeName: (ctx) => UserScreen(),
        InboxScreen.routeName: (ctx) => InboxScreen(),
        SetupMatchScreen.routeName: (ctx) => SetupMatchScreen(),
        SettingsScreen.routeName: (ctx) => SettingsScreen(),
        ChangeMatchSetupScreen.routeName: (ctx) => ChangeMatchSetupScreen(),
        TrashScreen.routeName: (ctx) => TrashScreen(),
        SetupFlic2Screen.routeName: (ctx) => SetupFlic2Screen(),
        AttributionsScreen.routeName: (ctx) => AttributionsScreen(),
        MatchHistoryScreen.routeName: (ctx) => MatchHistoryScreen(),
        MatchMomentumScreen.routeName: (ctx) => MatchMomentumScreen(),
        PlayTennisScreen.routeName: (ctx) => PlayTennisScreen(),
        EndTennisScreen.routeName: (ctx) => EndTennisScreen(),
        PlayBadmintonScreen.routeName: (ctx) => PlayBadmintonScreen(),
        EndBadmintonScreen.routeName: (ctx) => EndBadmintonScreen(),
        PlayPingPongScreen.routeName: (ctx) => PlayPingPongScreen(),
        EndPingPongScreen.routeName: (ctx) => EndPingPongScreen(),
      };
}

List<SingleChildWidget> _initProviders() => <SingleChildWidget>[
      // global providers because they are simple enough to setup
      // and this way we won't forget
      ChangeNotifierProvider<Players>(create: (ctx) => Players()),
      ChangeNotifierProvider<Sports>(create: (ctx) => Sports()),
      ChangeNotifierProvider<MatchPersistence>(
          create: (ctx) => MatchPersistence()),
      ChangeNotifierProvider<MatchInbox>(create: (ctx) => MatchInbox()),
      ChangeNotifierProvider<SpeakService>(create: (ctx) => SpeakService()),
      _activeSportProvider(),
      _activeSetupProvider(),
      _activeMatchProvider(),
    ];

SingleChildWidget _activeSportProvider() =>
    ChangeNotifierProxyProvider<Sports, ActiveSport>(
      // this proxy is called after the specified sports object is built to set the active one
      update: (ctx, sports, previousActiveSport) {
        // update the selection to the available sports then
        previousActiveSport.updateSportFromAvailable(sports);
        return previousActiveSport;
      },
      create: (ctx) {
        Log.info('created the single active sport provider');
        return ActiveSport();
      },
    );

SingleChildWidget _activeSetupProvider() =>
    ChangeNotifierProxyProvider<ActiveSport, ActiveSetup>(
      // this proxy is called after the specified active selection object is changed
      // to let us supply the setup held in the selection object
      update: (ctx, activeSport, previousSetup) {
        if (activeSport.matchToResume != null) {
          // there is a match to resume so use the setup from that
          final setup = activeSport.matchToResume.getSetup();
          setup.resumeMatch(activeSport.matchToResume);
          Log.info(
              'using the setup for the match of ${activeSport.sport.id.toString()} we want to resume');
          return setup;
        } else if (activeSport.isCreateNewMatch ||
            null == previousSetup ||
            previousSetup.sport != activeSport.sport) {
          // we want to create a new one (or sport different), so do that
          Log.info(
              'creating a new setup of ${activeSport.sport.id.toString()}');
          final newSetup = activeSport.sport == null
              ? null
              : activeSport.sport.createSetup();
          if (activeSport.isCreateNewMatch) {
            // tell the setup to also create a new match please
            newSetup.createNewMatch();
            // reset the active sport to not create a new one
            activeSport.newMatchCreated();
          }
          return newSetup;
        } else {
          Log.info(
              'using the existing setup for ${activeSport.sport.id.toString()}');
          // we can just use the previous setup as-is
          return previousSetup;
        }
      },
      // create an empty one initially - needs the active match setting
      create: (ctx) {
        // there is no setup when there is no sport
        return null;
      },
    );

SingleChildWidget _activeMatchProvider() =>
    ChangeNotifierProxyProvider<ActiveSetup, ActiveMatch>(
      update: (ctx, activeSetup, previousMatch) {
        // the match has to update to reflect any changes in the active setup
        if (activeSetup.matchToResume != null) {
          // there is a match to resume so use that match
          Log.info(
              'using the match of ${activeSetup.sport.id.toString()} we want to resume');
          // but this might be a change in the setup too
          activeSetup.matchToResume.applyChangedMatchSettings();
          // and return the match resumed
          return activeSetup.matchToResume;
        } else if (activeSetup.isCreateNewMatch ||
            null == previousMatch ||
            previousMatch.sport != activeSetup.sport) {
          // the setup wants a new match, or the sport has changed so make a new match
          Log.info(
              'creating a new match of ${activeSetup.sport.id.toString()}');
          // create the new one
          final newMatch = activeSetup.sport.createMatch(activeSetup);
          // and reset the flag on the setup
          activeSetup.newMatchCreated();
          // and return the new one
          return newMatch;
        } else {
          // we can just use the previous match as-is (but the setup changed)
          previousMatch.applyChangedMatchSettings();
          Log.info(
              'using the existing match for ${activeSetup.sport.id.toString()}');
          // and return the updated match
          return previousMatch;
        }
      },
      create: (ctx) {
        // there is no match initially
        return null;
      },
    );
