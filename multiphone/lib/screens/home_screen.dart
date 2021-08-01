import 'package:flutter/material.dart';
import 'package:multiphone/helpers/match_persistence.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/match/match_writer.dart';
import 'package:multiphone/providers/active_match.dart';
import 'package:multiphone/screens/setup_match_screen.dart';
import 'package:multiphone/widgets/side_drawer_widget.dart';

class HomeScreen extends StatefulWidget {
  static const String routeName = '/';

  HomeScreen();

  @override
  _HomeScreenState createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  final GlobalKey<ScaffoldState> _scaffoldKey = GlobalKey();

  @override
  Widget build(BuildContext context) {
    // get the values to get strings from
    var values = Values(context);
    // and return the scaffold
    return Scaffold(
      key: _scaffoldKey,
      appBar: AppBar(
        title: Text(values.strings.title),
        leading: IconButton(
            onPressed: () => _scaffoldKey.currentState.openDrawer(),
            icon: Icon(Icons.more_vert)),
      ),
      drawer: SideDrawer(
          menuItems: MenuItem.mainMenuItems(context),
          currentPath: HomeScreen.routeName),
      body: FutureBuilder(
        future: MatchPersistence().getMatches(null),
        builder: (ctx, snapshot) {
          if (snapshot.connectionState == ConnectionState.done) {
            if (snapshot.data == null) {
              return Center(child: Text('no data'));
            }
            // return the list of data
            Iterable<ActiveMatch> matches = snapshot.data.values;
            return ListView.builder(
                itemCount: matches.length,
                itemBuilder: (ctx, index) {
                  final match = matches.elementAt(index);
                  return ListTile(
                    title: Text(match.getSport().id),
                    subtitle:
                        Text(match.getDescription(DescriptionLevel.SHORT, ctx)),
                  );
                });
          } else {
            return Center(child: CircularProgressIndicator());
          }
        },
      ),
      floatingActionButton: FloatingActionButton(
        heroTag: ValueKey<String>('play_match'),
        onPressed: () {
          Navigator.of(context).pushNamed(SetupMatchScreen.routeName);
        },
        child: const Icon(Icons.play_arrow),
        backgroundColor: Theme.of(context).accentColor,
      ),
    );
  }
}
