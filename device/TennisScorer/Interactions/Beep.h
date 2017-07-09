/*
 * Beep.h
 *
 *  Created on: 22 Apr 2017
 *      Author: douglasbrain
 */

#ifndef INTERACTIONS_BEEP_H_
#define INTERACTIONS_BEEP_H_

#include <Arduino.h>

namespace Interactions {

class Beep {
public:
	Beep();
	virtual ~Beep();
	void setup();

	void cheep(int8_t cheepCount);

	void beep(int8_t beepCount);

//private:
	void makeSound(int8_t count, unsigned long duration);
	float beep_time;
};


} /*namespace Interactions*/

#endif /* INTERACTIONS_BEEP_H_ */
