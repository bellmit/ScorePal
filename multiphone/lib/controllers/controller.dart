import 'package:multiphone/controllers/controllers.dart';

abstract class Controller {
  final Controllers provider;

  Controller(Controllers provider) : this.provider = provider;

  void dispose();
}
