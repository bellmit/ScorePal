/*
 * StackTest.cpp
 *
 *  Created on: 26 Apr 2017
 *      Author: douglasbrain
 */

#include "StringsTest.h"

#include <stdio.h>
#include <stdlib.h>

StringsTest::StringsTest() {
	// constructor
}

StringsTest::~StringsTest() {
	// destructor
}

/*
void PrintString(const char *str) {
    const char *p;
    p = str;
    while (*p) {
        Serial.print(*p);
        p++;
    }
}
*/

int StringsTest::runClassTests() {
	/*
	 * https://hackingmajenkoblog.wordpress.com/2016/02/04/the-evils-of-arduino-strings/
	 */

	// create a string
	char string[30] = "This is a string";
	// new content
	strncpy(string, "New content", sizeof(string));

	// concatenate
	char hi[7] = "Hello ";
	char name[5] = "Fred";
	char all[14] = "";
	strcat(all, hi);
	strncat(all, name, 14);

	char dynString[] = "This is text we can change";
	check(strcmp(dynString, "This is text we can change") == 0, "Dynamic string creation not working");

	const char * constString = "This is text we cannot change (who knows where it is)";
	check(strcmp(constString, "This is text we cannot change (who knows where it is)") == 0, "Const string creation not working");

	char smallBuffer[3];
	// safe string copy - limit to size - 1 for the ending character of zero
	strncpy(smallBuffer, "hello", 2);
	check(strcmp(smallBuffer, "he") == 0, "Small buffer not working properly");


	char stringBuffer[255];
	// define some strings like in the arduino code
	char empty[1];
	char lessThan[2];
	char  greaterThan[2];

	strncpy(stringBuffer, lessThan, 255);
	check(strcmp(stringBuffer, lessThan) == 0, "String copy not working on <");

	strncpy(stringBuffer, greaterThan, 255);
	check(strcmp(stringBuffer, greaterThan) == 0, "String copy not working on >");

	strncpy(stringBuffer, empty, 255);
	check(strcmp(stringBuffer, empty) == 0, "String copy not working on empty");



	return ClassTest::errors;
}
