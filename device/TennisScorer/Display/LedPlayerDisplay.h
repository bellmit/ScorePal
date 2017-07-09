/*
 * LedPlayerDisplay.h
 *
 *  Created on: 24 May 2017
 *      Author: douglasbrain
 */

#ifndef LEDPLAYERDISPLAY_H_
#define LEDPLAYERDISPLAY_H_

#include <Arduino.h>

namespace Display {

class LedPlayerDisplay {
public:
	LedPlayerDisplay();
	virtual ~LedPlayerDisplay();

	void initialise();
	void setDisplayBrightness(uint8_t brightness);
	void setDisplayPlayer(bool isPlayerTwoFront);

protected:
	void showDisplay();

private:
	bool is_player_two_front;
	unsigned int ledValue;
};

} /* namespace Display */

#endif /* LEDPLAYERDISPLAY_H_ */
