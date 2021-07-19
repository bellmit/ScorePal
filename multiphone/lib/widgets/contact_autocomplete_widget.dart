import 'package:flutter/material.dart';

class ContactAutoCompleteWidget extends StatefulWidget {
  const ContactAutoCompleteWidget({Key key}) : super(key: key);

  @override
  _ContactAutoCompleteWidgetState createState() =>
      _ContactAutoCompleteWidgetState();
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

class _ContactAutoCompleteWidgetState extends State<ContactAutoCompleteWidget> {
  final TextEditingController _textEditingController = TextEditingController();
  final FocusNode _focusNode = FocusNode();
  final GlobalKey _autocompleteKey = GlobalKey();

  @override
  void initState() {
    super.initState();

    _textEditingController.addListener(_textChanged);
  }

  @override
  void dispose() {
    _textEditingController.removeListener(_textChanged);
    _textEditingController.dispose();
    super.dispose();
  }

  void clear() {
    _textEditingController.clear();
  }

  void _textChanged() {
    print('text is now ${_textEditingController.text}');
  }

  void _onContactSelected(Continent contact) {
    print('Selected: ${contact.name}');
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: EdgeInsets.all(15.0),
      child: RawAutocomplete<Continent>(
        key: _autocompleteKey,
        focusNode: _focusNode,
        textEditingController: _textEditingController,
        optionsBuilder: (TextEditingValue textEditingValue) {
          return continentOptions
              .where((Continent continent) => continent.name
                  .toLowerCase()
                  .startsWith(textEditingValue.text.toLowerCase()))
              .toList();
        },
        displayStringForOption: (Continent option) => option.name,
        fieldViewBuilder: (BuildContext context,
            TextEditingController fieldTextEditingController,
            FocusNode fieldFocusNode,
            VoidCallback onFieldSubmitted) {
          return TextField(
            controller: fieldTextEditingController,
            focusNode: fieldFocusNode,
            //style: const TextStyle(fontWeight: FontWeight.bold),
          );
        },
        onSelected: (Continent selection) {
          _onContactSelected(selection);
        },
        optionsViewBuilder: (BuildContext context,
            AutocompleteOnSelected<Continent> onSelected,
            Iterable<Continent> options) {
          return Align(
            alignment: Alignment.topLeft,
            child: Material(
              child: Container(
                width: 300,
                color: Colors.teal,
                child: ListView.builder(
                  padding: EdgeInsets.all(10.0),
                  itemCount: options.length,
                  itemBuilder: (BuildContext context, int index) {
                    final Continent option = options.elementAt(index);

                    return GestureDetector(
                      onTap: () {
                        onSelected(option);
                      },
                      child: ListTile(
                        title: Text(option.name,
                            style: const TextStyle(color: Colors.white)),
                      ),
                    );
                  },
                ),
              ),
            ),
          );
        },
      ),
    );
  }
}
