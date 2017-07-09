/*
 * Beep.cpp
 *
 *  Created on: 20 May 2017
 *      Author: douglasbrain
 */

#include "Beep.h"
#include "pitches.h"
#include "../Pins.h"

namespace Interactions {

#define K_SILENCE_DURATION 	75
#define K_BEEP_FREQUENCY 	220

//#define K_PIEZO

#ifdef K_PIEZO
// notes in the melody:
int melody[] = {
  NOTE_C4, NOTE_G3, NOTE_G3, NOTE_A3, NOTE_G3, 0, NOTE_B3, NOTE_C4
};

// note durations: 4 = quarter note, 8 = eighth note, etc.:
int noteDurations[] = {
  4, 8, 8, 4, 4, 4, 4, 4
};
#endif

Beep::Beep() {
	// constructor
	beep_time = millis();
}
Beep::~Beep() {
	// destructor
}

void Beep::setup() {
	// put your setup code here, to run once:
	pinMode(K_PIN_SOUNDSWITCH, INPUT_PULLUP);
#ifdef K_PIEZO
	pinMode(K_PIN_SPEAKER, OUTPUT);
	// iterate over the notes of the melody:
	for (int thisNote = 0; thisNote < 8; thisNote++) {
		// to calculate the note duration, take one second
		// divided by the note type.
		//e.g. quarter note = 1000 / 4, eighth note = 1000/8, etc.
		int noteDuration = 1000 / noteDurations[thisNote];
		Serial.print(F("Playing:"));
		Serial.print(melody[thisNote], 10);
		Serial.print(F("for:"));
		Serial.print(noteDuration, 10);
		tone(8, melody[thisNote], noteDuration);

		// to distinguish the notes, set a minimum time between them.
		// the note's duration + 30% seems to work well:
		int pauseBetweenNotes = noteDuration * 1.30;
		Serial.print(F("Pausing:"));
		Serial.println(pauseBetweenNotes, 10);
		delay(pauseBetweenNotes);
		// stop the tone playing:
		noTone(8);
	}
#endif
	// ensure we are silent at the start
	cheep(0);
}

void Beep::cheep(int8_t cheepCount) {
	makeSound(cheepCount, 25);
}

void Beep::beep(int8_t beepCount) {
	makeSound(beepCount, 150);
}

void Beep::makeSound(int8_t count, unsigned long duration) {
	// remember if sounding or not
	if (count > 0 && digitalRead(K_PIN_SOUNDSWITCH) == LOW) {
		for (int8_t i = 0; i < count; ++i) {
#ifdef K_PIEZO
			// play the tone on the buzzer
			tone(K_PIN_SPEAKER, K_BEEP_FREQUENCY, duration);
			delay(duration + K_SILENCE_DURATION);
#else
			// for some reason the beeper I have is being rubbish, turn on and off to make a bad sound
			// for the duration that we require, then turn off again
			pinMode(K_PIN_SPEAKER, OUTPUT);
			delay(duration);
			pinMode(K_PIN_SPEAKER, INPUT);
			delay(K_SILENCE_DURATION);
#endif
		}
	}
	// no sound please
#ifdef K_PIEZO
	noTone(K_PIN_SPEAKER);
#else
	// turn off the crappy whining speaker thing
	pinMode(K_PIN_SPEAKER, INPUT);
#endif
}

} /* namespace Interactions */
