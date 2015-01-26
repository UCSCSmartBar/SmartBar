// File: GF_Ports.h
// Author: Brendan Short
// Date: 1/17/2014
// Version 1.0
// Description:  Gravity feed port and variable definitions

// Includes

#include "GPIOManager.h"
#include "GPIOConst.h"
#include "DataTypes.h"

// Defines

#define GControlCount 4  // Number of bottles being controlled by the gravity controller

#define GControl1 "GPIO0_7" // Gravity control header #1

#define GControl2 "GPIO2_11" // Gravity control header #2

#define GControl3 "GPIO1_2" // Gravity control header #3

#define GControl4 "GPIO1_3" // Gravity control header #3

#define ContainerWidth .06096 // Width of container - 2.4" -> .0696 m

#define ContainerDepth  .33147// Depth of container - 13" -> .3302 m

#define ContainerPortDiameter .0127

#define PortHeight .01016

#define ConstrictionCoefficient .0245

#define ContainerAreaCalculated .025

#define LineHeightMeasurement .1651




class GDispenser {

	public:

		// Variables

		static GPIO::GPIOManager* G_GPIO; // GPIO Manager 

		static GDispenser* Instance; // Pointer to current GDispenser

		static int GControlPin[GControlCount]; // Gravity control pin number - for GPIO Manager usage

		static ContainerData GContainerData[]; // Current container liquid volume

		static const char *GControlPinNames[];  // Gravity control pin header names


		// Functions

		GDispenser(); // Constructor

		static GDispenser* GetInstance();

		static void UpdateContainerContents(int ContainerNumber, float NewVolume);

		static float CalculateValveTime(int ContainerNumber, float DispenseVolume);

		static void TestDispense(int ContainerNumber, float DispenseVolume);

		void PinTest(); // Tests pins

	private:

		// Variables

		static float ContainerArea;

		static float ContainerPortArea;

		static float ContainerPortHeight;

		static float Coefficient;

		static float LineHeight;

		// Functions

		static int Initialize(); // Starts GPIO Manager and starts pins

		static void PinSetUp(); // Starts pins




};