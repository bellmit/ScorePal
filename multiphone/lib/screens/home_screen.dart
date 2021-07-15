import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:multiphone/widgets/side_drawer.dart';

class HomeScreen extends StatelessWidget {
  final GlobalKey<ScaffoldState> _scaffoldKey = GlobalKey(); // Create a key

  final User _currentUser;
  HomeScreen(this._currentUser);
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      key: _scaffoldKey,
      appBar: AppBar(
        title: Text('Scorepal'),
        leading: IconButton(
            onPressed: () => _scaffoldKey.currentState.openDrawer(),
            icon: Icon(Icons.more_vert)),
      ),
      backgroundColor: Theme.of(context).primaryColor,
      drawer: SideDrawer(currentUser: _currentUser),
      body: Center(
        child: Text(
            'Hello ${_currentUser.displayName}\n${_currentUser.email}\n${_currentUser.uid}'),
      ),
    );
  }
}
