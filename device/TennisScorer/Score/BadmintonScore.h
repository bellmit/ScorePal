/*
 * BadmintonScore.h
 *
 *  Created on: 16 Apr 2017
 *      Author: douglasbrain
 */

#ifndef BADMINTONSCORE_H_
#define BADMINTONSCORE_H_

#include "BaseScore.h"
#include "MatchState.h"

namespace Score {

class BadmintonScore {
public:
	BadmintonScore();
	virtual ~BadmintonScore();

	bool isMatchOver(uint8_t noGames, MatchState& state);
	void addPoint(Player player, uint8_t noGames, MatchState& state, BaseScore& scorer);

protected:
	void addGame(Player player, uint8_t noGames, MatchState& state, BaseScore& scorer);
};

} // end namespace

#endif /* BADMINTONSCORE_H_ */
