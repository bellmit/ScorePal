/*
 * WimbledonScoreTest.h
 *
 *  Created on: 26 Apr 2017
 *      Author: douglasbrain
 */

#ifndef WIMBLEDONSCORETEST_H_
#define WIMBLEDONSCORETEST_H_

#include "ClassTest.h"
#include "../../TennisScorer/Score/Scorer.h"

class WimbledonScoreTest : public ClassTest {
public:
	WimbledonScoreTest();
	virtual ~WimbledonScoreTest();

	int runClassTests();

	void testGameState();
	void testGeneralScoring();
	void testPointRemoval();
	void testPointRemoval2();
	void testSetRecordingAndGetting();
	void testTieBreakServing();
	void testUndoPastChangeSides();

private:
	void bringToTie(Score::Scorer& scorer);
};

#endif /* WIMBLEDONSCORETEST_H_ */
