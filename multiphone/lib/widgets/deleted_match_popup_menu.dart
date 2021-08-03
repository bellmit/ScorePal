import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';

enum DeletedMatchMenuItem {
  wipe,
  resume,
}

class DeletedMatchPopupMenu extends StatelessWidget {
  final void Function(DeletedMatchMenuItem option) onMenuItemSelected;
  const DeletedMatchPopupMenu({Key key, @required this.onMenuItemSelected})
      : super(key: key);

  PopupMenuItem<DeletedMatchMenuItem> createMenuItem(BuildContext context,
      DeletedMatchMenuItem item, IconData icon, String title) {
    return PopupMenuItem<DeletedMatchMenuItem>(
      value: item,
      child: Row(
        children: [
          Icon(icon, color: Theme.of(context).primaryColorDark),
          SizedBox(width: Values.default_space),
          Text(
            title,
            style: TextStyle(color: Theme.of(context).primaryColorDark),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final values = Values(context);
    return PopupMenuButton<DeletedMatchMenuItem>(
      onSelected: onMenuItemSelected,
      itemBuilder: (ctx) => <PopupMenuEntry<DeletedMatchMenuItem>>[
        createMenuItem(
          context,
          DeletedMatchMenuItem.resume,
          Icons.play_arrow,
          values.strings.deleted_match_menu_resume,
        ),
        createMenuItem(
          context,
          DeletedMatchMenuItem.wipe,
          Icons.delete_forever,
          values.strings.deleted_match_menu_delete,
        ),
      ],
    );
  }
}
