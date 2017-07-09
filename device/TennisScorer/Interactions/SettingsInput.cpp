/*
 * ModeInput.cpp
 *
 *  Created on: 20 May 2017
 *      Author: douglasbrain
 */

#include "SettingsInput.h"
#include "../Pins.h"
#include <Arduino.h>

namespace Interactions {

//#define DEBUG_BRIGHTNESS
//#define DEBUG_MODE

#ifdef DEBUG_MODE
uint8_t last_mode;
unsigned long last_mode_print_time;
#endif
#ifdef DEBUG_BRIGHTNESS
unsigned long last_bright_print_time;
#endif

#ifndef K_PIN_MODEDIAL
	uint8_t mode_pins[K_NO_MODES] = {K_PIN_MODEONE, K_PIN_MODETWO, K_PIN_MODETHREE, K_PIN_MODEFOUR, K_PIN_MODEFIVE, K_PIN_MODESIX};
#endif

SettingsInput::SettingsInput() {
	// constructor
#ifdef DEBUG_MODE
	last_mode = -1;
#endif
	last_brightness = 5;
#ifdef K_PIN_MODEDIAL
	mode_range = 1;
#endif
	brightness_threshold = 0;
	// setup the array of brightness values
	uint16_t brightnessStep = (uint16_t) (K_MAXBRIGHTVAL - K_MINBRIGHTVAL) / (K_NO_BRIGHTNESS * 1.0);
	// the threshold for selecting this value is 1/6 of the step - 1/3 either way leaves a dead zone between values
	brightness_threshold = (uint8_t)(brightnessStep / 6.0);
	uint16_t brightnessValue = K_MINBRIGHTVAL;
	// setup the array of values that correspond to positions on the potentiometer
	for (uint16_t i = 0; i < K_NO_BRIGHTNESS; ++i) {
		brightness_values[i] = brightnessValue;
		brightnessValue += brightnessStep;
	}
}

SettingsInput::~SettingsInput() {
	// destructor
}

void SettingsInput::setup() {
#ifdef K_PIN_MODEDIAL
	// calculate the range of values on the potentiometer that will represent each actual value
	mode_range = (K_MAXMODEVAL - K_MINMODEVAL) / (K_NO_MODES * 1.0);
	pinMode(K_PIN_MODEDIAL, INPUT);
#else
	// initialise the pins that are to receive the signals from the selection dial
	for (uint8_t i = 0; i < K_NO_MODES; ++i) {
		pinMode(mode_pins[i], INPUT_PULLUP);
	}
#endif
	// initialise the input pin
	pinMode(K_PIN_BRIGHTDIAL, INPUT);
#ifdef DEBUG_MODE
	last_mode_print_time = millis();
#endif
#ifdef DEBUG_BRIGHTNESS
	last_bright_print_time = millis();
#endif
}

uint8_t SettingsInput::readMode() {
	uint8_t mode;
#ifdef K_PIN_MODEDIAL
	int value = 0;
	value = analogRead(K_PIN_MODEDIAL);
	mode = (uint8_t)(value / mode_range);
#ifdef DEBUG_MODE
	if (millis() - last_mode_print_time > 2000 || mode != last_mode) {
		Serial.print(F("Value of mode: "));
		Serial.print(value, 10);
		Serial.print(F(" is mode: "));
		Serial.println(mode, 10);
		last_mode_print_time = millis();
		last_mode = mode;
	}
#endif
#else
	// use the selection dial to determine the mode
	mode = K_NO_MODES - 1; // by default it is the last one so we don't have to use the last pin if we don't want to solder it

#ifdef DEBUG_MODE
	Serial.print(F("Reading pins: "));
#endif
	for (uint8_t i = 0; i < K_NO_MODES; ++i) {
#ifdef DEBUG_MODE
		Serial.print(i);
		Serial.print('=');
		Serial.print(digitalRead(mode_pins[i]), 10);
		Serial.print(',');
#endif
		if (digitalRead(mode_pins[i]) == LOW) {
			mode = i;
		}
	}
#ifdef DEBUG_MODE
	Serial.print(F(" leaving mode as:"));
	Serial.println(mode);
#endif
#endif
	return mode;
}

uint8_t SettingsInput::readBrightness() {
	int value = 0;
	value = analogRead(K_PIN_BRIGHTDIAL);
	// find the brightness value that is closest to this
	uint8_t selectedValue = last_brightness;
	uint8_t valueDifference = 255;
	for (uint8_t i = 0; i < K_NO_BRIGHTNESS; ++i) {
		uint8_t diff = abs(brightness_values[i] - value);
		if (diff < valueDifference) {
			// this is the closest we found at this time, use this valus
			selectedValue = i;
			valueDifference = diff;
		}
	}
#ifdef DEBUG_BRIGHTNESS
	if (millis() - last_bright_print_time > 2000 || selectedValue != last_brightness) {
		Serial.print(F("Value of brightness from ["));
		for (uint8_t i = 0; i < K_NO_BRIGHTNESS; ++i) {
			Serial.print(brightness_values[i], 10);
			Serial.print(F(","));
		}
		Serial.print(F("] is: "));
		Serial.print(value, 10);
		Serial.print(F(" is brightness: "));
		Serial.println(selectedValue, 10);
		last_bright_print_time = millis();
	}
#endif
	last_brightness = selectedValue;
	return selectedValue;
}

} /* namespace Display */
