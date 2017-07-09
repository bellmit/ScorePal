/*
 * ClassTests.cpp
 *
 *  Created on: 16 Apr 2017
 *      Author: douglasbrain
 */

#include "ClassTest.h"

ClassTest::ClassTest() {
	// constructor
	errors = 0;
	isDots = -1;
}

ClassTest::~ClassTest() {
	// destructor
}

void ClassTest::error(std::string str) {
	if (isDots >= 0) {
		std::cout << std::endl;
		isDots = -1;
	}
	std::cout << "ERROR! " << str << std::endl;
	++errors;
}
void ClassTest::check(bool condition, std::string str) {
	if (false == condition) {
		error(str);
	}
	else {
		++isDots;
		if (isDots > 8000) {
			std::cout << std::endl;
			isDots = 0;
		}
		else if (isDots % 100 == 0) {
			std::cout << ".";
		}
	}
}
