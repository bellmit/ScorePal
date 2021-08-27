import 'package:flutter/material.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/widgets/common/common_widgets.dart';
import 'package:url_launcher/url_launcher.dart';

class PurchaseFlicWidget extends StatelessWidget {
  const PurchaseFlicWidget({Key key}) : super(key: key);

  static const flicUrl = "https://flic.io/shop/flic-2-single-pack";

  static void navUserToPurchaseFlic(BuildContext context) {
    canLaunch(flicUrl).then((value) {
      return launch(flicUrl);
    }).onError((error, stackTrace) {
      // failed to launch
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(
          content:
              TextWidget(Values(context).strings.error_navigating_to_flic)));
      return false;
    });
  }

  @override
  Widget build(BuildContext context) {
    final values = Values(context);
    final media = MediaQuery.of(context);
    final cardWidth = media.orientation == Orientation.landscape
        ? media.size.width * 0.4
        : media.size.width * 0.9;
    return Card(
      margin: const EdgeInsets.all(Values.default_space),
      child: ConstrainedBox(
        constraints: BoxConstraints.loose(Size.fromWidth(cardWidth)),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Padding(
              padding: EdgeInsets.only(
                  left: Values.default_space, right: Values.default_space),
              child: IconSvgWidget('flic-two'),
            ),
            Flexible(
              child: Column(
                children: [
                  TextWidget(
                    values.strings.description_purchase_flic,
                  ),
                  Align(
                    alignment: Alignment.bottomRight,
                    child: TextButton(
                        onPressed: () => navUserToPurchaseFlic(context),
                        child: TextWidget(values.strings.action_purchase_flic)),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
