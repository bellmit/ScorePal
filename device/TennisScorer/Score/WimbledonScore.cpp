/*
 * WimbledonScore.cpp
 *
 *  Created on: 16 Apr 2017
 *      Author: douglasbrain
 */

#include "WimbledonScore.h"
#include "../Player.h"

namespace Score {

#define K_POINTS40 3

WimbledonScore::WimbledonScore() {
	// constructor
}

WimbledonScore::~WimbledonScore() {
	// destructor
}

bool WimbledonScore::isMatchOver(uint8_t noSets, MatchState& state) {
	float setsToWin = noSets / 2.0;
	// return if someone has exceeded the number of sets required to win
	return (state.getSets(player_one) * 1.0f > setsToWin) ||
		   (state.getSets(player_two) * 1.0f > setsToWin);
}

void WimbledonScore::addPoint(Player player, uint8_t noSets, MatchState& state, BaseScore& scorer) {
	Player otherPlayer = scorer.getOtherPlayer(player);

	// check to see if we are in a tie-breaker
	if (false == state.is_tiebreak) {
		// now we do our thing, normall stuff here
		if (state.points[player] < K_POINTS40) {
			// have we 0, 15, or 30, just add
			++state.points[player];
		}
		else if (state.points[player] == K_POINTS40) {
			// we are on 40
			if (state.points[otherPlayer] == K_POINTS40) {
				// they are on 40 too, advantage to the player with the Point
				++state.points[player];
			}
			else if (state.points[otherPlayer] > K_POINTS40) {
				// they are on ad, remove theirs
				--state.points[otherPlayer];
			}
			else {
				// we won the game
				addGame(player, noSets, state, scorer);
			}
		}
		else {
			// just won advantage, player wins this game
			addGame(player, noSets, state, scorer);
		}
	}
	else {
		// do the tie-breaker scoring, add the Point first
		++state.points[player];
		// now check to see if they have won
		if (state.points[player] > 6 && state.points[player] - state.points[otherPlayer] > 1) {
			// they won the tie-break as they have 7 Points at least and are 2 ahead of the other, so they won the tie
			addGame(player, noSets, state, scorer);
		}
		else {
			// we need to swap servers every two Points, except the first
			uint8_t pointSum = state.points[player_one] + state.points[player_two];
			if ((pointSum + 1) % 2 == 0) {
				// swap servers
				scorer.swapServers();
			}
			// also we want to swap servers every six points in a tie-break
			if (pointSum % 6 == 0) {
				// swap ends
				scorer.swapEnds();
			}
		}
	}
}

void WimbledonScore::addGame(Player player, uint8_t noSets, MatchState& state, BaseScore& scorer) {
	// add the game to our score
	Player otherPlayer = scorer.getOtherPlayer(player);
	// just add to the game counter
	++state.games[player];
	bool isSetWon = false;
	if (state.is_tiebreak) {
		// might be 6-6 or whatever, but either way when you win a tie you win the set
		scorer.recordSetWon();
		isSetWon = true;
		// not in a tie break now!
		state.is_tiebreak = false;
		// make the current server the server that started the tie, that way we will swap now to the other
		state.setCurrentServer(state.tie_break_server);
	}
	else if (state.games[player] > 5) {
		// player has six games, check to see if they have won the set
		if (state.games[player] - state.games[otherPlayer] > 1) {
			// the player is now two games ahead having won at least 6, they just won the set
			scorer.recordSetWon();
			isSetWon = true;
		}
		else {
			if (state.games[otherPlayer] == 6) {
				// the other player has six too, this is a tie-breaker as can't get too far ahead
				if (state.getSetsPlayed() < noSets - 1) {
					// this is a tie-breaker as we are not in the final set
					state.is_tiebreak = true;
					// and remember the server, as the other as about to swap
					state.tie_break_server = scorer.getOtherPlayer(state.getCurrentServer());
				}
			}
		}
	}
	if (false == isSetWon) {
		// set not over - if we are on the first, third fifth, seventh game then we need to swap ends here
		// change ends every two games, except the first
		uint8_t gameSum = state.games[player_one] + state.games[player_two];
		// change ends on the first game, then every two (when Points are zero, ie the start)
		if ((gameSum + 1) % 2 == 0) {
			// we are on an odd numbered game in the set, swap ends
			scorer.swapEnds();
		}
	}
	// game over - reset our Points for the next game
	state.resetPoints();
	// swap servers every time we play a new game
	scorer.swapServers();
}

} /* end namespace Score */

