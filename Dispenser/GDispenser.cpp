// File: GDispeser.cpp
// Author: Brendan Short
// Date: 1/17/2014
// Version 1.0
// Description:  Gravity dispensing controller class.

// Includes

#include <iostream>
#include <fstream>
#include <cmath>
#include <stdio.h>
#include <unistd.h>
#include "GPIOManager.h"
#include "GPIOConst.h"
#include "GDispenser.h"
#include "DataTypes.h"


// Variables

GPIO::GPIOManager* GDispenser::G_GPIO;  // GPIO Manager

GDispenser* GDispenser::Instance = NULL; // Current running instance of GDispenser

const char* GDispenser::GControlPinNames[] = {GControl1,GControl2,GControl3,GControl4}; // Pointers to the header names of the gravity control pins

int GDispenser::GControlPin[GControlCount]; // Stores pin numbers of gravity control pins

ContainerData GDispenser::GContainerData[GControlCount]; // Stores volume and content types of all containers

float GDispenser::ContainerArea = ContainerAreaCalculated; //ContainerWidth * ContainerDepth; // Surface area of bottom of container

float GDispenser::ContainerPortArea = 3.1415 * pow((ContainerPortDiameter/2),2); // Surface area of container spigot opening

float GDispenser::ContainerPortHeight = PortHeight; // Port height in container

float GDispenser::Coefficient = ConstrictionCoefficient;

float GDispenser::LineHeight = LineHeightMeasurement;

// Functions


// Function: Constructor Function
// Description: Creates and initializes the GDispenser

GDispenser::GDispenser() {

	Initialize();

}


// Function: GetInstance
// Description: Gets the current running instance of GDispenser, a new GDispenser is created if there is no instance running 

GDispenser* GDispenser::GetInstance() {

	if (Instance == NULL) {

		Instance = new GDispenser();
	}

	return Instance;
}



// Function: Initialize
// Description: Starts the GPIO manager and sets up the output pins

 int GDispenser::Initialize(void) {

  	G_GPIO = GPIO::GPIOManager::getInstance(); // declaring gpio manager GP_IO

	std::printf("GPIO Manager Started \n");

  	PinSetUp();

   	return 1;
 }



// Function: PinSetUp
// Description: Finds pin number based on header name (defined in GFData.h), enables pin for GPIO useage, and sets the pin to be an output

void GDispenser::PinSetUp(void) {

	for (int i = 0; i < GControlCount; i++) {

		GControlPin[i] = GPIO::GPIOConst::getInstance()->getGpioByName(GControlPinNames[i]); // looking up pin number from pin name
		
		G_GPIO->exportPin(GControlPin[i]); // enabling gpio pin

		G_GPIO->setDirection(GControlPin[i],GPIO::OUTPUT); // setting pin direction to output

		std::printf("Pin #%d Configured to Gravity Control Output \n",GControlPin[i]);
	}
}



// Function: UpdateContainerContents
// Description: Updates the contents of the specified container to the new value

void GDispenser::UpdateContainerContents(int ContainerNumber, float NewVolume) {
	
	GContainerData[ContainerNumber-1].Volume = NewVolume;

	std::printf("Container #%d volume : %f mL\n",ContainerNumber,NewVolume);

}



// Function: CalculateValveTime
// Description: Calculates the time in which the specified container's valve must be open to dispense the specified quantity

float GDispenser::CalculateValveTime(int ContainerNumber, float DispenseVolume) {

	float Height = (GContainerData[ContainerNumber-1].Volume*mL_m3)/ContainerArea - PortHeight + LineHeight;

	std::printf("Container #%d height : %f m\n",ContainerNumber,Height);
	
	float ValveTiming = (ContainerArea/(Coefficient * ContainerPortArea * sqrt(2*Gravity)))*(2*sqrt(Height)-2*sqrt((Height - (DispenseVolume*mL_m3/ContainerArea))));

	std::printf("Container #%d - %f Seconds to dispense %f \n",ContainerNumber,ValveTiming,DispenseVolume);

	return ValveTiming;

}



// Function: TestDispense
// Description: Dispenses the specified volume from the specified container using delays

void GDispenser::TestDispense(int ContainerNumber, float DispenseVolume) {

	float ValveTime = CalculateValveTime(ContainerNumber,DispenseVolume); // Get valve timing

	G_GPIO->setValue(GControlPin[ContainerNumber-1],GPIO::HIGH); // Open valve

	usleep(int(ValveTime*1000000));  //  Wait for the valve time

	G_GPIO->setValue(GControlPin[ContainerNumber-1],GPIO::LOW); // Close valve

	GContainerData[ContainerNumber-1].Volume = GContainerData[ContainerNumber-1].Volume-DispenseVolume;
}

// Function: Constructor Function
// Description: Sets all of the active gravity control pins high for one second each in their control order

void GDispenser::PinTest(void) {

	std::printf("Gravity Dispenser Pin Test \n");

 	for (int i = 0; i < GControlCount; i++) {

 		std::printf("Setting Pin #%d High\n",GControlPin[i]);

 		G_GPIO->setValue(GControlPin[i],GPIO::HIGH); // Set current pin high

 		usleep(1000000); // Wait a second

 		G_GPIO->setValue(GControlPin[i],GPIO::LOW); // Set current pin low

 	}
 	
}
