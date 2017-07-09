/*
 * InputReceiver.cpp
 *
 *  Created on: 30 Apr 2017
 *      Author: douglasbrain
 */

#include "InputReceiver.h"
#include <Arduino.h>
#include <assert.h>
#include "MacAddress.h"
#include "../Pins.h"

#include "../Lib/SPI.h"
#include "../Lib/nRF24L01.h"
#include "../Lib/RF24.h"

//#define DEBUG

namespace Communications {

RF24 rxRadio(K_PIN_RADIORXCE, K_PIN_RADIORXCS);

const uint64_t pipe = 0xE8E8F0F0E1LL;

InputReceiver::InputReceiver() {
	last_received_index = -1;
	// get the MAC address of this device to use as the unique portion of the code
	MacAddress address;
	uint8_t addressBuffer[6];
	address.getAddress(addressBuffer);
	code.setUniqueCode(addressBuffer);
}

InputReceiver::~InputReceiver() {
	// destructor
}

void InputReceiver::setup() {
	rxRadio.begin();
	// disabling the auto ACK as some copies don't have this working
	rxRadio.setAutoAck(false);

	// Set the PA Level low to prevent power supply related issues since this is a
	// getting_started sketch, and the likelihood of close proximity of the devices. RF24_PA_MAX is default.
	//radio.setPALevel(RF24_PA_MIN);
	rxRadio.setPALevel(RF24_PA_MAX);

	// set the data rate lower to get better range
	rxRadio.setDataRate(RF24_250KBPS);
	// and a high radio channel to avoid people's wifi routers
	rxRadio.setChannel(108); //2.508 Ghz

	rxRadio.openReadingPipe(1,pipe);
	rxRadio.startListening();
}

int8_t InputReceiver::receiveNewAction() {
	int8_t action = -1;
	while (rxRadio.available()) {
		// read in the data we are expecting (11 values "{abbccdd00}")
		rxRadio.read(msg, K_SENDING_CODE_LENGTH);
#ifdef DEBUG
		for (uint8_t i = 0; i < K_SENDING_CODE_LENGTH; ++i) {
			Serial.print(msg[i], 10);
			Serial.print(F(":"));
		}
		Serial.println();
#endif
		uint8_t index = last_received_index;
		uint8_t receivedAction = 0;
		if (code.extractActionCode(msg, receivedAction, index)) {
#ifdef DEBUG
			Serial.println(F("Is for us!"));
#endif
			// this code is for us - the unique part matches, is it new too?
			if (index != last_received_index) {
				// this is new data as well, return this to the user
				action = receivedAction;
				// remember the index of the data received so we don't keep receiving duplicates
				last_received_index = index;
			}
		}
#ifdef DEBUG
		else {
			// this is not for us
			Serial.print(F("Expecting MAC: "));
			Communications::MacAddress mac;
			mac.printToSerial();
			Serial.print(F("  in base 10 is: "));
			uint8_t buffer[6];
			mac.getAddress(buffer);
			for (uint8_t i = 0; i < 6; ++i) {
				Serial.print(buffer[i], 10);
				Serial.print(F(":"));
			}
			Serial.println();
		}
#endif
	}
	// return the data action received (will be -1 if none new or none for us)
	return action;
}
} /* namespace Communications */
