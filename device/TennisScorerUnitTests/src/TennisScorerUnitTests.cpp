//============================================================================
// Name        : TennisScorerUnitTests.cpp
// Author      : Douglas Brain
// Version     :
// Copyright   : Darker Waters LTD (2017)
// Description : Hello World in C++, Ansi-style
//============================================================================

#include <iostream>

#include "CommsCodeTest.h"
#include "StackTest.h"
#include "WimbledonScoreTest.h"
#include "PointsScoreTest.h"
#include "StringsTest.h"

int main() {
	std::cout << "Running Unit Tests..." << std::endl;

	int testsFailed = 0;
	// run all the tests here and add up the failures
	testsFailed += StackTest().runClassTests();
	testsFailed += CommsCodeTest().runClassTests();
	testsFailed += WimbledonScoreTest().runClassTests();
	testsFailed += PointsScoreTest().runClassTests();
	testsFailed += StringsTest().runClassTests();

	std::cout << std::endl<< std::endl << "Tests completed with " << testsFailed << " errors..." << std::endl;

	return testsFailed;
}

