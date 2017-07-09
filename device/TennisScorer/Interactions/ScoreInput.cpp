/*
 * ScoreInput.cpp
 *
 *  Created on: 22 Apr 2017
 *      Author: douglasbrain
 */

#include "ScoreInput.h"
#include "../Communications/PhoneConnection.h"
#include <Arduino.h>
#include "../Player.h"
#include "../Pins.h"

namespace Interactions {

#ifdef K_PIN_UNDBTN
#define K_NUMBERBUTTONS 4		// the number of buttons to process (undo makes 4)
int8_t buttonPins[K_NUMBERBUTTONS] = {K_PIN_SVRBTN, K_PIN_RVRBTN, K_PIN_RSTBTN, K_PIN_UNDBTN};	// the input pins for the buttons
#else
#define K_NUMBERBUTTONS 3		// the number of buttons to process (not having a specific UNDO button, both for undo)
int8_t buttonPins[K_NUMBERBUTTONS] = {K_PIN_SVRBTN, K_PIN_RVRBTN, K_PIN_RSTBTN};//, K_PIN_UNDBTN};	// the input pins for the buttons
#endif
#define K_DEBOUNCEDELAY 50UL	// the debounce time; increase if the output flickers

int8_t buttonStates[K_NUMBERBUTTONS];           // the current reading from the input pins
int8_t lastButtonStates[K_NUMBERBUTTONS];       // the previous reading from the input pins

// the following variables are unsigned long's because the time, measured in miliseconds,
// will quickly become a bigger number than can be stored in an int8_t.
unsigned long lastDebounceTimes[K_NUMBERBUTTONS];  	// the last time the pins were toggled
uint8_t undoButtonReleases;

ScoreInput::ScoreInput() {
	// initialise
	scorer = NULL;
	undoButtonReleases = 0;
	int8_t i;
	for (i = 0; i < K_NUMBERBUTTONS; ++i) {
		buttonStates[i] = HIGH;
		lastButtonStates[i] = LOW;
		lastDebounceTimes[i] = millis();
	}
}

ScoreInput::~ScoreInput() {
	// destructor
}

void ScoreInput::setup(Score::Scorer* scorerInUse) {
	// remember the scorer
	scorer = scorerInUse;
	// setup the button pins to use their int8_ternal resisters
	for (int8_t i = 0; i < K_NUMBERBUTTONS; ++i) {
		pinMode(buttonPins[i], INPUT_PULLUP);
	}
	// and the receiver
	receiver.setup();
}

bool ScoreInput::process(Beep& beeper, Communications::PhoneConnection& phone) {
	// process the buttons on the device
	bool isActionProcessed = false;
	int8_t buttonChanges [K_NUMBERBUTTONS];
	// let's do away with the debouncing as are not in this loop very often any more
	for (int8_t i = 0; i < K_NUMBERBUTTONS; ++i) {
		int8_t reading = digitalRead(buttonPins[i]);
		// check to see if you just pressed the button
		// (i.e. the input went from LOW to HIGH),  and you've waited
		// long enough since the last press to ignore any noise:

		// If the switch changed, due to noise or pressing:
		if (reading != lastButtonStates[i]) {
			// reset the debouncing timer
			lastDebounceTimes[i] = millis();
		}
		// by default this was not pressed
		buttonChanges[i] = 0;
		if ((millis() - lastDebounceTimes[i]) > K_DEBOUNCEDELAY) {
			// whatever the reading is at, it's been there for longer
			// than the debounce delay, so take it as the actual current state:

			// if the button state has changed:
			if (reading != buttonStates[i]) {
				buttonStates[i] = reading;
#ifdef K_DEBUG
				Serial.print(F("Button "));
				Serial.print(i, 10);
				Serial.print(F(" changed to "));
				Serial.println(reading, 10);
#endif
				// count the buttons that are pressed at this time...
				if (buttonStates[i] == LOW) {
					buttonChanges[i] = -1;
				}
				else {
					buttonChanges[i] = 1;
				}
			}
		}
		// save the reading.  Next time through the loop,
		// it'll be the lastButtonState:
		lastButtonStates[i] = reading;
	}
	if (buttonChanges[0] == -1 || buttonChanges[1] == -1) {
		// some button was just depressed, if the states of both are LOW then both pressed, this is undo
		if (buttonStates[0] == LOW && buttonStates[1] == LOW) {
			// just pressed a button while the other was already down, undo the last point
			isActionProcessed = processActionCode(3, beeper);
			// expecting two releases now...
			undoButtonReleases = 2;
		}
	}
	if (buttonChanges[0] == 1) {
		// the first button was just released, if the other is up already this is a press
		if (undoButtonReleases > 0) {
			// this is a release we were expecting from an undo action
			--undoButtonReleases;
		}
		else {
			// just released the first button, this is an action
			isActionProcessed = processActionCode(4, beeper);
		}
	}
	if (buttonChanges[1] == 1) {
		// the second button was just released, if the other is up already this is a press
		if (undoButtonReleases > 0) {
			// this is a release we were expecting from an undo action
			--undoButtonReleases;
		}
		else {
			// just released the second button, this is an action
			isActionProcessed = processActionCode(5, beeper);
		}
	}
	if (buttonChanges[2] == 1) {
		// just released the reset button, reset the match
		isActionProcessed = processActionCode(9, beeper);
	}
	// process any incomming messages from the remote device
	int8_t action = receiver.receiveNewAction();
	if (action > 0) {
		// this action is okay - process it
		if (processActionCode(action, beeper)) {
			// somethis was processed from the remote
			isActionProcessed = true;
		}
	}
	// and from the phone too (bluetooth)
	action = phone.receiveNewAction(scorer);
	if (action > 0) {
		// this action is okay - process it
		if (processActionCode(action, beeper)) {
			// somethis was processed from the remote
			isActionProcessed = true;
		}
	}
	return isActionProcessed;
}

bool ScoreInput::processActionCode(int8_t actionCode, Beep& beeper) {
	bool isCodeProcessed = false;
	Player pointWinner;
	switch(actionCode) {
	case 0:
		// no action
		break;
	case 1:
		// action code 1 from the remote, increment the server score
		pointWinner = scorer->getCurrentServer();
		scorer->addPoint(pointWinner);
		beeper.cheep(pointWinner == player_one ? 1 : 2);
		isCodeProcessed = true;
		break;
	case 2:
		// action code 2 from the remote, increment the receiver score
		pointWinner = scorer->getOtherPlayer(scorer->getCurrentServer());
		scorer->addPoint(pointWinner);
		beeper.cheep(pointWinner == player_one ? 1 : 2);
		isCodeProcessed = true;
		break;
	case 3:
		// action code 3 from the remote, undo the last score
		if (scorer->removeLastPoint()) {
			beeper.beep(1);
			isCodeProcessed = true;
		}
		break;
	case 4:
		// button 0 or action code 4 from the remote, increment the left (North) scorer
		pointWinner = scorer->getCurrentNorthPlayer();
		scorer->addPoint(pointWinner);
		beeper.cheep(pointWinner == player_one ? 1 : 2);
		isCodeProcessed = true;
		break;
	case 5:
		// button 1 or action code 5 from the remote, increment the right (South) scorer
		pointWinner = scorer->getOtherPlayer(scorer->getCurrentNorthPlayer());
		scorer->addPoint(pointWinner);
		beeper.cheep(pointWinner == player_one ? 1 : 2);
		isCodeProcessed = true;
		break;
	case 6:
		// button 2 or action code 6 from the remote, undo the last score
		if (scorer->removeLastPoint()) {
			beeper.beep(1);
			isCodeProcessed = true;
		}
		break;
	case 7:
		// action code 7 from a phone, add a point for player_one
		scorer->addPoint(player_one);
		beeper.cheep(1);
		isCodeProcessed = true;
		break;
	case 8:
		// action code 8 from a phone, add a point for player_two
		scorer->addPoint(player_two);
		beeper.cheep(2);
		isCodeProcessed = true;
		break;
	case 9:
		// this is the reset game code (from the phone)
		scorer->resetMatch();
		beeper.beep(3);
		beeper.cheep(2);
		isCodeProcessed = true;
		break;
	default:
		// no action
		break;
	}
	return isCodeProcessed;
}

} /* namespace Interactions */
