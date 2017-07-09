/*
 * Stack.h
 *
 *  Created on: 18 Apr 2017
 *      Author: douglasbrain
 */

#ifndef STACK_H_
#define STACK_H_

#ifdef ARDUINO
#include <Arduino.h>
#else
#include <Stdint.h>
#endif

template <typename T> class Stack {
#define K_STACK_SIZE 10
public:
	Stack() {
		// setup the members
		top = -1;
	}
	virtual ~Stack() {
		// destructor
	}

	void push (T item) {
		// push something new to the stack
		if (top == (K_STACK_SIZE - 1)) {
			// we are full, push them all down one
			for (int8_t i = 0; i < top; ++i) {
				// for every item in the stack, Point it at the later one
				stack[i] = stack[i + 1];
			}
		}
		else {
			// increase the top
			++top;
		}
		// set the top item to be a copy of that passed us
		stack[top] = T(item);
	}

	int8_t getNoItems() {
		// return the active count of items in the stack, so can while loop it down...
		return top + 1;
	}

	int8_t clear() {
		int8_t ret = getNoItems();
		top = -1;
		return ret;
	}

	T pop (T voidValue) {
		// pop the last from the stack
		T ret;
		if (top == -1) {
			// no more left on the stack, returning the void value that was passed in
			ret = voidValue;
		} else {
			// get the item to return
			ret = stack [top];
			--top;
		}
		// return a copy of the status we had
		return ret;
	}

private:
	T stack[K_STACK_SIZE];
	int8_t top;
};

#endif /* STACK_H_ */
