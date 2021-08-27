import 'package:flutter/material.dart';
import 'package:flutter_contacts/flutter_contacts.dart';

import 'package:multiphone/helpers/values.dart';

import 'common/common_widgets.dart';

class PlayerNameWidget extends StatefulWidget {
  final void Function(String) onTextChanged;
  final void Function(Contact) onContactSelected;
  final void Function() onPlayerSelectedToServe;
  final bool isPlayerServer;
  final List<Contact> availableOpponents;
  final String hintText;
  final String initialText;

  const PlayerNameWidget({
    Key key,
    @required this.initialText,
    @required this.hintText,
    @required this.availableOpponents,
    @required this.onPlayerSelectedToServe,
    @required this.isPlayerServer,
    @required this.onTextChanged,
    @required this.onContactSelected,
  }) : super(key: key);

  @override
  _PlayerNameWidgetState createState() => _PlayerNameWidgetState();
}

class _PlayerNameWidgetState extends State<PlayerNameWidget> {
  final TextEditingController _textEditingController = TextEditingController();
  final FocusNode _focusNode = FocusNode();
  final GlobalKey _autocompleteKey = GlobalKey();
  Contact _selectedContact;

  @override
  void initState() {
    super.initState();
    // listen for the user entering a nice name
    if (widget.initialText != widget.hintText) {
      _textEditingController.text = widget.initialText;
    }
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
    final contactName = _textEditingController.text.trim();
    if (widget.onTextChanged != null) {
      // inform the passed function then of this change
      widget.onTextChanged(contactName);
    }
    if (null != widget.availableOpponents &&
        (null == _selectedContact ||
            _selectedContact.displayName != contactName)) {
      // we didn't select this contact - but the might have properly entered their name
      // so does the text entered specify a contact we can email etc?
      _selectedContact = null;
      final lcContactName = contactName.toLowerCase();
      for (Contact contact in widget.availableOpponents) {
        if (contact.displayName.toLowerCase().trim() == lcContactName) {
          // this is a match - they typed in their name
          _selectedContact = contact;
          break;
        }
      }
      // and inform the widget of this change (might be null)
      widget.onContactSelected(_selectedContact);
    }
  }

  void _onContactSelected(Contact contact) {
    // release the focus when selected a name (text changed will also be called)
    FocusScope.of(context).requestFocus(FocusNode());
    // this is the contact of the selection - set this on the widget
    _selectedContact = contact;
    widget.onContactSelected(_selectedContact);
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

  Widget _createAutoComplete(BuildContext context) {
    return RawAutocomplete<Contact>(
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
          decoration: InputDecoration(
            hintText: widget.hintText,
          ),
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
                          : IconWidget(Icons.person),
                      title: TextWidget(contact.displayName),
                    ),
                  );
                },
              ),
            ),
          ),
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: EdgeInsets.all(Values.default_space),
      child: Row(
        children: [
          Expanded(
            child: _createAutoComplete(context),
          ),
          Container(
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              color: widget.isPlayerServer
                  ? Theme.of(context).accentColor
                  : Colors.transparent,
            ),
            child: IconButton(
              onPressed: () {
                // close the keyboard then please
                FocusScope.of(context).requestFocus(FocusNode());
                // and inform the widget this was pressed
                widget.onPlayerSelectedToServe();
              },
              icon: AnimatedSwitcher(
                duration:
                    const Duration(milliseconds: Values.animation_duration_ms),
                transitionBuilder: (Widget child, Animation<double> animation) {
                  return ScaleTransition(child: child, scale: animation);
                },
                child: IconSvgWidget(
                  widget.isPlayerServer
                      ? 'player-serving'
                      : 'player-receiving-backhand',
                  isOnBackground: widget.isPlayerServer,
                  key: ValueKey<bool>(widget.isPlayerServer),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
