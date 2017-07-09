/*
 * ScoreInput.h
 *
 *  Created on: 22 Apr 2017
 *      Author: douglasbrain
 */

#ifndef SCOREINPUT_H_
#define SCOREINPUT_H_

#include "Beep.h"
#include "../Communications/InputReceiver.h"
#include "../Communications/PhoneConnection.h"
#include "../Score/Scorer.h"

namespace Interactions {

class ScoreInput {
public:
	ScoreInput();
	virtual ~ScoreInput();

	void setup(Score::Scorer* scorerInUse);
	bool process(Beep& beeper, Communications::PhoneConnection& phone);

private:
	bool processActionCode(int8_t actionCode, Beep& beeper);
	Score::Scorer* scorer;
	Communications::InputReceiver receiver;

};

} /* namespace Interactions */

#endif /* SCOREINPUT_H_ */
