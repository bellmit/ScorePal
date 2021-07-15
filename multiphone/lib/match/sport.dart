enum SportType {
  TENNIS,
  BADMINTON,
  PING_PONG,
}

class Sport {
  static const TENNIS = Sport(id: SportType.TENNIS, title: 'Tennis', icon: 'tennis.jpg', image: 'tennis.jpg');
  static const BADMINTON = Sport(id: SportType.BADMINTON, title: 'Badminton', icon: 'badminton.jpg', image: 'badminton.jpg');
  static const PING_PONG = Sport(id: SportType.PING_PONG, title: 'Ping Pong', icon: 'ping_pong.jpg', image: 'ping_pong.jpg');

  final SportType id;
  final String title;
  final String icon;
  final String image;

  const Sport({this.id, this.title, this.icon, this.image});
}