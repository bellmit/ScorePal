/*
 * StringMessage.h
 *
 *  Created on: 5 May 2017
 *      Author: douglasbrain
 */

#ifndef STRINGMESSAGE_H_
#define STRINGMESSAGE_H_

namespace Display {

#define K_CONTENTMESSAGESIZE 63

class StringMessage {
public:
	StringMessage(const char* contentF,
			const char* contentB,
			int startX = 0, int startY = 9,
			int movementX = 0, int movementY = -1,
			float movementTime = 3.0,
			float speedSec = 3.0,
			float existTime = 6.0,
			bool isCancelable = true);

	virtual ~StringMessage();

	void setContent(const char* contentF, const char* contentB,
			int startX = 0, int startY = 9,
			int movementX = 0, int movementY = -1,
			float movementTime = 3.0,
			float speedSec = 3.0,
			float existTime = 6.0,
			bool isCancelable = true);

	void resetPosition();
	void clear(bool force = false);
	bool isReset();
	bool isLooping();
	void setContentStrings(const char* contentF, const char* contentB);
	bool update(float timeElapsedSec);
	int getXPosition();
	int getYPosition();
	bool isCompleted();
	void setIsCancelable(bool isCancelable = true) {is_cancelable = isCancelable;}
	const char* getContentF();
	const char* getContentB();

private:
	char content_messageF[K_CONTENTMESSAGESIZE + 1]; // add one for the ending null char
	char content_messageB[K_CONTENTMESSAGESIZE + 1]; // add one for the ending null char

	int movement_vector[2];
	int last_position[2];
	float current_position[4];
	float speed_seconds;
	float time_to_exist;
	float time_to_move;
	float time_existed;
	bool is_cancelable;
};

} /* namespace Display */

#endif /* STRINGMESSAGE_H_ */
