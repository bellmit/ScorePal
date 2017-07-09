/*
 * PhoneConnection.cpp
 *
 *  Created on: 15 May 2017
 *      Author: douglasbrain
 */

#include "PhoneConnection.h"
#include <Arduino.h>
#include "../Player.h"
#include "../Pins.h"

namespace Communications {

//#define K_DEBUG
#define K_SENDTIMEPERIOD 5000

#define K_HISTORYRADIX 32

#if defined(__arm__) && defined(TEENSYDUINO)
#define K_ENABLE_BLUETOOTH
#endif

PhoneConnection::PhoneConnection() {
	// constructor
	ch = 0;
	rxCommand = '0';
	rxBuffer[0] = 0;
	rxIndex = 0;
	last_code_received = -1;
	code_to_send = 0;
	last_send_time = millis();
}

PhoneConnection::~PhoneConnection() {
	// destructor
}

void PhoneConnection::initialise() {
	// setup the BT module with the correct information we want it to have
	/*pinMode(K_PIN_BTKEY, OUTPUT);  // this pin will pull the HC-05 pin 34 (key pin) HIGH to switch module to AT mode
	digitalWrite(K_PIN_BTKEY, HIGH);
	Serial1.begin(38400);  // HC-05 default speed in AT command more
	// name it properly
	Serial1.print(F("AT+NAME=SCR-PAL\r\n"));
	Serial1.print(F("AT+PSWD=2987\r\n"));
	// and close this session
	Serial1.end();
	digitalWrite(K_PIN_BTKEY, LOW);*/
#ifdef K_ENABLE_BLUETOOTH
	// setup our bluetooth serial connection on serial port number 1 for receiving data now
	Serial1.flush ();
	Serial1.begin (9600);
#endif
}

int8_t PhoneConnection::receiveNewAction(Score::Scorer* scorer) {
	// process any input we received
	int8_t actionReceived = 0;
#ifdef K_ENABLE_BLUETOOTH
	// try and receive a little data
	while (Serial1.available() && rxIndex < K_RX_BUFFER_SIZE) {
		// read in and process all the data on the serial BT buffer line we can handle
		ch = Serial1.read();
#ifdef K_DEBUG
		Serial.print(ch);
#endif
		// read in a value, add to the buffer
		if (ch == '{') {
			// starting, set the rx counter to the start of the buffer
			rxIndex = 0;
		}
		else if (ch == '}') {
			// ended, process this command
			if (rxCommand == 'a') {
				// this is an action, process this
				actionReceived = atoi(rxBuffer);
#ifdef K_DEBUG
				Serial.print(F("Received Action:"));
				Serial.println(actionReceived, 10);
#endif
			}
			else if (rxCommand == 'r') {
				// this is a response to a sent message, remember this
				last_code_received = atoi(rxBuffer);
#ifdef K_DEBUG
				Serial.print(F("Received R Code:"));
				Serial.println(last_code_received, 10);
#endif
			}
			else if (rxCommand == 'u' && NULL != scorer) {
				// this is a whole bunch of data to reset what we currently have, reset the data
				// on the scorer with this data
				receiveGameStatus(scorer);

			}
			// reset for any more data
			rxCommand = '0';
			// reset the buffer to start again, wipe out the processed data
			rxIndex = 0;
			rxBuffer[0] = '\0';
		}
		else {
			if (rxCommand == '0') {
				// the first char is the command, set this
				rxCommand = ch;
			}
			else {
				// put the value after the command into the buffer
				rxBuffer[rxIndex++] = ch;
				// and terminate it here for the time being
				rxBuffer[rxIndex] = '\0';
			}
		}
	}
	if (rxIndex >= K_RX_BUFFER_SIZE) {
		// there is no more room left in the buffer (maxed out with room for the terminating char)
		// wipe out all that data to ignore it
		rxIndex = 0;
		rxCommand = '\0';
		rxBuffer[0] = '\0';
	}
#endif
	return actionReceived;
}

void PhoneConnection::incrementSendingCode() {
	// the score has changed, update our sending code
	if (++code_to_send > 32250) {
		// loop this back to zero
		code_to_send = 0;
	}
}

bool PhoneConnection::isPhoneUpToDate() {
	// does our score match one received?
	return code_to_send == last_code_received;
}

bool PhoneConnection::sendGameStatus(Score::Scorer& scorer, bool isForceSend) {
	// limit the amount of times we send data to the phone
#ifdef K_ENABLE_BLUETOOTH
	unsigned long time = millis();
	// if the current time is less than the last send time it has overrun, send now whatever
	if (isForceSend || time < last_send_time || time - last_send_time > K_SENDTIMEPERIOD) {
		// only send every five seconds to limit the traffic we transmit and waste our time over
		// send the current status of the game to the phone and return the success of this action
#ifdef K_DEBUG
		Serial.print(F("Sending data of code: "));
		Serial.println(code_to_send, 10);
#endif
		if (false == isPhoneUpToDate() && Serial1.availableForWrite() > 0) {
			// the last code received is not the code we are sending
			// as the phone has not received it
			// send the data to the phone, starting char first
			Serial1.print('{');
			// then the command
			Serial1.print('u');
			// then very importantly the version of this data
			Serial1.print('a');
			// and the code
			Serial1.print(code_to_send, 10); Serial1.print(':');
			// followed by the start time in seconds
			int secondsStart = (int)(scorer.startMillis() / 1000.0);
			Serial1.print(secondsStart, 10); Serial1.print(':');
			// and so we can also send the duration of the game
			int gameDurationSeconds = (int)((millis() - scorer.startMillis()) / 1000.0);
			Serial1.print(gameDurationSeconds, 10); Serial1.print(':');
			// send the current mode
			Serial1.print((int)scorer.getCurrentScoreMode(), 10);
			// send the current winner
			if (scorer.isMatchOver()) {
				Serial1.print((int)scorer.getMatchWinner(), 10);
			}
			else {
				// send a fake number to show it is still playing
				Serial1.print((int)8, 10);
			}
			// send if we are in a tie
			Serial1.print((int)scorer.getIsTiebreak() ? 1 : 0, 10);
			// send the current server
			Serial1.print((int)scorer.getCurrentServer(), 10);
			// and the current North player
			Serial1.print((int)scorer.getCurrentNorthPlayer(), 10);
			// send the sets
			Serial1.print((int)scorer.getSets(player_one), 10); Serial1.print(':');
			Serial1.print((int)scorer.getSets(player_two), 10); Serial1.print(':');
			// send the games per set
			int numberSets = scorer.getSets(player_one) + scorer.getSets(player_two);
			for (int i = 0; i < numberSets; ++i) {
				// send the games for each set
				Serial1.print((int)scorer.getSetGames(player_one, i), 10); Serial1.print(':');
				Serial1.print((int)scorer.getSetGames(player_two,  i), 10); Serial1.print(':');
			}
			// now the current points
			Serial1.print((int)scorer.getPoints(player_one), 10); Serial1.print(':');
			Serial1.print((int)scorer.getPoints(player_two), 10); Serial1.print(':');
			// now the current games
			Serial1.print((int)scorer.getGames(player_one), 10); Serial1.print(':');
			Serial1.print((int)scorer.getGames(player_two), 10); Serial1.print(':');
			// now the current total points
			Serial1.print((int)scorer.getTotalPoints(player_one), 10); Serial1.print(':');
			Serial1.print((int)scorer.getTotalPoints(player_two), 10); Serial1.print(':');
			// also now, for the last bit, we can send all the history of this game for the phone to remember
			uint16_t numberPoints = scorer.getNumberHistoricPoints();

			// to save space we can encode these binary values into a series of data packets...
			// so eight at a time put then into an int and send them away
			Serial1.print((unsigned int)numberPoints, 10); Serial1.print(':');
			uint16_t bitCounter = 0;
			unsigned int dataPacket = 0;
			for (uint16_t i = 0; i < numberPoints; ++i) {
				// add this value to the data packet
				dataPacket |= scorer.getHistoricPoint(i) << bitCounter;
				// and increment the counter, sending as radix32 number means we can store 10 bits of data (up to 1023 base 10)
				if (++bitCounter >= 10) {
					// exceeded the size for next time, send this packet
					if (dataPacket < K_HISTORYRADIX) {
						// this will be print as '0' up to 'F' but we need it to be '0F' as expecting a fixed length...
						// this is true for hex - who knows how a radix32 number is printed - but whatever nice that we get 10 values
						Serial1.print('0');
					}
					Serial1.print(dataPacket, K_HISTORYRADIX);
					// and reset the counter and data
					bitCounter = 0;
					dataPacket = 0;
				}
			}
			if (bitCounter > 0) {
				// there was data we failed to send, only partially filled - send this anyway
				if (dataPacket < K_HISTORYRADIX) {
					// this will be print as '0' up to 'F' but we need it to be '0F' as expecting a fixed length...
					// this is true for hex - who knows how a radix64 number is printed - but whatever nice that we get 10 values
					Serial1.print('0');
				}
				Serial1.print(dataPacket, K_HISTORYRADIX);
			}
			// and the end
			Serial1.print('}');
		}
		// store the code for this send time
		last_send_time = millis();
	}
	// return that this was sent ok
	return true;
#else
	Serial.println(F("Trying to send bluetooth data but bluetooth is no enabled"));
	return true;
#endif
}

int PhoneConnection::extractValue(char searchChar, int& startIndex) {
	// from the start index, find the search char and extract the data recovered
	char dataBuffer[16];
	uint8_t dataIndex = 0;
#ifdef K_DEBUG
	Serial.print(F("from: "));
	Serial.println(startIndex, 10);
#endif
	for (int index = startIndex; index < K_RX_BUFFER_SIZE; ++index) {
		// for the whole buffer, search for the specified character
		if (rxBuffer[index] == searchChar) {
			// have reached the char we are looking for, move the index to search to the next one
			startIndex = index + 1;
			// terminate the string buffer so we will return the string read as a value
			dataBuffer[dataIndex] = 0;
			// and stop looking
			break;
		}
		else {
			// remember the data as we go, starting at 0 and building up
			dataBuffer[dataIndex++] = rxBuffer[index];
			if (dataIndex > 14) {
				// too much data in the buffer, terminate it
				dataBuffer[dataIndex] = 0;
				break;
			}
		}
	}
#ifdef K_DEBUG
	Serial.print(F(" to:"));
	Serial.print(startIndex, 10);
	Serial.print(F(" found:"));
	Serial.print(dataBuffer);
	Serial.print(F(" which is:"));
	Serial.println(atoi(dataBuffer), 10);
#endif
	return atoi(dataBuffer);
}

bool PhoneConnection::receiveGameStatus(Score::Scorer* scorer) {
	// receive the data from the string and put into the scorer, decode all the data from the buffer
	int findIndex = 0;
#ifdef K_DEBUG
	Serial.print(F("Received game status: "));
	Serial.println(rxBuffer);
#endif
	// the first char (at index 0) is the version of data to expect
	char version = rxBuffer[findIndex++];
	if (version == 'a') {
		// extract the version one data
		/*int sendCode =*/ extractValue(':', findIndex);	// scan past the code to send
		// get the timing settings for this match
		int secondsStart = extractValue(':', findIndex);
		int secondsDuration = extractValue(':', findIndex);
		// set this on the scorer
		scorer->setStartMillis(secondsStart * 1000);
		// now we can scan past all the stuff that will be reconstructed by replaying the history
		++findIndex;	// current mode
		++findIndex;	// current winner
		++findIndex;	// is in tie
		++findIndex;	// current server
		++findIndex;	// current north player
		int playerOneSets = extractValue(':', findIndex);	// scan past the number sets player one
		int playerTwoSets = extractValue(':', findIndex);	// scan past the number sets player two
		// send the games per set
		int numberSets = playerOneSets + playerTwoSets;
		for (int i = 0; i < numberSets; ++i) {
			// send the games for each set
			extractValue(':', findIndex);	// scan past the player one set games
			extractValue(':', findIndex);	// scan past the player two set games
		}
		// now the current points
		extractValue(':', findIndex);	// scan past the player one points
		extractValue(':', findIndex);	// scan past the player two points
		// now the current games
		extractValue(':', findIndex);	// scan past the player one games
		extractValue(':', findIndex);	// scan past the player two games
		// now the current total points
		extractValue(':', findIndex);	// scan past the player one total points
		extractValue(':', findIndex);	// scan past the player two total points
		// also now, for the last bit, we can send all the history of this game for the phone to remember
		uint16_t noHistoricPoints = extractValue(':', findIndex);
		uint16_t dataCounter = 0;
		char historyValue [3];
		// null terminal this two character string
		historyValue[2] = 0;
		// reset the match data ready to read in the history values to totally reconstruct the match
		scorer->resetMatch();
		// the match has reset but it will have been being played for a while, this we want to set
		scorer->setStartMillis(millis() - (secondsDuration * 1000UL));
		// this is the data we are interested in, the history so get these values
		while (dataCounter < noHistoricPoints) {
			// while there are points to get, get them, set the two values to use
			historyValue[0] = rxBuffer[findIndex++];
			historyValue[1] = rxBuffer[findIndex++];
			// create the string from this
	#ifdef K_DEBUG
			Serial.print(F("Received history entry: "));
			Serial.print(historyValue);
	#endif
			int dataReceived = strtol(historyValue, NULL, K_HISTORYRADIX);
	#ifdef K_DEBUG
			Serial.print(F(" read as: "));
			Serial.println(dataReceived, 10);
	#endif
			// this char contains somewhere between one and eight values all bit-shifted, extract them now
			int bitCounter = 0;
			while (bitCounter < 10 && dataCounter < noHistoricPoints) {
				int bitValue = 1 & (dataReceived >> bitCounter++);
				// add this to the list of value received
				scorer->addPoint(bitValue);
	#ifdef K_DEBUG
				Serial.print(bitValue, 10);
	#endif
				++dataCounter;
			}
	#ifdef K_DEBUG
			Serial.println();
	#endif
		}
	}
	else {
	#ifdef K_DEBUG
			Serial.print(F("Received unknown version: "));
			Serial.println(version);
	#endif
	}
	return rxBuffer[findIndex] == '}';	// and the end '}' is what we expect finally
}

} /* namespace Communications */
