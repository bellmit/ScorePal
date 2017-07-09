/*
 * LedMatrixDisplay.cpp
 *
 *  Created on: 16 Apr 2017
 *      Author: douglasbrain
 */

#include "LedMatrixDisplay.h"
#include "../Pins.h"

#include "../Lib/MaxMatrix.h"
#include <avr/pgmspace.h>
#include "LedMatrixChars.h"

//#define DEBUG

namespace Display {

#define K_MAXSCREENSINUSE   4  /*change this variable to set how many MAX7219's you'll use in each display*/
#define K_MATRIXDOTWIDTH 	K_MAXSCREENSINUSE*8

MaxMatrix matricies[] = {
	MaxMatrix (K_PIN_LEDDATAF, K_PIN_LEDLOADF, K_PIN_LEDCLOCKF, K_MAXSCREENSINUSE),
	MaxMatrix (K_PIN_DATAB, K_PIN_LOADB, K_PIN_CLOCKB, K_MAXSCREENSINUSE)
};

#define K_ROTATION 3		// 0 for blinky and 3 for combined displays
#define K_LEFT_BORDER 4

#define K_ONE_GAM_COL 		0
#define K_TWO_GAM_COL 		K_MATRIXDOTWIDTH-1

#define K_P1GAMES 0
#define K_P1SETS 1
#define K_P2GAMES 2
#define K_P2SETS 3

// set to true to enable dimming
bool enableDimmer = true;

LedMatrixDisplay::LedMatrixDisplay() {
	// prepare all the members
	is_dirty = true;
	is_invalidate = true;
	is_score_displayed = false;
	is_second_display = true;
	brightness_time = millis();
	brightness_max = 5;
	brightness_current = 5;
}

LedMatrixDisplay::~LedMatrixDisplay() {
	// destructor
}

void LedMatrixDisplay::initialise() {
	// initialise the switch we are testing for auto dimming functionality
	pinMode(K_PIN_DIMMERSW, INPUT_PULLUP);
	// and the LED player displays
	playerDisplay.initialise();
	// and initialise the big LED matrices
	matricies[0].init();
	matricies[0].clear();
	if (is_second_display) {
		matricies[1].init();
		matricies[1].clear();
	}
	// set a nice default brightness
	setBrightness(brightness_current);
	// and intialise the flags and things
	is_invalidate = false;
	is_score_displayed = false;
	time_checkPoint = millis();
#ifdef DEBUG
	Serial.println(F("initialised displays:"));
#endif
}

void LedMatrixDisplay::setBrightnessMax(uint8_t brightnessMax) {
	// this is set from the user's choice on the dial
	if (abs(brightnessMax - brightness_max) > 2) {
		// change this value - it is different...
		brightness_max = brightnessMax;
		// and show the user their selected brightness
		setBrightness(brightness_max, true);
	}
}

void LedMatrixDisplay::setBrightness(uint8_t brightness, bool resetFade) {
	// dot matrix intensity 0-15
#ifdef DEBUG
	if (brightness != brightness_current) {
		Serial.print(F("Setting brightness:"));
		Serial.println(brightness, 10);
	}
#endif
	matricies[0].setIntensity(brightness);
	if (is_second_display) {
		matricies[1].setIntensity(brightness);
	}
	// set the player display too
	playerDisplay.setDisplayBrightness(brightness);
	if (resetFade) {
		// and the time from which we are counting
		brightness_time = millis();
	}
	//else this is called for dimming and we don't want to interfere with that
	//remember the current brightness
	if (brightness == 0 && brightness_current > 0) {
		// this is a fade from 1 to zero, clear the displays
		matricies[0].clear();
		if (is_second_display) {
			matricies[1].clear();
		}
	}
	brightness_current = brightness;
}

uint16_t LedMatrixDisplay::getMatrixDisplayWidth() {
	return K_MATRIXDOTWIDTH;
}

const char* LedMatrixDisplay::getNumberString(int number) {
	itoa(number, temp_number_string, 10);
	return temp_number_string;
}

void LedMatrixDisplay::setContent(uint8_t playerOneGames, uint8_t playerOneSets, uint8_t playerTwoGames, uint8_t playerTwoSets) {
	// first we need to limit the games we are displaying. We only have seven dots so if they have more than that
	// like they might in the last set of a tie-breaker we will have to reduce the number so the dots fit
	while (playerOneGames > 7 || playerTwoGames > 7) {
		// not a nice way to do it, but reliable, keep removing four sets until the dots can be drawn in the
		// space available
		playerOneGames -= 4;
		playerTwoGames -= 4;
	}
	// create the score string
	if (playerOneGames < current_score[K_P1GAMES] ||
		playerOneSets < current_score[K_P1SETS] ||
		playerTwoGames < current_score[K_P2GAMES] ||
		playerTwoSets < current_score[K_P2SETS]) {
		// any reduction in the dots requires an invalidation
		is_invalidate = true;
	}
	// remember this data, will be drawn in the loop where appropriate
	current_score[K_P1GAMES] = playerOneGames;
	current_score[K_P1SETS] = playerOneSets;
	current_score[K_P2GAMES] = playerTwoGames;
	current_score[K_P2SETS] = playerTwoSets;
	// this data is new and different, remember to draw it
	is_dirty = true;
	// and cancel any current message
	current_message.clear();
}

const char* LedMatrixDisplay::getPointString(Score::Scorer& scorer, uint8_t point) {
	if (scorer.isWimbledonPoints() && scorer.getIsTiebreak() == false) {
		// return the string that corresponds to the Points
		switch (point) {
		    case 0:
		    	strncpy(temp_return_string, strings.getString(K_STR_ZERO2), K_TEMP_RETURNSTRINGSIZE);
		    	return temp_return_string;
		    case 1:
		    	strncpy(temp_return_string, strings.getString(K_STR_FIFT), K_TEMP_RETURNSTRINGSIZE);
		    	return temp_return_string;
		    case 2:
		    	strncpy(temp_return_string, strings.getString(K_STR_THIRT), K_TEMP_RETURNSTRINGSIZE);
		    	return temp_return_string;
		    case 3:
		    	strncpy(temp_return_string, strings.getString(K_STR_FORTY), K_TEMP_RETURNSTRINGSIZE);
		    	return temp_return_string;
		    case 4:
		    	strncpy(temp_return_string, strings.getString(K_STR_ADV), K_TEMP_RETURNSTRINGSIZE);
		    	return temp_return_string;
		    default:
		        // if nothing else matches, do the default
		    	break;
		  };
	}
	// put the number into the return string
	if (point < 10) {
		// fill the string with a zero first
		itoa(0, temp_return_string, 10);
		// and add the number to the end of this
		strncat(temp_return_string, getNumberString(point), K_TEMP_RETURNSTRINGSIZE);
	}
	else {
		// just put the number string into the buffer
		itoa(point, temp_return_string, 10);
	}
	// return a pointer to the score string we just constructed
	return temp_return_string;
}

bool LedMatrixDisplay::process(Score::Scorer& scorer) {
	// how long since the last update, seeing how we are timing things
	unsigned long currentTime = millis();
	float timeElapsed = (currentTime - time_checkPoint) / 1000.0;
	bool isProcessBusy = false;
	// and remember this time
	time_checkPoint = currentTime;
	// are we trying to show a message?
	if (is_invalidate) {
		// clear the matrix as requested
		matricies[0].clear();
		if (is_second_display) {
			matricies[1].clear();
		}
		is_invalidate = false;
	}
	if (false == current_message.isCompleted()) {
		// be sure to update the message
		if (current_message.update(timeElapsed)) {
			// the message has moved, show it where it wants to be
			writeString(matricies[0], current_message.getXPosition(), current_message.getYPosition(), current_message.getContentF());
			if (is_second_display) {
				writeString(matricies[1], current_message.getXPosition(), current_message.getYPosition(), current_message.getContentB());
			}
			// the display is busy and would like more processing time please
			isProcessBusy = true;
		}
		if (current_message.isReset() && current_message.isLooping()) {
			// message just reset - invalidate
			is_invalidate = true;
		}
		// always maintain brightness while showing the message
		setBrightness(brightness_max);
		// showing a message, hide the player display
		playerDisplay.setDisplayBrightness(0);
	}
	else if (is_dirty) {
		if (false == is_score_displayed) {
			// we were showing the message, we are not now, clear the matrix
			matricies[0].clear();
			if (is_second_display) {
				matricies[1].clear();
			}
#ifdef DEBUG
			Serial.println(F("Showing current score"));
#endif
		}

		// show the score on the first screen
		int newStringStart;
		newStringStart = writeString(matricies[0], K_LEFT_BORDER, 0, getPointString(scorer, scorer.getPoints(getFirstPlayer())));
		newStringStart = writeString(matricies[0], newStringStart, 0,
				scorer.getIsTrackingServer() ? (scorer.getCurrentServer() == getFirstPlayer() ? strings.getString(K_STR_LESS) : strings.getString(K_STR_GTR)) : strings.getString(K_STR_SPACE));
		newStringStart = writeString(matricies[0], newStringStart, 0, getPointString(scorer, scorer.getPoints(getSecondPlayer())));
		if (is_second_display) {
			// show the score on the back screen
			newStringStart = writeString(matricies[1], K_LEFT_BORDER, 0, getPointString(scorer, scorer.getPoints(getSecondPlayer())));
			newStringStart = writeString(matricies[1], newStringStart, 0,
					scorer.getIsTrackingServer() ? (scorer.getCurrentServer() == getFirstPlayer() ? strings.getString(K_STR_GTR) : strings.getString(K_STR_LESS)) : strings.getString(K_STR_SPACE));
			newStringStart = writeString(matricies[1], newStringStart, 0, getPointString(scorer, scorer.getPoints(getFirstPlayer())));
		}

		// put in the number of sets for the left player
		int valueIndex = is_points_reversed ? K_P2SETS : K_P1SETS;
		for (int i = 0; i < current_score[valueIndex]; ++i) {
			// put the Points on the front screen
			setDotValue(matricies[0], i, 7, 1);
			if (is_second_display) {
				setDotValue(matricies[1], K_MATRIXDOTWIDTH - (1 + i), 7, 1);
			}
		}
		// put in the number of sets for the right player
		valueIndex = is_points_reversed ? K_P1SETS : K_P2SETS;
		for (int i = 0; i < current_score[valueIndex]; ++i) {
			// put the Points on the front screen
			setDotValue(matricies[0], K_MATRIXDOTWIDTH - (1 + i), 7, 1);
			if (is_second_display) {
				setDotValue(matricies[1], i, 7, 1);
			}
		}
		// and the number of games for the left player
		valueIndex = is_points_reversed ? K_P2GAMES : K_P1GAMES;
		for (int i = 0; i < 7 && i < current_score[valueIndex]; ++i) {
			// put the Points on the front screen
			setDotValue(matricies[0], K_ONE_GAM_COL, i, 1);
			if (is_second_display) {
				// put the Points on the back screen
				setDotValue(matricies[1], K_TWO_GAM_COL, i, 1);
			}
		}
		// and the number of games for the right player
		valueIndex = is_points_reversed ? K_P1GAMES : K_P2GAMES;
		for (int i = 0; i < 7 && i < current_score[valueIndex]; ++i) {
			// put the Points on the front screen
			setDotValue(matricies[0], K_TWO_GAM_COL, i, 1);
			if (is_second_display) {
				// put the Points on the back screen
				setDotValue(matricies[1], K_ONE_GAM_COL, i, 1);
			}
		}
		// no longer dirty
		is_dirty = false;
		is_score_displayed = true;
		// set the max brightness each time the score changes - to show the user the score
		setBrightness(brightness_max);
	}
	else {
		// not dirty and no message, if we are auto dimming then we need to dim here
		if (enableDimmer && digitalRead(K_PIN_DIMMERSW) == LOW) {
			// we are auto dimming - so reduce the brightness from the max down to zero here
			unsigned long secondsPassed = ((millis() - brightness_time) / 1000);
			uint8_t newBrightness = max(brightness_max - (uint8_t)secondsPassed, 0);
#ifdef DEBUG
			if (newBrightness != brightness_current) {
				Serial.print(F("Dimming to:"));
				Serial.println(newBrightness, 10);
			}
#endif
			// set this new value of brightness on the display, don't reset the time and max values
			setBrightness(newBrightness, false);
		}
	}
	// set the active player on the player display
	playerDisplay.setDisplayPlayer(scorer.getCurrentNorthPlayer() == player_two);
	// return if we are busy and want more processing, basically showing scrolling text
	return isProcessBusy;
}

const char* LedMatrixDisplay::getScoreSummary(Score::Scorer& scorer, Player leftPlayer) {
	// summarise the score as it currently is (left player first)
	Player rightPlayer = scorer.getOtherPlayer(leftPlayer);
	// summarise the set count
	strcpy(temp_return_string, strings.getString(K_STR_EMPTY));
	uint8_t totalSets = scorer.getSets(leftPlayer) + scorer.getSets(rightPlayer);
	// get the indexes into our local Points arrays
	if (totalSets > 0 && scorer.isShowSets()) {
		// there are previous sets, add this to the string first
		strncat(temp_return_string, getNumberString(scorer.getSets(leftPlayer)), K_TEMP_RETURNSTRINGSIZE);
		strncat(temp_return_string, strings.getString(K_STR_DASH), K_TEMP_RETURNSTRINGSIZE);
		strncat(temp_return_string, getNumberString(scorer.getSets(rightPlayer)), K_TEMP_RETURNSTRINGSIZE);
		strncat(temp_return_string, strings.getString(K_STR_SETS), K_TEMP_RETURNSTRINGSIZE);
		// show the results of these sets
		for (uint8_t i = 0; i < totalSets; ++i) {
			strncat(temp_return_string, getNumberString(scorer.getSetGames(leftPlayer, i)), K_TEMP_RETURNSTRINGSIZE);
			strncat(temp_return_string, strings.getString(K_STR_DASH), K_TEMP_RETURNSTRINGSIZE);
			strncat(temp_return_string, getNumberString(scorer.getSetGames(rightPlayer, i)), K_TEMP_RETURNSTRINGSIZE);
			strncat(temp_return_string, strings.getString(K_STR_SPACE), K_TEMP_RETURNSTRINGSIZE);
		}
	}
	if (scorer.getGames(leftPlayer) + scorer.getGames(rightPlayer) > 0) {
		// and the games then please...
		strncat(temp_return_string, getNumberString(scorer.getGames(leftPlayer)), K_TEMP_RETURNSTRINGSIZE);
		strncat(temp_return_string, strings.getString(K_STR_DASH), K_TEMP_RETURNSTRINGSIZE);
		strncat(temp_return_string, getNumberString(scorer.getGames(rightPlayer)), K_TEMP_RETURNSTRINGSIZE);
	}
	return temp_return_string;
}

bool LedMatrixDisplay::reversePoints() {
	is_points_reversed = !is_points_reversed;
	return is_points_reversed;
}

Player LedMatrixDisplay::getFirstPlayer() {
	if (is_points_reversed) {
		return player_two;
	}
	else {
		return player_one;
	}
}
Player LedMatrixDisplay::getSecondPlayer() {
	if (is_points_reversed) {
		return player_one;
	}
	else {
		return player_two;
	}
}

bool LedMatrixDisplay::isScoreDisplayed() {
	// return if we displayed the score, or the messages are done so it will be displayed
	return is_score_displayed || current_message.isCompleted();
}

int LedMatrixDisplay::getStringLength(const char* string) {
	int stringLength = 0;
	byte buffer[6];
	for (size_t i = 0; i < strlen(string); ++i) {
		char character = string[i];
		// add the width of this char to the length and the blank line after it
		memcpy_P(buffer, getCharBuffer(character), 7);
		stringLength += buffer[0] + 1;
	}
	// also add a space for every character
	return stringLength;
}

int LedMatrixDisplay::writeString(MaxMatrix& matrix, int x, int y, const char* message) {
	int charPosition = x;
	if (brightness_current <= 0 || NULL == message) {
		// don't show anything
		return charPosition;
	}
	byte buffer[6];
	for (size_t i = 0; i < strlen(message); ++i) {
		char character = message[i];
		//as the charset is in PROGMEM we need to copy it out for us to use
		memcpy_P(buffer, getCharBuffer(character), 7);
		charPosition += writeSprite(matrix, charPosition, y, buffer);
		// clear this column as a line of space after the character
		if (charPosition >= 0 && charPosition < K_MATRIXDOTWIDTH) {
			// we are within the bounds of the matrix, clear the column
			for (int j = 0; j < 8; ++j) {
				setDotValue(matrix, charPosition, j, 0);
			}
		}
		// move on the char position to the next location for the loop to start at
		++charPosition;
	}
	// return the new starting position
	return charPosition;
}

int LedMatrixDisplay::writeSprite(MaxMatrix& matrix, int x, int y, const byte sprite[]) {
	int w = sprite[0];
	if (brightness_current <= 0) {
		// don't show anything
		return w;
	}
	int i, j, c, r;
	for (i = 0; i < w; i++) {
		byte charRow = sprite[i+1];
		for (j = 0; j < 8; j++) {
			c = x + i;
			r = y + j;
			if (c >= 0 && c < K_MATRIXDOTWIDTH && r >= 0 && r < 8) {
				byte value = bitRead(charRow, j);
				// this Point is on the screen, set the dot accordingly
				setDotValue(matrix, c, r, value);
			}
		}
	}
	return w;
}

void LedMatrixDisplay::testScreenRotation() {
	int i, j;
	for (j = 0; j < 8; ++j) {
		for (i = 0; i < 32; ++i) {
			matricies[0].setDot(i, j, 1);
			if (is_second_display) {
				matricies[1].setDot(i, j, 1);
			}
			delay(5);
		}
	}
	int newLocation[2];
	for (j = 0; j < 8; ++j) {
		for (i = 0; i < 32; ++i) {
			adjustXY(i, j, newLocation);
			matricies[0].setDot(newLocation[0], newLocation[1], 0);
			if (is_second_display) {
				matricies[1].setDot(newLocation[0], newLocation[1], 0);
			}
			delay(5);
		}
	}
}

void LedMatrixDisplay::setDotValue(MaxMatrix& matrix, int x, int y, byte value) {
	// perform the rotation
	int newLocation[2];
	adjustXY(x, y, newLocation);
	if (brightness_current > 0) {
		// and set the data we want to show
		matrix.setDot(newLocation[0], newLocation[1], value);
	}
}

void LedMatrixDisplay::adjustXY(int& x, int& y, int result[]) {
	int screenNumber = (int)(x / 8);

	if (K_ROTATION == 1) {
		// 90 degrees, flip to left
		result[0] = 7 - y;
		result[1] = x;
	}
	else if (K_ROTATION == 2) {
		result[0] = 7 - x;
		result[1] = 7 - y;
	}
	else if (K_ROTATION == 3) {
		// 90 degrees, flip to right
		result[0] = y + (screenNumber * 8);
		result[1] = ((screenNumber * 8) + 7) - x;
	}
	else {// if (K_ROTATION == 0) {
		result[0] = x;
		result[1] = y;
	}
}

void LedMatrixDisplay::displayMessage(
		const char* contentF,
		const char* contentB,
		int startX, int startY,
		int movementX, int movementY,
		float movementTime,
		float speedSec,
		float existTime,
		bool isCancelable) {
#ifdef DEBUG
	Serial.print(F("Showing message:"));
	Serial.print(contentF);
	Serial.println();
#endif
	// push the message onto the stack to show it in our loop
	current_message.setContent(contentF, contentB, startX, startY, movementX, movementY, movementTime, speedSec, existTime, isCancelable);
	// don't show the score
	is_score_displayed = false;
	// and we are dirty - not showing the score
	is_dirty = true;
	// invalidate the screens
	is_invalidate = true;
}

const byte* LedMatrixDisplay::getCharBuffer(char& character) {
#ifdef K_REDUCED_CHARSET
	switch (character) {
	case '0' :
		return CHARSET_NUMBERS[0];
	case '1' :
		return CHARSET_NUMBERS[1];
	case '2' :
		return CHARSET_NUMBERS[2];
	case '3' :
		return CHARSET_NUMBERS[3];
	case '4' :
		return CHARSET_NUMBERS[4];
	case '5' :
		return CHARSET_NUMBERS[5];
	case '6' :
		return CHARSET_NUMBERS[6];
	case '7' :
		return CHARSET_NUMBERS[7];
	case '8' :
		return CHARSET_NUMBERS[8];
	case '9' :
		return CHARSET_NUMBERS[9];
	case '.' :
		return CHARSET_NUMBERS[10];
	case ' ' :
		return CHARSET_NUMBERS[11];
	case '<' :
		return CHARSET_NUMBERS[12];
	case '=' :
		return CHARSET_NUMBERS[13];
	case '>' :
		return CHARSET_NUMBERS[14];
	case '-' :
		return CHARSET_NUMBERS[15];
	case '$' :
		return CHARSET_NUMBERS[16];
	default:
		int index = character - 'a';
		if (index < 26 && index > -1) {
			// this is a lower-case letter
			return CHARSET_LETTERS[index];
		}
		else {
			// not a letter - do something else
			index = character - 'A';
			if (index < 26 && index > -1) {
				// this is a capital letter, show lower case
				return CHARSET_LETTERS[index];
			}
			else {
				// show error '$'
				return CHARSET_NUMBERS[16];
			}
		}
	}
#else
	// return the raw position in the full char set array
	return (const byte*)(CHARSET + 6*(character - 32));
#endif //K_REDUCED_CHARSET
}

} /* namespace Display */
