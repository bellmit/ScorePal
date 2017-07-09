/*
 * PointsScoreTest.cpp
 *
 *  Created on: 26 Apr 2017
 *      Author: douglasbrain
 */

#include "PointsScoreTest.h"
#include "../../TennisScorer/Score/Scorer.h"
#include <sstream>

#ifndef ARDUINO
#define bitRead(value, bit) (((value) >> (bit)) & 0x01)
#define bitSet(value, bit) ((value) |= (1UL << (bit)))
#define bitClear(value, bit) ((value) &= ~(1UL << (bit)))
#define bitWrite(value, bit, bitvalue) (bitvalue ? bitSet(value, bit) : bitClear(value, bit))
#endif

PointsScoreTest::PointsScoreTest() {
	// constructor

}

PointsScoreTest::~PointsScoreTest() {
	// destructor
}

int PointsScoreTest::runClassTests() {

	testGeneralScoring();
	testPointRemoval();
	testBasePointsRemembering();

	return ClassTest::errors;
}

void PointsScoreTest::testGeneralScoring() {
	// start clean
	Score::Scorer score(K_SCOREPOINTS);
	Player startNorthPlayer = score.getCurrentNorthPlayer();

	check(startNorthPlayer == score.getCurrentNorthPlayer(), std::string("incorrectly changing ends on Points: ", score.getPoints(score.getCurrentServer())));
	Player currentServer = score.getCurrentServer();
	score.addPoint(player_two);
	check(startNorthPlayer == score.getCurrentNorthPlayer(), std::string("incorrectly changing ends on Points: ", score.getPoints(score.getCurrentServer())));
	if (currentServer != score.getCurrentServer()) {
		error("server has changed after just one Point...");
	}
	score.addPoint(player_one);
	check(startNorthPlayer == score.getCurrentNorthPlayer(), std::string("incorrectly changing ends on Points: ", score.getPoints(score.getCurrentServer())));
	score.addPoint(player_two);
	check(startNorthPlayer == score.getCurrentNorthPlayer(), std::string("incorrectly changing ends on Points: ", score.getPoints(score.getCurrentServer())));
	score.addPoint(player_one);
	check(startNorthPlayer == score.getCurrentNorthPlayer(), std::string("incorrectly changing ends on Points: ", score.getPoints(score.getCurrentServer())));
	if (currentServer != score.getCurrentServer()) {
		error("server has changed after just four Points...");
	}
	score.addPoint(player_two);
	// change ends here - five Points
	check(startNorthPlayer != score.getCurrentNorthPlayer(), std::string("incorrectly not changing ends on Points: ", score.getPoints(score.getCurrentServer())));
	if (currentServer == score.getCurrentServer()) {
		error("server is still the same after five Points...");
	}
	// play another five
	currentServer = score.getCurrentServer();
	startNorthPlayer = score.getCurrentNorthPlayer();
	score.addPoint(player_two);
	check(startNorthPlayer == score.getCurrentNorthPlayer(), std::string("incorrectly changing ends on Points: ", score.getPoints(score.getCurrentServer())));
	if (currentServer != score.getCurrentServer()) {
		error("server has changed after just one Point...");
	}
	score.addPoint(player_one);
	check(startNorthPlayer == score.getCurrentNorthPlayer(), std::string("incorrectly changing ends on Points: ", score.getPoints(score.getCurrentServer())));
	score.addPoint(player_two);
	check(startNorthPlayer == score.getCurrentNorthPlayer(), std::string("incorrectly changing ends on Points: ", score.getPoints(score.getCurrentServer())));
	score.addPoint(player_one);
	check(startNorthPlayer == score.getCurrentNorthPlayer(), std::string("incorrectly changing ends on Points: ", score.getPoints(score.getCurrentServer())));
	if (currentServer != score.getCurrentServer()) {
		error("server has changed after just four Points...");
	}
	score.addPoint(player_two);
	check(startNorthPlayer == score.getCurrentNorthPlayer(), std::string("incorrectly changing ends on Points: ", score.getPoints(score.getCurrentServer())));
	if (currentServer == score.getCurrentServer()) {
		error("server is still the same after five Points...");
	}
	// and another five
	currentServer = score.getCurrentServer();
	score.addPoint(player_two);
	check(startNorthPlayer == score.getCurrentNorthPlayer(), std::string("incorrectly changing ends on Points: ", score.getPoints(score.getCurrentServer())));
	if (currentServer != score.getCurrentServer()) {
		error("server has changed after just one Point...");
	}
	score.addPoint(player_one);
	check(startNorthPlayer == score.getCurrentNorthPlayer(), std::string("incorrectly changing ends on Points: ", score.getPoints(score.getCurrentServer())));
	score.addPoint(player_two);
	check(startNorthPlayer == score.getCurrentNorthPlayer(), std::string("incorrectly changing ends on Points: ", score.getPoints(score.getCurrentServer())));
	score.addPoint(player_one);
	check(startNorthPlayer == score.getCurrentNorthPlayer(), std::string("incorrectly changing ends on Points: ", score.getPoints(score.getCurrentServer())));
	if (currentServer != score.getCurrentServer()) {
		error("server has changed after just four Points...");
	}
	score.addPoint(player_two);
	check(startNorthPlayer != score.getCurrentNorthPlayer(), std::string("incorrectly not changing ends on Points: ", score.getPoints(score.getCurrentServer())));
	if (currentServer == score.getCurrentServer()) {
		error("server is still the same after five Points...");
	}
}

void PointsScoreTest::testPointRemoval() {
	// start clean
	Score::Scorer score(K_SCOREPOINTS);

	// add some Points, remembering the sequence
	for (int j = 20; j < 127; ++j) {
		// do this test a lot as it is random and we want to get a nice coverage
		// first let us put ourselves somewhere around winning a game and a set
		score.addPoint(j);
		// now lets play ten Points and be sure we can rewind them again
		std::stringstream PointSequence;
		PointSequence << score.getPoints(player_one) << "-" << score.getPoints(player_two) << std::string(",");
		for (int i = 0; i < K_STACK_SIZE; ++i) {
			int r = ((double) rand() / (RAND_MAX)) + 1;
			score.addPoint(r);
			PointSequence << score.getPoints(player_one) << "-" << score.getPoints(player_two) << std::string(",");
		}
		// now roll back, creating the same string hopefully
		std::string testSequence;
		while (score.getNoPreviousStates() > 0) {
			std::stringstream ss;
			ss << score.getPoints(player_one) << "-" << score.getPoints(player_two) << "," << testSequence;
			testSequence = ss.str();
			score.removeLastPoint();
		}
		// need to add the score at the start too
		std::stringstream ss;
		ss << score.getPoints(player_one) << "-" << score.getPoints(player_two) << "," << testSequence;
		testSequence = ss.str();
		if (0 != testSequence.compare(PointSequence.str())) {
			check(false, "expecting sequences to be the same, they are not, they are...");
			std::cout << PointSequence << std::endl;
			std::cout << testSequence << std::endl;
		}
	}
}

void PointsScoreTest::testBasePointsRemembering() {
	// start clean
	Score::Scorer score(K_SCOREPOINTS);
#ifdef SDCARD
	TestFileStore store;
#endif
	Player winner = player_one;
	int playerOnePoints = 0;
	int playerTwoPoints = 0;
	// add a load of points, then ensure they are recalled okay
	for (uint16_t i = 0; i < K_POINTSTORESIZE * 16; ++i) {
		// store the points, five of each as 16 isn't divisible by five so will cross lots of boundries
		if (i % 5 == 0) {
			// swap the winner
			winner = winner == player_one ? player_two : player_one;
		}
		// okay - store this point
		score.addPoint(winner);
		if (winner == player_one) {
			++playerOnePoints;
		}
		else {
			++playerTwoPoints;
		}
		// check the counter
		//std::cout << "storing " << winner << " at " << i << std::endl;
		check(score.getNumberHistoricPoints() == i + 1, "Historic point counter not working");
	}
	// check the totals
	check(score.getTotalPoints(player_one) == playerOnePoints, "player one total points not as expected");
	check(score.getTotalPoints(player_two) == playerTwoPoints, "player two total points not as expected");

	// all added, let's check they are recalled okay
	winner = player_one;
	for (uint16_t i = 0; i < score.getNumberHistoricPoints(); ++i) {
		// recall the points as they were entered
		if (i % 5 == 0) {
			// swap the winner
			winner = winner == player_one ? player_two : player_one;
		}
		// check the point at this position
		check(score.getHistoricPoint(i) == winner, "Historic point isn't as expected");
		// printing more information to help us sort this out
		if (score.getHistoricPoint(i) != winner) {
			std::cout << "expecting " << winner << " at " << i << " but getting " << score.getHistoricPoint(i) << std::endl;
		}
	}
	// clear this history
#ifdef SDCARD
	score.resetMatch(store);
#else
	score.resetMatch();
#endif
	// test that removing too many points (more than added, doesn't crash the system)
	for (uint16_t i = 0; i < K_POINTSTORESIZE; ++i) {
		// store the points - player two as defaults to player one (zero)
		score.addPoint(player_two);
		// check the counter
		check(score.getNumberHistoricPoints() == i + 1, "Historic point counter not working in adding some to remove");
	}
	// check the totals
	check(score.getTotalPoints(player_one) == 0, "player one total points being zero not as expected");
	check(score.getTotalPoints(player_two) == K_POINTSTORESIZE, "player two total points being store size not as expected");
	// all added, let's check they are recalled okay
	check(score.getNumberHistoricPoints() == K_POINTSTORESIZE, "Historic point counter not working after added some to remove");

	// clear it out and try removing more than there are
#ifdef SDCARD
	score.resetMatch(store);
#else
	score.resetMatch();
#endif
	score.addPoint(player_two, K_STACK_SIZE);
	// can only add the stack size as this limits the amount we can remove as well as the history size
	for (uint16_t i = 0; i < K_STACK_SIZE; ++i) {
		// remove these points
		score.removeLastPoint();
		check(score.getNumberHistoricPoints() == K_STACK_SIZE - (i + 1), "Point removal of history counter not working as expected");
	}
	check(score.getTotalPoints(player_one) == 0, "player one total points being zero not as expected");
	check(score.getTotalPoints(player_two) == 0, "player two total points being zero not as expected");
	// now the list is empty
	check(score.getNumberHistoricPoints() == 0, "History isn't empty as expected");
	// remove another load to check responds okay
	for (uint16_t i = 0; i < K_STACK_SIZE; ++i) {
		// remove these points
		score.removeLastPoint();
		check(score.getNumberHistoricPoints() == 0, "Point removal of empty history counter not working as expected");
	}
	check(score.getTotalPoints(player_one) == 0, "player one total points being zero not as expected");
	check(score.getTotalPoints(player_two) == 0, "player two total points being zero not as expected");

	// just test the logic we are using for binning here to help my brain
	int counter = -1;
	int testBinIndex = 0;
	for (int16_t i = 0; i < K_POINTSTORESIZE * 16; ++i) {
		int binIndex = (int)(i / 16);
		if (++counter == 16) {
			++testBinIndex;
			counter = 0;
		}
		check(binIndex == testBinIndex, "Bin indexing not working.");
		// and the index in the value
		int valIndex = i - (binIndex * 16);
		check(valIndex == counter, "Val indexing not working");
	}
}

