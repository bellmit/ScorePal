import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:url_launcher/url_launcher.dart';

class PurchaseFlicWidget extends StatelessWidget {
  const PurchaseFlicWidget({Key key}) : super(key: key);

  static const flicUrl = "https://flic.io/shop/flic-2-single-pack";

  void _navUserToPurchaseFlic(BuildContext context) {
    canLaunch(flicUrl).then((value) {
      return launch(flicUrl);
    }).onError((error, stackTrace) {
      // failed to launch
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(
          content: Text(Values(context).strings.error_navigating_to_flic)));
      return false;
    });
  }

  @override
  Widget build(BuildContext context) {
    final values = Values(context);
    return Card(
      margin: const EdgeInsets.all(Values.default_space),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Padding(
            padding: EdgeInsets.only(
                left: Values.default_space, right: Values.default_space),
            child: SvgPicture.asset(
              'images/svg/flic-two.svg',
              height: Values.image_large,
              width: Values.image_large,
            ),
          ),
          Expanded(
            child: Padding(
              padding: const EdgeInsets.all(Values.default_space),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.end,
                crossAxisAlignment: CrossAxisAlignment.end,
                children: [
                  Text(
                    values.strings.description_purchase_flic,
                    style: TextStyle(
                        fontSize: Values.font_size_title,
                        color: Theme.of(context).primaryColorDark),
                  ),
                  TextButton(
                      onPressed: () => _navUserToPurchaseFlic(context),
                      child: Text(values.strings.action_purchase_flic)),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}
