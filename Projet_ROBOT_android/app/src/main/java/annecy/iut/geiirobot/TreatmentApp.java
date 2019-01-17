package annecy.iut.geiirobot;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;


public class TreatmentApp extends AppCompatActivity {
    private static ControlRobotActivity aControlRobotActivity;
    private RobotBDD_Manager robotBDD_manager ;

    String strState = "None";
    boolean bTurn = false;
    long lTime = 0;
    int iCompteur = 0;

    public TreatmentApp(ControlRobotActivity Activity) {

        this.aControlRobotActivity = Activity;
        this.robotBDD_manager = new RobotBDD_Manager(this);
    }

    //joystick values are received by this method, and are converted to wheels speed
    public void CalculManuState(Integer iAcceleration, Integer iDirection){
        //variables
        int iRightWheel = 0;        //right wheel value
        String strRM = "";          //right wheel value in String
        int iLeftWheel = 0;        //left wheel value
        String strLM = "";          //left wheel value in String
        String strServoMotor = "";
        String strBDD = "";

        //Math
        iRightWheel = (iAcceleration - iDirection) + 255;   //equation for right wheel speed
        iLeftWheel = (iAcceleration + iDirection) + 255;   //equation for left wheel speed

        //security
        //in case speed is to high or to low for:
        //right wheel
        if(iRightWheel > 510){
            iRightWheel = 510;
        }else if(iRightWheel < 0){
            iRightWheel = 0;
        }
        //left wheel
        if(iLeftWheel > 510){
            iLeftWheel = 510;
        }else if(iLeftWheel < 0){
            iLeftWheel = 0;
        }

        //frame making
        //we are always sending the same amount of characters eg: 000000 or 510510
        //right wheel adding missing characters
        if(iRightWheel < 10){
            strRM = "00"+String.valueOf(iRightWheel);
        }else if(iRightWheel >= 10 && iRightWheel < 100){
            strRM = "0"+String.valueOf(iRightWheel);
        }else{
            strRM = String.valueOf(iRightWheel);
        }

        //left wheel adding missing characters
        if(iLeftWheel < 10){
            strLM = "00"+String.valueOf(iLeftWheel);
        }else if(iLeftWheel >= 10 && iLeftWheel < 100){
            strLM = "0"+String.valueOf(iLeftWheel);
        }else{
            strLM = String.valueOf(iLeftWheel);
        }
        if(this.aControlRobotActivity.aiOrientationServoMotor < 100) {
            if(this.aControlRobotActivity.aiOrientationServoMotor < 10) {
                strServoMotor = "00" + String.valueOf(this.aControlRobotActivity.aiOrientationServoMotor);
            }
            else {
                strServoMotor = "0" + String.valueOf(this.aControlRobotActivity.aiOrientationServoMotor);
            }
        }
        else {
            strServoMotor = String.valueOf(this.aControlRobotActivity.aiOrientationServoMotor);
        }
        //Log.i("Trame", strLM + strRM + strServoMotor);

        // RECORD THE WEFT IN THE DATABASE
        iCompteur++;
        if (iCompteur > 10 ) {                                                         //Save only one value out of 10
            strBDD = CallBack_Calculation(Integer.valueOf(strLM), Integer.valueOf(strRM));    //Calculate the position the robot should have to comeback
            robotBDD_manager.Inser_data(strBDD);                                       //Save this value in the database
            iCompteur = 0;                                                             //Initialize the counter to 0
        }


        this.aControlRobotActivity.SendThreadRobotManuState(strLM + strRM + strServoMotor);

    }

    public void AutomaticSensorRecovering(boolean bLeftSensor , boolean bMiddleSensor , boolean bRightSensor){   // It allows to control the robot in automatic mode thanks to the infrared sensor
        int iSpeed=0;                                                                                            //  We initialize the value to 0
        int iDirection=0;

        if(!bRightSensor && !bLeftSensor && !bMiddleSensor && !bTurn) {                                          // If all sensors are off and not in 'special mode', the robot moves forward
            strState = "None";
            iSpeed=255;
            iDirection=0;
        }
        else if(bRightSensor && bLeftSensor && bMiddleSensor && !bTurn){                                         // If all sensors are 'on', and not on 'special mode'
            strState = "All";
            lTime = System.currentTimeMillis();                                                                  // We retrieve the current time in milliseconds
            bTurn = true;                                                                                        // 'Special mode' activated
        }
        else if(bRightSensor && bLeftSensor && !bTurn){                                                          // If the right and left sensors are 'on' and not on 'special mode'
            strState = "Error";
            lTime = System.currentTimeMillis();                                                                  // We retrieve the current time in milliseconds = start time
            bTurn = true;                                                                                        // 'Special mode' activated
        }
        else if (bRightSensor && !bTurn) {                                                                      // If the right sensor is 'on', the robot turns left
            strState = "Right";
            iSpeed=0;
            iDirection=-255;
        }
        else if (bLeftSensor && !bTurn) {                                                                       // If the left sensor is 'on', the robot turns right
            strState = "Left";
            iSpeed=0;
            iDirection=255;
        }
        else if(bMiddleSensor && !bTurn){                                                                       // If the middle sensor is 'on'
            strState = "Middle";
            lTime = System.currentTimeMillis();                                                                 // We retrieve the current time in milliseconds
            bTurn = true;                                                                                       // 'Special mode' activated
        }

        //      SPECIAL MODE (half turn/backwards)

        if(bTurn && (strState.equals("Middle") || strState.equals("All"))){                                     // If the middle sensor or all sensors are 'on', the robot makes a half turn
            if(System.currentTimeMillis() - lTime < 1600){                                                      //  We retrieve the current time in milliseconds and we subtract start time to know the elapsed time. If this time is less than 1600 millisecond, the robot makes a half turn
                iSpeed = 0;
                iDirection = 255;
            }else{                                                                                              // Else 'special mode' disabled
                bTurn = false;
            }
        }
        else if(bTurn && strState.equals("Error")){                                                             // If the right and left sensors are on
            if(System.currentTimeMillis() - lTime < 2500){                                                      // The robot moves back during 2500 milliseconds (2.5 sec)
                iSpeed = -200;
                iDirection = 0;
            }
            else if(System.currentTimeMillis() - lTime > 2500 && System.currentTimeMillis() - lTime < 4100){    // After the robot makes a half turn during 1600 millisecond (1.6 sec)
                iSpeed = 0;
                iDirection = 255;
            }
            else{
                bTurn = false;
            }
        }


        aControlRobotActivity.SendThreadRobotAutoState(iSpeed,iDirection);                                      // we send the speed and direction values in the function SendThreadRobotAutoState
    }

    public void Comeback(){  //Used to make a comeback
        String strTrame = "";
        strTrame = this.robotBDD_manager.Read_Database();                //Reads the data in the database
        this.aControlRobotActivity.SendThreadRobotManuState(strTrame);   //Sends it to the robot
    }

    public String CallBack_Calculation(int iValMoteurGauche, int iValMoteurDroit) {  //Calculates the speed and direction the robot has to do in "callback" mode
        int iGauche_Retour = 0;
        int iDroite_Retour = 0;
        String strMD ;
        String strMG ;



        iGauche_Retour = 510 - iValMoteurDroit;



        iDroite_Retour = 510 - iValMoteurGauche;




        strMD = String.format("%03d", iGauche_Retour);
        strMG = String.format("%03d", iDroite_Retour);

        return strMG + strMD ;
    }

    public static void DecodeThreadRobot(String strThread){
        char cThreadInChar[] = strThread.toCharArray();
        if (cThreadInChar[0] == '1'){
            aControlRobotActivity.abStateSensorProximityLeft = true;
        }
        else {
            aControlRobotActivity.abStateSensorProximityLeft = false;
        }
        if (cThreadInChar[1] == '1'){
            aControlRobotActivity.abStateSensorProximityMiddle = true;
        }
        else {
            aControlRobotActivity.abStateSensorProximityMiddle = false;
        }
        if (cThreadInChar[2] == '1'){
            aControlRobotActivity.abStateSensorProximityRight = true;
        }
        else {
            aControlRobotActivity.abStateSensorProximityRight = false;
        }
        aControlRobotActivity.aiDistanceLeftSensor = (((int)cThreadInChar[3]-48)*10) + ((int)cThreadInChar[4]-48);
        aControlRobotActivity.aiDistanceRightSensor = (((int)cThreadInChar[5]-48)*10) + ((int)cThreadInChar[6]-48);
    }




}
