import 'package:multiphone/match/player.dart';

enum TeamIndex {
  T_ONE,
  T_TWO,
}

class Teams {
  static const Team teamOne = Team(TeamIndex.T_ONE, "Team One");
  static const Team teamTwo = Team(TeamIndex.T_TWO, "Team Two");
}

class Team {
  final TeamIndex index;
  final String name;

  const Team(this.index, this.name);

  Team get otherTeam {
    if (index == TeamIndex.T_ONE) {
      return Teams.teamTwo;
    } else {
      return Teams.teamOne;
    }
  }

  Player get player {
    if (index == TeamIndex.T_ONE) {
      return Players.playerOne;
    } else {
      return Players.playerTwo;
    }
  }

  Player get partner {
    if (index == TeamIndex.T_ONE) {
      return Players.partnerOne;
    } else {
      return Players.partnerTwo;
    }
  }
}