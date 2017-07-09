/*
 * WimbledonScore.h
 *
 *  Created on: 16 Apr 2017
 *      Author: douglasbrain
 */

#ifndef WIMBLEDONSCORE_H_
#define WIMBLEDONSCORE_H_

#include "BaseScore.h"
#include "MatchState.h"

namespace Score {

class WimbledonScore {
public:
	WimbledonScore();
	virtual ~WimbledonScore();

	bool isMatchOver(uint8_t noSets, MatchState& state);

	void addPoint(Player player, uint8_t noSets, MatchState& state, BaseScore& scorer);
	void addGame(Player player, uint8_t noSets, MatchState& state, BaseScore& scorer);

};

} // end namespace

#endif /* WIMBLEDONSCORE_H_ */
