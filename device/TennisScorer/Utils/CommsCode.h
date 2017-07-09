/*
 * Communications.h
 *
 *  Created on: 30 Apr 2017
 *      Author: douglasbrain
 */

#ifndef COMMSCODE_H_
#define COMMSCODE_H_

#ifdef ARDUINO
#include <Arduino.h>
#else
#include <Stdint.h>
#endif

class CommsCode {
#define K_SENDING_CODE_LENGTH 11
public:
	CommsCode(uint8_t* uniqueCode = NULL) {
		// initialise the sending index
		sending_index = 0;
		// the first char in the array is a '{'
		unique_code[0] = '{';
		// the second specifies it is an action
		unique_code[1] = 'a';
		// the last is the closing brace
		unique_code[10] = '}';
		// set the MAC address 6 values...
		setUniqueCode(uniqueCode);
	}
	virtual ~CommsCode() {
		// destruction
	}

	void setUniqueCode(uint8_t* uniqueCode) {
		if (NULL != uniqueCode) {
			// set the unique code
			for (uint8_t i = 0; i < 6; ++i) {
				unique_code[i+2] = uniqueCode[i];
			}
		}
	}
	uint8_t* createSendingCode(uint8_t actionCode) {
		// create the unique sending code to receive, only encoding actions <= 63 and >=0 as only four bits used...
		if (++sending_index >= 128) {
			// index can only be 1-128 as read in as int8_t sometimes
			sending_index = 1;
		}
		// put the code together, shifting the action by the correct amount
		unique_code[8] = sending_index;
		unique_code[9] = actionCode;
		return unique_code;
	}
	bool extractActionCode(uint8_t* receivedCode, uint8_t& actionCode, uint8_t& rxIndex) {
		bool isDataIntended = false;
		if (receivedCode[1] == 'a') {
			// extract the index of the message and the action code in the message
			rxIndex = receivedCode[8];
			actionCode = receivedCode[9];
			// check to see if this data is int8_tended for us
			isDataIntended = true;
			for (uint8_t i = 2; i < 8; ++i) {
				if (receivedCode[i] != unique_code[i]) {
					// this is not a match, this isn't for us to use
					isDataIntended = false;
					break;
				}
			}
		}
		// this is int8_tended for us, return this if this is new
		return isDataIntended;
	}

private:
	// remember the sending index to not repeat communications by mistake
	uint8_t sending_index;
	// create the unique codes for this device (the MAC address of this device, then the index, then the command)
	uint8_t unique_code[K_SENDING_CODE_LENGTH];
};





#endif /* COMMSCODE_H_ */
