/**
 * @Title: Main Activity
 * @project: SmartBarSDP
 * @author: Tyler Rider
 * @dateCreated: January 24, 2015
 */




package com.example.trider.smartbarui;
import android.content.Context;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.os.ParcelFileDescriptor;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;


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

    private static final String Q_URL = "http://www.ucscsmartbar.com/getQ.php";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    Boolean searchFailure = false;
    JSONParser jsonParser = new JSONParser();
    Boolean searching = false;

    //Default strings for sending/receiving messages
    String[] tokens;
    String InMessage;
    String OutMessage;
    String TAG = "DebugPy";
    String[] ltokens;


    static boolean AppStarted = false;
    boolean isActive = true;

    // Toggle values for Toggle Buttons
    boolean[] toggle_val = {false,false,false,false,false,false};


    //The Broadcast Receiver to warn the app of connections/disconnections
    DetectUSB detectUSB = new DetectUSB();


    /**
     * @title: mListenerTask
     * @description: The background thread that receives serial communication from the raspberry pi,
     *
     */

    Runnable mListenerTask = new Runnable() {
        @Override
        public void run() {
        if(isActive) {
            InMessage = PiComm.readString();

            if (InMessage != null) {
                //Toast.makeText(getApplicationContext(),"got somethin",Toast.LENGTH_SHORT).show();
                mText.post(mUpdateUI2);
            }
            //Waits for new input communication
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //Restarts this thread.
        }
                new Thread(this).start();
//          }
        }
    };



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
    @Override
    public void onStop(){
        super.onStop();
        Log.d("LIFE","Main-- onStop");
        //isActive = false;
    }
    protected void onResume(){
        super.onResume();
        //PiComm.writeString("Resume");
        isActive = true;
    }

    /**
     * OnCreate goes through a system walkthrough and sets appropriate flags for program use
     * @param savedInstanceState
     */
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

        //mText.setText("Current Time....%l",System.currentTimeMillis());

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
                mText.append("USB Connection..... Detected\n");
            }else{
                usbConn.setVisibility(View.INVISIBLE);
                mText.setText("USB Connection.......... Not Detected\n");
            }

            //If the accessory is not there, the PiComm class has yet to be made/instantiated
            //Most likely caused by being opened by User first
            if (mAccessory == null) {
                mText.append("Not started by the Accessory directly" + System.getProperty("line.separator"));
                PiComm.SetStatus(CommStream.Status_Created);
                //TODO Add loop to continuously try to connect to R-Pi
                mText.append("Raspberry Pi........... Not Connected\n");
                //Try Server anyway
                new AttemptGetQ().execute();
                searching = true;
                mText.append("Checking Server communication......\n");
                //while(searching);
                //Trying again to get usb upon return to main screen
                //the Usb manager and USB accessory is declared and connected here
                TryToReconnect(null);
                return;
            }
            //If the device was successfully connected, open open new file streams as per the
            //Android Open Accessory Protocol(AOA).
            mText.append("Raspberry Pi......... Connected\n");
            Log.v(TAG, mAccessory.toString());
            mFileDescriptor = mUsbManager.openAccessory(mAccessory);
            if (mFileDescriptor != null) {
                FileDescriptor fd = mFileDescriptor.getFileDescriptor();
                mInputStream = new FileInputStream(fd);
                mOutputStream = new FileOutputStream(fd);
                DetectUSB.Connection = true;

                //Console Log
                mText.append("\tFileDescriptor....."+mFileDescriptor.toString()+"\n");
                mText.append("\tInputString........"+mInputStream.toString()+   "\n");
                mText.append("\tOutputString......."+mOutputStream.toString()+  "\n");
                //Creates Singleton class for other activities to use
                PiComm = new CommStream(mInputStream, mOutputStream, mAccessory, mUsbManager, mFileDescriptor);
                mText.append("CommStream......... created\n");

                PiComm.writeString("Hello Raspberry");
                AppStarted = true;
                new AttemptGetQ().execute();
                searching = true;
                mText.append("Checking Server communication......\n");

                //while(searching);
                new Thread(mListenerTask).start();
                //Trying again to get usb upon return to main screen
                //the Usb manager and USB accessory is declared and connected here
                //TryToReconnect(null);
            }
            //Log.v(TAG, mFileDescriptor.toString());
            eText.clearFocus();

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
            }

        }
    }

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

    //For testing Inventory, it automatically stocks it with different types of whiskey
    public void stockInventory(View view) {
            eText = (EditText) findViewById(R.id.editText);
            OutMessage = eText.getText().toString();
            OutMessage = "$IV,2,2@0,WH,1,54.3,59.2@1,GN,1,49.9,59.2@16,BO,2,3.4,37.1";
            String s = new SystemCodeParser().DecodeAccessoryMessage(OutMessage);
            Inventory inventory = new Inventory();
            inventory.AddToInventory(3,"WH","Windsor Canadian",39.2,39.2);
            inventory.AddToInventory(4,"WH","Rich and Rare",39.2,39.2);
            inventory.AddToInventory(5,"WH","Wild Turkey",39.2,39.2);
            inventory.AddToInventory(6,"WH","Seagram's",39.2,39.2);
            inventory.AddToInventory(7,"WH","Kessler",39.2,39.2);
            inventory.AddToInventory(8,"WH","Canadian Club",39.2,39.2);
            inventory.AddToInventory(9,"WH","Dewar's Scotch",39.2,39.2);
            inventory.AddToInventory(10,"WH","Candian Mist",39.2,39.2);
            inventory.AddToInventory(11,"WH","Jack Daniel's Tennessee Honey",39.2,39.2);
            inventory.AddToInventory(12,"WH","Evan William's",39.2,39.2);
            inventory.AddToInventory(13,"WH","Southern Comfort",39.2,39.2);
            inventory.AddToInventory(14,"WH","Black Velvet",39.2,39.2);
            inventory.AddToInventory(15,"WH","Jameson Irish",39.2,39.2);
            inventory.AddToInventory(16,"WH","Seagram's 7 Crown",39.2,39.2);
            inventory.AddToInventory(18,"WH","Fireball",39.2,39.2);
//            inventory.AddToInventory(19,"WH","Jim Bean",39.2,39.2);
//            inventory.AddToInventory(20,"WH","Crown Royal",39.2,39.2);
            mText.append("Stocking Bar.......... Whiskey\n");


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
//                OutMessage = (toggle_val[0]) ? "LED.OFF" : "LED.ON";
//                toggle_val[0] = !toggle_val[0];
                OutMessage = "$FPIDEN,STRTHD";

                break;
            case R.id.toggleButton2:
//                OutMessage = (toggle_val[1]) ? "IO.1.1" : "IO.1.0";
//                toggle_val[1] = !toggle_val[1];
                OutMessage = "$FPIDEN,ENDTHD";
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
        //isActive = false;
        startActivity(new Intent(this,MainMenu.class));
    }


    public void TestKeyboard(View view){
        startActivity(new Intent(this,TestKeyBoard.class));
    }
    //Attempts to Reconnect. Currently not working.
    public void TryToReconnect(View view){

        if(DetectUSB.Connection){return;}
        Log.d("Con", "TryingToReconnect");
        intent = getIntent();
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mAccessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);


        if (mAccessory == null) {
            //mText.append("Still not connected" + System.getProperty("line.separator"));
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


    public void ClearWindow(View view){
        mText.setText("");
    }

    /**Grabs the Queue Initially**/
    class AttemptGetQ extends AsyncTask<String, String, String> {
        int success;
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("AGD","Pre-Exec");
        }

        @Override
        protected String doInBackground(String... args) {
            try
            {
                Log.d("AGD", "Mid-Execute");
                Log.d("request!", "starting");
                // getting product details by making HTTP request

                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("pin", "1"));
                JSONObject json = jsonParser.makeHttpRequest(Q_URL, "POST", params);

                // check your log for json response
                Log.d("Q", json.toString());
                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("Q", json.toString());
                    searchFailure = false;
                    return json.getString(TAG_MESSAGE);
                } else {
                    Log.d("Q", json.getString(TAG_MESSAGE));
                    searchFailure = true;
                    return json.getString(TAG_MESSAGE);
                }
            }catch(JSONException e){
                e.printStackTrace();
            } catch(NullPointerException npe){
                npe.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(String file_url) {
            // dismiss the dialog once product deleted
            searching = false;
            Toast.makeText(getApplicationContext(),file_url,Toast.LENGTH_SHORT).show();
            if (file_url != null){
                Log.d("IDLE",file_url);
                ltokens = file_url.split("[,]");
                mText.append("Connection to server...........Success\n");
                mText.append("\t......."+ltokens.length+" drink(s) on Q\n");
                mText.append("\t Current Q:"+file_url+"\n");
            }else{
                mText.append("Connection to server.......Failed\n");
            }

        }
    }

/**Default System Functions**/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


}




