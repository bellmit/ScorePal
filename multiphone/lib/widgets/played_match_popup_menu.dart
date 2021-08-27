import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';

import 'common/common_widgets.dart';

enum PlayedMatchMenuItem {
  delete,
  resume,
  share,
}

class PlayedMatchPopupMenu extends StatelessWidget {
  final void Function(PlayedMatchMenuItem option) onMenuItemSelected;
  const PlayedMatchPopupMenu({Key key, @required this.onMenuItemSelected})
      : super(key: key);

  PopupMenuItem<PlayedMatchMenuItem> createMenuItem(BuildContext context,
      PlayedMatchMenuItem item, IconData icon, String title) {
    return PopupMenuItem<PlayedMatchMenuItem>(
      value: item,
      child: Row(
        children: [
          IconWidget(icon, size: null),
          SizedBox(width: Values.default_space),
          TextWidget(title),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final values = Values(context);
    return PopupMenuButton<PlayedMatchMenuItem>(
      onSelected: onMenuItemSelected,
      itemBuilder: (ctx) => <PopupMenuEntry<PlayedMatchMenuItem>>[
        createMenuItem(
          context,
          PlayedMatchMenuItem.resume,
          Icons.play_arrow,
          values.strings.played_match_menu_resume,
        ),
        createMenuItem(
          context,
          PlayedMatchMenuItem.delete,
          Icons.delete,
          values.strings.played_match_menu_delete,
        ),
        createMenuItem(
          context,
          PlayedMatchMenuItem.share,
          Icons.share,
          values.strings.played_match_menu_share,
        ),
      ],
    );
  }
}
