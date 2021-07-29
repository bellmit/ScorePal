import 'dart:developer';

import 'package:flutter/material.dart';
import 'package:multiphone/helpers/log.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/providers/active_selection.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/providers/player.dart';
import 'package:multiphone/providers/sport.dart';
import 'package:multiphone/screens/auth_screen.dart';
import 'package:multiphone/screens/home_screen.dart';
import 'package:multiphone/widgets/badminton/play_badminton_screen.dart';
import 'package:multiphone/widgets/pingpong/play_ping_pong_screen.dart';
import 'package:multiphone/screens/setup_match_screen.dart';
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
        ChangeNotifierProvider(create: (ctx) => Players()),
        ChangeNotifierProvider(create: (ctx) => Sports()),
        ChangeNotifierProxyProvider<Sports, ActiveSelection>(
          // this proxy is called after the specified sports object is build
          update: (ctx, sports, previousMatch) {
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
          update: (ctx, activeMatch, previousSetup) {
            // create the correct match setup from the sport
            Log.debug(
                'creating a new setup for the sport of ${activeMatch.sport == null ? 'null' : activeMatch.sport.id}');
            return activeMatch.sport.createSetup();
          },
          // create an empty one initially - needs the active match setting
          create: (ctx) {
            Log.debug('creating a null setup, no sport selected at this time');
            return null;
          },
        ),
        ChangeNotifierProxyProvider<ActiveSetup, ActiveMatch>(
            update: (ctx, setup, previousMatch) {
          if (previousMatch == null ||
              setup.sport.id != previousMatch.getSport().id) {
            // this is a change in sport, create the new match needed
            Log.debug(
                'new setup as switching sport to ${setup.sport == null ? 'null' : setup.sport.id}');
            return setup.sport.createMatch(setup);
          } else {
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
              return SplashScreen(SplashScreenState.loading, '');
            }
          },
        ),
        initialRoute: HomeScreen.routeName,
        routes: {
          AuthScreen.routeName: (ctx) => AuthScreen(),
          SetupMatchScreen.routeName: (ctx) => SetupMatchScreen(),
          SettingsScreen.routeName: (ctx) => SettingsScreen(),
          PlayTennisScreen.routeName: (ctx) => PlayTennisScreen(),
          PlayBadmintonScreen.routeName: (ctx) => PlayBadmintonScreen(),
          PlayPingPongScreen.routeName: (ctx) => PlayPingPongScreen(),
        },
      ),
    );
  }
}

class MyHomePage extends StatefulWidget {
  MyHomePage({Key key, this.title = ''}) : super(key: key);

  // This widget is the home page of your application. It is stateful, meaning
  // that it has a State object (defined below) that contains fields that affect
  // how it looks.

  // This class is the configuration for the state. It holds the values (in this
  // case the title) provided by the parent (in this case the App widget) and
  // used by the build method of the State. Fields in a Widget subclass are
  // always marked "final".

  final String title;

  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  int _counter = 0;

  void _incrementCounter() {
    setState(() {
      // This call to setState tells the Flutter framework that something has
      // changed in this State, which causes it to rerun the build method below
      // so that the display can reflect the updated values. If we changed
      // _counter without calling setState(), then the build method would not be
      // called again, and so nothing would appear to happen.
      _counter++;
    });
  }

  @override
  Widget build(BuildContext context) {
    // This method is rerun every time setState is called, for instance as done
    // by the _incrementCounter method above.
    //
    // The Flutter framework has been optimized to make rerunning build methods
    // fast, so that you can just rebuild anything that needs updating rather
    // than having to individually change instances of widgets.
    return Scaffold(
      appBar: AppBar(
        // Here we take the value from the MyHomePage object that was created by
        // the App.build method, and use it to set our appbar title.
        title: Text(widget.title),
      ),
      body: Center(
        // Center is a layout widget. It takes a single child and positions it
        // in the middle of the parent.
        child: Column(
          // Column is also a layout widget. It takes a list of children and
          // arranges them vertically. By default, it sizes itself to fit its
          // children horizontally, and tries to be as tall as its parent.
          //
          // Invoke "debug painting" (press "p" in the console, choose the
          // "Toggle Debug Paint" action from the Flutter Inspector in Android
          // Studio, or the "Toggle Debug Paint" command in Visual Studio Code)
          // to see the wireframe for each widget.
          //
          // Column has various properties to control how it sizes itself and
          // how it positions its children. Here we use mainAxisAlignment to
          // center the children vertically; the main axis here is the vertical
          // axis because Columns are vertical (the cross axis would be
          // horizontal).
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            Text(
              'You have pushed the button this many times:',
            ),
            Text(
              '$_counter',
              style: Theme.of(context).textTheme.headline4,
            ),
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: _incrementCounter,
        tooltip: 'Increment',
        child: Icon(Icons.add),
      ), // This trailing comma makes auto-formatting nicer for build methods.
    );
  }
}
