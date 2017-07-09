/*
 * VoltageMonitor.h
 *
 *  Created on: 15 May 2017
 *      Author: douglasbrain
 */

#ifndef VOLTAGEMONITOR_H_
#define VOLTAGEMONITOR_H_

#include "../Lib/Adafruit_INA219.h"

namespace Communications {

class VoltageMonitor {
public:
	VoltageMonitor();
	virtual ~VoltageMonitor();

	void initialise();
	void process();

	float getShuntVoltage();
	float getBusVoltage();
	float getCurrent();
	float getLoadVoltage();

private:
	Adafruit_INA219 ina219;
	uint32_t currentFrequency;
	unsigned long lastTimeDisplayed;
};

} /* namespace Communications */

#endif /* VOLTAGEMONITOR_H_ */
