// Visual Micro is in vMicro>General>Tutorial Mode
// 
/*
    Name:       RobotGEII.ino
    Created:	10/01/2019 15:17:03
    Author:     DESKTOP-3MJIBAF\enzob
*/



#include "FunctionRobotGEII.h"

//Déclaration of the class
RobotGeii Robotgeii;


//Déclaration of variables
bool bStateProximitySensorLeft = 0;
bool bStateProximitySensorMiddle = 0;
bool bStateProximitySensorRight = 0;
uint16_t ui16tDistanceSensorLeft = 0;
uint16_t ui16tDistanceSensorRight = 0;

uint16_t ui16tValueLeftMotor = 0;
bool bStateDirectionLeftMotor = 0;
uint16_t ui16tValueRightMotor = 0;
bool bStateDirectionRightMotor = 0;
uint8_t	ui8tOrientationServomotor = 0;




void setup()
{
	Robotgeii.InitRobot();	//Call the init process of our class
}

// Add the main program code into the continuous loop() function
void loop()
{
	Robotgeii.ReceiveFrameFromRobot(&ui16tValueLeftMotor, &bStateDirectionLeftMotor, &ui16tValueRightMotor, &bStateDirectionRightMotor, &ui8tOrientationServomotor);

	Robotgeii.SetLeftMotor(ui16tValueLeftMotor, bStateDirectionLeftMotor);
	Robotgeii.SetRightMotor(ui16tValueRightMotor, bStateDirectionRightMotor);
	Robotgeii.SetServomotor(ui8tOrientationServomotor);

	Robotgeii.ReadAllSensor(&bStateProximitySensorLeft, &bStateProximitySensorMiddle, &bStateProximitySensorRight, &ui16tDistanceSensorLeft, &ui16tDistanceSensorRight);
	Robotgeii.SendFrameToRobot(bStateProximitySensorLeft, bStateProximitySensorMiddle, bStateProximitySensorRight, ui16tDistanceSensorLeft, ui16tDistanceSensorRight);
}
	
	


	
	

	



