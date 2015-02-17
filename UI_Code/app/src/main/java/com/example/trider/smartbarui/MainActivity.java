package com.example.trider.smartbarui;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedInputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class MainActivity extends Activity {

    //Text View and Edit Text for the sending/receiving messages
    TextView mText;
    EditText eText;

    //Delcares the instances of the connection
    Intent intent;
    static UsbManager mUsbManager;
    static UsbAccessory mAccessory;
    static ParcelFileDescriptor mFileDescriptor;
    String[] tokens;
    //Where the file streams are inputted and outputted
    static FileInputStream mInputStream;
    static FileOutputStream mOutputStream;

    //Singleton Class
    static CommStream PiComm;

    //Default strings for sending/receiving messages
    String InMessage;
    String OutMessage;
    String mMessage3;
    String TAG = "DebugPy";


    static boolean ConnectionMade =false;
    //App context needed for toast
    Context context;


    /*Toggle Values*/
    boolean[] toggle_val = {false,false,false,false,false,false};


    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            context = getApplicationContext();
            Toast toast = Toast.makeText(context,"Broadcast Received" + intent.getAction().toString(),Toast.LENGTH_LONG);
            toast.show();
        }
    };



    //Will update the screen based on whatever message was received
    Runnable mUpdateUI = new Runnable() {
        @Override
        public void run() {
            mText.setText(InMessage);
        }
    };
//Can update screen on what was sent
    Runnable mUpdateUI2 = new Runnable() {
        @Override
        public void run() {
            //mText.setText(OutMessage);
            tokens = InMessage.split("[.]");
            for(int i =0; i < tokens.length; i++){
                mText.append(tokens[i] + "\n");
                if(tokens[i].contains("Error")){
                    mText.append("makToast");
                    Context context = getApplicationContext();
                    Toast toast = Toast.makeText(context,"Error"+tokens[i+1],Toast.LENGTH_SHORT);
                    toast.show();
                }else{
                    //mText.append(Integer.toString(tokens[i].compareTo("Error")));
                }
            }
        }
    };

    //The background task to read in input from the accessory
    Runnable mListenerTask = new Runnable() {
        @Override
        public void run() {

            byte[] buffer = new byte[32];
            //ret is the size of the size of the incoming buffer
            int ret;
            try {
                //InMessage = "> ";
                ret = mInputStream.read(buffer);
                if (ret < 32) {
                    String msg = new String(buffer);
                    InMessage = msg;

                    mText.post(mUpdateUI2);
                } else {

                }
            } catch (IOException e) {
                e.printStackTrace();
                /*
                    context = getApplicationContext();
                    Toast toast = Toast.makeText(context,"Error Reading: ret >= 32",Toast.LENGTH_LONG);
                    toast.show();*/
            }

            //mText.post(mUpdateUI);
            /**
             *
             * //??String Decode Method will be implemented here.
             *
             *
             *
             *
             */



            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            new Thread(this).start();
        }
    };



/**
 * SendCustomText()
 * Sending out the user inputted text, by first getting the
 * text in the editText box, converting it to an array of bytes,
 * and trying to write to the output stream*/
    public void SendCustomText(View view){


        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);


        if(!ConnectionMade){
            ConnectionNotMadeWarning(view);
            return;
        }

        eText= (EditText) findViewById(R.id.editText);
        byte[] outBuffer;

        OutMessage = eText.getText().toString();
        outBuffer = OutMessage.getBytes();
        //Writes to output
        try {
            mOutputStream.write(outBuffer);
        } catch (IOException e) {
            e.printStackTrace();
            context = getApplicationContext();
            Toast toast = Toast.makeText(context,"Error Writing: IO out error",Toast.LENGTH_LONG);
            toast.show();
        }
    }

    /** sendMessage
     * Called when the user clicks a button.
     * The method sends a preset message to be decoded my the PI, and updates
     * the appropriate variables
     * */
    public void sendMessage(View view) {

        ToggleButton tBut;
        Button b;
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
                OutMessage = (toggle_val[2]) ? "IO.2.1" : "IO.2.0";
                toggle_val[2] = !toggle_val[2];
                break;
            case R.id.toggleButton4:
                OutMessage = (toggle_val[3]) ? "$LED.0" : "$LED.0";
                toggle_val[3] = !toggle_val[3];
                break;
            case R.id.toggleButton5:
                OutMessage = (toggle_val[4]) ? "$LED.1" : "$LED.1";
                toggle_val[4] = !toggle_val[4];
                break;
            case R.id.toggleButton6:
                OutMessage = (toggle_val[5]) ? "$LED" : "$LED";
                toggle_val[5] = !toggle_val[5];
                break;
            default:
                context = getApplicationContext();
                Toast toast = Toast.makeText(context,"Unknown View called send",Toast.LENGTH_LONG);
                toast.show();
                break;
        }
        if(!ConnectionMade){
            ConnectionNotMadeWarning(view);
            return;
        }

/*
        byte[] buffer = new byte[5];
        buffer[0] = (byte) 'A';
        for (int i = 1; i < 5; i++) {
            buffer[i] = (byte) direction;
        }*/
        /*Converts string into serializable mesage to be sent over Comm Link*/
        byte[] buffer2 = new byte[256] ;
        buffer2 = OutMessage.getBytes();

        try {
            mOutputStream.write(buffer2);
        } catch (IOException e) {
            e.printStackTrace();
            context = getApplicationContext();
            Toast toast = Toast.makeText(context,"Error Writing: IO out error",Toast.LENGTH_LONG);
            toast.show();
        }

        //mText.post(mUpdateUI2); // Can optionally view sent messages

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Creates the incoming buffer text box
        mText = (TextView) findViewById(R.id.display_area);
        mText.setMovementMethod(new ScrollingMovementMethod());
        eText = (EditText) findViewById(R.id.editText);

        intent = getIntent();
        PiComm = new CommStream("hey");


        /*PiComm gets initialized once, and if returning to the main activity, do not make another one*/
        if(PiComm.isInitialized()){
            context = getApplicationContext();
            Toast toast = Toast.makeText(context,"PiComm already initialized",Toast.LENGTH_LONG);
            toast.show();

/*
            mUsbManager =   PiComm.getUSB();
            mAccessory =    PiComm.getAcc();
            mInputStream =  PiComm.getIStream();
            mOutputStream = PiComm.getOStream();*/
            //new Thread(mListenerTask).start();
            toast = Toast.makeText(context,"PiComm Reset",Toast.LENGTH_LONG);
            toast.show();
            new Thread(mListenerTask).start();
            return;
        }
        //the Usb manager and USB accessory is declared and connected here
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mAccessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);

        if (mAccessory == null) {
            mText.append("Not started by the Accessory directly" + System.getProperty("line.separator"));
            PiComm.writeString("Not Started yet");
            ImageView usbConn = (ImageView) findViewById(R.id.usbCon3);
            usbConn.setVisibility(View.INVISIBLE);
            return;
        }





        //If the device was successfully connected, open open new file streams
        Log.v(TAG, mAccessory.toString());
        mFileDescriptor = mUsbManager.openAccessory(mAccessory);
        if (mFileDescriptor != null) {
            FileDescriptor fd = mFileDescriptor.getFileDescriptor();
            mInputStream = new FileInputStream(fd);
            mOutputStream = new FileOutputStream(fd);
            ConnectionMade = true;
            //Trying to created singleton class to move between other activities
            //PiComm = new CommStream(mInputStream, mOutputStream);
            PiComm = new CommStream(mInputStream,mOutputStream,  mAccessory, mUsbManager,mFileDescriptor);
        }
        Log.v(TAG, mFileDescriptor.toString());
        eText.clearFocus();
        new Thread(mListenerTask).start();

//
//        InputMethodManager inputManager = (InputMethodManager)
//                getSystemService(Context.INPUT_METHOD_SERVICE);
//
//        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
//                InputMethodManager.HIDE_NOT_ALWAYS);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }



    public void ConnectionNotMadeWarning(View view){
        context = getApplicationContext();
        Toast toast = Toast.makeText(context,"No device detected, cannot perform task",Toast.LENGTH_LONG);
        toast.show();
    }

    public void TryNewWindow(View view){

        Intent intent = new Intent(this,TestActivity.class);
        startActivity(intent);
    }




    public void TryToReconnect(View view){

        if(ConnectionMade){return;}

        intent = getIntent();
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mAccessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);



        //PiComm = new CommStream("hey");


        if (mAccessory == null) {
            context = getApplicationContext();
            Toast toast = Toast.makeText(context,"Failed To Connect to Pi",Toast.LENGTH_LONG);
            toast.show();
            return;
        }

        //If the device was successfully connected, open open new file streams
        Log.v(TAG, mAccessory.toString());
        mFileDescriptor = mUsbManager.openAccessory(mAccessory);
        if (mFileDescriptor != null) {
            FileDescriptor fd = mFileDescriptor.getFileDescriptor();
            mInputStream = new FileInputStream(fd);
            mOutputStream = new FileOutputStream(fd);
            ConnectionMade = true;
            //Trying to created singleton class to move between other activities
            //PiComm = new CommStream(mInputStream, mOutputStream);
            PiComm = new CommStream(mInputStream,mOutputStream,  mAccessory, mUsbManager,mFileDescriptor);
        }
        Log.v(TAG, mFileDescriptor.toString());
        new Thread(mListenerTask).start();
        ConnectionMade = true;
    }

}




