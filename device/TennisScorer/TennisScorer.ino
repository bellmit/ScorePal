#include "Arduino.h"

#define PRINT_MAC
//#define PWR_MONITOR

#include "Score/Scorer.h"
#include "Interactions/ScoreInput.h"
#include "Communications/MacAddress.h"
#include "Communications/PhoneConnection.h"
#ifdef PWR_MONITOR
#include "Communications/VoltageMonitor.h"
#endif

#include "Display/LedMatrixDisplay.h"
#include "Display/StringMessage.h"
#include "Interactions/Beep.h"
#include "Interactions/SettingsInput.h"

bool isFirstLoop = true;
Display::LedMatrixDisplay matrix;
Interactions::Beep beeper;
Interactions::SettingsInput settings;
Score::Scorer scorer;
Interactions::ScoreInput score_input;
Communications::PhoneConnection phone;
#ifdef PWR_MONITOR
Communications::VoltageMonitor power;
#endif

float messageScrollSpeed = 8.0;
bool isPhoneScoreDifferent = false;
bool isShowingWinningMessage = false;
uint8_t current_score_mode = K_SCOREWIMBLEDON5;
uint8_t displayed_score_mode = K_SCOREWIMBLEDON5;


// the setup routine runs once when you press reset:
void setup() {
	// setup serial communication for debugging etc
	Serial.begin(9600);
	Serial.println(F("initialising sound..."));
	beeper.setup();
	Serial.println(F("initialising settings..."));
	settings.setup();
	// initialise matrix
	Serial.println(F("initialising matrix..."));
	matrix.initialise();
	// initialise phone
	Serial.println(F("initialising phone..."));
	phone.initialise();
#ifdef PWR_MONITOR
	// initialise power monitor
	Serial.println(F("initialising power..."));
	power.initialise();
#endif
	// initialise score
	Serial.print(F("initialising score to mode "));
	// and set this up with the input reader
	score_input.setup(&scorer);
	// and now get the current score mode to initialise it
	current_score_mode = settings.readMode() + 1;
	displayed_score_mode = current_score_mode;
	scorer.setScoreMode(current_score_mode);
	Serial.print(current_score_mode, 10);
	Serial.println(F("..."));
	// setup the Displays
	Serial.println(F("initialised successfully..."));
}

// the loop routine runs over and over again forever:
void loop() {
	// setup the device for all the settings right away, setting the brightness and the mode
	uint8_t brightness = settings.readBrightness() + 1;
	matrix.setBrightnessMax(brightness);
	// say hello?
	if (isFirstLoop) {
		Serial.print(F("Brightness set to: "));
		Serial.println(brightness);
#ifdef PRINT_MAC
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
#endif
		// first time through - say hello
		char frontBuffer[8];
		char backBuffer[8];
		if (matrix.getFirstPlayer() == player_one) {
			matrix.strings.getString(frontBuffer, K_STR_PLYR1);
			matrix.strings.getString(backBuffer, K_STR_PLYR2);
		}
		else {
			matrix.strings.getString(frontBuffer, K_STR_PLYR2);
			matrix.strings.getString(backBuffer, K_STR_PLYR1);
		}
		// create the message here with the correct strings
		matrix.displayMessage(frontBuffer, backBuffer, 0, 9, 0, -1, 3, 3, 6, false);
		beeper.cheep(3);
		// this message will overwrite any winners message
		isShowingWinningMessage = false;
		//TODO recall the last match now from the phone?
	}
	// loop the matrix then
	if (false == matrix.process(scorer)) {
		// the matrix is relaxed, let's do our other stuff here
		// like checking our score to display it all okay
		checkForScore();
	}
	else if (scorer.isMatchOver()) {
		// will always be processing the match over status, need to send the final
		// score change to the phone though
		checkForScore();
	}
	// completed our first loop
	isFirstLoop = false;
}

int checkForScore() {
	// check for the winners message
	if (isShowingWinningMessage && false == scorer.isMatchOver()) {
		// we are showing the winners message, but the match isn't over (undo occurred)
		// get rid of that message
		Serial.println(F("Match no longer over"));
		matrix.displayMessage(NULL, NULL, 0, 0, 0, 0, 0, 0.0, 0.0, true);
		isShowingWinningMessage = false;
	}
	if (scorer.isMatchOver()) {
		if (false == isShowingWinningMessage) {
			Serial.println(F("Match over"));
			// we are not showing the winning message but the match is over, show the message
			char messageBufferF[64];
			char messageBufferB[64];
			// someone has won, reset the match here, create the string for the first player first
			strncpy(messageBufferF, matrix.getScoreSummary(scorer, matrix.getFirstPlayer()), 63);
			strncpy(messageBufferB, matrix.getScoreSummary(scorer, matrix.getSecondPlayer()), 63);
			// add the message to the end
			if (scorer.getMatchWinner() == matrix.getFirstPlayer()) {
				strncat(messageBufferF, matrix.strings.getString(K_STR_WIN), 63);
				strncat(messageBufferB, matrix.strings.getString(K_STR_LOSE), 63);
			}
			else {
				strncat(messageBufferF, matrix.strings.getString(K_STR_LOSE), 63);
				strncat(messageBufferB, matrix.strings.getString(K_STR_WIN), 63);
			}
			int messageLength = matrix.getStringLength(messageBufferF);
			//beeper.cheep(3);
			matrix.displayMessage(messageBufferF, messageBufferB,
					(int)matrix.getMatrixDisplayWidth(), 0, -1, 0,
					messageLength / messageScrollSpeed,
					messageScrollSpeed,
					-1,
					false);	// loop forever and don't let them cancel
			isShowingWinningMessage = true;
		}
	}
	// check the serial input for a score having taken place
	int lastPointChange = scorer.getLastPointChange();
	if (lastPointChange != 0) {
		// remember that the status on the phone will be out-of-date now the score changed
		phone.incrementSendingCode();
		isPhoneScoreDifferent = true;
	}
	if (lastPointChange != 0 && false == scorer.isMatchOver()) {
		// set the score content on the matrix, the points changed since we last checked and the match isn't over
		matrix.setContent(
				scorer.getGames(player_one),
				scorer.isShowSets() ? scorer.getSets(player_one) : 0,
				scorer.getGames(player_two),
				scorer.isShowSets() ? scorer.getSets(player_two) : 0);
		if (lastPointChange > 0) {
			// the last change was an addition of points, we can show messages for this as it is not an undo action
			if (scorer.getCurrentNorthPlayer() != matrix.getFirstPlayer() && scorer.getIsShowMessages() && false == scorer.isMatchOver()) {
				//need to inform the players that they need to swap sides, and remember this so that we reverse the scores
				char messageBufferF[64];
				char messageBufferB[64];
				// someone has won, reset the match here, create the string for the first player first
				strncpy(messageBufferF, matrix.getScoreSummary(scorer, matrix.getFirstPlayer()), 63);
				strncpy(messageBufferB, matrix.getScoreSummary(scorer, matrix.getSecondPlayer()), 63);
				// add the change message to the end
				strncat(messageBufferF, matrix.strings.getString(K_STR_CHANGE), 63);
				strncat(messageBufferB, matrix.strings.getString(K_STR_CHANGE), 63);
				int messageLength = matrix.getStringLength(messageBufferF);
				// show this message
				//beeper.cheep(3);
				matrix.displayMessage(messageBufferF, messageBufferB,
						(int)matrix.getMatrixDisplayWidth(), 0, -1, 0,
						messageLength / messageScrollSpeed,
						messageScrollSpeed,
						(messageLength / messageScrollSpeed) + 3,
						true);
				matrix.reversePoints();
				// this message will overwrite any winners message
				isShowingWinningMessage = false;
			}
		}
		else if (lastPointChange < 0) {
			// this is a point removal, we might not want to show the change ends message, but we do want to change ends
			if (scorer.getCurrentNorthPlayer() != matrix.getFirstPlayer()) {
				// the ends were changed in this undo action, reverse them on the display
				matrix.reversePoints();
			}
		}
	}
	else {// if (matrix.isScoreDisplayed()) {
		if (false == score_input.process(beeper, phone)) {
			// the matrix was not busy and there was no sore inputted, we have time to handle some
			// other stuff, we can return the state of this game to the phone, if connected
			// using the isPhoneScoreDifferent flag to force the send on each point change
			if (false == phone.isPhoneUpToDate()) {
				// send the status to the phone for it to send
				phone.sendGameStatus(scorer, isPhoneScoreDifferent);
			}
			// wether the phone accepted this or not, the score is not different from what we sent
			isPhoneScoreDifferent = false;
			// are we showing the proper mode of game (did we get chance to show this message?)
			if (current_score_mode != displayed_score_mode) {
				// show the change in mode to the user now it is sorted out with the score etc
				char scoreModeBuffer[24];
				switch(current_score_mode) {
				case K_SCOREWIMBLEDON5 :
					matrix.strings.getString(scoreModeBuffer, K_STR_ITF5);
					break;
				case K_SCOREWIMBLEDON3 :
					matrix.strings.getString(scoreModeBuffer, K_STR_ITF3);
					break;
				case K_SCOREFAST4 :
					matrix.strings.getString(scoreModeBuffer, K_STR_FAST4);
					break;
				case K_SCOREBADMINTON5 :
					matrix.strings.getString(scoreModeBuffer, K_STR_BAD5);
					break;
				case K_SCOREBADMINTON3 :
					matrix.strings.getString(scoreModeBuffer, K_STR_BAD3);
					break;
				case K_SCOREPOINTS :
				default:
					// make sure it is something valid all the time
					current_score_mode = K_SCOREPOINTS;
					matrix.strings.getString(scoreModeBuffer, K_STR_POINTS);
					break;
				}
				// time the message so it scrolls in right
				int messageLength = matrix.getStringLength(scoreModeBuffer);
				// remove the width of the display as just want to scroll to fit
				messageLength -= (int)matrix.getMatrixDisplayWidth();
				// show this message
				matrix.displayMessage(scoreModeBuffer, scoreModeBuffer,
						0, 0, -1, 0,
						messageLength / messageScrollSpeed,
						messageScrollSpeed,
						(messageLength / messageScrollSpeed) + 1,
						true);
				// only show the once, remember we changed
				displayed_score_mode = current_score_mode;
				// this message will overwrite the winners message
				isShowingWinningMessage = false;
			}
#ifdef PWR_MONITOR
			power.process();
#endif
		}
	}
	// read in the mode and set the mode (one based instead of zero)
	uint8_t newMode = settings.readMode() + 1;
	if (newMode != current_score_mode) {
		// this is a change in the mode
		current_score_mode = newMode;
		scorer.setScoreMode(current_score_mode);
		// the change in the current mode will display the change as a message soon enough
		beeper.cheep(1);
	}
	return lastPointChange;
}


