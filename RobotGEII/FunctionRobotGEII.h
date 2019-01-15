#ifndef FONCTIONROBOTGEII_H
#define FONCTIONROBOTGEII_H

#include <Arduino.h>
#include <Servo.h>

//Class of our Robot
class RobotGeii {
#define PORT_VITESSE_M1 5
#define PORT_VITESSE_M2 6

#define PORT_DIRECTION_M1 4
#define PORT_DIRECTION_M2 7

#define AVANCER 1
#define RECULER 0
	
#define PWN_PIN 3 
#define COMPTRIG_PIN 9 

#define BLUETOOTH Serial1
#define DELAY_UPDATE_CAPTEUR 15
#define TAILLE_BUF 13

#define BIT0 0b00000001
#define BIT1 0b00000010
#define BIT2 0b00000100
#define BIT3 0b00001000
#define BIT4 0b00010000
#define BIT5 0b00100000
#define BIT6 0b01000000
#define BIT7 0b10000000
	
//Declaration of variables
private:
	
	uint8_t EnPwmCmd[4] = { 0x44,0x22,0xbb,0x01 };
	unsigned long int iSensorSendTime;
	Servo monServo;

public:
	
	int iT1, iT2, iT3, iT4, iT5, iT6 = 0;
	int iMotorArduino1, iMotorArduino2;
	int iStep1, iStep2, iStep3 = 0;
	int iLoopReceive;
	char strFrame[TAILLE_BUF];

//Initialisiation of the robot and bluetooth
	void InitRobot() {
		Serial.begin(115200);
		DDRF = DDRF & ~BIT1 & ~BIT4 & ~BIT5;																				//Set BIT1 , BIT4 and BIT5 at 0 in the register F
		for (int iBcl = 0; iBcl < 4; iBcl++) {
			Serial.write(EnPwmCmd[iBcl]);
		}

		iSensorSendTime = millis();
		BLUETOOTH.begin(115200, SERIAL_8N2);																				//Transmission speed of the bluetooth
		monServo.attach(A0);
		monServo.write(0);
	}

//Reads the 3 infrared sensor values , true or false
	bool ReadProximitySensorLeft() {
		bool bState = !(BIT5 == (PINF & BIT5));																				//True if the BIT5 of the F register is at 1 , it's at 1 when the sensor is activate
		return bState;
	}

	bool ReadProximitySensorMiddle() {
		bool bState = !(BIT1 == (PINF & BIT1));																				//True if the BIT1 of the F register is at 1...
		return bState;
	}

	bool ReadProximitySensorRight() {
		bool bState = !(BIT4 == (PINF & BIT4));																				//True if the BIT4 of the F register is at 1...
		return bState;
	}
//Reads the ultrasonic sensor values 
	uint16_t ReadDistanceSensorLeft() {
		uint16_t ui16tDistance = 0;
		return ui16tDistance;
	}

	uint16_t ReadDistanceSensorRight() {
		uint16_t ui16tDistance = 0;
		return ui16tDistance;
	}

//Recovery of all sensors 
	void ReadAllSensor(bool *bProximitySensorLeft, bool *bProximitySensorMiddle, bool *bProximitySensorRight, uint16_t *ui16tDistanceSensorLeft, uint16_t *ui16tDistanceSensorRight) {
		bool bStateProximitySensorLeft = ReadProximitySensorLeft();
		*bProximitySensorLeft = bStateProximitySensorLeft;

		bool bStateProximitySensorMiddle = ReadProximitySensorMiddle();
		*bProximitySensorMiddle = bStateProximitySensorMiddle;

		bool bStateProximitySensorRight = ReadProximitySensorRight();
		*bProximitySensorRight = bStateProximitySensorRight;

		uint16_t ui16tValueDistanceSensorLeft = ReadDistanceSensorLeft();
		*ui16tDistanceSensorLeft = ui16tValueDistanceSensorLeft;

		uint16_t ui16tValueDistanceSensorRight = ReadDistanceSensorRight();
		*ui16tDistanceSensorRight = ui16tValueDistanceSensorRight;
	}
//Sets to the left motors the speed and direction, which are sent by the other functions
	void SetLeftMotor(uint16_t ui16tLeftMotor, boolean bDirectionLeftMotor) {
		analogWrite(PORT_VITESSE_M1, ui16tLeftMotor);																		//Writes on the pin 5 which is associated to the register C , pin C6
		digitalWrite(PORT_DIRECTION_M1, bDirectionLeftMotor);																//Writes on the pin 4 which is associated to the register D , pin D4
		
	}
//Sets to the right motors the speed and direction, which are sent by the other functions
	void SetRightMotor(uint16_t ui16tRightMotor, boolean bDirectionRightMotor) {
		analogWrite(PORT_VITESSE_M2, ui16tRightMotor);																		//Write on the pin 6 is associate to the register D , pin D7
		digitalWrite(PORT_DIRECTION_M2, bDirectionRightMotor);																//Write on the pin 7 is associate to the register E , pin E6
	}
//Sets the ServoMotors the angle which is sent by the other fonction
	void SetServomotor(uint8_t ui8tServomotor) {
		monServo.write(ui8tServomotor);
	}
//Sends the frame to android application , finally we didn't achieve the send of ultrasonics sensors , we deleted values and replace them by 99 and 99 to still send values to the application , cf code at the end
	void SendFrameToRobot(bool bProximitySensorLeft, bool bProximitySensorMiddle, bool bProximitySensorRight, uint16_t ui16tDistanceSensorLeft, uint16_t ui16tDistanceSensorRight) {
		char sSend[7] = { bProximitySensorLeft + 48, bProximitySensorMiddle + 48, bProximitySensorRight + 48, '9', '9', '9', '9' };	//Recovery all sensor values and add to them + 48 because we need to convert in ASCII (bool to char)
		if ((iSensorSendTime + DELAY_UPDATE_CAPTEUR) < millis()) {																	//Write on the bluetooth our string sSend
			BLUETOOTH.write(0);
			BLUETOOTH.write(sSend);	
			iSensorSendTime = millis();
		}
	}
//Receive all the frame which is sent by the Android application
	void ReceiveFrameFromRobot(uint16_t *ui16tLeftMotor, boolean *bDirectionLeftMotor, uint16_t *ui16tRightMotor, boolean *bDirectionRightMotor, uint8_t *ui8tServomotor) {

		
		if (BLUETOOTH.available() > 0) {																					//If the bluetooth is activated
			strFrame[iLoopReceive] = BLUETOOTH.read();																		//We read the frame character per character until "\0" (end of frame) appears
			if (strFrame[iLoopReceive] != '\0') {																			//if the frame isn't at it's end, then counts characters
				iLoopReceive++;
			}
			else {																											//When we receive "\0" , the frame is complete.
				
				//Left wheels reception
				iT1 = strFrame[0] - 48;																						//In the android app we decide that the 3 first characteres are for the left motors
				iT2 = strFrame[1] - 48;
				iT3 = strFrame[2] - 48;
				
				//Right wheels reception																					//Then charactere 3, 4 ,5 are for the right motors
				iT4 = strFrame[3] - 48;
				iT5 = strFrame[4] - 48;
				iT6 = strFrame[5] - 48;



				//servo motor reception																						//And finally we receive values for servo motor
				iStep1 = strFrame[6] - 48;
				iStep2 = strFrame[7] - 48;
				iStep3 = strFrame[8] - 48;

				//units association															
				iMotorArduino1 = iT1 * 100 + iT2 * 10 + iT3;																//Recovers all values to make the true value		
				iMotorArduino2 = iT4 * 100 + iT5 * 10 + iT6;																//Simple calculation to stock the values which we received in only one variable.
				*ui8tServomotor = iStep1 * 100 + iStep2 * 10 + iStep3;														//Same for Servo motor
				*ui8tServomotor = 120 - *ui8tServomotor;					

				Serial.println(iMotorArduino1);
				Serial.println(iMotorArduino2);

//Android program send a value between 0 and 510 

				if ((iMotorArduino1 >= 0 && iMotorArduino1 <= 510) && (iMotorArduino2 >= 0 && iMotorArduino2 <= 510)) {		//If we are on the right range of values we send values on the motors
					if (iMotorArduino1 > 255) {																				//If we are on the upper part of the joystick ( 256 -> 510)
						*ui16tLeftMotor = iMotorArduino1 - 255;																//We substract 255 to the value , because we can only apply on the motor a value beetwen 0 and 255
						*bDirectionLeftMotor = 1;																			//Then if we are on the upper part , we set rotation sense at 1 , to go forward
					}
					else {																									//Else if we are on the lower part of joystick ( 0 -> 255)
						*ui16tLeftMotor = 255 - iMotorArduino1;																//We substract the value to 255 to have the invert value (exemple if we receive 0 , we would send 255 and not 0)
						*bDirectionLeftMotor = 0;																			//Set the rotation sense at 0 , to go backward
					}

					if (iMotorArduino2 > 255) {																				//Same for the 2nd Motors
						*ui16tRightMotor = iMotorArduino2 - 255;
						*bDirectionRightMotor = 1;
					}
					else {
						*ui16tRightMotor = 255 - iMotorArduino2;
						*bDirectionRightMotor = 0;
					}
				}
				iLoopReceive = 0;																							//Set to 0 the loop value..
			}
		}
	}
	/* Prototype for ultrasonic sensors , we receive perfectly the range in cm, but when we try to send them in the frame with infrared sensor , all of our project dont works...
	//Send sonic wave
	digitalWrite(COMPTRIG_PIN, LOW);
	digitalWrite(COMPTRIG_PIN, HIGH);

	//Get time duration of pwm 
	ldistanceMeasured = pulseIn(URPWM, LOW);
	iDistance = (int)(ldistanceMeasured / 2) / 29.1;				//The speed of sound is: 343m/s = 0.0343 cm/uS = 1/29.1 cm/uS , we can multiply by 0.0343 or divide by 29.1 like here. We need to divide the traveltime by 2.


	if (iDistance > 99) {											//We have done différents test but we conclude that sensor can only detect a maximal range of 130cm and we need to be extremly precise to receive sonic waves
		distdozg = 'x';												//Then we decide to have a maximal range of 99 cm to use only 2 caracters on the sent frame. If the range is higher we send 'xx'.	
		distunitg = 'x';
	}

	//Serial.println("cm");
	//units dissociation
	else {
		distdozg = (iDistance / 10);
		distunitg = (iDistance % 10);
		distdozg = distdozg + 48;									//+48 for ASCII code
		distunitg = distunitg + 48;
	}
	*/

};

#endif // !FONCTIONROBOTGEII_H
#pragma once
