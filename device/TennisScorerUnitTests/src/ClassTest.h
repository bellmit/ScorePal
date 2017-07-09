/*
 * ClassTests.h
 *
 *  Created on: 16 Apr 2017
 *      Author: douglasbrain
 */

#ifndef CLASSTEST_H_
#define CLASSTEST_H_

#include <string>
#include <iostream>

class ClassTest {
public:
	ClassTest();
	virtual ~ClassTest();

	virtual int runClassTests() = 0;

protected:
	void error(std::string str);
	void errorFatal(std::string str);
	void checkFatal(bool condition, std::string reason);
	void check(bool condition, std::string reason);
	int errors;
	int isDots;
};

#endif /* CLASSTEST_H_ */
