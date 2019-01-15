package annecy.iut.geiirobot;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

                                                /* This class is used to manage the database */
public class RobotBDD_Manager {


    int iBcl = 0;

    String strTrame = "255255";
    private static final String TABLE_NAME = "Robot";                   //Name of the table
    public static final String COL_ID ="num_id";             //Name of the column witch contain the number of the recording
    public static final String COL_SPEED = "val_moteur";                 //Name of the column witch contain the recording of the robot's movement

    public static final String CREATE_TABLE_ROBOT =                     //SQL request to create the table
            "CREATE TABLE "+TABLE_NAME + " " +
            " (" + " "+ COL_ID +" INTEGER PRIMARY KEY AUTOINCREMENT ,"
            + " "+ COL_SPEED +" TEXT" + ");";


    private MySQLite maBaseSQLite;

    private SQLiteDatabase BDD = null;

    public RobotBDD_Manager(Context context)
    {

        maBaseSQLite = MySQLite.getInstance(context);

    }

    public void open()     // Open the database to read/write
    {
        BDD = maBaseSQLite.getWritableDatabase();
    }

    public void close()     //Close the access to the  database
    {
        BDD.close();
    }


    public long add_speed(Robot  robot) {  // To add a record in the table
        long lTest = -1;

        this.open();
        ContentValues values = new ContentValues();                 //Create a new line in the table
        values.put(this.COL_SPEED, robot.getVal_donnees());         //Add in the column "speed" a new recording
        lTest = BDD.insert(TABLE_NAME,null,values);   //Insert in the database
        this.close();

        return lTest;                                               // return the number of the recording or -1 if an error occurred.

    }


   public int supRobot(Robot robot) {      // To delete a recording

        String where = COL_ID +" = ?";
        String[] whereArgs = {robot.getId_robot()+""};

        //delete() is a method for deleting rows in the database
        return BDD.delete(TABLE_NAME, where, whereArgs); // return the number of lines affected , 0 if not

    }

    public Robot getRobot(int id) {          //Use to returns the recording whose id passed as a parameter

        Robot robot=new Robot(0,"");

        //SQL request to find the recording associate with the ID
        Cursor c = BDD.rawQuery("SELECT * FROM "+TABLE_NAME+" WHERE "+COL_ID+"="+id, null);

        if (c.moveToFirst()) { //Move the cursor to the first row of the request answer , it will return false if the cursor is empty.

            //Retrieve the ID and the recording's value of the first row.
            robot.setId_robot(c.getInt(c.getColumnIndex(COL_ID)));
            robot.setVal_speed(String.valueOf(c.getColumnIndex(COL_SPEED)));
            c.close();
        }
        return robot  ;
    }

    public Cursor getValeurs() { // Select all the recording

        //SQL request to find all the recording
        return BDD.rawQuery("SELECT * FROM "+TABLE_NAME, null);
    }


    public void Delete_all(){    //Use to delete all the recording in the table
            Robot robot ;
            int ID = 0;

            this.open();
            Cursor c = this.getValeurs();
            if (c.moveToLast())   //Move to the last row
            {
                do {
                    ID = c.getInt(c.getColumnIndex(RobotBDD_Manager.COL_ID));  //Retrieve the ID
                    //Log.i("SUP", String.valueOf(ID));
                    robot = this.getRobot(ID);  // Retrieve the recording of the corresponding line
                    this.supRobot(robot);       //Delete this record

                } while (c.moveToPrevious());   //Move the cursor to the previous row, return false if the cursor is already before the first entry in the result set
            }
        }


    public String Read_Database() {  //Use to read and delete the last record of the table
        Robot robot;
        int ID;
        iBcl++;

        if (iBcl > 10) {
            iBcl =0;
            this.open();
            Cursor c = this.getValeurs();
            if (c.moveToLast()) {                                                       //  Move to the last row
                strTrame = c.getString(c.getColumnIndex(RobotBDD_Manager.COL_SPEED));   //  Retrieve the value of the position of the robot

                // Search and delete the ID and the corresponding value
                ID = c.getInt(c.getColumnIndex(RobotBDD_Manager.COL_ID));
                robot = this.getRobot(ID);
                this.supRobot(robot);
            }
            c.close();
            this.close();
        }

        strTrame  = String.format("%06d",Integer.valueOf(strTrame));   //Use to have a fixed weft of 6 digits
        Log.i("LECT", strTrame+"000");
        return strTrame+"000" ;                                        //Return the weft
    }

    public void Inser_data(String strTrame){    //Use to insert a data
        long ID =0;
        int val =0;

        if(strTrame != "255255") {              //Not save the moment where the robot is stopped
            ID = this.add_speed(new Robot(0, strTrame));
            Log.i("AJOUT", strTrame);
        }

    }




}

