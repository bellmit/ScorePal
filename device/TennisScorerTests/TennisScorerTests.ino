#include "Arduino.h"

#include "../TennisScorer/Display/LedMatrixDisplay.h"
#include "../TennisScorer/Display/StringMessage.h"
#include "../TennisScorer/Interactions/Beep.h"
#include "../TennisScorer/Score/Scorer.h"
#include "../TennisScorer/Interactions/ScoreInput.h"
#include "../TennisScorer/Communications/MacAddress.h"
#include "../TennisScorer/Communications/PhoneConnection.h"

//#include "../TennisScorer/Lib/MemoryFree.h"

Display::LedMatrixDisplay matrix;
Interactions::Beep beeper;
Score::Scorer scorer;
Interactions::ScoreInput score_input;
Communications::PhoneConnection phone;

unsigned long loopCounter = 0;
int free_memory = 0;
bool isPhoneUpToDate = false;

unsigned long serialOutTime;

//The setup function is called once at startup of the sketch
void setup() {
	// setup serial communication for debugging etc
	Serial.begin(9600);
	// setup the Displays
	Serial.println(F("initialising matrix..."));
	matrix.initialise();
	// initialise phone
	Serial.println(F("initialising phone..."));
	phone.initialise();
	// setup the beeper
	Serial.println(F("initialising beeper..."));
	beeper.setup();
	// setup the input
	Serial.println(F("initialising input..."));
	score_input.setup(&scorer);

	serialOutTime = millis();
}

// The loop function is called in an endless loop
void loop() {
	//Add your repeated code here
	/*int availableMem = freeMemory();
	if (free_memory != availableMem) {
		Serial.print(F("freeMemory()="));
		Serial.println(availableMem);
		free_memory = availableMem;
	}*/
	// say hello?
	if (loopCounter == 0) {
		// print the MAC address so we can pair the remote with us
		Serial.print(F("MAC: "));
		Communications::MacAddress mac;
		mac.printToSerial();
		Serial.println();
		uint8_t buffer[6];
		mac.getAddress(buffer);
		for (uint8_t i = 0; i < 6; ++i) {
			Serial.print(buffer[i], 10);
			Serial.print(F(":"));
		}
		Serial.println();
		// first time through - test the matrix
		matrix.testScreenRotation();
		char frontBuffer[6];
		char backBuffer[6];
		if (matrix.getFirstPlayer() == player_one) {
			matrix.strings.getString(frontBuffer, K_STR_PLYR1);
			matrix.strings.getString(backBuffer, K_STR_PLYR2);
		}
		else {
			matrix.strings.getString(frontBuffer, K_STR_PLYR2);
			matrix.strings.getString(backBuffer, K_STR_PLYR1);
		}
		// first time through - say hello
		matrix.displayMessage(frontBuffer, backBuffer);
	}
	else if (millis() - serialOutTime > 5000) {
		Serial.println(F("Hello"));
		serialOutTime = millis();
		//matrix.testScreenRotation();
	}

	if (loopCounter % 2000 == 0) {
		scorer.addPoint(random(2));
	}
	// loop the matrix then
	if (false == matrix.process(scorer)) {
		// the matrix is relaxed, let's do our other stuff here
		// like checking our score to display it all okay
		checkForScore();
		// add a load of random points to test the application
		scorer.addPoint(random(2));
	}
	// inc the loop counter used to test things
	++loopCounter;
}

void checkForScore() {
	// check the serial input for a score having taken place
	int lastPointChange = scorer.getLastPointChange();
	if (lastPointChange != 0) {
		// set the score content on the matrix, the points changed since we last checked
		matrix.setContent(
				scorer.getGames(player_one),
				scorer.getSets(player_one),
				scorer.getGames(player_two),
				scorer.getSets(player_two));
		if (lastPointChange > 0) {
			// the last change was an addition of points, we can show messages for this as it is not an undo action
			// first check if we want to change sides?
			if (scorer.getCurrentNorthPlayer() != matrix.getFirstPlayer()) {
				//need to inform the players that they need to swap sides, and remember this so that we reverse the scores
				beeper.cheep(3);
				matrix.reversePoints();
			}
			if (scorer.isMatchOver()) {
				// someone has won, reset the match here
				beeper.cheep(5);
				scorer.resetMatch();
			}
		}
		// remember that the status on the phone will be out-of-date now the score changed
		isPhoneUpToDate = false;
	}
	else if (matrix.isScoreDisplayed()) {
		// check for any user input now we are all done
		if (false == score_input.process(beeper, phone)) {
			// the matrix was not busy and there was no sore inputted, we have time to handle some
			// other process here then, manage the saving of data
			// seeing how we have some time, we can return the state of this game to the phone, if connected
			if (false == isPhoneUpToDate && phone.sendGameStatus(scorer, false)) {
				// phone has the latest game status
				isPhoneUpToDate = true;
			}
		}
	}
}

