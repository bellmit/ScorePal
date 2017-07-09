/*
 * Scorer.cpp
 *
 *  Created on: 17 May 2017
 *      Author: douglasbrain
 */

#include "Scorer.h"
#ifdef ARDUINO
#include <Arduino.h>
#else
#define bitRead(value, bit) (((value) >> (bit)) & 0x01)
#define bitSet(value, bit) ((value) |= (1UL << (bit)))
#define bitClear(value, bit) ((value) &= ~(1UL << (bit)))
#define bitWrite(value, bit, bitvalue) (bitvalue ? bitSet(value, bit) : bitClear(value, bit))
#endif

namespace Score {

#define POINT_ADDITION 	 1
#define POINT_REMOVAL 	-1

Scorer::Scorer(uint8_t scoreMode /*= K_SCOREWIMBLEDON5*/) {
	// initialise the score here
	point_change = 0;
	score_mode = 0;
	is_show_messages = true;
#ifdef ARDUINO
	start_millis = millis();
#else
	start_millis = 0;
#endif
	// fix the data to ensure it is all clean and empty
	current_state.reset();

	// initialise the store of points to all be empty
	point_store_index = -1;
	for (int i = 0; i < K_POINTSTORESIZE; ++i) {
		// set it all to be empty data
		point_store[i] = 0;
	}
	// and the points total to be empty
	points_total[player_one] = 0;
	points_total[player_two] = 0;
	// set the scoring mode
	setScoreMode(scoreMode);
}


void Scorer::setScoreMode(uint8_t scoreMode) {
	if (score_mode == scoreMode) {
		// this is okay - no change so do nothing
	}
	else {
		// we need to change the score mode now to the new mode, replay the entire history of the match here
		score_mode = scoreMode;
		// don't show messages while we do this
		is_show_messages = false;
		// copy the historic stack of data to a local store before we reset it
		uint16_t noPoints = getNumberHistoricPoints();
		Player history [noPoints];
		for (uint16_t i = 0; i < noPoints; ++i) {
			history[i] = getHistoricPoint(i);
		}
		// before we clear the history, remember the start time which we want to retain
		unsigned long oldStart = start_millis;
		// now clear the score history
		resetMatch();
		// and put the time back in
		start_millis = oldStart;
		// now put the score back in now the mode is different
		for (uint16_t i = 0; i < noPoints; ++i) {
			addPoint(history[i]);
		}
		is_show_messages = true;
	}
}

Scorer::~Scorer() {
	// destructor
}

bool Scorer::getIsTrackingServer() {
	switch(score_mode) {
	case K_SCOREWIMBLEDON3 :
	case K_SCOREWIMBLEDON5 :
	case K_SCOREFAST4 :
	case K_SCOREPOINTS :
		return true;
	case K_SCOREBADMINTON5 :
	case K_SCOREBADMINTON3 :
	default :
		return false;
	}
}

bool Scorer::isWimbledonPoints() {
	switch(score_mode) {
	case K_SCOREBADMINTON3 :
	case K_SCOREBADMINTON5 :
	case K_SCOREPOINTS :
		return false;
	case K_SCOREWIMBLEDON3 :
	case K_SCOREWIMBLEDON5 :
	case K_SCOREFAST4 :
		return true;
	default :
		return false;
	}
}

bool Scorer::isShowSets() {
	// only show sets if in ITF scoring mode
	switch(score_mode) {
	case K_SCOREBADMINTON3 :
	case K_SCOREBADMINTON5 :
	case K_SCOREPOINTS :
	case K_SCOREFAST4 :
		return false;
	case K_SCOREWIMBLEDON3 :
	case K_SCOREWIMBLEDON5 :
		return true;
	default :
		return false;
	}
}

bool Scorer::isMatchOver() {
	switch(score_mode) {
	case K_SCOREBADMINTON3 :
		return badminton.isMatchOver(3, current_state);
		break;
	case K_SCOREBADMINTON5 :
		return badminton.isMatchOver(5, current_state);
		break;
	case K_SCOREPOINTS :
		// never over
		return false;
	case K_SCOREWIMBLEDON3 :
		return wimbledon.isMatchOver(3, current_state);
	case K_SCOREWIMBLEDON5 :
		return wimbledon.isMatchOver(5, current_state);
	case K_SCOREFAST4 :
		return fast4.isMatchOver(current_state);
	default:
		return false;
	}
}

Player Scorer::getMatchWinner() {
	switch(score_mode) {
	case K_SCOREBADMINTON3 :
	case K_SCOREBADMINTON5 :
	case K_SCOREFAST4 :
		// return the player with the most games - one will have exceeded the required amount
		if (current_state.games[player_one] > current_state.games[player_two]) {
			return player_one;
		}
		else {
			return player_two;
		}
	case K_SCOREPOINTS :
		// return the player with the most points - one will have exceeded the required amount
		if (current_state.points[player_one] > current_state.points[player_two]) {
			return player_one;
		}
		else {
			return player_two;
		}
	case K_SCOREWIMBLEDON3 :
	case K_SCOREWIMBLEDON5 :
		// return the player with the most sets - one will have exceeded the required amount
		if (current_state.getSets(player_one) > current_state.getSets(player_two)) {
			return player_one;
		}
		else {
			return player_two;
		}
	default:
		return player_one;
	}
}

void Scorer::addGame(Player player) {
	// add the game to our score
	// just add to the game counter
	++current_state.games[player];
	// game over - reset our Points for the next game
	current_state.resetPoints();
}

void Scorer::swapServers() {
	// swap over the servers or whatever reason - usually a new game
	current_state.setCurrentServer(getOtherPlayer(getCurrentServer()));
}

void Scorer::swapEnds() {
	// swap over the ends - usually a new second game

	/*The players shall change ends at the end of the first, third and every subsequent odd
		game of each set. The players shall also change ends at the end of each set unless the
		total number of games in that set is even, in which case the players change ends at the
		end of the first game of the next set.
		During a tie-break game, players shall change ends after every six points*/
	current_state.setCurrentNorthPlayer(getOtherPlayer(getCurrentNorthPlayer()));
}

Player Scorer::getCurrentServer() {
	return current_state.getCurrentServer();
}

Player Scorer::getCurrentNorthPlayer() {
	return current_state.getCurrentNorthPlayer();
}

void Scorer::addPoint(Player player, uint8_t noPoints) {
	for (uint8_t i = 0; i < noPoints; ++i) {
		addPoint(player);
	}
}

void Scorer::addPoint(Player player) {
	// a Point is added, we are dirty
	point_change = POINT_ADDITION;
	// before we do anything, remember the state as it is so we can go back to it later
	previous_states.push(current_state);
	// is the game won?
	switch (score_mode) {
	case K_SCOREBADMINTON3 :
		// add the point for this player they just won
		badminton.addPoint(player, 3, current_state, *this);
		break;
	case K_SCOREBADMINTON5 :
		// add the point for this player they just won
		badminton.addPoint(player, 5, current_state, *this);
		break;
	case K_SCOREPOINTS :
		// add the point for this player they just won
		points.addPoint(player, current_state, *this);
		break;
	case K_SCOREWIMBLEDON3 :
		// wimbledon does its own thing, do it here
		wimbledon.addPoint(player, 3, current_state, *this);
		break;
	case K_SCOREWIMBLEDON5 :
		// wimbledon does its own thing, do it here
		wimbledon.addPoint(player, 5, current_state, *this);
		break;
	case K_SCOREFAST4 :
		// add the point for fast 4
		fast4.addPoint(player, current_state, *this);
	}
	// store this in the total for this player
	++points_total[player];
	// and we can remember this point
	if (++point_store_index < K_POINTSTORESIZE * 16) {
		// this is okay, we can store this index in the data, store it then, first get the correct bin
		// as we store a series of unsigned 16 bit integers, each index in the array stores 16 points
		// get the correct bin then
		int binIndex = (int)(point_store_index / 16);
		// and the index of the value in this bin
		int valIndex = point_store_index - (binIndex * 16);
		// just quickly test this to be totally safe and secure
		if (binIndex >= 0 && binIndex < K_POINTSTORESIZE && valIndex >=0 && valIndex < 16) {
			// this is all good - store this by writing the bit in the correct place
			bitWrite(point_store[binIndex], valIndex, player);
		}
		else {
			// failed, rewind the counter that we are storing back to what was valid
			--point_store_index;
		}
	}
}

bool Scorer::removeLastPoint() {
	// a Point is removed, we are dirty
	point_change = POINT_REMOVAL;
	if (point_store_index >= 0 && previous_states.getNoItems() > 0) {
		// this point is stored in the history - remove the point from the total
		Player player = getHistoricPoint(point_store_index);
		--points_total[player];
		// and pull the store index back one to remove from the history
		--point_store_index;
		// there is a state to revert to, so do so now...
		current_state = previous_states.pop(current_state);
		// and return that this was okay and worked
		return true;
	}
	else {
		// none left to pop from the stack of history, sorry
		return false;
	}
}

void Scorer::recordGameWon() {
	// when the user wins a game the game is incremented, but if we are not using the history of
	// sets for anything then we can record the game history here instead
	if (false == current_state.recordGameWon()) {
		//assert("Failed to store the state of the set that was just won");
	}
}

void Scorer::recordSetWon() {
	// sets we can just add - someone might have won but that is cool, we handle that somewhere else
	if (false == current_state.recordSetWon()) {
		//assert("Failed to store the state of the set that was just won");
	}
	// we need to swap ends every new set (unless the set has an even number of games won)
	uint8_t gameSum = current_state.games[player_one] + current_state.games[player_two];
	if (gameSum % 2 != 0) {
		// there were an odd number of games in this set - swap ends for the new set
		swapEnds();
	}
	// and reset the games now the set has been won
	current_state.resetGames();
}

uint8_t Scorer::getPoints(Player player) {
	return current_state.points[player];
}

uint8_t Scorer::getGames(Player player) {
	// the caller wants the actual current games
	return current_state.games[player];
}

uint8_t Scorer::getSetGames(Player player, uint8_t set) {
	// they want the games for the set that was completed already
	return current_state.getSetPoints(set, player);
}

uint8_t Scorer::getSets(Player player) {
	return current_state.getSets(player);
}

void Scorer::resetMatch() {
	// reset the match data stored in this base class
	point_change = 0;
	point_store_index = -1;
	// and the points total to be empty
	points_total[player_one] = 0;
	points_total[player_two] = 0;
	// reset the state
	current_state.reset();
#ifdef ARDUINO
	start_millis = millis();
#else
	start_millis = 0;
#endif
	// set the dirty flag, no Point addition so doesn't show any messages
	setIsDirty(false);
}

Player Scorer::getHistoricPoint(uint16_t pointIndex) {
	// return the historic point requested
	Player player = player_one;
	// get the bin index this value is inside
	int binIndex = (int)(pointIndex / 16);
	// and the index of the value in this bin
	int valIndex = pointIndex - (binIndex * 16);
	// just quickly test this to be totally safe and secure
	if (binIndex >= 0 && binIndex < K_POINTSTORESIZE && valIndex >=0 && valIndex < 16) {
		// this is all good - extract the bit at this location
		player = bitRead(point_store[binIndex], valIndex);
	}
	// else failed, by default will return player one but nothing we can do about that
	return player;
}

uint16_t Scorer::getNumberHistoricPoints() {
	// return the number of points we are currently storing, can be -1 but we are adding 1 to return the size
	return (uint16_t) (point_store_index + 1);
}

uint16_t Scorer::getTotalPoints(Player player) {
	return points_total[player];
}

void Scorer::setIsDirty(bool isAddition) {
	if (isAddition) {
		point_change = POINT_ADDITION;
	}
	else {
		point_change = POINT_REMOVAL;
	}
}

int8_t Scorer::getLastPointChange() {
	int8_t returnVal = point_change;
	point_change = 0;
	return returnVal;
}

} /* namespace Score */
