/*
 * Scorer.h
 *
 *  Created on: 17 May 2017
 *      Author: douglasbrain
 */

#ifndef SCORER_H_
#define SCORER_H_

#include "../Player.h"
#include "../Utils/Stack.h"
#include "MatchState.h"
#include "WimbledonScore.h"
#include "BadmintonScore.h"
#include "FastFourScore.h"
#include "PointsScore.h"
#include "BaseScore.h"

#define K_SCOREWIMBLEDON5	1
#define K_SCOREWIMBLEDON3	2
#define K_SCOREBADMINTON3	3
#define K_SCOREBADMINTON5	4
#define K_SCOREPOINTS		5
#define K_SCOREFAST4		6

namespace Score {

#define K_POINTSTORESIZE 75

class Scorer : public Score::BaseScore {
public:
	Scorer(uint8_t scoreMode = K_SCOREWIMBLEDON5);
	~Scorer();

	void setScoreMode(uint8_t scoreMode);

	Player getCurrentServer();
	Player getCurrentNorthPlayer();
	bool getIsTrackingServer();

	bool isMatchOver();
	Player getMatchWinner();
	void resetMatch();

	uint8_t getPoints(Player player);
	uint8_t getGames(Player player);
	uint8_t getSetGames(Player player, uint8_t set);
	uint8_t getSets(Player player);
	bool isWimbledonPoints();
	bool isShowSets();

	int8_t getLastPointChange();
	void addPoint(Player player, uint8_t noPoints);
	void addPoint(Player player);
	bool removeLastPoint();

	Player getHistoricPoint(uint16_t pointIndex);
	uint16_t getNumberHistoricPoints();

	uint16_t getTotalPoints(Player player);

	bool getIsTiebreak() {return current_state.is_tiebreak;}
	int8_t getNoPreviousStates() {return previous_states.getNoItems();}

	virtual void addGame(Player player);
	virtual void recordSetWon();
	virtual void recordGameWon();
	void swapServers();
	void swapEnds();

	unsigned long startMillis() {return start_millis;}
	void setStartMillis(unsigned long start) {start_millis = start;}

	void setIsDirty(bool isAddition);
	bool getIsShowMessages() {return is_show_messages;}
	uint8_t getCurrentScoreMode() {return score_mode;}

private:
	uint8_t point_change;
	uint16_t point_store[K_POINTSTORESIZE]; // most points ever recorded 980 (Isner / Mahut) so 16 per int so 75 should be fine (1200)
	int16_t point_store_index;
	uint16_t points_total[players_count];	// the total points per player in the match

	bool is_show_messages;
	unsigned long start_millis;

	uint8_t score_mode;

	MatchState current_state;
	Stack<MatchState> previous_states;

	FastFourScore fast4;
	WimbledonScore wimbledon;
	BadmintonScore badminton;
	PointsScore points;
};

} /* namespace Score */

#endif /* SCORER_H_ */
