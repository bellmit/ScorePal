/*
 * CommunicationsTest.h
 *
 *  Created on: 26 Apr 2017
 *      Author: douglasbrain
 */

#ifndef COMMSCODETEST_H_
#define COMMSCODETEST_H_

#include "ClassTest.h"

class CommsCodeTest : public ClassTest {
public:
	CommsCodeTest();
	virtual ~CommsCodeTest();

	virtual int runClassTests();

	void testUniqueCodesInLimits();
	void testUniqueCodesOutsideLimits();
	void checkCommsActions(int day, int mth, int yr, int no, bool checkUniqueCodes);
};

#endif /* COMMSCODETEST_H_ */
