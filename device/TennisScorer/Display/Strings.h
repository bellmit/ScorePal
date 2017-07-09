/*
 * const const char* s.h
 *
 *  Created on: 30 Apr 2017
 *      Author: douglasbrain
 */

#ifndef STRINGS_H_
#define STRINGS_H_

#include <avr/pgmspace.h>

namespace Display {
#define K_STR_EMPTY		0
#define K_STR_LESS		1
#define K_STR_GTR		2
#define K_STR_DASH		3
#define K_STR_SPACE		4
#define K_STR_SETS		5
#define K_STR_GAMES		6
#define K_STR_ZERO		7
#define K_STR_ZERO2		8
#define K_STR_FIFT		9
#define K_STR_THIRT		10
#define K_STR_FORTY		11
#define K_STR_ADV		12
#define K_STR_WIN		13
#define K_STR_LOSE		14
#define K_STR_CHANGE	15
#define K_STR_PLYR1		16
#define K_STR_PLYR2		17
#define K_STR_ITF5		18
#define K_STR_ITF3		19
#define K_STR_BAD5		20
#define K_STR_BAD3		21
#define K_STR_POINTS	22
#define K_STR_FAST4		23

#define K_STR_MAXSIZE   29

// and all our strings
const char  empty[] PROGMEM =  "";
const char  lessThan[] PROGMEM =  "<";
const char  greaterThan[] PROGMEM = ">";
const char  dash[] PROGMEM = "-";
const char  space[] PROGMEM = " ";
const char  sets[] PROGMEM = " sets ";
const char  games[] PROGMEM = " games ";
const char  zero[] PROGMEM = "0";
const char  zerozero[] PROGMEM = "00";
const char  fifteen[] PROGMEM = "15";
const char  thirty[] PROGMEM = "30";
const char  forty[] PROGMEM = "40";
const char  advantage[] PROGMEM = "ad";
const char  congratulations[] PROGMEM = "... congrats.     ";	// make these the same width as shown at the same time
const char  commiserations[] PROGMEM  = "... sorry....     ";	// make these the same width as shown at the same time
const char  changeEnds[] PROGMEM = 		"... change ends...";
const char  playerOne[] PROGMEM = "plyr 1";
const char  playerTwo[] PROGMEM = "plyr 2";
const char  wimbledon5[] PROGMEM = "  5 Set Tennis";
const char  wimbledon3[] PROGMEM = "  3 Set Tennis";
const char  badminton5[] PROGMEM = "  5 Game Badminton";
const char  badminton3[] PROGMEM = "  3 Game Badminton";
const char  points[] PROGMEM = "  Points";
const char  fast4[] PROGMEM = "  Fast 4";

// Then set up a table to refer to your strings.
const char* const string_table[] PROGMEM = {
		empty, lessThan, greaterThan, dash, space, sets, games,
		zero, zerozero, fifteen, thirty, forty, advantage,
		congratulations, commiserations, changeEnds, playerOne, playerTwo,
		wimbledon5, wimbledon3, badminton5, badminton3, points, fast4};

class Strings {
public:
	Strings() { /* constructor */ };
	virtual ~Strings() { /* destructor */ };

	const char* getString(int id) {
		// they don't have a buffer, use ours instead
		return getString(temp_buffer, id);
	}

	const char* getString(char* buffer, int id) {
		// Necessary casts and dereferencing, just copy.
		strcpy_P(buffer, (char*)pgm_read_word(&(string_table[id])));
		return buffer;
	}

private:
	char temp_buffer[K_STR_MAXSIZE + 1];    // make sure this is large enough for the largest string it must hold


};

} /* namespace Display */


#endif /*STRINGS_H_ */
