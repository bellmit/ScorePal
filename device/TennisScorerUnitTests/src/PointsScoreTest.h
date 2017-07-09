/*
 * PointsScoreTest.h
 *
 *  Created on: 26 Apr 2017
 *      Author: douglasbrain
 */

#ifndef PointSCORETEST_H_
#define PointSCORETEST_H_

#include "ClassTest.h"

class PointsScoreTest : public ClassTest {
public:
	PointsScoreTest();
	virtual ~PointsScoreTest();

	int runClassTests();

	void testGeneralScoring();
	void testPointRemoval();
	void testBasePointsRemembering();
};

#endif /* PointSCORETEST_H_ */
