/*
 * WimbledonScoreTest.cpp
 *
 *  Created on: 26 Apr 2017
 *      Author: douglasbrain
 */

#include "WimbledonScoreTest.h"
#include "../../TennisScorer/Score/Scorer.h"
#include <sstream>

WimbledonScoreTest::WimbledonScoreTest() {
	// constructor

}

WimbledonScoreTest::~WimbledonScoreTest() {
	// destructor
}

int WimbledonScoreTest::runClassTests() {

	testGameState();
	testGeneralScoring();
	testPointRemoval();
	testPointRemoval2();
	testSetRecordingAndGetting();
	testTieBreakServing();
	testUndoPastChangeSides();

	return ClassTest::errors;
}


void WimbledonScoreTest::testGeneralScoring() {
	// initialise
	Score::Scorer score(K_SCOREWIMBLEDON5);
	// test simple win
	Player startNorthPlayer = score.getCurrentNorthPlayer();
	score.addPoint(player_one); //0-15
	check(score.getPoints(player_one) == 1, std::string("Score isn't 1, instead ", score.getPoints(player_one)));
	check(startNorthPlayer == score.getCurrentNorthPlayer(), "incorrectly changing ends after one Point");
	score.addPoint(player_one); //0-30
	check(score.getPoints(player_one) == 2, std::string("Score 1 not Printing 30, instead ", score.getPoints(player_one)));
	score.addPoint(player_one); //0-40
	check(score.getPoints(player_one) == 3, std::string("Score 1 not Printing 40, instead ", score.getPoints(player_one)));
	score.addPoint(player_one); //game
	if (score.getGames(player_one) != 1) {
		error("player one didn't win the first game...");
	}
	check(startNorthPlayer != score.getCurrentNorthPlayer(), "incorrectly not changing ends after the first game");
	// now test deuce
	score.addPoint(player_one); //0-15
	check(startNorthPlayer != score.getCurrentNorthPlayer(), "incorrectly changing ends");
	score.addPoint(player_one); //0-30
	score.addPoint(player_one); //0-40
	score.addPoint(player_two); //15-15
	check(score.getPoints(player_two) == 1, std::string("Score 2 not Printing 15, instead ", score.getPoints(player_two)));
	score.addPoint(player_two); //30-30
	check(score.getPoints(player_two) == 2, std::string("Score 2 not Printing 30, instead ", score.getPoints(player_two)));
	score.addPoint(player_two); //40-40
	check(score.getPoints(player_two) == 3, std::string("Score 2 not Printing 40, instead ", score.getPoints(player_two)));
	score.addPoint(player_one); //40-ad
	check(score.getPoints(player_one) == 4, std::string("Score 1 not Printing ad, instead ", score.getPoints(player_one)));
	score.addPoint(player_two); //40-40
	score.addPoint(player_two); //ad-40
	check(score.getPoints(player_two) == 4, std::string("Score 2 not Printing ad, instead ", score.getPoints(player_two)));
	score.addPoint(player_one); //40-40
	score.addPoint(player_one); //40-ad
	score.addPoint(player_one); //win
	if (score.getGames(player_one) != 2) {
		error("player one didn't win the second game...");
	}
	check(startNorthPlayer != score.getCurrentNorthPlayer(), "incorrectly changing ends after two games");
	check(false == score.isMatchOver(), "Someone won the match during set 1 ");

	// player one is on two games to none, lets send him to the end of the set now
	score.addPoint(player_one, 4);
	check(score.getGames(player_one) == 3, "Player one didn't win game 3");
	score.addPoint(player_one, 4);
	check(score.getGames(player_one) == 4, "Player one didn't win game 4");
	score.addPoint(player_one, 4);
	check(score.getGames(player_one) == 5, "Player one didn't win game 5");
	// push him over the edge to the set
	score.addPoint(player_one, 4);
	check(score.getSets(player_one) == 1, "Player one didn't win set 1");
	check(false == score.isMatchOver(), "Someone won the match after set 1");

	// new set - take it to 7-5
	score.addPoint(player_one, 20);
	check(score.getGames(player_one) == 5, "Player one didn't win 5 games in a row");
	score.addPoint(player_two, 20);
	check(score.getGames(player_two) == 5, "Player two didn't win 5 games in a row");
	score.addPoint(player_one, 4);
	check(score.getGames(player_one) == 6, "Player one didn't win game 6");
	// push him over the edge to the set
	score.addPoint(player_one, 4);
	check(score.getSets(player_one) == 2, "Player one didn't win set 2");
	check(false == score.isMatchOver(), "Someone won the match after set 2 with ");

	// okay, let's try out a tie breaker, 6-6
	score.addPoint(player_one, 20);
	score.addPoint(player_two, 20);
	// now it's 5-5, let them win one each
	score.addPoint(player_one, 4);
	score.addPoint(player_two, 4);
	check(score.getIsTiebreak(), "Six all and not in a tie breaker");
	check(score.getGames(player_one) == 6, "Player one didn't win game 6 to be in a tie");
	check(score.getGames(player_two) == 6, "Player two didn't win game 6 to be in a tie");
	// add some tie breaker Points
	score.addPoint(player_one);
	check(score.getPoints(player_one) == 1, std::string("Score 1 not Printing 1 in a tie, instead ", score.getPoints(player_one)));
	score.addPoint(player_one);
	check(score.getPoints(player_one) == 2, std::string("Score 1 not Printing 2 in a tie, instead ", score.getPoints(player_one)));
	score.addPoint(player_one);
	check(score.getPoints(player_one) == 3, std::string("Score 1 not Printing 3 in a tie, instead ", score.getPoints(player_one)));
	score.addPoint(player_one);
	check(score.getPoints(player_one) == 4, std::string("Score 1 not Printing 4 in a tie, instead ", score.getPoints(player_one)));
	score.addPoint(player_one);
	check(score.getPoints(player_one) == 5, std::string("Score 1 not Printing 5 in a tie, instead ", score.getPoints(player_one)));
	// let 2 have 6 Points now, should'nt win
	score.addPoint(player_two, 6);
	check(score.getPoints(player_two) == 6, std::string("Score 2 not Printing 6 in a tie, instead ", score.getPoints(player_two)));
	// and let 2 win it
	score.addPoint(player_two);
	check(score.getSets(player_one) == 2, "Player one incorrectly won set 3");
	check(score.getSets(player_two) == 1, "Player two didn't win set 3");
	check(false == score.isMatchOver(), "Someone won the match after set 3 with ");

	// cool, let player 1 win the match with the final set, check goes to a tie, give two their five games
	score.addPoint(player_one, 20);
	score.addPoint(player_two, 20);
	// now it's 5-5, let them win one each
	score.addPoint(player_one, 4);
	score.addPoint(player_two, 4);
	check(score.getIsTiebreak(), "Six all and not in a tie breaker in set 4");
	check(score.getGames(player_one) == 6, "Player one didn't win game 6 to be in a tie in set 4");
	check(score.getGames(player_two) == 6, "Player two didn't win game 6 to be in a tie in set 4");
	// let them get to six all
	score.addPoint(player_one, 6);
	score.addPoint(player_two, 6);
	// so seven won't cut it
	score.addPoint(player_one);
	check(score.getPoints(player_one) == 7, std::string("Score 1 not Printing 7 in a tie, instead ", score.getPoints(player_one)));
	// and two to player two
	score.addPoint(player_two, 2);
	check(score.getPoints(player_two) == 8, std::string("Score 2 not Printing 8 in a tie, instead ", score.getPoints(player_one)));
	// let player one take the match in four sets then
	score.addPoint(player_one, 2);
	check(score.getPoints(player_one) == 9, std::string("Score 1 not Printing 9 in a tie, instead ", score.getPoints(player_one)));
	score.addPoint(player_one);
	check(score.getSets(player_one) == 3, "Player one didn't win set 4");
	check(score.getSets(player_two) == 1, "Player two incorrectly won set 4");
	check(score.isMatchOver(), "Player one winning didn't signal the end of the match");
	check(score.getMatchWinner() == player_one, "Player one didn't win the match");

	//put back to try another 5 setter, getting all the way to the end this time
	score.resetMatch();

	// wanting to check we don't tie break in the final set - let one get two sets
	score.addPoint(player_one, 48);
	check(score.getSets(player_one) == 2, "Player one didn't win two sets in a row in match 2");
	score.addPoint(player_two, 48);
	check(score.getSets(player_two) == 2, "Player two didn't win two sets in a row in match 2");
	// okay, now five games each to get to the end
	score.addPoint(player_one, 20);
	score.addPoint(player_two, 20);
	check(score.getGames(player_one) == 5, "Player one didn't win five games in a row in match 2");
	check(score.getGames(player_two) == 5, "Player two didn't win five games in a row in match 2");
	// now it's 5-5, let them win one each
	score.addPoint(player_one, 4);
	score.addPoint(player_two, 4);
	check(score.getIsTiebreak() == false, "Six all but somehow in a tie breaker in final set");
	// 6-6 so another game doesn't do it
	score.addPoint(player_one, 4);
	check(score.getGames(player_one) == 7, "Player one didn't win game 7 in match 2");
	// 7-6 adding two for player two will still do no good
	score.addPoint(player_two, 8);
	check(score.getGames(player_two) == 8, "Player two didn't win games 7 and 8 in match 2");
	check(false == score.isMatchOver(), "Someone won the match in the final set that isn't a tie");
	// let player two take it now then
	score.addPoint(player_two, 4);
	check(score.getSets(player_two) == 3, "Player two didn't win the fifth set in match 2");
	check(score.isMatchOver(), "Player two winning didn't signal the end of the match");
	check(score.getMatchWinner() == player_two, "Player two didn't win the second match");
}

void WimbledonScoreTest::testGameState() {
	// create a game state
	Score::MatchState current_state;
	Stack<Score::MatchState> previous_states;
	previous_states.clear();
	current_state.is_tiebreak = false;
	previous_states.push(current_state);
	current_state.is_tiebreak = true;
	previous_states.push(current_state);

	// pop the last state
	current_state = previous_states.pop(current_state);
	check(current_state.is_tiebreak == true, "tie state is not as expected");
	current_state = previous_states.pop(current_state);
	check(current_state.is_tiebreak == false, "tie state is not as expected");
	check(previous_states.getNoItems() == 0, "expecting previous states stack to be empty");

	int lastValuePushed = -1;
	for (int i = 0; i < 20; ++i) {
		// set some data
		current_state.points[0] = i;
		current_state.games[0] = i + 1;
		//current_state.sets[0] = i + 2;
		previous_states.push(current_state);
		check(current_state.points[0] == i, "state Point is not as expected");
		check(current_state.games[0] == i+1, "state Point is not as expected");
		//check(current_state.sets[0] == i+2, "state Point is not " + String(i+2) + " it is " + String(current_state.sets[0]));
		lastValuePushed = i;
	}

	while (previous_states.getNoItems() > 0) {
		current_state = previous_states.pop(current_state);
		check(current_state.points[0] == lastValuePushed, "Points state popped is not as expected");
		check(current_state.games[0] == lastValuePushed + 1, "games state popped is not as expected");
		//check(current_state.sets[0] == lastValuePushed + 2, "sets state popped is not as expected");
		--lastValuePushed;

	}
	check(previous_states.getNoItems() == 0, "expecting previous states stack to be empty finally");

	//put back
	current_state.reset();
}

void WimbledonScoreTest::testPointRemoval() {
	// initialise
	Score::Scorer score(K_SCOREWIMBLEDON5);

	// add some Points, remembering the sequence
	for (int j = 20; j < 200; ++j) {
		// do this test a lot as it is random and we want to get a nice coverage
		// first let us put ourselves somewhere around winning a game and a set
		score.addPoint(j);
		std::stringstream PointSequence;
		PointSequence << score.getPoints(player_one) << "-" << score.getPoints(player_two) << ",";
		// now lets play ten Points and be sure we can rewind them again
		for (int i = 0; i < K_STACK_SIZE; ++i) {
			int r = ((double) rand() / (RAND_MAX)) + 1;
			score.addPoint(r);
			PointSequence << score.getPoints(player_one) << "-" << score.getPoints(player_two) << ",";
		}
		// now roll back, creating the same string hopefully
		std::string testSequence = std::string("");
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

void WimbledonScoreTest::testPointRemoval2() {
	// initialise
	Score::Scorer score(K_SCOREWIMBLEDON5);
	// okay, add some Points
	for (int i = 0; i < 5; ++i) {
		score.addPoint(player_one);
		score.addPoint(player_two);
	}
	// and roll back all ten
	check(score.getNoPreviousStates() == 10, "expecting 10 items in previous states and there are not");
	check(score.getPoints(player_one) == 3, "expecting player_one to be at deuce after 10 Points, they don't");
	check(score.getPoints(player_two) == 3, "expecting player_two to be at deuce after 10 Points, they don't");
	score.removeLastPoint();
	check(score.getPoints(player_one) == 4, "expecting player_one to had advantage after 9 Points, they don't");
	score.removeLastPoint();
	check(score.getPoints(player_one) == 3, "expecting player_one to at deuce after 8 Points, they don't");
	score.removeLastPoint();
	check(score.getPoints(player_one) == 4, "expecting player_one to be at advantage after 7 Points, they don't");
	score.removeLastPoint();
	check(score.getPoints(player_two) == 3, "expecting player_two to have 40 after 6 Points, they don't");
	score.removeLastPoint();
	check(score.getPoints(player_one) == 3, "expecting player_one to have 40 after 5 Points, they don't");
	score.removeLastPoint();
	check(score.getPoints(player_two) == 2, "expecting player_two to have 30 after 4 Points, they don't");
	score.removeLastPoint();
	check(score.getPoints(player_one) == 2, "expecting player_one to have 30 after 3 Points, they don't");
	score.removeLastPoint();
	check(score.getPoints(player_two) == 1, "expecting player_two to have 15 after 2 Points, they don't");
	score.removeLastPoint();
	check(score.getPoints(player_one) == 1, "expecting player_one to have 15 after 1 Points, they don't");
	score.removeLastPoint();
	check(score.getPoints(player_two) == 0, "expecting player_two to have 0 after 0 Points, they don't");
	check(score.getPoints(player_one) == 0, "expecting player_one to have 0 after 0 Points, they don't");
	// try again, shouldn't be a problem but will not change
	check(score.getNoPreviousStates() == 0, "expecting 0 items in previous states and there are not");
	score.removeLastPoint();
	check(score.getPoints(player_one) == 0, "expecting player_one to have 0 after 0 again Points, they don't");
}

void WimbledonScoreTest::testSetRecordingAndGetting() {
	// because something was broken in the code
	Score::Scorer scorer(K_SCOREWIMBLEDON5);
	// let player_one win the first seven games - then the set results are displayed which breaks - see why
	scorer.addPoint(player_one, 4 * 7);

	// replicate the string creation to see what breaks
	int8_t totalSets = scorer.getSets(player_one) + scorer.getSets(player_two);
	check(totalSets == 1, "expecting a set to be won");
	check(scorer.getSets(player_one) == (int8_t)1, "expecting player one to have won a set");
	check(scorer.getSetGames(player_one, 0) == (int8_t)6, "expecting player one to have won 6 games in the first set");
	check(scorer.getGames(player_one) == (int8_t)1, "expecting player one to have won 1 game in the current set");
	// get the indexes into our local Points arrays
	if (totalSets > 0) {
		// there are previous sets, add this to the string first
		std::stringstream ss;
		ss << ((int)scorer.getSets(player_one)) << "-" << ((int)scorer.getSets(player_two)) << " sets ";
		// show the results of these sets
		for (int8_t i = 0; i < totalSets; ++i) {
			ss << ((int)scorer.getSetGames(player_one, i)) << "-" << ((int)scorer.getSetGames(player_two, i)) << " ";
		}
		check(ss.str() == std::string("1-0 sets 6-0 "), "expecting the score string to be '1-0 sets 6-0'");
	}
}

void WimbledonScoreTest::bringToTie(Score::Scorer& scorer) {
	scorer.addPoint(player_one, 20);	// 5 games
	scorer.addPoint(player_two, 24);	// 6 games
	// and bring to the tie
	scorer.addPoint(player_one, 4);	// 1 game to 6-6

	check(scorer.getIsTiebreak(), "Not tie as expected");
	check(scorer.getGames(player_one) == 6, "not six games as expected");
	check(scorer.getGames(player_two) == 6, "not six games as expected");
	// we are in a tie, serving as follows
	// 1, 2, 1, 2, 1 to 5-0, then 2, 1, 2, 1, 2, 1 to 5-6, then 2 to 6-6 so player one should serve - starting server
}

void WimbledonScoreTest::testTieBreakServing() {
	// first to serve in a tie break is the person to next serve

	// get it to a tie
	Score::Scorer scorer(K_SCOREWIMBLEDON5);
	Player openingServer = scorer.getCurrentServer();
	// bring to a tie - even serves
	bringToTie(scorer);
	check(scorer.getCurrentServer() == openingServer, "server is not first one as expected");
	// play a little tie breaker now
	scorer.addPoint(player_one, 6);		// 6-0 - served as follows 1, 2, 2, 1, 1, 2
	scorer.addPoint(player_two, 5);		// 6-5 - served as follows 2, 1, 1, 2, 2

	//once the tie break is over though - the next to serve is the person who first served in the tie
	scorer.addPoint(player_one, 1);		// 7-5 - player one wins, serving and is next to serve - but player 2 should serve now
	check(scorer.getGames(player_one) == 0, "not zero games as expected");
	check(scorer.getGames(player_two) == 0, "not zero games as expected");
	check(scorer.getSets(player_one) == 1, "not one set for player one as expected");
	check(scorer.getSets(player_two) == 0, "not zero sets for player two as expected");
	check(scorer.getCurrentServer() != openingServer, "server is not second one as expected after the tie");

	// try again, ending with the other player serving
	scorer.resetMatch();
	openingServer = scorer.getCurrentServer();
	// bring to a tie - even serves
	bringToTie(scorer);
	check(scorer.getCurrentServer() == openingServer, "server is not first one as expected");
	// play a little tie breaker now
	scorer.addPoint(player_one, 6);		// 6-0 - served as follows 1, 2, 2, 1, 1, 2
	scorer.addPoint(player_two, 8);		// 6-8 - served as follows 2, 1, 1, 2, 2, 1, 1, 2
	// player two won that
	check(scorer.getGames(player_one) == 0, "not zero games as expected");
	check(scorer.getGames(player_two) == 0, "not zero games as expected");
	check(scorer.getSets(player_two) == 1, "not one set for player two as expected");
	check(scorer.getSets(player_one) == 0, "not zero sets for player one as expected");

	//once the tie break is over though - the next to serve is the person who didn't serve in the tie
	// player two wins, serving and is next to serve also
	check(scorer.getCurrentServer() != openingServer, "server is not second one as expected after the second tie");

	// now let's try it without the starting server starting the tie
	scorer.resetMatch();

	openingServer = scorer.getCurrentServer();
	Player openingReceiver = scorer.getOtherPlayer(openingServer);
	// let the first player win the first set for the second to start serving the next set
	scorer.addPoint(openingReceiver, 4);// 0-1 - served as follows 1
	scorer.addPoint(openingServer, 24);	// 6-1 - served as follows 2, 1, 2, 1, 2, 1
	check(scorer.getGames(player_one) == 0, "not zero games as expected");
	check(scorer.getGames(player_two) == 0, "not zero games as expected");
	check(scorer.getSets(openingServer) == 1, "not one set for server as expected");
	check(scorer.getSets(openingReceiver) == 0, "not zero sets for receiver as expected");
	// now bring to the tie - with player_two starting the set instead of player_one
	bringToTie(scorer);
	check(scorer.getCurrentServer() == openingReceiver, "server is not second one as expected");
	// play a little tie breaker now
	scorer.addPoint(player_one, 6);		// 6-0 - served as follows 2, 1, 1, 2, 2, 1
	scorer.addPoint(player_two, 5);		// 6-5 - served as follows 1, 2, 2, 1, 1

	//once the tie break is over though - the next to serve is the person who first served in the tie
	scorer.addPoint(player_one, 1);		// 7-5 - player one wins, with server two next to serve - but player 1 should serve
	check(scorer.getGames(player_one) == 0, "not zero games as expected");
	check(scorer.getGames(player_two) == 0, "not zero games as expected");
	check(scorer.getSets(player_one) == 2, "not two sets for player one as expected");
	check(scorer.getSets(player_two) == 0, "not zero sets for player two as expected");
	check(scorer.getCurrentServer() == openingServer, "server is not second one as expected after the tie");

	// try again, ending with the other player serving
	scorer.addPoint(openingServer, 4);// 1-0 - served as follows 1
	scorer.addPoint(openingReceiver, 24);	// 1-6 - served as follows 2, 1, 2, 1, 2, 1
	check(scorer.getGames(player_one) == 0, "not zero games as expected");
	check(scorer.getGames(player_two) == 0, "not zero games as expected");
	check(scorer.getSets(player_one) == 2, "not one set for player_one as expected");
	check(scorer.getSets(player_two) == 1, "not one sets for player_two on second set as expected");
	// now bring to the tie - with player_two starting the set instead of player_one
	bringToTie(scorer);
	check(scorer.getCurrentServer() == openingReceiver, "server is not second one as expected second tie");
	// play a little tie breaker now
	scorer.addPoint(player_one, 6);		// 6-0 - served as follows 2, 1, 1, 2, 2, 1
	scorer.addPoint(player_two, 8);		// 6-8 - served as follows 1, 2, 2, 1, 1, 2, 2, 1
	// player two won that
	check(scorer.getGames(player_one) == 0, "not zero games as expected");
	check(scorer.getGames(player_two) == 0, "not zero games as expected");
	check(scorer.getSets(openingServer) == 2, "not two sets for server as expected in second tie");
	check(scorer.getSets(openingReceiver) == 2, "not two sets for receiver as expected");

	//once the tie break is over though - the next to serve is the person who didn't serve in the tie
	// player two wins, but player one was next to serve in the tie, player two should serve the next game though
	check(scorer.getCurrentServer() == openingServer, "server is not second one as expected after the second tie");
}

void WimbledonScoreTest::testUndoPastChangeSides() {
	// there was a problem when you pressed undo past when you changed sides, it asked to change sides
	// on the very next point
	Score::Scorer scorer(K_SCOREWIMBLEDON5);
	Player openingServer = scorer.getCurrentServer();
	Player openingNorth = scorer.getCurrentNorthPlayer();
	// bring to a tie of deuce
	scorer.addPoint(player_one, 3);
	scorer.addPoint(player_two, 3); //40-40
	// and let player one win it
	scorer.addPoint(player_one, 2);
	check(scorer.getGames(player_one) == 1, "player one didn't win game preparing for undo");
	check(scorer.getGames(player_two) == 0, "player one didn't lose game preparing for undo");
	check(scorer.getCurrentServer() != openingServer, "server not changed after one game");
	check(scorer.getCurrentNorthPlayer() != openingNorth, "ends not changed after one game");

	// now play a couple points
	scorer.addPoint(player_one);
	scorer.addPoint(player_two); //15-15
	// undo these in the second game
	scorer.removeLastPoint(); //0-15
	check(scorer.getCurrentServer() != openingServer, "server not changed after undo of a point in second game");
	check(scorer.getCurrentNorthPlayer() != openingNorth, "ends not changed after undo of a point in second game");
	// remove another point
	scorer.removeLastPoint(); //0-0
	check(scorer.getCurrentServer() != openingServer, "server not changed after undo of all points in second game");
	check(scorer.getCurrentNorthPlayer() != openingNorth, "ends not changed after undo of all points in second game");
	// remove past the point of no return - to the previous game
	scorer.removeLastPoint(); //ad-40
	check(scorer.getCurrentServer() == openingServer, "server changed after undon back to the first game");
	check(scorer.getCurrentNorthPlayer() == openingNorth, "ends changed after undon back to the first game");
	// remove another couple
	scorer.removeLastPoint(); //40-40
	scorer.removeLastPoint(); //40-30

	// now let player two win it
	scorer.addPoint(player_two); // 40-40
	check(scorer.getCurrentServer() == openingServer, "server changed after playing last point of undon first game");
	check(scorer.getCurrentNorthPlayer() == openingNorth, "ends changed after playing last point of undon first game");
	scorer.addPoint(player_two); // 40-ad
	check(scorer.getCurrentServer() == openingServer, "server changed after playing another last point of undon first game");
	check(scorer.getCurrentNorthPlayer() == openingNorth, "ends changed after playing another last point of undon first game");
	scorer.addPoint(player_two); // pl2 wins
	check(scorer.getCurrentServer() != openingServer, "server not changed after one game following undo");
	check(scorer.getCurrentNorthPlayer() != openingNorth, "ends not changed after one game following undo");
	check(scorer.getGames(player_two) == 1, "player two didn't win game following undo");
	check(scorer.getGames(player_one) == 0, "player one didn't lose game following undo");
}
