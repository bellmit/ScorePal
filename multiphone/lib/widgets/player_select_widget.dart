import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/widgets/contact_autocomplete_widget.dart';

class PlayerSelectWidget extends StatefulWidget {
  const PlayerSelectWidget({Key key}) : super(key: key);

  @override
  _PlayerSelectWidgetState createState() => _PlayerSelectWidgetState();
}

class Continent {
  const Continent({
    @required this.name,
    @required this.size,
  });

  final String name;

  final int size;

  @override
  String toString() {
    return '$name ($size)';
  }
}

const List<Continent> continentOptions = [
  Continent(name: 'Africa', size: 30370000),
  Continent(name: 'Antarctica', size: 14000000),
  Continent(name: 'Asia', size: 44579000),
  Continent(name: 'Australia', size: 8600000),
  Continent(name: 'Europe', size: 10180000),
  Continent(name: 'North America', size: 24709000),
  Continent(name: 'South America', size: 17840000),
];

class _PlayerSelectWidgetState extends State<PlayerSelectWidget> {
  @override
  Widget build(BuildContext context) {
    return Container(
      height: Values.image_large,
      width: double.infinity,
      child: Row(
        children: [
          Container(
            height: Values.image_large,
            width: Values.image_large,
            decoration: BoxDecoration(
              color: const Color(0xff7c94b6),
              border: Border.all(
                color: Colors.black,
                width: 1.0,
              ),
              borderRadius: BorderRadius.circular(12),
            ),
          ),
          Expanded(
            child: ContactAutoCompleteWidget(),
          ),
        ],
      ),
    );
  }
}
