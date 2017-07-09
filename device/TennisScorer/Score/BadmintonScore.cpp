/*
 * BadmintonScore.cpp
 *
 *  Created on: 16 Apr 2017
 *      Author: douglasbrain
 */

#include "BadmintonScore.h"
#include "../Player.h"

namespace Score {

BadmintonScore::BadmintonScore() {
	// constructor
}

BadmintonScore::~BadmintonScore() {
	// destructor
}

bool BadmintonScore::isMatchOver(uint8_t noGames, MatchState& state) {
	float gamesToWin = noGames / 2.0f;
	// return if someone has exceeded the number of games required to win
	return (state.games[player_one] * 1.0f > gamesToWin) ||
		   (state.games[player_two] * 1.0f > gamesToWin);
}

void BadmintonScore::addPoint(Player player, uint8_t noGames, MatchState& state, BaseScore& scorer) {

	Player otherPlayer = scorer.getOtherPlayer(player);

	// add the point for the winner
	++state.points[player];
	// now check to see if they have won
	if ((state.points[player] >= 30) ||
		(state.points[player] >= 21 && state.points[player] - state.points[otherPlayer] > 1)) {
		// they won the game as they have at least 21 points and are 2 ahead, or 30 points
		// NOW... as badminton don't have sets we can store the results of this game in the sets history store
		scorer.recordGameWon();
		// now the game is recorded we can add the game for the player which will reset our points
		scorer.addGame(player);
	}
}

} /* end namespace Score */

