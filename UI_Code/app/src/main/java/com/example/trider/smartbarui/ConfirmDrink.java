package com.example.trider.smartbarui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.paypal.android.MEP.CheckoutButton;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class ConfirmDrink extends Activity {

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    Boolean searchFailure = false;
    JSONParser jsonParser = new JSONParser();

    Boolean deleting = true;
    Boolean isActive = false;

    CommStream PiComm = new CommStream();
    DrinkOrder Do = new DrinkOrder();

    Boolean pouring = false;
    Boolean cupChanged = false;
    Boolean WATER_ONLY;

    MediaPlayer mp;
    TextView DrinkListing;
    String nameToDecode;

    byte warningCode = 0x0;
    String warningMsg;



    public enum CupState {
        DRINKNOTPOURED,
        DRINKPOURED,
        DRINKPOURING,
        PURGED
    }
    CupState curstate = CupState.DRINKNOTPOURED;

    ProgressDialog progress;//Alert user that drink is pouring dialog

    Timer WatchDog = new Timer();
    TimerTask WatchTask = new TimerTask() {
        @Override
        public void run() {
            Log.d("NewUser", "Timeout");
            //finish();
            if(isActive) {
                startActivity(new Intent(ConfirmDrink.this, IdleMenu.class));
            }
        }
    };


    /**
     * Background Listening task
     */
    Timer backG;
    TimerTask T= new TimerTask() {
        @Override
        public void run() {
            if (isActive) {
                ConfirmDrink.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        /**
                         * @subTimeTask3
                         */
                        String t = CommStream.ReadBuffer();

                        if(PiComm.isDisconnected()){
                            finish();
                        }
                        //new SystemCodeParser().DecodeAccessoryMessage(t);
                        if (t != null) {
                            RunPourDrinkSM(t);
                        }
                        //Changes the Pour Drink Button color.
                        if(cupChanged && !pouring && (curstate == CupState.DRINKNOTPOURED)) {
                            try {
                                if (DrinkOrder.CupReady) {
                                    Button b = (Button) findViewById(R.id.pourdrinkBut);
                                    b.setText("Pour Drink");
                                    b.setTextColor(Color.parseColor("#ff000000"));
                                    b.setBackground(getResources().getDrawable(R.drawable.pour_drink_on));
                                } else {
                                    Button b = (Button) findViewById(R.id.pourdrinkBut);
                                    b.setText("Place Cup");
                                    b.setTextColor(Color.parseColor("#ffffffff"));
                                    b.setBackground(getResources().getDrawable(R.drawable.pour_drink_off));
                                }
                            } catch (NullPointerException npe) {
                                npe.printStackTrace();
                            }
                            cupChanged = false;
                        }
                    }
                });
            }else{
                //Ends this timertask
                //this.cancel();
            }

        }
    };

    @Override
    public void onStop(){
        super.onStop();
        try {
            isActive = false;
            PiComm.PiLog("onStop()");
            CommStream.writeString("$CUP,END");
            //mp.stop();
            T.cancel();

            backG.cancel();
        }catch(NullPointerException npe){
            npe.printStackTrace();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DrinkOrder.CupReady = false;
        Log.d("CUP", "Setting CupReady to true in 2s");
        PiComm.PiLog(this.getLocalClassName(), DrinkOrder.OrderStatus());

        setContentView(R.layout.activity_confirm_drink);
        hideSystemUI();
        new Timer().scheduleAtFixedRate(HideTask, 100, 100);
        isActive = true;


//TODO Discards the image until stock photos are implemented
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setVisibility(View.INVISIBLE);
        DrinkListing = (TextView) findViewById(R.id.drink_listing);
        findViewById(R.id.pour_progress).setVisibility(View.INVISIBLE);


        pouring = false;
        curstate = CupState.DRINKNOTPOURED;


        backG = new Timer();
        backG.schedule(T, 1, 100);



        /***************Test Harness Options**********************/

        if(!CommStream.DEBUG_MODE){
            Sound.playPlaceCup(getApplicationContext());
        }else{
            DrinkListing.append(DrinkOrder.InUserPinString+"\n"+DrinkOrder.InDrinkString);
        }
        if(CommStream.TEST_HARNESS){
            DrinkOrder.InDrinkString = "3,0@VO,1,0.5@GN,1,2.0@KL,1,.25";
            nameToDecode = DrinkOrder.InDrinkString;
            Log.d("CD","IDS:"+ DrinkOrder.InDrinkString);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(6000);
                        //DrinkOrder.CupReady = true;
                        CommStream.StoreBuffer("$CUP,TIMEOUT");
                        Thread.sleep(3000);
                        CommStream.StoreBuffer("$CUP,PLACED");
                        Thread.sleep(2000);
                        CommStream.StoreBuffer("CUP,REMOVED");
                        Thread.sleep(3000);
                        CommStream.StoreBuffer("$CUP,PLACED");
                        //Log.d("CUP","CupReady is: "+DrinkOrder.CupReady);
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                }
            }).start();
        }


        /**************************************************************************/

    if(getIntent().getBooleanExtra("Water",false)) {
        WATER_ONLY = true;
        DrinkListing.setText("Water");
        ((TextView) findViewById(R.id.price_listing)).setText("FREE");
    }else{
        WATER_ONLY= false;
        /**Sets the final "receipt" of the order and displays it.**/
        if (Do.getCurrentDrinkOrder() != null) {
            //Gets the Price
            DrinkListing.setText(Do.DecodeString(Do.getCurrentDrinkOrder()));
        }
        nameToDecode = DrinkOrder.InDrinkString;
        String IDP = DrinkOrder.getDrinkPriceString();
        Log.d("CD", "IDP" + IDP);


        ((TextView) findViewById(R.id.price_listing)).setText("$" + IDP);
        //DrinkListing.append("\nTotal:" + IDP);
        new AttemptGetDrinkName().execute();
    }

       // Toast.makeText(getApplicationContext(),"Extra is"+getIntent().getBooleanExtra("Water",false),Toast.LENGTH_SHORT);
        /********************/
        //Begin cup detection system.
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                CommStream.writeString("$CUP,START");
            }
        }, 1000);
        WatchDog.schedule(WatchTask, 50000);//45s timeout
        if(!WATER_ONLY) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    ConfirmData();
                }
            }, 4000);
        }

    }

    /**
     * Double check that all the neccessary information t o
     */
    public void ConfirmData(){
        Boolean DrinkStatus = true;

        if(DrinkOrder.InUserPinString == null || DrinkOrder.InUserPinString.contains("00000000000")){
            DrinkStatus = false;
            warningCode |= 0x1;
        }
        Log.d("#CO","Number"+DrinkOrder.InUserPinString);


        if(DrinkOrder.InDrinkString == null || !DrinkOrder.InDrinkString.contains("@")){
            DrinkStatus = false;
            warningCode |= 0x2;
        } //Number
        Log.d("#CO","Drink Recipe"+DrinkOrder.InDrinkString);      //DrinkString

        //DrinkName
        if(DrinkOrder.InDrinkNameString == null || DrinkOrder.InDrinkNameString.contains("failed") || DrinkOrder.InDrinkNameString.contains("Failed")){
            DrinkStatus = false;
            warningCode |= 0x4;
        }
        Log.d("#CO","Drink Name"+DrinkOrder.InDrinkNameString);


        //BrainTreeId
        if(DrinkOrder.InBrainID == null || DrinkOrder.InBrainID.contains("not found")){
            DrinkStatus = false;
            warningCode |= 0x8;
        }
        Log.d("#CO","BrainTreeID"+DrinkOrder.InBrainID);


        if(DrinkOrder.InDrinkPrice < 1.00){
            DrinkStatus = false;
            warningCode |= 0x10;
        }
        Log.d("#CO","Drink Price"+DrinkOrder.getDrinkPriceString());


        if(!DrinkStatus){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Missing Parameter for Drink Order Completion", Toast.LENGTH_LONG).show();
                }
            });

        }



    }

    public void PayForDrink(){
        PiComm.PiLog("Paying For Drink");
        new AttemptGetCust().execute();

    }

    public void PourDrink(View view){
        if(DrinkOrder.CupReady && !pouring) {
            pouring = true;
            //TODO removed cup end from here
            //CommStream.writeString("$CUP,END");

            if(WATER_ONLY){
                CommStream.writeString("$AK");
                CommStream.writeString("$DS,$DO,0,1@WA,0,0,5.0");
            }else {
                if (Do.getCurrentDrinkOrder() != null) {
                    CommStream.writeString("$DS,$DO," + Do.getCurrentDrinkOrder());
                }
            }
            curstate = CupState.DRINKPOURING;
            //DrinkOrder.InUserPinString = MyApplication.myPin;
            findViewById(R.id.pour_progress).setVisibility(View.VISIBLE);

            progress = ProgressDialog.show(this, "Drink Pouring","Please do not remove cup", true);

            progress.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
            progress.getWindow().addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            //progress.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            progress.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
            //Toast.makeText(getApplicationContext(),"Please Wait For Drink To Finish Pouring",Toast.LENGTH_LONG).show();

            if(CommStream.TEST_HARNESS){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(3000);
                            CommStream.StoreBuffer("$DS,NA,DO");
                        }catch (InterruptedException ie){
                            ie.printStackTrace();
                        }
                    }
                }).start();
            }
        }else{
            if(CommStream.DEBUG_MODE){Toast.makeText(getApplicationContext(),"Raspberry Pi Not Ready",Toast.LENGTH_SHORT).show();}
        }





    }

    public void FinishOrder(){
//        MyApplication.myUsername = null;
//        MyApplication.myPin = null;
        //finish();
        progress.dismiss();
        Sound.playSuccess(getApplicationContext());
        new AttemptDeleteQ().execute();
        new AttemptIncreaseDrinkCount().execute();
        pouring = false;


        if(CommStream.TEST_HARNESS){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(6000);
                        CommStream.StoreBuffer("$DS,AK,PG");

                    }catch(InterruptedException ie){
                        ie.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public void AlertErrorPouringDrink(){

        final Dialog dialog2 = new Dialog(new ContextThemeWrapper(ConfirmDrink.this, R.style.SmartUIDialog));
        dialog2.setCanceledOnTouchOutside(false);
        dialog2.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        dialog2.getWindow().addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        dialog2.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog2.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        dialog2.setContentView(R.layout.dialog_cf_menu);
        //dialog.setTitle("Do You Have an Account with Smart Bar?");
        dialog2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#44000000")));
        ((TextView)dialog2.findViewById(R.id.textView_failed_pour)).append("EC:" + warningCode + "\n" + warningMsg);
        dialog2.findViewById(R.id.diag_pour_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog2.dismiss();
                startActivity(new Intent(ConfirmDrink.this, IdleMenu.class));

            }
        });
        dialog2.show();
        dialog2.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }

    public void AlertWaitingForClean() {
        progress = ProgressDialog.show(this, "Cleaning System EC: "+ warningCode, "Please Remove Cup", true);

        progress.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        progress.getWindow().addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //progress.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        progress.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        //Don't halt the process if the purge failed
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if(progress.isShowing()){
                    progress.dismiss();
                    startActivity(new Intent(ConfirmDrink.this, IdleMenu.class));
                }
            }
        },6000);

        if (CommStream.TEST_HARNESS) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    CommStream.StoreBuffer("$DS,AK,PG");
                }
            }, 4000);
        }

    }

    public boolean RunPourDrinkSM(String t){

        if(t==null){return false;}
        t = t.trim();
        if(CommStream.DEBUG_MODE) {
            Toast.makeText(getApplicationContext(), "Message In:" + t, Toast.LENGTH_LONG).show();
        }
        Log.d("CUP","Reading"+t);
        if (t.contains("$CUP,PLACED")) {
            cupChanged = true;
            //RaspberryPiReady = true;
            DrinkOrder.CupReady = true;
        } else if (t.contains("$CUP,REMOVED")) {
            cupChanged = true;
            DrinkOrder.CupReady = false;
        } else if (t.contains("$CUP,TIMEOUT")) {
            CommStream.writeString("$CUP");
            //DrinkListing.append("Please Set Cup In Holder");
            if (!CommStream.DEBUG_MODE) {
                mp = Sound.playPlaceCupRepeat(getApplicationContext());
            }
        } else if (t.contains("$DO,SUCC") || t.contains("$DS,AK,DO")) {
            curstate = CupState.DRINKPOURED;
            findViewById(R.id.pour_progress).setVisibility(View.INVISIBLE);
            if(!WATER_ONLY){
                PayForDrink();
                //TODO WATER!!!
                FinishOrder();
            }else{
                //finish();
                progress.dismiss();
                Sound.playSuccess(getApplicationContext());
                pouring = false;
        }
            CommStream.writeString("$CUP,START");
            AlertWaitingForClean();
            //finish();
        } else if(t.contains("$DS,AK,PG")){
            //Toast.makeText(getApplicationContext(),"got ",Toast.LENGTH_SHORT).show();
            progress.dismiss();
            startActivity(new Intent(ConfirmDrink.this,IdleMenu.class));
        } else if(t.contains("$DS,NA")){//Occurs if bad drink string format
            progress.dismiss();

            warningCode |= 0x20;
            try {
                warningMsg = t.split("[,]")[3] + t.split("[,]")[4];
            }catch(ArrayIndexOutOfBoundsException aio){
                aio.printStackTrace();
            }
            AlertErrorPouringDrink();
        }else if (t.contains("$DS,ER")){ // Occurs if error in drink dispensing
            progress.dismiss();
            warningCode |= 0x20;
            try {
                warningMsg = t.split("[,]")[3] + t.split("[,]")[4];
            }catch(ArrayIndexOutOfBoundsException aio){
                aio.printStackTrace();
            }
            AlertErrorPouringDrink();
            //Toast.makeText(getApplicationContext(),"got "+t,Toast.LENGTH_SHORT).show();
            return false;
        }

        if(curstate == CupState.DRINKPOURED){
            if(CommStream.DEBUG_MODE){Toast.makeText(getApplicationContext(),"DRINKPOURED",Toast.LENGTH_SHORT).show();}
            if(DrinkOrder.CupReady == false){
                CommStream.writeString("$DS,$PG");
                curstate = CupState.PURGED;
                Toast.makeText(getApplicationContext(),"PURGED",Toast.LENGTH_LONG).show();
            }
        }


            return true;

    }

/****************************************************************/
    /**/
    class AttemptGetDrinkName extends AsyncTask<String, String, String> {
        int success;
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("AGDN", "Pre-Exec");
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                Log.d("AGDN", "Mid-Execute. IDS:"+nameToDecode+"\n"+DrinkOrder.InDrinkString);
                // getting product details by making HTTP request

                String recipe =  nameToDecode.replace("$DO,","");

                Log.d("AGDN", "InDrinkString:" + DrinkOrder.InDrinkString);
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("recipe", recipe));
                Log.d("AGDN","HTTP getting Posted:" + ServerAccess.RECIPE_URL);
                JSONObject json = jsonParser.makeHttpRequest(ServerAccess.RECIPE_URL, "POST", params);

                // check your log for json response
                Log.d("AGDN", json.toString());
                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("AGDN", "Success String:" + json.toString());
                    searchFailure = false;
                    return json.getString(TAG_MESSAGE);
                } else {
                    Log.d("AGDN","Failure with :"+ json.getString(TAG_MESSAGE));
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
                DrinkOrder.InDrinkNameString = file_url;
                //Toast.makeText(DisplayQueue.this, file_url, Toast.LENGTH_LONG).show();
                Log.d("AGDN","Returned URL:"+ file_url);
                //Updates the drink name
                ConfirmDrink.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        DrinkListing.setText(DrinkOrder.InDrinkNameString + "\n" + DrinkListing.getText());
                        if(CommStream.TEST_HARNESS){DrinkListing.setText("Vesper" + "\n" + DrinkListing.getText());}
                    }
                });

            } else {
                Toast.makeText(ConfirmDrink.this, "Failure to Access Server. Check Internet Connection"
                        , Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Deletes the User from the Drink Queue so that they may not order again
     */
    class AttemptDeleteQ extends AsyncTask<String, String, String> {
        int success;


        @Override
        protected String doInBackground(String... args) {
            try {
                Log.d("DelQ", "Mid-Execute");
                // getting product details by making HTTP request
                String PinToDelete =  DrinkOrder.InUserPinString;
                Log.d("DelQ","PinToDelete:" + PinToDelete);
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("pin", PinToDelete));
                Log.d("DelQ","HTTP getting Posted:" + ServerAccess.DELETE_URL);
                JSONObject json = jsonParser.makeHttpRequest(ServerAccess.DELETE_URL, "POST", params);

                // check your log for json response
                Log.d("DelQ", json.toString());
                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("DelQ", "Success String:" + json.toString());
                    searchFailure = false;
                    return json.getString(TAG_MESSAGE);
                } else {
                    Log.d("DelQ","Failure with :"+ json.getString(TAG_MESSAGE));
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
                Log.d("DelQ","Returned URL:"+ file_url);
                //@TODO startActivity(new Intent(ConfirmDrink.this, IdleMenu.class));

            } else {
                Toast.makeText(ConfirmDrink.this, "Failure to Access Server. Check Internet Connection"
                        , Toast.LENGTH_SHORT).show();
            }
        }
    }
    /**Accesses server for BrainTree ID**/
    class AttemptGetCust extends AsyncTask<String, String, String> {
        int success;
        @Override
        protected String doInBackground(String... args) {
            try {
                Log.d("AGC", "Mid-Execute");
                // getting product details by making HTTP request

                //String token = "token";
                String pinToPost = DrinkOrder.InUserPinString;
                PiComm.PiLog("AGC--> pinToPost");
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
                PiComm.PiLog(" AGC+++> BrainTree ID:" + file_url);
                DrinkOrder.InBrainID = file_url;
                new AttemptPostPayment().execute(file_url);

            } else {
                Log.d("AGC","file_url is null");
                PiComm.PiLog("---->AGC","file_url is null");
                Toast.makeText(ConfirmDrink.this, "Failure to Access Server. Check Internet Connection"
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

                //PiComm.PiLog("Attempting payment");
                // getting product details by making HTTP request
                Log.d("APP", "Incoming Customer Id:" + DrinkOrder.InBrainID);
                //String token = "token";
                String customerID = DrinkOrder.InBrainID;
                Log.d("APP", "Pin getting Posted:" + customerID);
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("customerID", customerID));
                params.add(new BasicNameValuePair("amount", DrinkOrder.getDrinkPriceString()));

                PiComm.PiLog("ID:"+customerID+" Price:"+ DrinkOrder.getDrinkPriceString());
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
                PiComm.PiLog("APP++> file_url:" + file_url);
                if(file_url.contains("Unsuccessful")){
                    Toast.makeText(getApplicationContext(),"Payment Unsuccessful",Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.d("APP","file_url is null");
//                Toast.makeText(MainActivity.this, "Failure to Access Server. Check Internet Connection"
//                        , Toast.LENGTH_SHORT).show();
            }
        }
    }
    /**
     * Deletes the User from the Drink Queue so that they may not order again
     */
    class AttemptIncreaseDrinkCount extends AsyncTask<String, String, String> {
        int success;


        @Override
        protected String doInBackground(String... args) {
            try {
                Log.d("IDC", "Mid-Execute");
                // getting product details by making HTTP request

                String PinToIncrease =  DrinkOrder.InUserPinString;
                Log.d("IDC","Pin getting Posted:" + PinToIncrease);
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("pin", PinToIncrease));
                Log.d("IDC","HTTP getting Posted:" + ServerAccess.COUNTER_URL);
                JSONObject json = jsonParser.makeHttpRequest(ServerAccess.COUNTER_URL, "POST", params);
                if(json == null)return null;
                // check your log for json response
                Log.d("IDC", json.toString());
                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("IDC", "Success String:" + json.toString());
                    searchFailure = false;
                    return json.getString(TAG_MESSAGE);
                } else {
                    Log.d("IDC","Failure with :"+ json.getString(TAG_MESSAGE));
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
                Log.d("IDC","Returned URL:"+ file_url);

                //SystemCodeParser.CupReady = false;

            } else {
                Toast.makeText(ConfirmDrink.this, "Failure to Access Server. Check Internet Connection"
                        , Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * System Level Functions*
     */
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
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }
    TimerTask HideTask = new TimerTask() {
        @Override
        public void run(){
            ConfirmDrink.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hideSystemUI();
                }
            });
        }
    };


    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_HOME)) {
            if(CommStream.DEBUG_MODE){Toast.makeText(this, "You pressed the home button!", Toast.LENGTH_LONG).show();}
            //return true;
        }else if(keyCode == KeyEvent.KEYCODE_BACK){
            if(CommStream.DEBUG_MODE){Toast.makeText(this, "You pressed the back button!", Toast.LENGTH_LONG).show();}
            //return true;
            startActivity(new Intent(this,IdleMenu.class));
        }else{
            if(CommStream.DEBUG_MODE){Toast.makeText(this,"You Pressed the:"+keyCode,Toast.LENGTH_LONG).show();}
        }
        return super.onKeyDown(keyCode, event);
        //return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_confirm_drink, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}