/**
 * @Title: Main Activity
 * @project: SmartBarSDP
 * @author: Tyler Rider
 * @dateCreated: January 24, 2015
 */




package com.example.trider.smartbarui;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.nfc.FormatException;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.os.ParcelFileDescriptor;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedInputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * MainActivity is the Debugging Dashboard used for testing communication between the RaspberryPi
 * and the android tablet(or alternate phone connected to raspberry pi).
 */
public class MainActivity extends Activity {

    //Text View and Edit Text for the sending/receiving messages
    TextView mText;
    EditText eText;
    Context context;//App context needed for Toast
    SeekBar sBar;

    //Declares the instances of the connection and USB abstract objects
    Intent intent;
    static UsbManager mUsbManager;
    static UsbAccessory mAccessory;
    static ParcelFileDescriptor mFileDescriptor;

    //Where the file streams are inputted and outputted
    static FileInputStream mInputStream;
    static FileOutputStream mOutputStream;

    //Singleton Class which contains all communication statically
    static CommStream PiComm;

    //Default strings for sending/receiving messages
    String[] tokens;
    String InMessage;
    String OutMessage;
    String TAG = "DebugPy";

    static boolean AppStarted = false;

    // Toggle values for Toggle Buttons
    boolean[] toggle_val = {false,false,false,false,false,false};


    //The Broadcast Receiver to warn the app of connections/disconnections
    DetectUSB detectUSB = new DetectUSB();


    /*Will update the screen based on whatever message was received
    Runnable mUpdateUI = new Runnable() {
        @Override
        public void run() {
            mText.setText(InMessage);
        }
    };*/

    /**
     * @title: mUpdateUI2
     * @description: Parses and splits incoming string from Raspberry Pi, and updates the information
     * onto the Display/ shows appropriate toasts
     */
    Runnable mUpdateUI2 = new Runnable() {
        @Override
        public void run() {
            //mText.setText(OutMessage);
            tokens = InMessage.split("[.]+");
            mText.append("->"+InMessage+ "\n");
            for(int i =0; i < tokens.length; i++){
                //Checks for $AD.command
               if(tokens[i].contains("$AD")){
                    //tokens[i+1] = tokens[i+1].replace("\n", "");
                    mText.append("Analog Value:{" + tokens[i+1] + "}\n");
                    int val = 0;
                    Context context = getApplicationContext();
                   ///Converts AD string to VAL
                    try{
                        String s = new String(tokens[i+1]);
                        val =  Integer.valueOf(s.trim());
                    }catch(NumberFormatException n){
                        Toast toast = Toast.makeText(context,"Error Converting val: "+ n.toString() + "\n",Toast.LENGTH_SHORT);
                        toast.show();
                    }
                    sBar.setProgress(val);
                }else{
                    //mText.append(Integer.toString(tokens[i].compareTo("Error")));
                }
            }
        }
    };



    /**
     * @title: mListenerTask
     * @description: The background thread that receives serial communication from the raspberry pi,
     *
     */
    Runnable mListenerTask = new Runnable() {
        @Override
        public void run() {

            InMessage = PiComm.readString();
            if(InMessage != null){
                mText.post(mUpdateUI2);
            }
            //Waits for new input communication
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //Restarts this thread.
            new Thread(this).start();
        }
    };



/**
 * @title SendCustomText()
 * @description Sending out the user inputted text, by first getting the
 * text in the editText box, converting it to an array of bytes,
 * and tries to write to the output stream contained in separate class*/
    public void SendCustomText(View view){


        //Hides the keyboard after hitting enter.
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);

        //If the USB isn't connected, warn and don't try sending.
        Boolean DEBUG_MODE = false;
        if(DEBUG_MODE){
            eText= (EditText) findViewById(R.id.editText);
            OutMessage = eText.getText().toString();
            DrinkOrder t = new DrinkOrder();
            //t.DecodeString(OutMessage);
            mText.setText(t.DecodeString(OutMessage));
            return;
        }

        if(!DetectUSB.Connection){
            ConnectionNotMadeWarning(view);
            return;
        }

        //Grabs the text from the edit text box, and converts it to string.
        eText= (EditText) findViewById(R.id.editText);
        OutMessage = eText.getText().toString();

        //Writes to output
        if(PiComm.writeString(OutMessage)){
            return;
        }
        //If there is an error writing the output stream. Could be redundant because of DetectUSB.
        context = getApplicationContext();
        Toast toast = Toast.makeText(context,"Error Writing: IO out error",Toast.LENGTH_LONG);
        toast.show();

    }

    /**
     * @title: sendMessage()
     * @description Called when the user clicks one of several buttons. The method sends a preset
     *  message to be decoded my the pi, and updates the appropriate variables.
     * */
    public void sendMessage(View view) {

        //ToggleButton tBut;
        //Button b;

        //view.getId() is the corresponding button that called the method.
        switch (view.getId()) {
            case R.id.hello_pi:
                OutMessage = "Hello Raspberry Pi";
                break;
            case R.id.fuck_pi:
                OutMessage = "FUCK YOU PI";
                break;
            case R.id.toggleButton:
                //Assigns string value based on toggle value, and then toggles the value.
                OutMessage = (toggle_val[0]) ? "LED.OFF" : "LED.ON";
                toggle_val[0] = !toggle_val[0];
                break;
            case R.id.toggleButton2:
                OutMessage = (toggle_val[1]) ? "IO.1.1" : "IO.1.0";
                toggle_val[1] = !toggle_val[1];
                break;
            case R.id.toggleButton3:
                OutMessage = (toggle_val[2]) ? "IO|2,1" : "IO.2.0";
                toggle_val[2] = !toggle_val[2];
                break;
            case R.id.toggleButton4:
                OutMessage = (toggle_val[3]) ? "$LED|0" : "$LED|0";
                toggle_val[3] = !toggle_val[3];
                break;
            case R.id.toggleButton5:
                OutMessage = (toggle_val[4]) ? "$LED.1" : "$LED.1";
                toggle_val[4] = !toggle_val[4];
                break;
            case R.id.toggleButton6:
                //Sends current time over serial link
                int t = (int)System.currentTimeMillis();
                sBar.setProgress(t % 100);
                OutMessage = "$DO|1,0@W,1,1.5";
                toggle_val[5] = !toggle_val[5];
                break;
            default:
                context = getApplicationContext();
                Toast toast = Toast.makeText(context,"Unknown View called send",Toast.LENGTH_SHORT);
                toast.show();
                break;
        }
        if(!DetectUSB.Connection){
            ConnectionNotMadeWarning(view);
            return;
        }

        if(!PiComm.writeString(OutMessage)) {
            context = getApplicationContext();
            Toast toast = Toast.makeText(context,"Error Writing: IO out error",Toast.LENGTH_LONG);
            toast.show();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Creates the incoming buffer text box
        mText = (TextView) findViewById(R.id.display_area);
        mText.setMovementMethod(new ScrollingMovementMethod());
        eText = (EditText) findViewById(R.id.editText);
        sBar = (SeekBar) findViewById(R.id.seekBar);

        sBar.setProgress((int)(System.currentTimeMillis() % 100));
        intent = getIntent();

        /**
        * On Opening the app or app getting started by accessory;
        */
        if (!AppStarted) {
            //Creates a new PiComm
            PiComm = new CommStream("hey");
            mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
            mAccessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);

            //Updates USB image indicator
            ImageView usbConn = (ImageView) findViewById(R.id.usbCon3);
            if(DetectUSB.Connection){
                usbConn.setVisibility(View.VISIBLE);
            }else{
                usbConn.setVisibility(View.INVISIBLE);
            }

            //If the accessory is not there, the PiComm class has yet to be made/instantiated
            //Most likely caused by being opened by User first
            if (mAccessory == null) {
                mText.append("Not started by the Accessory directly" + System.getProperty("line.separator"));
                PiComm.SetStatus(CommStream.Status_Created);
                return;
            }
            //If the device was successfully connected, open open new file streams as per the
            //Android Open Accessory Protocol(AOA).
            Log.v(TAG, mAccessory.toString());
            mFileDescriptor = mUsbManager.openAccessory(mAccessory);
            if (mFileDescriptor != null) {
                FileDescriptor fd = mFileDescriptor.getFileDescriptor();
                mInputStream = new FileInputStream(fd);
                mOutputStream = new FileOutputStream(fd);
                DetectUSB.Connection = true;
                //Creates Singleton class for other activities to use
                PiComm = new CommStream(mInputStream, mOutputStream, mAccessory, mUsbManager, mFileDescriptor);
                AppStarted = true;
            }
            Log.v(TAG, mFileDescriptor.toString());
            eText.clearFocus();
            new Thread(mListenerTask).start();
            /**
             * Returning to this screen for a second time
              */
        }else {
        /*PiComm gets initialized once, and if returning to the main activity, do not make another one*/
            if (PiComm.isInitialized()) {
                context = getApplicationContext();
                Toast toast = Toast.makeText(context, "PiComm already initialized", Toast.LENGTH_LONG);
                toast.show();
                new Thread(mListenerTask).start();
                return;
            }
            //Trying again to get usb upon return to main screen
            //the Usb manager and USB accessory is declared and connected here
            TryToReconnect(null);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /*
    *Warns that USB is not connected.
     */
    public void ConnectionNotMadeWarning(View view){
        context = getApplicationContext();
        Toast toast = Toast.makeText(context,"No device detected, cannot perform task",Toast.LENGTH_SHORT);
        toast.show();
    }
    /*
    *Moves onto next windows
    */
    public void TryNewWindow(View view){
        Intent intent = new Intent(this,TestActivity.class);
        startActivity(intent);
    }


    //Attempts to Reconnect. Currently not working.
    public void TryToReconnect(View view){

        if(DetectUSB.Connection){return;}
        Log.d("Con", "TryingToReconnect");
        intent = getIntent();
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mAccessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);


        if (mAccessory == null) {
            mText.append("Still not connected" + System.getProperty("line.separator"));
            ImageView usbConn = (ImageView) findViewById(R.id.usbCon3);
            usbConn.setVisibility(View.INVISIBLE);
            return;
        }
        //If the device was successfully connected, upon return
        Log.v(TAG, mAccessory.toString());
        mFileDescriptor = mUsbManager.openAccessory(mAccessory);
        if (mFileDescriptor != null) {
            FileDescriptor fd = mFileDescriptor.getFileDescriptor();
            mInputStream = new FileInputStream(fd);
            mOutputStream = new FileOutputStream(fd);
            DetectUSB.Connection = true;
            //Trying to created singleton class to move between other activities
            PiComm = new CommStream(mInputStream, mOutputStream, mAccessory, mUsbManager, mFileDescriptor);
        }
        Log.v(TAG, mFileDescriptor.toString());
        eText.clearFocus();
        new Thread(mListenerTask).start();
    }

    /*
    public void DecodeString(String s){

    }
    */


    public void ClearWindow(View view){
        mText.setText("");
    }
}




