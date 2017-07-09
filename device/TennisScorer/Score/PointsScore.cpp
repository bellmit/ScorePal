/*
 * PointsScore.cpp
 *
 *  Created on: 16 Apr 2017
 *      Author: douglasbrain
 */

#include "PointsScore.h"
#include "../Player.h"

namespace Score {

PointsScore::PointsScore() {
	// construction
}

PointsScore::~PointsScore() {
	// destructor
}

void PointsScore::addPoint(Player player, MatchState& state, BaseScore& scorer) {
	// add the points for the player
	++state.points[player];
	// limit the points won to something sensible here
	uint8_t pointSum = state.points[player_one] + state.points[player_two];
	if (pointSum > 0 && pointSum % 5 == 0) {
		// change the server
		if (state.getCurrentServer() == player_one) {
			state.setCurrentServer(player_two);
		}
		else {
			state.setCurrentServer(player_one);
		}
	}

	if (state.points[player_one] > 99 || state.points[player_two] > 99) {
		// gone over the edge of Points, reset a little
		uint8_t toRemove;
		if (state.points[player_one] < state.points[player_two]) {
			toRemove = state.points[player_one];
		}
		else {
			toRemove = state.points[player_two];
		}
		state.points[player_one] -= toRemove;
		state.points[player_two] -= toRemove;
		// this might not be enough if they have a huge advantage - so disadvantage them a little
		if (state.points[player_one] > 90) {
			state.points[player_one] = 50;
		}
		if (state.points[player_two] > 90) {
			state.points[player_two] = 50;
		}
	}

	// we change ends after five Points, then every ten Points
	if (pointSum > 0 && (pointSum + 5) % 10 == 0) {
		// change ends then
		state.setCurrentNorthPlayer(scorer.getOtherPlayer(state.getCurrentNorthPlayer()));
	}
}

} /* end namespace Score */

