/*
 * StackTest.cpp
 *
 *  Created on: 26 Apr 2017
 *      Author: douglasbrain
 */

#include <sstream>
#include "../../TennisScorer/Utils/CommsCode.h"
#include "CommsCodeTest.h"

CommsCodeTest::CommsCodeTest() {
	// constructor

}

CommsCodeTest::~CommsCodeTest() {
	// destructor
}


int CommsCodeTest::runClassTests() {
	// test normal operation
	/*CommsCode comms = CommsCode(30, 04, 17, 01);
	check(comms.getDayCreated() == 30UL, std::string("comms month code is not correct, expecting 30 where it is", comms.getDayCreated()));
	check(comms.getMthCreated() == 04UL, std::string("comms month code is not correct, expecting 04 where it is", comms.getMthCreated()));
	check(comms.getYrCreated() == 17UL, std::string("comms year code is not correct, expecting 17 where it is", comms.getYrCreated()));
	check(comms.getNoCreated() == 01UL, std::string("comms number code is not correct, expecting 01 where it is", comms.getNoCreated()));
	// create a code to send
	unsigned long createdCode = comms.createSendingCode(12);
	int8_t rxCode = 0;
	int8_t rxIndex = 0;
	check(comms.extractActionCode(createdCode, rxCode, rxIndex), "extract code failed");
	check(rxCode == 12, std::string("rx code is not 12 as expected, instead it is", rxCode));
	check(rxIndex == 1, std::string("rx index is not 1 as expected, instead it is", rxIndex));

	//On class 30 12 48 0 Action code received 0 is not as expected 37
	comms = CommsCode(30, 12, 48, 00);
	createdCode = comms.createSendingCode(37);
	check(comms.extractActionCode(createdCode, rxCode, rxIndex), "extract code failed");
	check(rxCode == 37, std::string("rx code is not 12 as expected, instead it is", rxCode));
	check(rxIndex == 1, std::string("rx index is not 1 as expected, instead it is", rxIndex));
	// and call the bigger tests
	testUniqueCodesInLimits();
	testUniqueCodesOutsideLimits();
*/
	return errors;
}

void CommsCodeTest::testUniqueCodesInLimits() {
	// test normal operation
	for (int day = 0; day < 32; day += 6) {
		for (int mth = 0; mth < 16; mth += 6) {
			for (int yr = 0; yr < 64; yr += 6) {
				for (int no = 0; no < 128; no += 32) {
					// we are systematically moving through the codes creating classes, create one for this data
					checkCommsActions(day, mth, yr, no, true);
				}
			}
		}
	}
}
void CommsCodeTest::testUniqueCodesOutsideLimits() {
	// test when the class is created with data outside it's range of unique values
	for (int day = 30; day < 64; day += 6) {
		for (int mth = 10; mth < 64; mth += 6) {
			for (int yr = 60; yr < 100; yr += 6) {
				for (int no = 120; no < 200; no += 16) {
					// we are systematically moving through the codes creating classes, create one for this data
					checkCommsActions(day, mth, yr, no, false);
				}
			}
		}
	}
}

void CommsCodeTest::checkCommsActions(int day, int mth, int yr, int no, bool checkUniqueCodes) {

	/*CommsCode comms(day, mth, yr, no);
	if (checkUniqueCodes) {
		// check the code is created / decoded for this ok
		check(day == comms.getDayCreated(), "Expected day not set in comms class");
		check(mth == comms.getMthCreated(), "Expected month not set in comms class");
		check(yr == comms.getYrCreated(), "Expected year not set in comms class");
		check(no == comms.getNoCreated(), "Expected number not set in comms class");
	}
	// okay, now we can check that the code is created / decoded properly
	int sentIndex = 0;
	for (int actionCode = 0; actionCode < 64; ++actionCode) {
		unsigned long createdCode = comms.createSendingCode(actionCode);
		if (++sentIndex > 15UL) {
			// index can only be 1-15
			sentIndex = 1UL;
		}
		int8_t rxCode = 0;
		int8_t rxIndex = 0;
		check(comms.extractActionCode(createdCode, rxCode, rxIndex), "extract code failed");
		// check this data
		if (rxCode != actionCode) {
			std::stringstream errorString;
			errorString << "On class " <<
					comms.getDayCreated() << " " <<
					comms.getMthCreated() << " " <<
					comms.getYrCreated() << " " <<
					comms.getNoCreated() << " " <<
					"Action code received " << rxCode << " is not as expected " << actionCode;
			check(rxCode == actionCode, errorString.str());
		}
		check(rxIndex == sentIndex, "sending index is not as expected");
	}*/
}
