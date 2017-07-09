/*
 * LedMatrixDisplay.h
 *
 *  Created on: 16 Apr 2017
 *      Author: douglasbrain
 */

#ifndef DISPLAY_LEDMATRIXDISPLAY_H_
#define DISPLAY_LEDMATRIXDISPLAY_H_

#include "StringMessage.h"
#include "../Score/Scorer.h"
#include "LedPlayerDisplay.h"

#include "../Lib/MaxMatrix.h"
#include <avr/pgmspace.h>
#include "../Player.h"
#include "Strings.h"

namespace Display {

#define K_TEMP_RETURNSTRINGSIZE 63
#define K_TEMP_NUMBERSTRINGSIZE 7

class LedMatrixDisplay {
public:
	LedMatrixDisplay();
	virtual ~LedMatrixDisplay();

	void initialise();
	bool process(Score::Scorer& scorer);

	void setContent(uint8_t oneGames, uint8_t oneSets, uint8_t twoGames, uint8_t twoSets);
	void displayMessage(const char* contentF,
						const char* contentB,
						int startX = 0, int startY = 9,
						int movementX = 0, int movementY = -1,
						float movementTime = 3.0,
						float speedSec = 3.0,
						float existTime = 6.0,
						bool isCancelable = true);

	bool isScoreDisplayed();
	const char* getScoreSummary(Score::Scorer& scorer, Player leftPlayer);

	bool reversePoints();
	Player getFirstPlayer();
	Player getSecondPlayer();

	int getStringLength(const char* string);
	void testScreenRotation();

	uint16_t getMatrixDisplayWidth();
	void setBrightnessMax(uint8_t brightnessMax);

protected:
	void setBrightness(uint8_t brightness, bool resetFade = true);
	const char* getPointString(Score::Scorer& scorer, uint8_t point);
	int writeString(MaxMatrix& matrix, int x, int y, const char* message);

private:
	const byte* getCharBuffer(char& character);
	const char* getNumberString(int number);
	int writeSprite(MaxMatrix& matrix, int x, int y, const byte sprite[]);
	void setDotValue(MaxMatrix& matrix, int x, int y, byte value);
	void adjustXY(int& x, int& y, int result[]);
	StringMessage current_message = StringMessage(strings.getString(K_STR_EMPTY), strings.getString(K_STR_EMPTY), 0, 0, 0.0, 0.0, 0.0, 0.0);
	bool is_dirty = true;
	bool is_points_reversed = false;
	uint8_t current_score [4] = {0,0,0,0};
	bool is_second_display = true;
	bool is_score_displayed = false;
	bool is_invalidate = true;
	unsigned long brightness_time;
	uint8_t brightness_current;
	uint8_t brightness_max;
	unsigned long time_checkPoint = 0UL;
	LedPlayerDisplay playerDisplay;
	char temp_return_string[K_TEMP_RETURNSTRINGSIZE + 1]; // add one to the size of the null char at the end
	char temp_number_string[K_TEMP_NUMBERSTRINGSIZE + 1]; // add one to the size of the null char at the end
public:
	Strings strings;
};

} /* namespace Display */

#endif /* DISPLAY_LEDMATRIXDISPLAY_H_ */
