/*
 * StringMessage.cpp
 *
 *  Created on: 5 May 2017
 *      Author: douglasbrain
 */

#include "StringMessage.h"
#include <Arduino.h>

namespace Display {

StringMessage::StringMessage(const char* contentF, const char* contentB,
							 int startX, int startY,
							 int movementX, int movementY,
							 float movementTime,
							 float speedSec,
							 float existTime,
							 bool isCancelable) {
	// setup the members
	setContent(contentF, contentB, startX, startY, movementX, movementY, movementTime, speedSec, existTime, isCancelable);
}

StringMessage::~StringMessage() {
	// destructor
}

void StringMessage::setContent(const char* contentF, const char* contentB,
							 int startX, int startY,
							 int movementX, int movementY,
							 float movementTime,
							 float speedSec,
							 float existTime,
							 bool isCancelable) {
	// setup the members
	setContentStrings(contentF, contentB);

	last_position[0] = 0;
	last_position[1] = 0;
	movement_vector[0] = movementX;
	movement_vector[1] = movementY;
	speed_seconds = speedSec;
	time_to_exist = existTime;
	time_to_move = movementTime;
	is_cancelable = isCancelable;
	// set the starting position in the sneakily large array (as floats please)
	current_position[0] = 0.0;
	current_position[1] = 0.0;
	current_position[2] = startX * 1.0;
	current_position[3] = startY * 1.0;
	// reset the positional data
	resetPosition();
}

void StringMessage::resetPosition() {
	// reset the time existed to be zero
	time_existed = 0.0;
	// setup our start position, as set in the slightly large array
	current_position[0] = current_position[2];
	current_position[1] = current_position[3];
	// remember out last position, make different to the current to start
	last_position[0] = -current_position[0];
	last_position[1] = -current_position[1];
	if (last_position[0] == 0 && last_position[1] == 0) {
		// making the current pos negative does not work when zero - set to -1 instead
		last_position[0] = -1;
		last_position[1] = -1;
	}
}

void StringMessage::clear(bool force) {
	// send this message to the end position now
	if (is_cancelable || force) {
		time_to_exist = 0;
	}
}

bool StringMessage::isReset() {
	// this is reset if our time is zero and our position isn't changed
	return time_existed == 0.0f &&
			current_position[0] == current_position[2] &&
			current_position[1] == current_position[3];
}

bool StringMessage::isLooping() {
	// we are looping if the time_to_exist is negative
	return time_to_exist < 0.0f;
}

void StringMessage::setContentStrings(const char* contentF, const char* contentB) {
	if (contentF == NULL) {
		content_messageF[0] = 0;
	}
	else {
		strncpy(content_messageF, contentF, K_CONTENTMESSAGESIZE);
	}
	if (contentB == NULL) {
		content_messageB[0] = 0;
	}
	else {
		strncpy(content_messageB, contentB, K_CONTENTMESSAGESIZE);
	}
}

bool StringMessage::update(float timeElapsedSec) {
	// update our location based on the movement vector times the speed at which to move
	if (time_existed < time_to_move) {
		// need to move a little more
		if (timeElapsedSec + time_existed > time_to_move) {
			// this is too much time to move, curtail this
			timeElapsedSec = time_to_move - time_existed;
		}
		// now move the correct amount
		float amtMoved = speed_seconds * timeElapsedSec;
		current_position[0] += movement_vector[0] * amtMoved;
		current_position[1] += movement_vector[1] * amtMoved;
	}
	else if (isLooping()) {
		// we have finished moving, which is fine we can just let the time_to_exist time to elapse
		// before we kill this message. But the negative time_to_exist means we are infinite, so reset
		resetPosition();
		// and the time elapsed we are about to add to this, so we can start properly at zero station
		timeElapsedSec = 0.0;
	}

	// update our time existed counter
	time_existed += timeElapsedSec;
	// did we move any proper (integer) amount?
	bool isMoved = last_position[0] != getXPosition() || last_position[1] != getYPosition();
	// remember the last position
	last_position[0] = getXPosition();
	last_position[1] = getYPosition();
	// return if this requires a redraw
	return isMoved;
}

int StringMessage::getXPosition() {
	return (int) current_position[0];
}

int StringMessage::getYPosition() {
	return (int) current_position[1];
}

bool StringMessage::isCompleted() {
	if (isLooping()) {
		// this is an infinite lasting message, never completed
		return false;
	}
	else {
		return time_to_exist < time_existed;
	}
}

const char* StringMessage::getContentF() {
	return content_messageF;
}

const char* StringMessage::getContentB() {
	return content_messageB;
}

} /* namespace Display */
