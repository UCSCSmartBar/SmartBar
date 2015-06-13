/**
 * @Title: Main Activity
 * @project: SmartBarSDP
 * @author: Tyler Rider
 * @dateCreated: January 24, 2015
 */




package com.example.trider.smartbarui;
import android.app.ActionBar;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.os.ParcelFileDescriptor;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.paypal.android.MEP.CheckoutButton;
import com.paypal.android.MEP.PayPal;
import com.paypal.android.MEP.PayPalActivity;
import com.paypal.android.MEP.PayPalInvoiceData;
import com.paypal.android.MEP.PayPalPayment;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * MainActivity is the Debugging Dashboard used for testing communication between the RaspberryPi
 * and the android tablet(or alternate phone connected to raspberry pi).
 */
public class MainActivity extends Activity {

    /***********Sets how the app will start******************/
    /********************************************************/
    final private static Boolean _CONSOLE_START = false;

/***********************************************************/
    //Text View and Edit Text for the sending/receiving messages
    TextView mText;
    EditText eText;
    Context context;//App context needed for Toast
    //SeekBar sBar;

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

    boolean reconnection = false;

    //Server Acces related variables

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    Boolean searchFailure = false;
    JSONParser jsonParser = new JSONParser();
    Boolean searching = false;

    //Default strings for sending/receiving messages
    String[] tokens;
    String InMessage;
    String OutMessage;
    String TAG = "DebugPi";
    String[] ltokens;

    //Lifetime variabels
    static boolean AppStarted = false;
    boolean isActive = true;

    // Toggle values for Toggle Buttons
    boolean[] toggle_val = {false,false,false,false,false,false};
    //The Broadcast Receiver to warn the app of connections/disconnections
    DetectUSB detectUSB = new DetectUSB();
    SystemCodeParser SCP;
    MediaPlayer mp;


    private static final int REQUEST_CODE_VENMO_APP_SWITCH = 1;
    public static final String _VERSION_NUMBER = ".9.7.6";



    private static final String PI_Manufacturer  = "SmartBar";

    /**********************************************************************************************/
    /*************************Background Methods***************************************************/
    /**********************************************************************************************/
    /**
     * @title: mListenerTask
     * @description: The background arbiter that receives serial communication from the raspberry pi,
     * and sends the message appropriately
     *
     */

    Runnable mListenerTask = new Runnable() {
        @Override
        public void run() {
            try {
                if (isActive) {
                    InMessage = PiComm.readString();
                    String res = SystemCodeParser.DecodeAccessoryMessage(InMessage);
                    String out = null;
                    if (InMessage != null) {
                        mText.post(mUpdateUI2);
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //Restarts this thread.
                }
            }catch(NullPointerException npe){
                npe.printStackTrace();
            }
            if(PiComm.isDisconnected()){
                finish();
            }else{
                new Thread(this).start();
            }
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
            //Toast.makeText(getApplicationContext(),"In:"+InMessage,Toast.LENGTH_SHORT).show();
            tokens = InMessage.split("[.]+");
            mText.append("->" + InMessage + "\n");
        }
    };




    /**
     * @title: mListenerTask
     * @description: The background arbiter that receives serial communication from the raspberry pi,
     * and sends the message appropriately
     *
     */

    Runnable accWatcherTask = new Runnable() {
        @Override
        public void run() {
            try {
                mText.post(mUpdateUI);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //Restarts this thread.
                if(!reconnection){
                    new Thread(this).start();
                }
            }catch(NullPointerException npe){
                npe.printStackTrace();
            }

        }
    };
    /**
     * @title: mUpdateUI2
     * @description: Parses and splits incoming string from Raspberry Pi, and updates the information
     * onto the Display/ shows appropriate toasts
     */
    Runnable mUpdateUI = new Runnable() {
        @Override
        public void run() {
            //mText.setText(OutMessage);
            try {
                mText.append("Current Accessory List:\n");
                if (mUsbManager != null) {
                    for (UsbAccessory usb : mUsbManager.getAccessoryList()) {
                        mText.append("\t Device:" + usb + "\n");
                        reconnection = TryConnectToAccessory(usb);
                    }
                } else {
                    mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
                }
                mText.append("Reconnected...." + String.valueOf(reconnection) + "\n");

            }catch(NullPointerException npe){
                mText.append("Oops in MUI\n");
                npe.printStackTrace();
            }

        }
    };

    /**********************************************************************************************/
    /***********************************Entry/Exit Methods*****************************************/
    /**********************************************************************************************/

    @Override
    public void onStop(){
        super.onStop();
        Log.d("LIFE","Main-- onStop");

        //isActive = false;
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.d("LIFE", "Main--- onDestroy");
        CommStream.CleanUp();
        finish();
        String filename =  String.format("Android %d",System.currentTimeMillis());
//        File file = new File(context.getFilesDir(),filename);
//        FileOutputStream outputStream;
//        try {
//            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
//            outputStream.write(mText.getText().toString().getBytes());
//        }catch(IOException ioe){
//            ioe.printStackTrace();
//        }
//        Toast.makeText(getApplicationContext(),"Goodbye",Toast.LENGTH_SHORT).show();
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
        Log.d("LIFE", "Main+++ onCreate");
        setContentView(R.layout.activity_main);

        //setDefaultKeyMode(DEFAULT_KEYS_DISABLE);
        hideSystemUI();

        SCP = new SystemCodeParser();

        //SCP.DecodeAccessoryMessage("$AD");
        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.theme);
        //mp.start();


        //Editing Views
        //Creates the incoming buffer text box
        mText = (TextView) findViewById(R.id.display_area);
        mText.setMovementMethod(new ScrollingMovementMethod());
        eText = (EditText) findViewById(R.id.editText);
        eText.clearFocus();
        intent = getIntent();

        //TODO Test functions
        /**********************************************************************/
        //new ResetFP().execute();
        new AttemptGetCust().execute();

        //new AttemptGetPhone().execute();
        //new AttemptGetPhone().execute();

//        new Timer().schedule(new TimerTask() {
//            @Override
//            public void run() {
//                new CheckExecCommand().execute();
//            }
//        },1000);

        /***********************************************************************/

        /**
         * On Opening the app or app getting started by accessory;
         */

        //Creates a new PiComm
        PiComm = new CommStream("New Instance");
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mAccessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);

        //Updates USB image indicator
        ImageView usbConn = (ImageView) findViewById(R.id.usbCon3);

        mText.setText("SmartBar UI version:" + _VERSION_NUMBER + "\n");
        mText.setText("Started at:" + (int) (System.currentTimeMillis() / 1000) + "\n");


        if(DetectUSB.Connection){
            usbConn.setVisibility(View.VISIBLE);
            mText.append("USB Connection..... Detected\n");
        }else{
            usbConn.setVisibility(View.INVISIBLE);
            mText.append("USB Connection.......... Not Detected\n");
        }

        initPPLibrary();
        mText.append("Initialized PayPal Library\n");

        //TODO TOKEN
        //new AttemptGetBTToken().execute();

        Inventory inv = new Inventory();
        mText.append("Initializing Inventory\n");

        new AttemptGetInventory().execute();//Gets current Inventory from DataBase


//        inv.AddToInventory(1,"WH","0",0,59.3,59.3,1.00);
//        inv.AddToInventory(2,"GN","0",0,59.3,59.3,2.00);

        //If the accessory is not there, the PiComm class has yet to be made/instantiated
        //Most likely caused by being opened by User first
        if (mAccessory == null) {
            mText.append("Not started by the Accessory directly." + System.getProperty("line.separator"));
            PiComm.SetStatus(CommStream.Status_Created);
            //TODO Add loop to continuously try to connect to R-Pi
            mText.append("Raspberry Pi........... Not Connected\n");
            //new Thread(accWatcherTask).start();

        }else {
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
                mText.append("\tFileDescriptor....." + mFileDescriptor.toString() + "\n");
                mText.append("\tInputStream........" + mInputStream.toString() + "\n");
                mText.append("\tOutputStream......." + mOutputStream.toString() + "\n");

                mText.append("\tAccessory Model...." + mAccessory.getModel() + "\n");
                mText.append("\tAccessory Descript." + mAccessory.getDescription() + "\n");
                mText.append("\tAccessory Manf....." + mAccessory.getManufacturer() + "\n");
                mText.append("\tAccessory Uri......" + mAccessory.getUri()+"\n");//TODO What is URI?


                //Creates Singleton class for other activities to use
                PiComm = new CommStream(mInputStream, mOutputStream, mAccessory, mUsbManager, mFileDescriptor);
                mText.append("CommStream......... created\n");
//                if(CommStream.Beagle.isInitialized()){
//                    mText.append("Beagle is on Watch......\n");
//                }

                new Thread(mListenerTask).start();
                //new Thread(accWatcherTask).start();//TODO don't need to watch if already connected
                CommStream.writeString("Hello Raspberry");
                PiComm.PiLog(this.getLocalClassName(), DrinkOrder.OrderStatus());
                AppStarted = true;

            }
            //Log.v(TAG, mFileDescriptor.toString());

            //PayPal.initWithAppID()
        }


        new AttemptGetQ().execute();
        searching = true;
        mText.append("Checking Server communication......\n");
        DrinkStrings.CreateLibrary();
        mText.append("Loaded Drink Library Codes\n");

        mText.append("Finding Venmo...\n");
        if(VenmoLibrary.isVenmoInstalled(getApplicationContext() )){
            mText.append("Found Venmo\n");
        }else{
            mText.append("Venmo not Found\n");
        }

            eText.clearFocus();
        //startActivity(new Intent(MainActivity.this, NewUserFinger.class));
        if(!_CONSOLE_START){startActivity(new Intent(MainActivity.this,MainMenu.class).putExtra("START",true));}

    }


    /**********************************************************************************************/
    /****************************************Console Buttons***************************************/
    /**********************************************************************************************/
    public void CalibrateCupSensors(View view){
        switch(view.getId()) {
            case R.id.cupCalib_ON:
                CommStream.writeString("$CUP,CAL,ON");
                break;
            case R.id.cupCalib_OFF:
                CommStream.writeString("$CUP,CAL,OFF");
                break;
            default:
                break;


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
//        Boolean DEBUG_MODE = false;
//        if(DEBUG_MODE){
//            eText= (EditText) findViewById(R.id.editText);
//            OutMessage = eText.getText().toString();
//            DrinkOrder t = new DrinkOrder();
//            //t.DecodeString(OutMessage);
//            mText.setText(t.DecodeString(OutMessage));
//            return;
//        }

        if(!DetectUSB.Connection){
            ConnectionNotMadeWarning(view);
            return;
        }

        //Grabs the text from the edit text box, and converts it to string.
        eText= (EditText) findViewById(R.id.editText);
        OutMessage = eText.getText().toString();

        //Writes to output
        if(CommStream.writeString(OutMessage)){
            return;
        }
        //If there is an error writing the output stream. Could be redundant because of DetectUSB.
        context = getApplicationContext();
        Toast toast = Toast.makeText(context, "Error Writing: IO out error", Toast.LENGTH_LONG);
        toast.show();

    }

    /**
     *For testing Inventory, it automatically stocks it with different types of whiskey
     */
    public void stockInventory(View view) {
            eText = (EditText) findViewById(R.id.editText);
            OutMessage = eText.getText().toString();
            //TODO no longer use carbonated identifiers

            Inventory inventory = new Inventory();
            inventory.AddToInventory(1,"WH","1",0,59.2,59.2,4.00);
            inventory.AddToInventory(2,"GN","0",0,59.2,59.2,4.00);
            inventory.AddToInventory(3,"KL","0",0,59.2,59.2,4.00);
            inventory.AddToInventory(4,"GN","1",0,59.2,59.2,5.00);
            inventory.AddToInventory(5,"VO","1",0,59.2,59.2,4.50);
            inventory.AddToInventory(6,"WH","0",0,59.2,59.2,3.25);
            inventory.AddToInventory(7,"CG","0",0,59.2,59.2,1.00);
            inventory.AddToInventory(8,"GT","0",0,59.2,59.2,3.40);
            inventory.AddToInventory(9,"GT","1",0,59.2,59.2,14.00);
            inventory.AddToInventory(10,"WH","Candian Mist",0,59.2,59.2,4.00);
            inventory.AddToInventory(11,"WH","Jack Daniel's Tennessee Honey",0,59.2,59.2,3.00);
            inventory.AddToInventory(12,"WH","Evan William's",0,59.2,59.2,2.50);
            inventory.AddToInventory(13,"WH", "Southern Comfort",0, 59.2,59.2,3.24);
            inventory.AddToInventory(14,"WH","Black Velvet",0,59.2,59.2,6.00);
            inventory.AddToInventory(15,"WH","Jameson Irish",0,59.2,59.2,5.55);
            inventory.AddToInventory(16,"WH","Seagram's 7 Crown",0,59.2,59.2,4.3);
            inventory.AddToInventory(18,"WH","Fireball",0,59.2,59.2,3.2);
            OutMessage = "$IV,2,2@0,WH,1,54.3,59.2@1,GN,0,49.9,59.2@16,BO,2,3.4,37.1";
            //SystemCodeParser.DecodeAccessoryMessage(OutMessage);
//            inventory.AddToInventory(19,"WH","Jim Bean",39.2,39.2,5.6);
//            inventory.AddToInventory(20,"WH","Crown Royal",39.2,39.2,3.4);
            mText.append("Stocking Bar.......... Whiskey\n");
            Inventory.LiquidContainerObj LCO = inventory.searchInventory("WH", "0");
        if(LCO!=null) {
            String str = Double.toString(LCO.getPricePerOz());
            mText.append(str + "\n");
        }
        //Sound.playImSorry(getApplicationContext());
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
//            case R.id.fuck_pi:
//                OutMessage = "FUCK YOU PI";
//                break;
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
//            case R.id.toggleButton3:
////                Intent venmoIntent = VenmoLibrary.openVenmoPayment(appId, appName, recipient, amount, note, txn);
////                startActivityForResult(venmoIntent, REQUEST_CODE_VENMO_APP_SWITCH);
//
//                break;
            case R.id.toggleButton4:
                //Sound.playRegister(getApplicationContext());
                CommStream.TEST_HARNESS = !CommStream.TEST_HARNESS;
                String t = (CommStream.TEST_HARNESS)?"ON":"OFF";
                mText.append("Test Harness toggled " + t + "\n");



                //stockInventory(view);
                //mText.append("Stocking Sample Inventory\n");
                break;
            case R.id.toggleButton5:
                //Sound.playThankYouForRegister(getApplicationContext());
                //mp.start();

                //Intent venmoIntent = VenmoLibrary.openVenmoPayment(appId, appName, recipient, amount, note, txn);
                //startActivityForResult(venmoIntent, REQUEST_CODE_VENMO _APP_SWITCH);

            //startActivity(new Intent(this,VenmoWebViewActivity.class).putExtra("url","http://www.google.com"));

                startActivity(new Intent(this,CheckBAC.class));
                OutMessage = null;
                break;
            case R.id.toggleButton6:
                //Sends current time over serial link
//                int time = (int)System.currentTimeMillis();
//                sBar.setProgress(time % 100);
//                OutMessage = "$DO,1,0@W,1,1.5";
//                toggle_val[5] = !toggle_val[5];

                OutMessage = "$DS,$QD";
                CommStream.SYSTEM_STATUS = CommStream.OFF;
                //startActivity( new Intent(MainActivity.this, TabbedActivity.class));

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
        if(OutMessage != null && !CommStream.writeString(OutMessage)) {
            context = getApplicationContext();
            Toast toast = Toast.makeText(context,"Error Writing: IO out error",Toast.LENGTH_LONG);
            toast.show();
        }

    }

    /**
     * Enables Debug Mode for the rest of the system. Enables Toasts to be displayed, allows skipping
     * also shows incoming messages from the pi in the middle of the activities. Normally Off.
     * @param view
     */
    public void ToggleDebugMode(View view){

        CommStream.DEBUG_MODE = !CommStream.DEBUG_MODE;
        if(CommStream.DEBUG_MODE){
            Sound.playDebugOn(getApplicationContext());
        }else{
            Sound.playDebugOff(getApplicationContext());
        }
    }

    /**
     * Clears the Console Log
     * @param view
     */
    public void ClearWindow(View view){
        mText.setText("");
    }

    /**
     * Tests the Drink decode function within the DrinkStrings Library
     * @param view
     */
    public void DecodeDrinkCode(View view){
        String s = eText.getText().toString();
        mText.append("\n CODE:" + s + "\tString:" + DrinkStrings.CodeToString(s) + "\n");
    }

    /**
     * Starts the SmartBar into its Main System Menu
     * @param view
     */
    public void StartSB(View view){
        //isActive = false;
        startActivity(new Intent(this,MainMenu.class));
    }

//    /**
//     * Goes To another class to test the inline Keyboard.
//     * @param view
//     */
//    public void TestKeyboard(View view){
//        startActivity(new Intent(this, TestKeyBoard2.class));
//    }

    /**
     * System Warning whenever an attempt to send a message to the Pi cannot be completed.
     * @param view
     */
    public void ConnectionNotMadeWarning(View view){
        context = getApplicationContext();
        Toast toast = Toast.makeText(context,"No device detected, cannot perform task",Toast.LENGTH_SHORT);
        toast.show();
    }


    //Attempts to Reconnect. Currently not working.

    //Attempts to connect to detected accessory if not already in accessory mode
    public boolean TryConnectToAccessory(UsbAccessory usbAccessory){
        if(usbAccessory == null){return false;}
        if(usbAccessory.getManufacturer().equals(PI_Manufacturer)){
            mText.append("Found Accessory"+usbAccessory.getManufacturer()+"\n");
            mText.append( "It is"+ usbAccessory.toString()+"\n");
                mFileDescriptor = mUsbManager.openAccessory(usbAccessory);
                mText.append("Trying to open accessory.... \n");
                if (mFileDescriptor != null) {
                    mText.append("Successfully opened Accessory.\n");
                    FileDescriptor fd = mFileDescriptor.getFileDescriptor();
                    mInputStream = new FileInputStream(fd);
                    mOutputStream = new FileOutputStream(fd);
                    DetectUSB.Connection = true;
                    //Console Log
                    mText.append("\tFileDescriptor....." + mFileDescriptor.toString() + "\n");
                    mText.append("\tInputStream........" + mInputStream.toString() + "\n");
                    mText.append("\tOutputStream......." + mOutputStream.toString() + "\n");
                    mText.append("\tAccessory Model...." + usbAccessory.getModel() + "\n");
                    mText.append("\tAccessory Descript." + usbAccessory.getDescription() + "\n");
                    mText.append("\tAccessory Manf....." + usbAccessory.getManufacturer() + "\n");
                    mText.append("\tAccessory Uri......" + usbAccessory.getUri() + "\n");//TODO What is URI?

                    //PiComm = new CommStream(mInputStream,mOutputStream,usbAccessory,mUsbManager,mFileDescriptor);
                    //new Thread(mListenerTask).start();
                    try{
                        String s = "Hello Again";
                        byte[] outBuffer;
                        outBuffer = s.getBytes();
                        mOutputStream.write(outBuffer);
//                        int ret = mInputStream.read(outBuffer);
//                        mText.append(outBuffer.toString());
                    }catch(IOException ioe){
                        ioe.printStackTrace();
                        mText.append("Error writing to PI\n"+ioe.getMessage()+"\n");
                    }

                    try{
                        byte[] buffer = new byte[512];
                        mInputStream.read(buffer);
                    }catch(IOException ioe){
                        ioe.printStackTrace();
                        mText.append("Error reading from PI\n"+ioe.getMessage()+"\n");
                    }

                    reconnection = true;
                    return true;
                }else{
                    mText.append("File descriptor is still null");
                    return false;
                }
        }


        mText.append("Found non-smartbar accessory\n"+usbAccessory.getManufacturer()+"\n");
        return false;
    }


    /**********************************************************************************************/
    /******************************************PAYMENT_TEST*****************************************/
    /**
     * Initializes the PayPal Library found at the developers developer.paypal.com
     */
    public void initPPLibrary() {
        PayPal pp = PayPal.getInstance();

        if (pp == null) {  // Test to see if the library is already initialized

            // This main initialization call takes your Context, AppID, and target server
            pp = PayPal.initWithAppID(this, "APP-80W284485P519543T", PayPal.ENV_NONE);

            // Required settings:

            // Set the language for the library
            pp.setLanguage("en_US");

            // Some Optional settings:

            // Sets who pays any transaction fees. Possible values are:
            // FEEPAYER_SENDER, FEEPAYER_PRIMARYRECEIVER, FEEPAYER_EACHRECEIVER, and FEEPAYER_SECONDARYONLY
            pp.setFeesPayer(PayPal.FEEPAYER_EACHRECEIVER);

            // true = transaction requires shipping
            pp.setShippingEnabled(true);

            //_paypalLibraryInit = true;
        }
    }

    //    private void showPayPalButton() {
//
//// Generate the PayPal checkout button and save it for later use
//        PayPal pp = PayPal.getInstance();
//        CheckoutButton launchPayPalButton = pp.getCheckoutButton(this, PayPal.BUTTON_278x43, CheckoutButton.TEXT_PAY);
//
//// The OnClick listener for the checkout button
//        launchPayPalButton.setOnClickListener(this);
//
//// Add the listener to the layout
//        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams (RadioGroup.LayoutParams.WRAP_CONTENT,
//                RadioGroup.LayoutParams.WRAP_CONTENT);
//        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//        params.bottomMargin = 10;
//        launchPayPalButton.setLayoutParams(params);
//        launchPayPalButton.setId(PAYPAL_BUTTON_ID);
//        ((RelativeLayout) findViewById(R.id.main_layout)).addView(launchPayPalButton);
//        ((RelativeLayout) findViewById(R.id.main_layout)).setGravity(Gravity.CENTER_HORIZONTAL);
//    }

//    public void PayPalButtonClick(View arg0) {
//// Create a basic PayPal payment
//        PayPalPayment payment = new PayPalPayment();
//
//        String _theSubtotal = "5.00";
//        String _taxAmount = "3.00";
//
//// Set the currency type
//        payment.setCurrencyType("USD");
//
//// Set the recipient for the payment (can be a phone number)
//        payment.setRecipient("ppalav_1285013097_biz@yahoo.com");
//
//// Set the payment amount, excluding tax and shipping costs
//        payment.setSubtotal(new BigDecimal(_theSubtotal));
//
//// Set the payment type--his can be PAYMENT_TYPE_GOODS,
//// PAYMENT_TYPE_SERVICE, PAYMENT_TYPE_PERSONAL, or PAYMENT_TYPE_NONE
//        payment.setPaymentType(PayPal.PAYMENT_TYPE_GOODS);
//
//// PayPalInvoiceData can contain tax and shipping amounts, and an
//// ArrayList of PayPalInvoiceItem that you can fill out.
//// These are not required for any transaction.
//        PayPalInvoiceData invoice = new PayPalInvoiceData();
//
//// Set the tax amount
//        invoice.setTax(new BigDecimal(_taxAmount));
//    }
//
//
//
//    public void PayPalActivityResult(int requestCode, int resultCode, Intent intent) {
//        switch (resultCode) {
//// The payment succeeded
//            case Activity.RESULT_OK:
//                String payKey = intent.getStringExtra(PayPalActivity.EXTRA_PAY_KEY);
//                //this.paymentSucceeded(payKey);
//                Toast.makeText(getApplicationContext(),"RESULT_OK",Toast.LENGTH_SHORT).show();
//                break;
//
//// The payment was canceled
//            case Activity.RESULT_CANCELED:
//                Toast.makeText(getApplicationContext(),"RESULT_CANCELED",Toast.LENGTH_SHORT).show();
//                //this.paymentCanceled();
//                break;
//
//// The payment failed, get the error from the EXTRA_ERROR_ID and EXTRA_ERROR_MESSAGE
//            case PayPalActivity.RESULT_FAILURE:
//                String errorID = intent.getStringExtra(PayPalActivity.EXTRA_ERROR_ID);
//                String errorMessage = intent.getStringExtra(PayPalActivity.EXTRA_ERROR_MESSAGE);
//                Toast.makeText(getApplicationContext(),"RESULT_FAILURE"+errorID+errorMessage,Toast.LENGTH_SHORT).show();
//                //this.paymentFailed(errorID, errorMessage);
//        }
//    }


/****************************************************************************************************/
/****************************************************************************************************/

/***********************Server Access Test Threads****************************************************/


    /**Grabs the Queue Initially**/
    class AttemptGetQ extends AsyncTask<String, String, String> {
        int success;

        @Override
        protected String doInBackground(String... args) {
            try
            {
                Log.d("AGD", "Mid-Execute");

                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("pin", "1"));
                Log.d("Q","Address:"+ServerAccess.GET_QUEUE_URL);
                JSONObject json = jsonParser.makeHttpRequest(ServerAccess.GET_QUEUE_URL, "POST", params);

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
            //Toast.makeText(getApplicationContext(),file_url,Toast.LENGTH_SHORT).show();
            if (file_url != null){
                Log.d("IDLE",file_url);
                ltokens = file_url.split("[,]");
                mText.append("Connection to server...........Success\n");
                mText.append("\t......."+(ltokens.length-1)+" drink(s) on Q\n");
                mText.append("\t Current Q:"+file_url+"\n");
                CommStream._INTERNETCONN = CommStream.ON;
            }else{
                mText.append("Connection to server.......Failed\n");
            }

        }
    }


    //Testing various server-related functions
    class AttemptGetInventory extends AsyncTask<String, String, String> {
        int success;
        @Override
        protected String doInBackground(String... args) {
            try {
                Log.d("AGI", "Mid-Execute");
                // getting product details by making HTTP request
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                Log.d("AGI", "HTTP getting Posted:" + ServerAccess.GET_INV);
                JSONObject json = jsonParser.makeHttpRequest(ServerAccess.GET_INV, "POST", params);

                // check your log for json response
                Log.d("AGI", json.toString());
                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("AGI", "Success String:" + json.toString());
                    searchFailure = false;
                    return json.getString(TAG_MESSAGE);
                } else {
                    Log.d("AGI", "Failure with :" + json.getString(TAG_MESSAGE));
                    searchFailure = true;
                    return json.getString(TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException npe) {
                npe.printStackTrace();
            }

            return null;

        }

        protected void onPostExecute(String file_url) {
            // dismiss the dialog once product deleted
            if (file_url != null) {
                //Toast.makeText(DisplayQueue.this, file_url, Toast.LENGTH_LONG).show();
                Log.d("AGI", "Returned URL:" + file_url);
                SystemCodeParser.DecodeAccessoryMessage(file_url);
            } else {
                Toast.makeText(MainActivity.this, "Failure to Access Server. Check Internet Connection"
                        , Toast.LENGTH_SHORT).show();
            }
        }
    }

    class AttemptIncreaseDrinkCount extends AsyncTask<String, String, String> {
        int success;


        @Override
        protected String doInBackground(String... args) {
            try {
                Log.d("IDC", "Mid-Execute");
                // getting product details by making HTTP request

                String PinToIncrease = "12345678901";
                Log.d("IDC", "Pin getting Posted:" + PinToIncrease);
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("pin", PinToIncrease));
                Log.d("IDC", "HTTP getting Posted:" + ServerAccess.COUNTER_URL);
                JSONObject json = jsonParser.makeHttpRequest(ServerAccess.COUNTER_URL, "POST", params);

                // check your log for json response
                Log.d("IDC", json.toString());
                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("IDC", "Success String:" + json.toString());
                    searchFailure = false;
                    return json.getString(TAG_MESSAGE);
                } else {
                    Log.d("IDC", "Failure with :" + json.getString(TAG_MESSAGE));
                    searchFailure = true;
                    return json.getString(TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException npe) {
                npe.printStackTrace();
            }

            return null;

        }

        protected void onPostExecute(String file_url) {
            // dismiss the dialog once product deleted
            if (file_url != null) {
                //Toast.makeText(DisplayQueue.this, file_url, Toast.LENGTH_LONG).show();
                Log.d("IDC", "Returned URL:" + file_url);

                //SystemCodeParser.CupReady = false;

            } else {
                Toast.makeText(MainActivity.this, "Failure to Access Server. Check Internet Connection"
                        , Toast.LENGTH_SHORT).show();
            }
        }
    }
    //Gets Client Token for Braintree SandBox
    class AttemptGetBTToken extends AsyncTask<String, String, String> {
        int success;


        @Override
        protected String doInBackground(String... args) {
            try {
                Log.d("BTT", "Mid-Execute");
                // getting product details by making HTTP request

                String token = "token";
                Log.d("BTT", "Pin getting Posted:" + token);
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("pin", token));
                Log.d("BTT", "HTTP getting Posted:" + ServerAccess.GET_TOKEN);
                JSONObject json = jsonParser.makeHttpRequest(ServerAccess.GET_TOKEN, "POST", params);

                // check your log for json response
                Log.d("BTT", json.toString());
                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("BTT", "Success String:" + json.toString());
                    searchFailure = false;
                    return json.getString(TAG_MESSAGE);
                } else {
                    Log.d("BTT", "Failure with :" + json.getString(TAG_MESSAGE));
                    searchFailure = true;
                    return json.getString(TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException npe) {
                npe.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String file_url) {
            // dismiss the dialog once product deleted
            if (file_url != null) {
                //Toast.makeText(DisplayQueue.this, file_url, Toast.LENGTH_LONG).show();
                Log.d("BTT", "Returned URL:" + file_url);
                //mText.append("Client Token" + file_url);
            } else {
                Toast.makeText(MainActivity.this, "Failure to Access Server. Check Internet Connection"
                        , Toast.LENGTH_SHORT).show();
            }
        }
    }


    class AttemptGetPhone extends AsyncTask<String, String, String> {
        int success;

        @Override
        protected String doInBackground(String... args) {
            try {

                String token = "tylerjrider@gmail.com";
                Log.d("AGP", "Pin getting Posted:" + token);
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("username", token));
                Log.d("AGP", "HTTP getting Posted:" + ServerAccess.FIND_PHONE);
                JSONObject json = jsonParser.makeHttpRequest(ServerAccess.FIND_PHONE, "POST", params);

                // check your log for json response
                Log.d("AGP", json.toString());
                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("AGP", "Success String:" + json.toString());

                    return json.getString(TAG_MESSAGE);
                } else {
                    Log.d("AGP", "Failure with :" + json.getString(TAG_MESSAGE));

                    return json.getString(TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException npe) {
                npe.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String file_url) {
            // dismiss the dialog once product deleted
            if (file_url != null) {
                Log.d("AGP", "Returned URL:" + file_url);
            } else {
                Toast.makeText(MainActivity.this, "Failure to Access Server. Check Internet Connection"
                        , Toast.LENGTH_SHORT).show();
            }
        }
    }

    //Returns the Braintree ID from the related user
    class AttemptGetCust extends AsyncTask<String, String, String> {
        int success;
        @Override
        protected String doInBackground(String... args) {
            try {
                Log.d("AGC", "Mid-Execute");
                // getting product details by making HTTP request

                //String token = "token";
                String pinToPost = "18316013559";
                Log.d("AGC", "Pin getting Posted:" + pinToPost);
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("phone", pinToPost));
                Log.d("AGC", "HTTP getting Posted:" + ServerAccess.GET_CUST_ID);
                JSONObject json = jsonParser.makeHttpRequest(ServerAccess.GET_CUST_ID, "POST", params);

                // check your log for json response
                Log.d("AGC", json.toString());
                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("AGC", "Success String:" + json.toString());
                    searchFailure = false;
                    return json.getString(TAG_MESSAGE);
                } else {
                    Log.d("AGC", "Failure with :" + json.getString(TAG_MESSAGE));
                    searchFailure = true;
                    return json.getString(TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException npe) {
                npe.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String file_url) {
            // dismiss the dialog once product deleted
            if (file_url != null) {
                //Toast.makeText(DisplayQueue.this, file_url, Toast.LENGTH_LONG).show();
                Log.d("AGC", "Returned URL:" + file_url);
                new AttemptPostPayment().execute(file_url);

            } else {
                Log.d("AGC","file_url is null");
                Toast.makeText(MainActivity.this, "Failure to Access Server. Check Internet Connection"
                        , Toast.LENGTH_SHORT).show();
            }
        }
    }

    class AttemptPostPayment extends AsyncTask<String, String, String> {
        int success;
        @Override
        protected String doInBackground(String... args) {
            try {
                Log.d("APP", "Mid-Execute");
                // getting product details by making HTTP request
                Log.d("APP","Incoming Customer Id:"+args[0]);
                //String token = "token";
                String customerID = args[0];
                Log.d("APP", "Pin getting Posted:" + customerID);
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("customerID", customerID));
                params.add(new BasicNameValuePair("amount","0.05"));

                Log.d("APP", "HTTP getting Posted:" + ServerAccess.PAY_URL);
                JSONObject json = jsonParser.makeHttpRequest(ServerAccess.PAY_URL, "POST", params);

                // check your log for json response
                Log.d("APP", json.toString());
                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("APP", "Success String:" + json.toString());
                    searchFailure = false;
                    return json.getString(TAG_MESSAGE);
                } else {
                    Log.d("APP", "Failure with :" + json.getString(TAG_MESSAGE));
                    searchFailure = true;
                    return json.getString(TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException npe) {
                npe.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String file_url) {
            // dismiss the dialog once product deleted
            if (file_url != null) {
                //Toast.makeText(DisplayQueue.this, file_url, Toast.LENGTH_LONG).show();
                Log.d("APP", "Returned URL:" + file_url);
                mText.append("Attempt Test Payment....." + file_url);
            } else {
                Log.d("APP","file_url is null");
                Toast.makeText(MainActivity.this, "Failure to Access Server. Check Internet Connection"
                        , Toast.LENGTH_SHORT).show();
            }
        }
    }
    //Checks if that user has a fingerprint
    class CheckForFP extends AsyncTask<String, String, String> {
        int success;


        @Override
        protected String doInBackground(String... args) {
            try {
                Log.d("HFP", "Mid-Execute");
                // getting product details by making HTTP request

                String PinToIncrease = "10000102108";
                Log.d("HFP", "Pin getting Posted:" + PinToIncrease);
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("pin", PinToIncrease));
                JSONObject json = jsonParser.makeHttpRequest(ServerAccess.HAS_FP, "POST", params);

                // check your log for json response
                Log.d("HFP", json.toString());
                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("HFP", "Success String:" + json.toString());
                    searchFailure = false;
                    return json.getString(TAG_MESSAGE);
                } else {
                    Log.d("HFP", "Failure with :" + json.getString(TAG_MESSAGE));
                    searchFailure = true;
                    return json.getString(TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException npe) {
                npe.printStackTrace();
            }

            return null;

        }

        protected void onPostExecute(String file_url) {
            // dismiss the dialog once product deleted
            if (file_url != null) {
                //Toast.makeText(DisplayQueue.this, file_url, Toast.LENGTH_LONG).show();
                Log.d("HFP", "Returned URL:" + file_url);

                //SystemCodeParser.CupReady = false;

            } else {
                Toast.makeText(MainActivity.this, "Failure to Access Server. Check Internet Connection"
                        , Toast.LENGTH_SHORT).show();
            }
        }
    }
    //Resets the users fingerprint//TODO Needs to delete it also
    class ResetFP extends AsyncTask<String, String, String> {
        int success;


        @Override
        protected String doInBackground(String... args) {
            try {
                Log.d("RFP", "Mid-Execute");
                // getting product details by making HTTP request

                String PinToDelFP = "10000002108";
                Log.d("RFP", "Pin getting Posted:" + PinToDelFP);
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("pin", PinToDelFP));
                JSONObject json = jsonParser.makeHttpRequest(ServerAccess.RESET_FP, "POST", params);

                // check your log for json response
                Log.d("RFP", json.toString());
                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("RFP", "Success String:" + json.toString());
                    searchFailure = false;
                    return json.getString(TAG_MESSAGE);
                } else {
                    Log.d("RFP", "Failure with :" + json.getString(TAG_MESSAGE));
                    searchFailure = true;
                    return json.getString(TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException npe) {
                npe.printStackTrace();
            }

            return null;

        }
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once product deleted
            if (file_url != null) {
                //Toast.makeText(DisplayQueue.this, file_url, Toast.LENGTH_LONG).show();
                Log.d("RFP", "Returned URL:" + file_url);

                //SystemCodeParser.CupReady = false;

            } else {
                Toast.makeText(MainActivity.this, "Failure to Access Server. Check Internet Connection"
                        , Toast.LENGTH_SHORT).show();
            }
        }
    }
    /**Default System Functions**/

    class CheckExecCommand extends AsyncTask<String, String, String> {
        int success;


        @Override
        protected String doInBackground(String... args) {
            try {
                Log.d("CEXEC","StartTime"+System.currentTimeMillis());
                // getting product details by making HTTP request

                String ExecPin = "18313940478";
                Log.d("CEXEC", "Pin getting Posted:" + ExecPin);
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("pin", ExecPin));
                JSONObject json = jsonParser.makeHttpRequest(ServerAccess.GET_DRINK_URL, "POST", params);

                // check your log for json response
                Log.d("CEXEC", json.toString());
                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("CEXEC", "Success String:" + json.toString());
                    searchFailure = false;
                    return json.getString(TAG_MESSAGE);
                } else {
                    Log.d("CEXEC", "Failure with :" + json.getString(TAG_MESSAGE));
                    searchFailure = true;
                    return json.getString(TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException npe) {
                npe.printStackTrace();
            }

            return null;

        }
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once product deleted
            if (file_url != null) {
                //Toast.makeText(DisplayQueue.this, file_url, Toast.LENGTH_LONG).show();
                Log.d("CEXEC", "Returned URL:" + "["+file_url+"]");
                if(file_url.contains("KILL")){
                    CommStream.writeString("$DS,$QD");
                }
                new ClearExecCommand().execute();
                Log.d("CEXEC", "EndTime" + System.currentTimeMillis());
                //SystemCodeParser.CupReady = false;

            } else {
                Toast.makeText(MainActivity.this, "Failure to Access Server. Check Internet Connection"
                        , Toast.LENGTH_SHORT).show();
            }
        }
    }


    class ClearExecCommand extends AsyncTask<String, String, String> {
        int success;

        @Override
        protected String doInBackground(String... args) {
            try {
                Log.d("XEXEC", "Mid-Execute");
                // getting product details by making HTTP request

                String ExecPin = "18313940478";
                Log.d("XEXEC", "Pin getting Posted:" + ExecPin);
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("pin", ExecPin));
                params.add(new BasicNameValuePair("drink","00000000000"));
                params.add(new BasicNameValuePair("username","exec"));

                JSONObject json = jsonParser.makeHttpRequest(ServerAccess.ADD_DRINK_URL, "POST", params);

                // check your log for json response
                Log.d("XEXEC", json.toString());
                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("XEXEC", "Success String:" + json.toString());
                    searchFailure = false;
                    return json.getString(TAG_MESSAGE);
                } else {
                    Log.d("XEXEC", "Failure with :" + json.getString(TAG_MESSAGE));
                    searchFailure = true;
                    return json.getString(TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException npe) {
                npe.printStackTrace();
            }

            return null;

        }
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once product deleted
            if (file_url != null) {
                //Toast.makeText(DisplayQueue.this, file_url, Toast.LENGTH_LONG).show();
                Log.d("XEXEC", "Returned URL:" + file_url);


                //SystemCodeParser.CupReady = false;

            } else {
                Toast.makeText(MainActivity.this, "Failure to Access Server. Check Internet Connection"
                        , Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * Attempts to Override main menu buttons
     * @param keyCode The Key pressed
     * @param event Unused
     * @return
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d("KEYBOARD","KEY:"+keyCode +"\tEvent"+ event.toString());

    if(keyCode == KeyEvent.KEYCODE_BACK){
        if(CommStream.DEBUG_MODE){Toast.makeText(this, "You pressed the back button!", Toast.LENGTH_LONG).show();}
        return true;
    }else if(keyCode == KeyEvent.KEYCODE_WINDOW){
        if(CommStream.DEBUG_MODE){Toast.makeText(this, "You pressed the window button!", Toast.LENGTH_LONG).show();}
        //return false;
    }else{
        if(CommStream.DEBUG_MODE){Toast.makeText(this,"You Pressed the:"+keyCode,Toast.LENGTH_LONG).show();}

    }
    return super.onKeyDown(keyCode, event);

}

    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        View mDecorView;
        mDecorView = getWindow().getDecorView();
        mDecorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


}




