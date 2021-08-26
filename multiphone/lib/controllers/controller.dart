import 'package:multiphone/controllers/controllers.dart';

abstract class Controller {
  final Controllers provider;
  final ClickSource clickSource;

  Controller(this.provider, this.clickSource);

  void dispose();
}
