import 'package:flutter/material.dart';
import 'package:flutter_contacts/flutter_contacts.dart';
import 'package:multiphone/helpers/values.dart';

class PlayerNameWidget extends StatefulWidget {
  final void Function(String) onTextChanged;
  final List<Contact> availableOpponents;
  final String hintText;

  const PlayerNameWidget({
    Key key,
    @required this.hintText,
    @required this.availableOpponents,
    this.onTextChanged,
  }) : super(key: key);

  @override
  _PlayerNameWidgetState createState() => _PlayerNameWidgetState();
}

class _PlayerNameWidgetState extends State<PlayerNameWidget> {
  final TextEditingController _textEditingController = TextEditingController();
  final FocusNode _focusNode = FocusNode();
  final GlobalKey _autocompleteKey = GlobalKey();

  @override
  void initState() {
    super.initState();
    // listen for the user entering a nice name
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
    // they are typing - change the settings accordingly
    if (widget.onTextChanged != null) {
      // inform the passed function then of this change
      widget.onTextChanged(_textEditingController.text);
    }
  }

  void _onContactSelected(Contact contact) {
    // release the focus when selected a name (text changed will also be called)
    _focusNode.nextFocus();
  }

  bool _isContactMatch(Contact contact, String text) {
    // only search when there is a contact and enough text to justify searching
    if (contact == null || text == null || text.isEmpty) {
      return false;
    }
    final textToTest = text.toLowerCase().trim();
    if (contact.displayName != null &&
        contact.displayName.toLowerCase().startsWith(textToTest)) {
      // matching display name
      return true;
    } else if (contact.emails != null) {
      // check all the emails
      for (int i = 0; i < contact.emails.length; ++i) {
        var email = contact.emails[i];
        if (email != null && email.address != null) {
          final emailToTest = email.address.toLowerCase().trim();
          if (emailToTest.startsWith(textToTest)) {
            // this is a valid search by email address
            return true;
          }
        }
      }
    }
    // if here, then not what they are looking for
    return false;
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: EdgeInsets.all(15.0),
      child: RawAutocomplete<Contact>(
        key: _autocompleteKey,
        focusNode: _focusNode,
        textEditingController: _textEditingController,
        optionsBuilder: (TextEditingValue textEditingValue) {
          if (widget.availableOpponents == null ||
              widget.availableOpponents.isEmpty) {
            return <Contact>[];
          }
          // else, search for the contacts that fit what the user is typing
          return widget.availableOpponents
              .where((Contact contact) =>
                  _isContactMatch(contact, textEditingValue.text))
              .toList();
        },
        displayStringForOption: (Contact contact) => contact.displayName,
        fieldViewBuilder: (BuildContext context,
            TextEditingController fieldTextEditingController,
            FocusNode fieldFocusNode,
            VoidCallback onFieldSubmitted) {
          return TextField(
            controller: fieldTextEditingController,
            focusNode: fieldFocusNode,
            //style: const TextStyle(fontWeight: FontWeight.bold),
            decoration: InputDecoration(hintText: widget.hintText),
          );
        },
        onSelected: (Contact selection) {
          _onContactSelected(selection);
        },
        optionsViewBuilder: (BuildContext context,
            AutocompleteOnSelected<Contact> onSelected,
            Iterable<Contact> options) {
          return Align(
            alignment: Alignment.topLeft,
            child: Material(
              child: Container(
                width: 300,
                color: Theme.of(context).primaryColorLight,
                child: ListView.builder(
                  padding: EdgeInsets.all(Values.default_space),
                  itemCount: options.length,
                  itemBuilder: (BuildContext context, int index) {
                    final Contact contact = options.elementAt(index);
                    return GestureDetector(
                      onTap: () {
                        onSelected(contact);
                      },
                      child: ListTile(
                        leading: contact.thumbnail != null
                            ? Image.memory(contact.thumbnail)
                            : Icon(Icons.person),
                        title: Text(contact.displayName),
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