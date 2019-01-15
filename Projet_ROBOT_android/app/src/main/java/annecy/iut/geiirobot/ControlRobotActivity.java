package annecy.iut.geiirobot;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import static java.lang.Math.abs;

public class ControlRobotActivity extends AppCompatActivity {

                                    /*WIDGETS ATTRIBUTES*/

    private static Button sensorProximityLeft = null;                          //button indicating the left proximity sensor state of the Robot
    private static Button sensorProximityMiddle = null;                        //button indicating the middle proximity sensor state of the Robot
    private static Button sensorProximityRight = null;                         //button indicating the right proximity sensor state of the Robot
    protected Button buttonConnectBT = null;                              //button used for connecting device to robot
    private Button buttonSwitchStateRobot = null;                       //button used for changing state between AUTOmatic and MANUal
    private Button buttonComeback = null ;                              //button used for calling the robot back

    private static TextView textViewSensorDistanceLeft = null;          //textView used to visualize the values sent by the left ultrasonic distance sensor (Min: 4 Max: 99cm)
    private static TextView textViewSensorDistanceRight = null;         //textView used to visualize the values sent by the right ultrasonic distance sensor (Min: 4 Max: 99cm)
    private static TextView textViewOrientationServoMotor = null;       //textView used for showing the robots stepper motors angle (between 0° and 120°)
    private TextView textViewStateRobot = null;                         //textView used to show the state of the robot (AUTOmatic and MANUal)

    private JoystickView joystickViewControlRobotSpeed = null;               //joystick used to control the speed of the Robot
    private JoystickView joystickViewControlRobotOrientation = null;         //joystick used to control the orientation of the Robot

    private SeekBar seekBarControlServomotor = null;                   //seekBar used to control the angle of the stepper motor


                                    /*ROBOT ATTRIBUTES*/

    /*!! USE THESE ATTRIBUTES FOR CALCULATION/BLUETOOTH/GRAPHIC INTERFACE  WHICH CONCERNS THE ROBOT   !!*/
    private static String asStateRobot = "MANU";                   //define the robots state
    private static Boolean Comeback  = false;

    public static boolean abStateSensorProximityLeft = false;     //defines the left proximity sensors state
    public static boolean abStateSensorProximityMiddle = false;   //defines the middle proximity sensors state
    public static boolean abStateSensorProximityRight = false;    //defines the right proximity sensors state


    public static int aiDistanceLeftSensor = 0;                   //defines the value of the left distance sensor
    public static int aiDistanceRightSensor = 0;                  //defines the value of the right distance sensor
    public int aiOrientationServoMotor = 60;               //defines the value of the stepper motors orientation

    private static float afSpeed = 0;              //defines the speed of the robot
    private static float afOrientation = 0;        //defines the orientation of the robot



                                    /*PROJECT ATTRIBUTES*/

    static TreatmentApp aTreatmentApp;                         //TreatmentApp object declaration used for motor control calculations
    static RobotBDD_Manager robotBDD_manager;
                                    /*BLUETOOTH ATTRIBUTES*/

    public static BlueT aPhoneBluetooth;                                   //BlueT used to control the Bluetooth connections between the device and the Robot


    static public Handler aHandlerReceiveBT = new Handler() {       //Handler used to receive robot state sensor via BT -> Calculation -> graphic interface modification -> send command by BT
        public void handleMessage(Message msg) {

            String strBTthread=(String) msg.obj;                    //gets the thread send by the BT
            //Log.i("Trame", strBTthread);
            if(strBTthread.length() == 7){
                aTreatmentApp.DecodeThreadRobot(strBTthread);
                SetStateProximitySensor(abStateSensorProximityLeft, abStateSensorProximityMiddle, abStateSensorProximityRight);
                SetValueDistanceSensor(aiDistanceLeftSensor, aiDistanceRightSensor);
                if(asStateRobot == "MANU"){
                    if (Comeback == true){  // If someone uses the comeback button
                        aTreatmentApp.Comeback(); // comes back
                    }else {
                        aTreatmentApp.CalculManuState((int) afSpeed, (int) afOrientation);
                    }
                }
                else if (asStateRobot == "AUTO"){
                    Comeback = false ;
                    //robotBDD_manager.Delete_all(); // Deletes the database
                    aTreatmentApp.AutomaticSensorRecovering(abStateSensorProximityLeft, abStateSensorProximityMiddle, abStateSensorProximityRight);
                }

            }

            }
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {                            //called when app is created
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_robot);

        sensorProximityLeft = (Button) findViewById(R.id.proximitySensorLeft);                          //notice sensorProximityLeft by its id on graphic interface
        sensorProximityMiddle = (Button) findViewById(R.id.proximitySensorMiddle);                      //notice sensorProximityMiddle by its id on graphic interface
        sensorProximityRight = (Button) findViewById(R.id.proximitySensorRight);                        //notice sensorProximityRight by its id on graphic interface
        buttonConnectBT = (Button) findViewById(R.id.buttonConnectBT);                                  //notice buttonConnectBT by its id on graphic interface
        buttonSwitchStateRobot = (Button) findViewById(R.id.buttonStateRobot);                          //notice buttonSwitchStateRobot by its id on graphic interface
        buttonComeback = (Button) findViewById(R.id.btn_comeback);

        textViewSensorDistanceLeft = (TextView) findViewById(R.id.textViewProximitySensorLeft);         //notice textViewSensorDistanceLeft by its id on graphic interface
        textViewSensorDistanceRight = (TextView) findViewById(R.id.textViewProximitySensorRight);       //notice textViewSensorDistanceRight by its id on graphic interface
        textViewOrientationServoMotor = (TextView) findViewById(R.id.textViewOrientationServoMoteur);   //notice textViewStateRobot by its id on graphic interface
        textViewStateRobot = (TextView) findViewById(R.id.textViewStateRobot);


        joystickViewControlRobotSpeed = (JoystickView)  findViewById(R.id.joystickViewControlRobotSpeed);               //notice joystickViewControlRobotSpeed by its id on graphic interface
        joystickViewControlRobotOrientation = (JoystickView)  findViewById(R.id.joystickViewControlRobotOrientation);   //notice joystickViewControlRobotOrientation by its id on graphic interface

        seekBarControlServomotor = (SeekBar) findViewById(R.id.seekBarOrientationServoMoteur);         //notice seekBarControlServomotor by its id on graphic interface


        //Deletes the table
        this.robotBDD_manager= new RobotBDD_Manager(this);
        robotBDD_manager.Delete_all();


        this.aPhoneBluetooth = new BlueT(this, aHandlerReceiveBT);          //creation of aPhoneBluetooth object

        this.aTreatmentApp = new TreatmentApp(this);                         //creation of aTreatmentApp object



        buttonConnectBT.setOnClickListener(new View.OnClickListener() {     //click listener on buttonConnectBT
            @Override
            public void onClick(View v) {           //when you click :
                aPhoneBluetooth.Connection();       //connects to BT
            }
        });


        buttonSwitchStateRobot.setOnClickListener(new View.OnClickListener() {  //click listener on buttonSwitchStateRobot
            @Override
            public void onClick(View v) {                                   //when you click :
                if(asStateRobot == "MANU") {                                 //if actual state in MANUal mode
                    buttonSwitchStateRobot.setText(asStateRobot);           //text of buttonSwitchStateRobot becomes "MANU"
                    asStateRobot = "AUTO";                                  //asStateRobot takes the value AUTO
                    textViewStateRobot.setText(asStateRobot);               //text of textViewStateRobot becomes AUTO
                    joystickViewControlRobotSpeed.setVisibility(View.GONE);          //joystickViewControlRobotSpeed becomes useless and disappears
                    joystickViewControlRobotOrientation.setVisibility(View.GONE);    //joystickViewControlRobotOrientation becomes useless and disappears
                    buttonComeback.setVisibility(View.GONE);
                }
                else if (asStateRobot == "AUTO") {
                    buttonSwitchStateRobot.setText(asStateRobot);           //text of buttonSwitchStateRobot becomes AUTO
                    asStateRobot = "MANU";                                  //asStateRobot takes the value MANU
                    textViewStateRobot.setText(asStateRobot);               //text of textViewStateRobot becomes MANU
                    joystickViewControlRobotSpeed.setVisibility(View.VISIBLE);           //joystickViewControlRobotSpeed becomes utilizable and appears
                    joystickViewControlRobotOrientation.setVisibility(View.VISIBLE);     //joystickViewControlRobotOrientation becomes utilizable and appears
                    buttonComeback.setVisibility(View.VISIBLE);
                }
            }
        });

        joystickViewControlRobotSpeed.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                SendAcc(angle , strength);
            }
        });

        joystickViewControlRobotOrientation.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                SendRot(angle, strength);

            }
        });

        seekBarControlServomotor.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {    //seekBar change listener on seekBarControlServomotor
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {            //when the value of seekBar changes :
                aiOrientationServoMotor = progress;                                                     //aiOrientationServoMotor takes the value of seekBar
                String strValSeekBar = String.valueOf(abs(aiOrientationServoMotor - 60)) ;              //calculation of stepper motors position
                textViewOrientationServoMotor.setText(strValSeekBar + "°");                             //text of textViewOrientationServoMotor becomes a value between (60 -> 0 -> 60)
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

    }

    protected void OnClick_comeback(View view){
        Comeback = true;
        joystickViewControlRobotSpeed.setVisibility(View.GONE);          //joystickViewControlRobotSpeed become useless and disappear
        joystickViewControlRobotOrientation.setVisibility(View.GONE);    //joystickViewControlRobotOrientation become useless and disappear
        buttonSwitchStateRobot.setVisibility(View.VISIBLE);
        textViewStateRobot.setText("");

    }



    private static void SetStateProximitySensor(boolean bStateSensorLeft, boolean bStateSensorMiddle, boolean bStateSensorRight){      //method used for the application proximity sensors color changing
        if (bStateSensorLeft == true){                                                              //if left sensor is active :
            sensorProximityLeft.setBackgroundResource(R.drawable.custom_shape_sensor_left_active);  //sensor brimming with orange color
        }
        else{
            sensorProximityLeft.setBackgroundResource(R.drawable.custom_shape_sensor_left);         //else if the sensor is empty
        }
        if (bStateSensorMiddle == true){                                                                //if middle sensor is active :
            sensorProximityMiddle.setBackgroundResource(R.drawable.custom_shape_sensor_middle_active);  //sensor brimming with orange color
        }
        else {
            sensorProximityMiddle.setBackgroundResource(R.drawable.custom_shape_sensor_middle);     //else if the sensor is empty
        }
        if(bStateSensorRight == true){                                                                  //if right sensor is active :
            sensorProximityRight.setBackgroundResource(R.drawable.custom_shape_sensor_right_active);    //sensor brimming with orange color
        }
        else{
            sensorProximityRight.setBackgroundResource(R.drawable.custom_shape_sensor_right);       //else if the sensor is empty
        }
    }


    private static void SetValueDistanceSensor(int iDistanceLeftSensor, int aiDistanceRightSensor){        //method use for changing the value of distance sensors
        String strValueDistance;            //string used for the distance value as string

            /*LEFT DISTANCE SENSOR*/
        if(iDistanceLeftSensor < 4){        //if distance value is below the minimum value measurable by the sensor
            textViewSensorDistanceLeft.setText(" < 4 cm");  // text of textViewSensorDistanceLeft becomes "< 4"
        }
        else if(iDistanceLeftSensor > 500){    //if distance value is above the maximum value measurable by the sensor
            textViewSensorDistanceLeft.setText(" > 99 cm");   // text of textViewSensorDistanceLeft become "> 5000"
        }
        else{                                                               //else (value between maximum and minimum)
            strValueDistance = String.valueOf(iDistanceLeftSensor);         //convert iDistanceLeftSensor in string
            textViewSensorDistanceLeft.setText(strValueDistance + " cm");   //text of textViewSensorDistanceLeft take the value of iDistanceLeftSensor
        }

            /*RIGHT DISTANCE SENSOR*/
        if(aiDistanceRightSensor < 4){        //if distance value is below the minimum value measurable by the sensor
            textViewSensorDistanceRight.setText(" < 4 cm");     // text of textViewSensorDistanceRight become "< 4"
        }
        else if(aiDistanceRightSensor > 500){      //if distance value is above the maximum value measurable by the sensor
            textViewSensorDistanceRight.setText(" > 99 cm");  // text of textViewSensorDistanceLeft become "> 5000"
        }
        else{                                                               //else (value between maximum and minimum)
            strValueDistance = String.valueOf(aiDistanceRightSensor);       //convert iDistanceLeftSensor in string
            textViewSensorDistanceRight.setText(strValueDistance + " cm");  //text of textViewSensorDistanceRight take the value of iDistanceLeftSensor
        }
    }



    public  void SendThreadRobotManuState(String strTxt){  //static
        if(aPhoneBluetooth.mbtConnected == true) {
            aPhoneBluetooth.Send(strTxt + "\0");

        }
    }



    public static void  SendThreadRobotAutoState(int iVitesse,int iDirection){
        aTreatmentApp.CalculManuState(iVitesse, iDirection);
    }

    public void SendAcc(int angle, int strength){


        if(strength != 0) {

            afSpeed = (float) (Math.round(Math.sin(Math.toRadians(angle))*2.55)*strength);
        }
        else{

            afSpeed = (float) (Math.round(Math.sin(Math.toRadians(angle))*2.55)*strength);
        }
    }

    public void SendRot(int angle, int strength){


        //afOrientation = (float) Math.cos(Math.toRadians(angle));
        if(strength != 0) {
            afOrientation = Math.round(Math.cos(Math.toRadians(angle))*2.55)*strength;
        }else{
            afOrientation = 0;
        }
    }




}
