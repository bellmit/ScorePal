/*
 * MacAddress.h
 *
 *  Created on: 12 May 2017
 *      Author: douglasbrain
 */

#ifndef MACADDRESS_H_
#define MACADDRESS_H_

#include <Arduino.h>

namespace Communications {

class MacAddress {
public:
	MacAddress();
	virtual ~MacAddress();

	void read(uint8_t word, uint8_t loc);
	void printToSerial();
	void getAddress(uint8_t* buffer);

private:
	uint8_t m[6];
};

} /* namespace Communications */

#endif /* MACADDRESS_H_ */
