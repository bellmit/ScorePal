/*
 * Pins.h
 *
 *  Created on: 23 May 2017
 *      Author: douglasbrain
 */

#ifndef PINS_H_
#define PINS_H_

//#define K_PROTOTYPE
#define K_PRODUCTION

#ifdef K_PRODUCTION
// Input Receiver (radio input pins)
#define K_PIN_RADIORXCE		9
#define K_PIN_RADIORXCS		10
// Input Sender (radio output pins) // comment out to disable
#define K_PIN_RADIOTXCE		7
#define K_PIN_RADIOTXCS		8
// hard-coded arduino PINS used that we have to use
#define K_PIN_RADIOMOSI		11	// corresponds to the DOUT on Teensy
#define K_PIN_RADIOMISO		12	// corresponds to the DIN on Teensy
#define K_PIN_RADIOSCK		13	// corresponds to the SCK on Teensy
// Phone
#define K_PIN_BTKEY 		2
// hard-coded arduino pins for the bluetooth connection we have to use
#define K_PIN_BTTXD			0	// corresponds to the RX1 on Teensy
#define K_PIN_BTRXD			1	// corresponds to the TX1 on Teensy
// ScoreInput (button pins)
#define K_PIN_SVRBTN 		3
#define K_PIN_RVRBTN 		4
//#define K_PIN_UNDBTN 		5	/* comment out if using no undo button - use the svr and rvr buttons together /*
#define K_PIN_RSTBTN 		6
// Beep (speaker ctl pin and switch)
#define K_PIN_SPEAKER 		8
#define K_PIN_SOUNDSWITCH 	7
//Led matrix (displays)
#define K_PIN_LEDDATAF  	23     /* DIN pin of MAX7219 module */
#define K_PIN_LEDLOADF  	22     /* CS pin of MAX7219 module */
#define K_PIN_LEDCLOCKF 	21 	   /* CLK pin of MAX7219 module */

#define K_PIN_DATAB  		20     /* DIN pin of MAX7219 module */
#define K_PIN_LOADB  		19     /* CS pin of MAX7219 module */
#define K_PIN_CLOCKB 		18     /* CLK pin of MAX7219 module */
// Settings Input (pots for controlling mode and brightness)
#define K_PIN_DIMMERSW 		14
//#define K_PIN_MODEDIAL 		15	/* comment out if using the selection dial on 5/6 pins instead of resistors /*
#define K_PIN_BRIGHTDIAL 	16
// the mode switch options when individually done
#define K_PIN_MODEONE 		24
#define K_PIN_MODETWO 		25
#define K_PIN_MODETHREE 	26
#define K_PIN_MODEFOUR 		27
#define K_PIN_MODEFIVE 		28
#define K_PIN_MODESIX 		29 // 32 for the plastic prototype case
// player number display pins
#define K_PIN_PL1LED 		31
#define K_PIN_PL2LEDB	 	32
#define K_PIN_PL2LEDF	 	33 // 29 for the plastic prototype case
#endif //K_PRODUCTION

#ifdef K_PROTOTYPE
// Input Receiver (radio input pins)
#define K_PIN_RADIORXCE		9
#define K_PIN_RADIORXCS		10
// Input Sender (radio output pins) // comment out to disable
#define K_PIN_RADIOTXCE		7
#define K_PIN_RADIOTXCS		8
// hard-coded arduino PINS used that we have to use
#define K_PIN_RADIOMOSI		11	// corresponds to the DOUT on Teensy
#define K_PIN_RADIOMISO		12	// corresponds to the DIN on Teensy
#define K_PIN_RADIOSCK		13	// corresponds to the SCK on Teensy
// Phone
#define K_PIN_BTKEY 		2
// hard-coded arduino pins for the bluetooth connection we have to use
#define K_PIN_BTTXD			0	// corresponds to the RX1 on Teensy
#define K_PIN_BTRXD			1	// corresponds to the TX1 on Teensy
// ScoreInput (button pins)
#define K_PIN_SVRBTN 		16
#define K_PIN_RVRBTN 		15
#define K_PIN_UNDBTN 		14
// Beep (speaker ctl pin and switch)
#define K_PIN_SPEAKER 		5
#define K_PIN_SOUNDSWITCH 	8
//Led matrix (displays)
#define K_PIN_LEDDATAF  	21     /* DIN pin of MAX7219 module */
#define K_PIN_LEDLOADF  	22     /* CS pin of MAX7219 module */
#define K_PIN_LEDCLOCKF 	23 	   /* CLK pin of MAX7219 module */

#define K_PIN_DATAB  		18     /* DIN pin of MAX7219 module */
#define K_PIN_LOADB  		19     /* CS pin of MAX7219 module */
#define K_PIN_CLOCKB 		20     /* CLK pin of MAX7219 module */
// Settings Input (pots for controlling mode and brightness)
#define K_PIN_DIMMERSW 		8
#define K_PIN_MODEDIAL 		17
#define K_PIN_BRIGHTDIAL 	14
// player number display pins
#define K_PIN_PL1LED 		3
#define K_PIN_PL2LEDB	 	4
#define K_PIN_PL2LEDF	 	5
#endif //K_PROTOTYPE


#endif /* PINS_H_ */
