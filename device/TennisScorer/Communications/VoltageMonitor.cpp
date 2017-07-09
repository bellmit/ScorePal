/*
 * VoltageMonitor.cpp
 *
 *  Created on: 15 May 2017
 *      Author: douglasbrain
 */

#include "VoltageMonitor.h"
#include <Wire.h>

namespace Communications {

VoltageMonitor::VoltageMonitor() {
	// constructor
	currentFrequency = 0;
	lastTimeDisplayed = millis();
}

VoltageMonitor::~VoltageMonitor() {
	// TODO Auto-generated destructor stub
}

void VoltageMonitor::initialise() {
	// Initialize the INA219.
	// By default the initialization will use the largest range (32V, 2A).  However
	// you can call a setCalibration function to change this range (see comments).
	ina219.begin();
	// To use a slightly lower 32V, 1A range (higher precision on amps):
	//ina219.setCalibration_32V_1A();
	// Or to use a lower 16V, 400mA range (higher precision on volts and amps):
	//ina219.setCalibration_16V_400mA();
}

void VoltageMonitor::process() {
	if (lastTimeDisplayed - millis() > 5000) {
		// display periodically
		Serial.print("Bus Voltage:   "); Serial.print(getBusVoltage()); Serial.println(" V");
		Serial.print("Shunt Voltage: "); Serial.print(getShuntVoltage()); Serial.println(" mV");
		Serial.print("Load Voltage:  "); Serial.print(getLoadVoltage()); Serial.println(" V");
		Serial.print("Current:       "); Serial.print(getCurrent()); Serial.println(" mA");
		Serial.println("");
		// reset the time
		lastTimeDisplayed = millis();
	}
}

float VoltageMonitor::getShuntVoltage() {
	return ina219.getShuntVoltage_mV();
}

float VoltageMonitor::getBusVoltage() {
	return ina219.getBusVoltage_V();
}

float VoltageMonitor::getCurrent() {
	return ina219.getCurrent_mA();
}

float VoltageMonitor::getLoadVoltage() {
	return getBusVoltage() + (getShuntVoltage() / 1000.0);
}

} /* namespace Communications */
