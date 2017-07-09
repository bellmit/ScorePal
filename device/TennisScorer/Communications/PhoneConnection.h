/*
 * PhoneConnection.h
 *
 *  Created on: 15 May 2017
 *      Author: douglasbrain
 */

#ifndef PHONECONNECTION_H_
#define PHONECONNECTION_H_

#include <Arduino.h>
#include "../Score/Scorer.h"

namespace Communications {

class PhoneConnection {
#define K_RX_BUFFER_SIZE 255	/* the buffer is large because can receive a whole match status in one go */
public:
	PhoneConnection();
	virtual ~PhoneConnection();

	void initialise();
	int8_t receiveNewAction(Score::Scorer* scorer);

	void incrementSendingCode();
	bool isPhoneUpToDate();
	bool sendGameStatus(Score::Scorer& scorer, bool isForceSend);

protected:
	bool receiveGameStatus(Score::Scorer* scorer);
	int extractValue(char searchChar, int& startIndex);

private:
	char ch;
	int last_code_received;
	int code_to_send;
	unsigned long last_send_time;

	char rxBuffer[K_RX_BUFFER_SIZE + 1];
	int rxIndex = 0;
	char rxCommand = '0';
};

} /* namespace Communications */

#endif /* PHONECONNECTION_H_ */
