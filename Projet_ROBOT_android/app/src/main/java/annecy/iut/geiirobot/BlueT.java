package annecy.iut.geiirobot;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.*;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;



public class BlueT {

    private static final String TAG = "BTT";

    private BluetoothAdapter mbtAdapt; //BT adapter of the phone
    private ControlRobotActivity mBTActivity; //main activity who instantiate blueT -> association
    private BluetoothDevice[]mPairedDevices;// table of known devices
    private int mDeviceSelected = -1; //the device choosen by the phone
    private String[] mstrDeviceName;
    public boolean mbtConnected = false;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");  // dummy UUID
    private BluetoothSocket mSocket;
    private Handler mHandler;
    public Boolean mbtActif;
    public String mstrRecu = " ";


    public BlueT(){}

    public BlueT(ControlRobotActivity BTActivity)
    {
        this.mBTActivity = BTActivity;
        this.Check();
    }
    public BlueT(ControlRobotActivity BTActivity, Handler Handler)
    {
        this.mBTActivity = BTActivity;
        this.mHandler = Handler;
        this.Check();
        Thread mThreadReception =null;	//thread that receives data from device
        mThreadReception = new Thread(new Runnable() { //create Thread for reception
            @Override
            public void run() {

                while(true)
                {
                    //Log.i(TAG, "etat="+mbtActif);
                    if(mbtAdapt != null)
                    {   //Log.i(TAG, "etat="+mbtAdapt);
                        if(mbtAdapt.isEnabled())
                        {
                            mbtActif = true;
                            //Log.i(TAG, "etat="+mbtActif);
                        }
                        else
                        {
                            mbtActif = false;
                            //Log.i(TAG, "etat="+mbtActif);
                        }
                    }

                    if(mbtConnected == true) // reception of data when connected
                    {

                        mstrRecu = Receive();
                        if (!mstrRecu.equals("")) { // if there is something -> send message to the handler of the activity
                            Message msg = mHandler.obtainMessage();
                            msg.obj = mstrRecu;
                            mHandler.sendMessage(msg);
                            //Log.i("Recu", mstrRecu);
                        }
                        //else
                        //Log.i("mstrRecu", "vide");
                    }
                    try {
                        Thread.sleep(10, 0); // this has to be lower than the period of the robot
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                        //Log.i("IT", "mstrRecu");
                    }
                }
            }
        });
        mThreadReception.start(); //start thread
    }

    public void Check() // Verification of BT adapter
    {
        mbtAdapt = BluetoothAdapter.getDefaultAdapter(); // recover BT informations on adapter
        if(mbtAdapt == null) {
            Log.i(TAG, "Not presentt");
        }
        else {
            Log.i(TAG, "Present");
        }
    }

    public void Connection() // connection to device
    {
        this.DeviceKnow(); //recover informations for each connected devices
        AlertDialog.Builder adBuilder = new AlertDialog.Builder(mBTActivity);//pop up off knoxn devices
        //        adBuilder.setTitle("device");
        //miDeviceDelected = mDeviceSelected;
        adBuilder.setSingleChoiceItems(mstrDeviceName, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mDeviceSelected = which;
                dialog.dismiss();
                Tryconnect(); //connection to the chosen device
            }
        });

        AlertDialog adb = adBuilder.create();
        adb.show();
    }

    public void DeviceKnow() // recover all known devices
    {   Set<BluetoothDevice> Devices; //liste of mDevices
        int iBlc = 0;				//used by connection
        Devices = mbtAdapt.getBondedDevices(); //recover the devices in a tab
        iBlc = Devices.size(); // number of known devices
        this.mstrDeviceName = new String[iBlc]; //table will be given to pop up menu
        iBlc = 0;
        for(BluetoothDevice dev : Devices) {
            this.mstrDeviceName[iBlc] = dev.getName();
            iBlc = iBlc + 1;
        }
        this.mPairedDevices = (BluetoothDevice[]) Devices.toArray(new BluetoothDevice[0]); //cast of set in array.
    }

    public void Tryconnect()
    {
        try {
            this.mSocket =this.mPairedDevices[this.mDeviceSelected].createRfcommSocketToServiceRecord(MY_UUID); //connection to vhchoosen device via Socket, mUUID: id of BT on device of the target
            this.mSocket.connect();
            Toast.makeText(this.mBTActivity, "Connected", Toast.LENGTH_SHORT).show();
            this.mBTActivity.buttonConnectBT.setVisibility(View.GONE);                              //buttonConnectBT become useless when BT connection is good
            this.mbtConnected = true;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this.mBTActivity, "Try again", Toast.LENGTH_SHORT).show();
            try {
                mSocket.close();
            }
            catch(Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    public Boolean Send(String strOrdre) // false -> error; true -> ok
    {   OutputStream OutStream;	//mSocket for communication
        try	{
            OutStream = this.mSocket.getOutputStream(); //open output stream

            byte[] trame = strOrdre.getBytes();

            OutStream.write(trame); //send frame via output stream
            OutStream.flush();
            Log.i(TAG, "Send");
        }
        catch(Exception e2) {
            Log.i(TAG, "Error");
            Tryconnect();
            try {
                this.mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.mbtConnected = false;
        }
        return this.mbtConnected;
    }
    private String Receive()
    {   InputStream InStream;		//mSocket for communication
        byte mbBuffer[] = new byte[200]; // large buffer !
        byte myByte;
        int iPos=0;
        int iNbLu = 0;
        String mstrData = "";
        try {
            InStream = this.mSocket.getInputStream();// input stream

            /*if(InStream.available() > 0 ) {
                // inBLu = number of characters
                // the following part has to be improved
                iNbLu=InStream.read(mbBuffer,iPos,199); // be aware -> a complete frame is not received
                mstrData = new String(mbBuffer,0,iNbLu); //create a string using byte received
            }*/


                while (InStream.available() > 0) {
                    myByte = (byte) InStream.read();
                    mbBuffer[iPos] = myByte;
                    if (myByte == 0) {
                        break;
                    }
                    iPos++;
                }



            mstrData = new String(mbBuffer, 0,iPos); //create a string using byte received
        }
        catch (Exception e) {
            Log.i(TAG, "Error");
            try {
                mSocket.close();
            }
            catch (IOException e1) {
                e1.printStackTrace();
            }
            this.mbtConnected = false;
        }
        return mstrData;
    }



}

