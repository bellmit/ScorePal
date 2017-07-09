/*
 * LedPlayerDisplay.cpp
 *
 *  Created on: 24 May 2017
 *      Author: douglasbrain
 */

#include "LedPlayerDisplay.h"
#include "../Pins.h"

#include <Arduino.h>

namespace Display {

LedPlayerDisplay::LedPlayerDisplay() {
	// constructor
	is_player_two_front = false;
	ledValue = 796;
}

LedPlayerDisplay::~LedPlayerDisplay() {
	// destructor
}

void LedPlayerDisplay::initialise() {
	// initialise the LEDs we want to turn on and off
	pinMode(K_PIN_PL1LED, OUTPUT);
	//pinMode(33, OUTPUT); // for the plastic prototype case
	pinMode(K_PIN_PL2LEDF, OUTPUT);
	pinMode(K_PIN_PL2LEDB, OUTPUT);
}

void LedPlayerDisplay::setDisplayBrightness(uint8_t brightness) {
	// set the brightness on the display here
	ledValue = 1024 / 16 * brightness;
	showDisplay();
}

void LedPlayerDisplay::setDisplayPlayer(bool isPlayerTwoFront) {
	// set the player display format
	is_player_two_front = isPlayerTwoFront;
	showDisplay();
}

void LedPlayerDisplay::showDisplay() {
	if (is_player_two_front) {
		// player two is on the front
		digitalWrite(K_PIN_PL2LEDF, ledValue);
		digitalWrite(K_PIN_PL2LEDB, 0);
	}
	else {
		// player two is on the back
		digitalWrite(K_PIN_PL2LEDB, ledValue);
		digitalWrite(K_PIN_PL2LEDF, 0);
	}
	// player one is always shown
	digitalWrite(K_PIN_PL1LED, ledValue);
	//digitalWrite(33, ledValue); // for the plastic prototype case
}

} /* namespace Display */
