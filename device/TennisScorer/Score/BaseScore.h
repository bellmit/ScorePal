/*
 * BaseScore.h
 *
 *  Created on: 17 May 2017
 *      Author: douglasbrain
 */

#ifndef BASESCORE_H_
#define BASESCORE_H_

#include "../Player.h"

namespace Score {

class BaseScore {
public:
	BaseScore() {

	}
	virtual ~BaseScore() {

	}
	Player getOtherPlayer(Player player) {
		if (player == player_one) {
			return player_two;
		}
		else {
			return player_one;
		}
	}
	virtual void swapServers() = 0;
	virtual void swapEnds() = 0;
	virtual void addGame(Player player) = 0;
	virtual void recordSetWon() = 0;
	virtual void recordGameWon() = 0;
};

} /* namespace Score */

#endif /* BASESCORE_H_ */
