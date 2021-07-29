class Log {
  static void debug(information) {
    print(information);
  }

  static void info(information) {
    print(information);
  }

  static void error(information) {
    print('ERROR:$information');
  }
}
