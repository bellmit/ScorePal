/*
 * InputSender.h
 *
 *  Created on: 30 Apr 2017
 *      Author: douglasbrain
 */

#ifndef INPUTSENDER_H_
#define INPUTSENDER_H_

#include "../Utils/CommsCode.h"

namespace Communications {

class InputSender {
public:
	InputSender();
	virtual ~InputSender();

	void setup();
	void sendAction(uint8_t actionCode);

private:
	CommsCode code;
	void initialiseRadio();
	void sleepRadio();

	bool isPowerDown;
};

} /* namespace Communications */

#endif /* INPUTSENDER_H_ */
