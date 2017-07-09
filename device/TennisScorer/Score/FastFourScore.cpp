/*
 * FastFourScore.cpp
 *
 *  Created on: 16 Apr 2017
 *      Author: douglasbrain
 */

#include "FastFourScore.h"
#include "../Player.h"

namespace Score {

#define K_POINTS40 3

FastFourScore::FastFourScore() {
	// constructor
}

FastFourScore::~FastFourScore() {
	// destructor
}

bool FastFourScore::isMatchOver(MatchState& state) {
	// first to four games wins it
	return state.games[player_one] >= 4 ||
		   state.games[player_two] >= 4;
}

void FastFourScore::addPoint(Player player, MatchState& state, BaseScore& scorer) {
	// add the point for the winner
	++state.points[player];
	// now check to see if they have won
	if (false == state.is_tiebreak) {
		if (state.points[player] > K_POINTS40) {
			// they won the game as sudden death from deuce
			scorer.recordGameWon();
			// now the game is recorded we can add the game for the player which will reset our points
			scorer.addGame(player);
			if (false == isMatchOver(state)) {
				// match not over - if we are on the first, third and fifth game then we need to swap ends here
				// change ends every two games, except the first
				uint8_t gameSum = state.games[player_one] + state.games[player_two];
				// change ends on the first game, then every two (when Points are zero, ie the start)
				if ((gameSum + 1) % 2 == 0) {
					// we are on an odd numbered game in the set, swap ends
					scorer.swapEnds();
				}
			}
			else if (state.games[player_one] == 3 && state.games[player_two] == 3) {
				state.is_tiebreak = true;
			}
			// swap servers every time we play a new game
			scorer.swapServers();
		}
	}
	else {
		// do the tie-breaker scoring, add the Point first
		++state.points[player];
		// now check to see if they have won the tie
		if (state.points[player] > 4) {
			// they won the tie-break as they have 5 points - winner!
			scorer.recordGameWon();
			// now the game is recorded we can add the game for the player which will reset our points
			scorer.addGame(player);
		}
		else {
			// we need to swap servers every two Points, except the first
			uint8_t pointSum = state.points[player_one] + state.points[player_two];
			if (pointSum % 2 == 0) {
				// swap servers, after every points played, swap every two
				scorer.swapServers();
			}
			// we will only swap ends after the first 4 points have been played
			if (pointSum > 4) {
				// swap ends
				scorer.swapEnds();
			}
		}
	}
}

} /* end namespace Score */

