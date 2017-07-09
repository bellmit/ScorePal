/*
 * FastFourScore.h
 *
 *  Created on: 16 Apr 2017
 *      Author: douglasbrain
 */

#ifndef FASTFOURSCORE_H_
#define FASTFOURSCORE_H_

#include "BaseScore.h"
#include "MatchState.h"

namespace Score {

class FastFourScore {
public:
	FastFourScore();
	virtual ~FastFourScore();

	bool isMatchOver(MatchState& state);
	void addPoint(Player player, MatchState& state, BaseScore& scorer);

protected:
	void addGame(Player player, MatchState& state, BaseScore& scorer);

};

} // end namespace

#endif /* FASTFOURSCORE_H_ */
