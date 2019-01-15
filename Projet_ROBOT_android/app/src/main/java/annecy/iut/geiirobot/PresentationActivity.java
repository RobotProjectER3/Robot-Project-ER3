package annecy.iut.geiirobot;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class PresentationActivity extends AppCompatActivity {

    /*WIDGETS ATTRIBUTES*/

    private Button buttonEnjoy = null;                  //button used to go to the ControlRobot activity

    private TextView textViewProjectCreator = null;     //textView which presents the creators of the project (VERY IMPORTANT !!!)
    private TextView textViewRobotControl = null;       //textView which presents what can be controlled on the robot

    @Override
    protected void onCreate(Bundle savedInstanceState) {    //called when the Activity is created
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_presentation);

        /*TRACKING WIDGETS BY ID*/
        buttonEnjoy = (Button) findViewById(R.id.buttonEnjoy);                              //the id's widget is the same than Widget Attribute

        textViewProjectCreator = (TextView)  findViewById(R.id.textViewProjectCreator);     //the id's widget is the same than Widget Attribute
        textViewRobotControl = (TextView)  findViewById(R.id.textViewRobotControl);         //the id's widget is the same than Widget Attribute

        /*DISPLAY OF 2 TEXTS FOR PRESENTATION APPLICATION*/
        textViewProjectCreator.setText("This application was made by\n" + "6 GEII students from the IUT of Anncey :\n\n" + "->  Camille VIALLET\n" + "->  Esma CIRAK\n" + "->  Antoine PHILIPPE\n" + "->  Alexy DUTATE\n" + "->  Quentin DEPEISSES\n" + "->  Enzo BONNARD\n\n");
        textViewRobotControl.setText("To control the Robot, use: \n" + "->  Speed and Direction joysticks\n" + "->  Laser orientation slider\n" + "->  Mode change button\n\n" + "Use the robots proximity sensors to avoid crash\n" + "Have fun\n" + "and enjoy\n");

        /*OPEN THE CONTROL ROBOT ACTIVITY*/
        buttonEnjoy.setOnClickListener(new View.OnClickListener() {         //placing a listener on the "enjoy" button
            @Override
            public void onClick(View v) {                                   //When you click do :
                Intent myIntent = new Intent(PresentationActivity.this, ControlRobotActivity.class);        //create a new intent for the ControlRobot activity
                startActivityForResult(myIntent, 0);                                                          //start the ControlRobot activity
            }
        });
    }
}
