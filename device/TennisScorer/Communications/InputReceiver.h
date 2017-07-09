/*
 * InputReceiver.h
 *
 *  Created on: 30 Apr 2017
 *      Author: douglasbrain
 */

#ifndef INPUTRECEIVER_H_
#define INPUTRECEIVER_H_

#include "../Utils/CommsCode.h"

namespace Communications {

class InputReceiver {
public:
	InputReceiver();
	virtual ~InputReceiver();

	void setup();
	int8_t receiveNewAction();

private:
	CommsCode code;
	int8_t last_received_index;
	uint8_t msg[K_SENDING_CODE_LENGTH];
};

} /* namespace Communications */

#endif /* INPUTRECEIVER_H_ */
