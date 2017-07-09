/*
 * InputSender.cpp
 *
 *  Created on: 30 Apr 2017
 *      Author: douglasbrain
 */

#include "InputSender.h"
#include <Arduino.h>
#include <assert.h>
#include "../Pins.h"

#include "../Lib/SPI.h"
#include "../Lib/nRF24L01.h"
#include "../Lib/RF24.h"

namespace Communications {

#define K_NORADIOSENDRETRIES	8

#ifdef K_PIN_RADIOTXCE
RF24 txRadio(K_PIN_RADIOTXCE, K_PIN_RADIOTXCS);
#endif

const uint64_t pipe = 0xE8E8F0F0E1LL;

InputSender::InputSender() {
	// set the MAC address of the device to which to send this data
	// soldered prototype
	//uint8_t addressBuffer[] = {4, 233, 229, 4, 195, 31};
	// breadboard
	//uint8_t addressBuffer[] = {4, 233, 229, 4, 194, 216};
	// testing board - with printed PCB
	//uint8_t addressBuffer[] = {4, 233, 229, 4, 205, 55};
	// prototype 2-sided case
	uint8_t addressBuffer[] = {4, 233, 229, 4, 204, 249};
	code.setUniqueCode(addressBuffer);
	isPowerDown = false;
}

InputSender::~InputSender() {
	// destructor
}

void InputSender::setup() {
	// initialise the data in this class, nothing for now as we will wait for something to send before doing anything
	initialiseRadio();
	// but put it right to sleep straight away
	sleepRadio();
}

void InputSender::initialiseRadio() {
#ifdef K_PIN_RADIOTXCE
	// powerup the radio and begin it doing it's thing
	if (isPowerDown) {
		txRadio.powerUp();
	}
	txRadio.begin();
	// disabling the auto ACK as some copies don't have this working
	txRadio.setAutoAck(false);
	// Set the PA Level low to prevent power supply related issues since this is a
	// getting_started sketch, and the likelihood of close proximity of the devices. RF24_PA_MAX is default.
	//radio.setPALevel(RF24_PA_MIN);
	//txRadio.setPALevel(RF24_PA_MAX);
	txRadio.setPALevel(RF24_PA_HIGH);

	// set the data rate lower to get better range
	txRadio.setDataRate(RF24_250KBPS);
	// and a high radio channel to avoid people's wifi routers
	txRadio.setChannel(108); //2.508 Ghz

	// and open the pipe
	txRadio.openWritingPipe(pipe);
#endif
}

void InputSender::sleepRadio() {
#ifdef K_PIN_RADIOTXCE
	// put the radio in power down mode to save battery when we don't want it
	txRadio.powerDown();
	isPowerDown = true;
#endif

}

void InputSender::sendAction(uint8_t actionCode) {
	// initialise the radio
	initialiseRadio();
	// send the data out on the device setup in the setup function
	uint8_t* sendingCode = code.createSendingCode(actionCode);
	// write this to the radio
#ifdef K_PIN_RADIOTXCE
	// just to be sure that the receiver gets it, let's send it in a loop a little
	for (uint8_t i = 0; i < K_NORADIOSENDRETRIES; ++i) {
		txRadio.write(sendingCode, K_SENDING_CODE_LENGTH);
	}
	for (int i = 0; i < K_SENDING_CODE_LENGTH; ++i) {
		Serial.print(sendingCode[i]);
		Serial.print(':');
	}
	Serial.println();
#else
	Serial.println(F("Cannot send data as radio sending disabled in pins.h"));
#endif
	// and let the radio sleep
	sleepRadio();
}

} /* namespace Communications */
