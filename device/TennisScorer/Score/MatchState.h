/*
 * WimbledonMatchState.h
 *
 *  Created on: 16 Apr 2017
 *      Author: douglasbrain
 */

#ifndef MATCHSTATE_H_
#define MATCHSTATE_H_

#include "../Player.h"

#ifdef ARDUINO
#include <Arduino.h>
#else
#include <Stdint.h>
#endif

namespace Score {

class MatchState {
public:
	MatchState() {
		// reset the state
		reset();
	}
	MatchState(MatchState& toCopy) {
		// copy constructor
		current_server = toCopy.current_server;
		north_player = toCopy.north_player;
		// copy constructor
		for (uint8_t i = 0; i < players_count; ++i) {
			points[i] = toCopy.points[i];
			games[i] = toCopy.games[i];
		}
		for (uint8_t i = 0; i < 5; ++i) {
			for (uint8_t j = 0; j < players_count; ++j) {
				sets[i][j] = toCopy.sets[i][j];
			}
		}
		// and the members
		is_tiebreak = toCopy.is_tiebreak;
		tie_break_server = toCopy.tie_break_server;
	}
	virtual ~MatchState() {
		// destructor
	}

	Player getCurrentServer() { return current_server; }
	void setCurrentServer(Player new_server) { current_server = new_server; }
	Player getCurrentNorthPlayer() { return north_player; }
	void setCurrentNorthPlayer(Player new_north) { north_player = new_north; }

	uint8_t getSets(Player player) {
		Player otherPlayer = player_one;
		if (player == player_one) {
			otherPlayer = player_two;
		}
		uint8_t counter = 0;
		for (uint8_t i = 0; i < 5; ++i) {
			if (sets[i][player] > sets[i][otherPlayer]) {
				// this is a set to the player, increment the counter
				++counter;
			}
		}
		return counter;
	}
	uint8_t getSetsPlayed() {
		// return the number of sets played so far
		uint8_t counter = 0;
		for (uint8_t i = 0; i < 5; ++i) {
			if (sets[i][player_one] > 0 || sets[i][player_two] > 0) {
				// this set has some games in it, so it was played
				++counter;
			}
		}
		return counter;
	}
	uint8_t getSetPoints(uint8_t setIndex, Player player) {
		return sets[setIndex][player];
	}
	bool recordGameWon() {
		// record the current points in the game as a game won (in the sets as not using that right now)
		bool isGameRecorded = false;
		for (uint8_t i = 0; i < 5; ++i) {
			if (sets[i][player_one] == 0 && sets[i][player_two] == 0) {
				// this GAME has no POINTS, it is empty so lets record the POINTS in here as won by someone
				sets[i][player_one] = points[player_one];
				sets[i][player_two] = points[player_two];
				isGameRecorded = true;
				break;
			}
		}
		return isGameRecorded;
	}
	bool recordSetWon() {
		// record the current state of games as a won set
		bool isSetRecorded = false;
		for (uint8_t i = 0; i < 5; ++i) {
			if (sets[i][player_one] == 0 && sets[i][player_two] == 0) {
				// this set has no games, it is empty so lets record the games in here as won by someone
				sets[i][player_one] = games[player_one];
				sets[i][player_two] = games[player_two];
				isSetRecorded = true;
				break;
			}
		}
		return isSetRecorded;
	}
	void resetPoints() {
		// reset the counters to zero
		for (uint8_t i = 0; i < players_count; ++i) {
			points[i] = 0;
		}
	}

	void resetGames() {
		// reset the counters to zero
		for (uint8_t i = 0; i < players_count; ++i) {
			games[i] = 0;
		}

	}

	void resetSets() {
		// reset the counters to zero
		for (uint8_t i = 0; i < 5; ++i) {
			sets[i][player_one] = 0;
			sets[i][player_two] = 0;
		}
	}
	void reset() {
		// reset the counters to zero
		resetPoints();
		resetGames();
		resetSets();
		// and the members
		current_server = player_one;
		north_player = player_one;
		is_tiebreak = false;
		tie_break_server = player_one;
	}
	// members
	uint8_t points[players_count];
	uint8_t games[players_count];
	bool is_tiebreak;
	Player tie_break_server;
private:
	Player current_server;
	Player north_player;
	// making the sets private as a little more complicated than the games etc
	uint8_t sets[5][players_count];
};

} // end namespace

#endif /* MATCHSTATE_H_ */
