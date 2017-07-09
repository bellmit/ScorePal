#include "Arduino.h"
#include "../TennisScorer/Communications/InputSender.h"
#include "../TennisScorer/Pins.h"

Communications::InputSender sender;

//#define K_DEBUG
// use the buttons - can comment when testing without buttons
#define USE_BUTTONS
// Pins for the remote buttons go here (Really IMPORTANT that they remain as INTERRUPTS and INDEX into array)
#define K_PIN_REMOTE_SVRBTN 2
#define K_PIN_REMOTE_RVRBTN 3

#define K_DEBOUNCEDELAY 200UL	// the debounce time; increase if the output flickers
unsigned long lastTime[3];

//The setup function is called once at startup of the sketch
void setup() {
	// setup serial communication for debugging etc
	Serial.begin(9600);
	// Add your initialization code here
	sender.setup();

	// let's do the buttons for the remote right here in the main .ino file
#ifdef USE_BUTTONS
	// attach interrupts so we are as power efficient as possible
	pinMode(K_PIN_REMOTE_SVRBTN, INPUT_PULLUP);
	attachInterrupt(digitalPinToInterrupt(K_PIN_REMOTE_SVRBTN), serverButtonPress, RISING);
	// and for the other button (using all the interrupts here then)
	pinMode(K_PIN_REMOTE_RVRBTN, INPUT_PULLUP);
	attachInterrupt(digitalPinToInterrupt(K_PIN_REMOTE_RVRBTN), receiverButtonPress, RISING);
#endif
	lastTime[0] = millis();
	lastTime[1] = millis();
	lastTime[2] = millis();
}

bool debounceButton(uint8_t buttonPin) {
	bool isButtonPress = false;
#ifdef K_DEBUG
	Serial.print(F("."));
#endif
	if (millis() - lastTime[buttonPin-1] > K_DEBOUNCEDELAY) {
		// still low and time elapsed since last press
#ifdef K_DEBUG
		Serial.println(F("pressed"));
#endif
		isButtonPress = true;//digitalRead(buttonPin) == HIGH;
	}
	lastTime[buttonPin-1] = millis();
	return isButtonPress;
}

void serverButtonPress() {
	// called as the server button interrupt fires, debounce
	if (debounceButton(K_PIN_REMOTE_SVRBTN)) {
		// button was pressed, but was the other one too?
		if (digitalRead(K_PIN_REMOTE_RVRBTN) == LOW) {
			// set the last press on this so we don't process the button press on release
			lastTime[K_PIN_REMOTE_RVRBTN-1] = millis();
			// the other button was pressed in the last 200ms, this is undo
			sender.sendAction(3);
		}
		else {
			// send the message
			sender.sendAction(7);
		}
	}
}

void receiverButtonPress() {
	// called as the receiver button interrupt fires, debounce
	if (debounceButton(K_PIN_REMOTE_RVRBTN)) {
		// button was pressed, but was the other one too?
		if (digitalRead(K_PIN_REMOTE_SVRBTN) == LOW) {
			// set the last press on this so we don't process the button press on release
			lastTime[K_PIN_REMOTE_SVRBTN-1] = millis();
			// the other button was pressed in the last 200ms, this is undo
			sender.sendAction(3);
		}
		else {
			// send the message
			sender.sendAction(8);
		}
	}
}

// The loop function is called in an endless loop
void loop() {
	//Add your repeated code here
#ifdef K_DEBUG
	if (millis() - lastTime[0] > 2000) {
#ifdef K_DEBUG
		Serial.print(F("sending"));
#endif
		// send periodically as we are debugging
		sender.sendAction(2);
		// remember when this was sent
		lastTime[0] = millis();
#ifdef K_DEBUG
		Serial.println(F("."));
#endif
	}
#else
	// put the arduino to sleep
	/*set_sleep_mode (SLEEP_MODE_PWR_DOWN);
	noInterrupts ();          // make sure we don't get interrupted before we sleep
	sleep_enable ();          // enables the sleep bit in the mcucr register
	attachInterrupt (digitalPinToInterrupt (2), wake, LOW);  // wake up on low level on D2
	interrupts ();           // interrupts allowed now, next instruction WILL be executed
	sleep_cpu ();            // here the device is put to sleep*/
#endif
}
