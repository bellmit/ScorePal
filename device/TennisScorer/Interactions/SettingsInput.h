/*
 * ModeInput.h
 *
 *  Created on: 20 May 2017
 *      Author: douglasbrain
 */

#ifndef SETTINGSINPUT_H_
#define SETTINGSINPUT_H_

#include <Arduino.h>

namespace Interactions {

#define K_NO_MODES 6
#define K_NO_BRIGHTNESS 15

#ifdef K_PIN_MODEDIAL
#define K_MINMODEVAL 0
#define K_MAXMODEVAL 1023
#endif

#define K_MINBRIGHTVAL 5
#define K_MAXBRIGHTVAL 1020

class SettingsInput {
public:
	SettingsInput();
	virtual ~SettingsInput();

	void setup();
	uint8_t readMode();
	uint8_t readBrightness();

private:
#ifdef K_PIN_MODEDIAL
	float mode_range;
#endif
	uint8_t last_brightness;
	uint8_t brightness_threshold;
	uint16_t brightness_values[K_NO_BRIGHTNESS];
};

} /* namespace Interactions */

#endif /* SETTINGSINPUT_H_ */
