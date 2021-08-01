import 'package:localstore/localstore.dart';
import 'package:multiphone/helpers/log.dart';
import 'package:multiphone/helpers/preferences.dart';
import 'package:multiphone/providers/active_setup.dart';
import 'package:multiphone/providers/sport.dart';

class SetupPersistence {
  static const setupCollection = 'setups';
  static const lastSetupPrefix = 'last';

  Future<Sport> getLastActiveSport() async {
    final preferences = await Preferences.create();
    return preferences.lastActiveSport;
  }

  Future<dynamic> saveAsLastSetupData(ActiveSetup setup) {
    // just send this off and hope it worked
    return Localstore.instance
        .collection(setupCollection)
        .doc('${lastSetupPrefix}_${setup.sport.id}')
        .set(_getSetupAsJSON(setup));
  }

  Map<String, Object> _getSetupAsJSON(ActiveSetup setup) {
    return {
      'ver': 1,
      'sport': setup.sport.id,
      'data': setup.getData(),
    };
  }

  ActiveSetup _createSetupFromJson(Map<String, Object> topLevel) {
    // what is this, get the sport from the JSON object;
    Sport sport = Sports.fromId(topLevel['sport'] as String);
    // and create the setup for this
    ActiveSetup setup = sport.createSetup();
    // set our data from this data under the top level
    setup.setData(topLevel['data']);
    // and return this now it's setup properly
    return setup;
  }

  Future<ActiveSetup> loadLastSetupData(ActiveSetup setup) async {
    // just get the last setup data and put into the already loaded setup
    final defaultData = await Localstore.instance
        .collection(setupCollection)
        .doc('${lastSetupPrefix}_${setup.sport.id}')
        .get();
    if (defaultData != null && defaultData['data'] != null) {
      // have the document, load ths data from this
      setup.setData(defaultData['data']);
    } else {
      Log.debug(
          'default match setup data ${defaultData == null ? null : defaultData} isn\'t valid for ${setup.sport.id}');
    }
    return setup;
  }

  Future<ActiveSetup> loadLastSetup({Sport sport}) async {
    if (sport == null) {
      sport = await getLastActiveSport();
    }
    // from this we can create the setup
    final setup = sport.createSetup();
    // and get the data for this setup
    return loadLastSetupData(setup);
  }
}
