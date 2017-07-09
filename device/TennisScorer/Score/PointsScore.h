/*
 * PointsScore.h
 *
 *  Created on: 16 Apr 2017
 *      Author: douglasbrain
 */

#ifndef PointSSCORE_H_
#define PointSSCORE_H_

#include "BaseScore.h"
#include "MatchState.h"

namespace Score {

class PointsScore {
public:
	PointsScore();
	virtual ~PointsScore();

	void addPoint(Player player, MatchState& state, BaseScore& scorer);
};

} // end namespace

#endif /* PointSSCORE_H_ */
