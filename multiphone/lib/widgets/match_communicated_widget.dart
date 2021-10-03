import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/providers/active_setup.dart';

import 'common/common_widgets.dart';

class MatchCommunicatedWidget extends StatelessWidget {
  final ActiveSetup setup;
  const MatchCommunicatedWidget({
    Key key,
    @required this.setup,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final values = Values(context);
    return Column(
      children: [
        if (setup.isCommunicatedFrom)
          ListTile(
            leading: IconWidget(Icons.hail),
            title: TextWidget(values.strings.auto_send_rx_summary),
          ),
        if (!setup.isCommunicatedFrom && setup.communicatedTo.isNotEmpty)
          ListTile(
            leading: IconWidget(Icons.person_add),
            title: TextWidget(
              values.construct(values.strings.auto_send_summary,
                  [setup.communicatedTo.length]),
            ),
            subtitle: Column(
                children: setup.communicatedTo
                    .map(
                      (e) => TextWidget(
                          setup.getPlayerNameForEmail(e.email) ?? e.username),
                    )
                    .toList()),
          ),
      ],
    );
  }
}
