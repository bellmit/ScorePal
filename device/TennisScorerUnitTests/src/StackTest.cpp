/*
 * StackTest.cpp
 *
 *  Created on: 26 Apr 2017
 *      Author: douglasbrain
 */

#include "StackTest.h"
#include "../../TennisScorer/Utils/Stack.h"

StackTest::StackTest() {
	// constructor

}

StackTest::~StackTest() {
	// destructor
}


int StackTest::runClassTests() {
	Stack<int> stack;
	for (int i = 0; i < K_STACK_SIZE; ++i) {
		stack.push(i);
	}
	int counter = K_STACK_SIZE-1;
	while (stack.getNoItems() > 0) {
		int item = stack.pop(-1);
		if (item != counter--) {
			std::cout << "Stack item should be " << counter << " but it is " << item << " instead..." << std::endl;
		}
	}
	return errors;
}
