import 'package:flutter/material.dart';
import 'package:flutter_html/flutter_html.dart';
import 'package:flutter_staggered_grid_view/flutter_staggered_grid_view.dart';
import 'package:multiphone/helpers/values.dart';
import 'package:multiphone/screens/base_nav_screen.dart';
import 'package:multiphone/widgets/common/common_widgets.dart';
import 'package:multiphone/widgets/side_drawer_widget.dart';
import 'package:url_launcher/url_launcher.dart';

class AttributionsScreen extends BaseNavScreen {
  static const String routeName = '/attributions';

  AttributionsScreen({Key key})
      : super(key: key, scaffoldKey: GlobalKey(debugLabel: 'attributions'));

  @override
  _AttributionsScreenState createState() => _AttributionsScreenState();
}

class _AttributionsScreenState extends BaseNavScreenState<AttributionsScreen> {
  final attributions = [
    {
      'image': 'images/img/badminton.jpg',
      'text':
          "<div about='https://farm6.static.flickr.com/5148/5787645528_baa3eeab85_m.jpg'>&quot;<a href='https://www.flickr.com/photos/davedugdale/5787645528/' target='_blank'>Stuck in the net</a>&quot;&nbsp;(<a rel='license' href='https://creativecommons.org/licenses/by-sa/2.0/' target='_blank'>CC BY-SA 2.0</a>)&nbsp;by&nbsp;<a xmlns:cc='http://creativecommons.org/ns#' rel='cc:attributionURL' property='cc:attributionName' href='https://www.flickr.com/people/davedugdale/' target='_blank'>Dave Dugdale</a></div>",
    },
    {
      'image': 'images/img/ping_pong.jpg',
      'text':
          "<div about='https://farm9.static.flickr.com/8355/8322410752_6c8d3c3ce0_m.jpg'>&quot;<a href='https://www.flickr.com/photos/onepointfour/8322410752/' target='_blank'>Ping Pong ~ Table Tennis</a>&quot;&nbsp;(<a rel='license' href='https://creativecommons.org/licenses/by/2.0/' target='_blank'>CC BY 2.0</a>)&nbsp;by&nbsp;<a rel='cc:attributionURL' property='cc:attributionName' href='https://www.flickr.com/people/onepointfour/' target='_blank'>Dusty J</a></div>",
    },
    {
      'image': 'images/img/points.jpg',
      'text':
          "<a title='score board ebhs' href='https://flickr.com/photos/juliejordanscott/6157459043'>&quot;score board ebhs&quot;</a> flickr photo by <a href='https://flickr.com/people/juliejordanscott'>juliejordanscott</a> shared under a <a href='https://creativecommons.org/licenses/by/2.0/'>Creative Commons (BY) license</a> </small>",
    },
    {
      'image': 'images/img/squash.jpg',
      'text':
          "<div about='https://farm8.static.flickr.com/7775/26772540206_24e784d85d_t.jpg'>&quot;<a href='https://www.flickr.com/photos/georgegillams/26772540206/' target='_blank'>20160415</a>&quot;&nbsp;(<a rel='license' href='https://creativecommons.org/licenses/by-sa/2.0/' target='_blank'>CC BY-SA 2.0</a>)&nbsp;by&nbsp;<a xmlns:cc='http://creativecommons.org/ns#' rel='cc:attributionURL' property='cc:attributionName' href='https://www.flickr.com/people/georgegillams/' target='_blank'>georgegillams</a></div>",
    },
    {
      'image': 'images/img/tennis.jpg',
      'text':
          "&quot;<a title='Jenni Schmidt' href='https://flickr.com/photos/triotex/920948462'>Jenni Schmidt&quot;</a> flickr photo by <a href='https://flickr.com/people/triotex'>Triotex</a> shared under a <a href='https://creativecommons.org/licenses/by-sa/2.0/'>Creative Commons (BY-SA) license</a> </small>",
    },
  ];

  @override
  String getScreenTitle(Values values) {
    return values.strings.option_attributions;
  }

  @override
  int getMenuSelectionIndex() {
    return MenuItem.menuAttributions;
  }

  @override
  Widget buildSideDrawer(BuildContext context) {
    // are hiding the side drawer as not coming from the main menu anymore
    return null;
  }

  @override
  Widget buildIconMenu(BuildContext context) {
    // are hiding the side drawer as not coming from the main menu anymore
    return null;
  }

  void _launchUrl(String url) {
    canLaunch(url).then((value) {
      return launch(url);
    }).onError((error, stackTrace) {
      // failed to launch
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(
          content: TextWidget(Values(context).strings.error_navigating)));
      return false;
    });
  }

  @override
  Widget buildScreenBody(BuildContext context) {
    return OrientationBuilder(
      builder: (ctx, orientation) {
        return StaggeredGridView.countBuilder(
          itemCount: attributions.length,
          crossAxisCount: orientation == Orientation.portrait ? 2 : 3,
          crossAxisSpacing: Values.default_space,
          mainAxisSpacing: Values.default_space,
          staggeredTileBuilder: (int index) => StaggeredTile.fit(1),
          itemBuilder: (BuildContext context, int index) {
            final attributionItem = attributions.elementAt(index);
            return Card(
              child: Column(
                children: [
                  Align(
                    alignment: Alignment.bottomLeft,
                    child: Padding(
                      padding: const EdgeInsets.all(Values.default_space),
                      child: Image.asset(
                        attributionItem['image'],
                        height: 150,
                        width: 150,
                      ),
                    ),
                  ),
                  Html(
                      data: attributionItem['text'],
                      onLinkTap: (url, renderContext, attributes, element) {
                        // send them out to this browser then
                        _launchUrl(url);
                      }),
                ],
              ),
            );
          },
        );
      },
    );
  }
}
