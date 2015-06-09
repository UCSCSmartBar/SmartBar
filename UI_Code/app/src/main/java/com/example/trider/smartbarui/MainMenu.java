package com.example.trider.smartbarui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MainMenu extends Activity {
    Context context;
    CommStream PiComm;
    boolean isActive = true;
    String InMessage;

    String DrinkLibString;

    Timer testTimer;
    TimerTask testTask;

    int index;
    ProgressDialog progress;

    static boolean _MAIN_START = false;
    static boolean _ADMIN_MODE = false;


    String PREFRENCES_NAME = "PRICE_PREFERENCES";


    //Server things.

    Boolean searchFailure = false;
    JSONParser jsonParser = new JSONParser();

/*Background Communications*/
    Runnable nListenerTask = new Runnable() {
        @Override
        public void run() {
            InMessage = CommStream.ReadBuffer();
            //Log.d("TEST","In String is "+InMessage);
            if(InMessage != null){

                if(InMessage.contains("$SYS,STARTED")){
                    if(progress!=null){progress.dismiss();}
                    CommStream.SYSTEM_STATUS=CommStream.ON;
                    PiComm.StartBeagle();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (CommStream.Beagle !=null && CommStream.Beagle.isInitialized())
                                Toast.makeText(getApplicationContext(), "Beagle On", Toast.LENGTH_SHORT).show();
                        }
                    });
                    //Sound.playSuspense(getApplicationContext());
                    //Sound.playTequila(getApplicationContext());
                }

                //Simple Toast showing incoming messages
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                       if(CommStream.DEBUG_MODE)Toast.makeText(getApplicationContext(),"In:"+InMessage,Toast.LENGTH_SHORT).show();
                    }
                });
//
//                if(PiComm.isDisconnected()){
//                    finish();
//                }
            }


            //if(CommStream.SYSTEM_STATUS == CommStream)
            //Waits for new input communication
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //Restarts this thread only if active
            if(isActive) {
                new Thread(this).start();
            }
        }
    };


    public void onStop(){
        super.onStop();
        //PiComm.writeString("STOP");
        isActive = false;
//        if(testTimer!=null && testTask!=null) {
//            testTimer.cancel();
//            testTask.cancel();
//        }

    }
    public void onDestroy(){
        super.onDestroy();
        isActive = false;
    }
//    public void onResume(){
//        super.onResume();
//        if(PiComm.isInitialized()){
//            PiComm.writeString("Resume");
//        }
//        hideSystemUI();
//        isActive = true;
//    }
protected void onStart(){
    super.onStart();
    isActive = true;
    new Thread(nListenerTask).start();
    new GetLibrary().execute();
    if(!_MAIN_START){
        setContentView(R.layout.activity_main_alt);
        ((TextView)findViewById(R.id.version_number)).setText("v" + MainActivity._VERSION_NUMBER);
        _MAIN_START = true;

    }else{//Returning to Main Menu after the second.

        setContentView(R.layout.activity_main_menu);


    }





    //Beings the hiding of the tasks.

}


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hideSystemUI();

        PiComm = new CommStream();
        PiComm.PiLog(this.getLocalClassName(),DrinkOrder.OrderStatus());
        context = getApplicationContext();
        isActive = true;

        //If the system is connected but hasn't been started
        if(CommStream.SYSTEM_STATUS==CommStream.OFF && PiComm.isInitialized()){
            //progress = ProgressDialog.show(this, "Starting Up","Device Initializing", true);
            CommStream.writeString("$SYS,MainStart");
            //The initial startup screen

        }


        //Set prices///




        new Timer().schedule(new TimerTask() {
            @Override
            public void run(){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Gives the Raspberry Pi 11 Seconds to start up, if it doesn't go back to main activity
                        if(CommStream.SYSTEM_STATUS == CommStream.OFF && PiComm.isInitialized()) {
                            //progress.dismiss();
                            finish();
                            //else if restarting the pi start the activity relatively soon after
                        }else if(CommStream.SYSTEM_STATUS == CommStream.ON){
                            //If gained active connection to pi && has internet connection
                            if(isActive && CommStream._INTERNETCONN) {
                                startActivity(new Intent(MainMenu.this, IdleMenu.class));
                            }else{
                                setContentView(R.layout.activity_main_menu);
                            }
                            //Most likely occuring if starting without pi
                        }else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setContentView(R.layout.activity_main_menu);
                                }
                            });
                        }
                    }
                });
            }
        },11000);

        TextView textView = (TextView) findViewById(R.id.testView);

        new Timer().scheduleAtFixedRate(HideTask, 100, 100);

/*********************TEST OPTION**********************************************/
        if(CommStream.DEBUG_MODE){
//            if (PiComm.isInitialized()) {
//                Toast.makeText(context,"PiComm is Initialized",Toast.LENGTH_LONG).show();
//            }else{
//                Toast.makeText(context,"PiComm is UnInitialized",Toast.LENGTH_LONG).show();
//            }
            //textView.setText(PiComm.ReadStatus());
        }

        if(CommStream.TEST_HARNESS){
            CommStream.StoreBuffer("$SYS,STARTED");

        }


    }



//Starts other menus
    public void StartUIClicked(View view){
        //isActive = false;
        if(CommStream._INTERNETCONN) {
            startActivity(new Intent(this, IdleMenu.class));
        }else{
            new GetLibrary().execute();
            AlertNoInternetConnection();
        }
    }
    public void InventoryClicked(View view){
        //isActive = false;
        if(CommStream._INTERNETCONN) {
            startActivity(new Intent(this, Container_Screen.class));
        }else{
            AlertNoInternetConnection();
            new GetLibrary().execute();
        }
    }
    public void SystemHalt(View view){
        CommStream.writeString("SYS,MainHalt");
        System.exit(0);
    }

    public void AlertNoInternetConnection(){

        AlertDialog alertDialog = new AlertDialog.Builder(MainMenu.this).create();

//        alertDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
//        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //alertDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
//        alertDialog.getWindow().getDecorView().setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                        | View.SYSTEM_UI_FLAG_FULLSCREEN
//                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        alertDialog.setTitle("No Internet Connection");
        alertDialog.setMessage("Please connect to network before starting smartbar.");


        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Open Internet",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));
                        startActivity(browserIntent);

                        dialog.dismiss();
                    }
                });


        alertDialog.show();
        //alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }


    public void MMgoBack(View view) {
        finish();
    }

    public void GoToDispensing(View view) {
        startActivity(new Intent(this, DispensingScreen.class));
    }


    public void UpdatePrices() {

        SharedPreferences settings = getSharedPreferences(PREFRENCES_NAME, Context.MODE_PRIVATE);
        //Updates the old prices from app close;
        Inventory INV = new Inventory();
        for (int i = 0; i < INV.GetNumberOfContainers(); i++) {
            String conNum = Integer.toString(i+1);
            String priceString = settings.getString(conNum, "0.0");
            Log.d("CS_$","Price in Container "+ (i+1)+"\t"+priceString);
            INV.getContainer(i + 1).setPricePerOz(Float.parseFloat(priceString));
        }


//Uses old drink prices to set drink pricing on server initially
        String[] parsedArray = DrinkLibString.split("#");
        if(parsedArray != null && parsedArray.length > 1) {
            String drinkNameString = parsedArray[0];
            String drinkRecipeString = parsedArray[1];
            int drinkCount = 0;
            String[] tempName, tempRecipe;
            for (int i = 0; i < drinkNameString.length(); i++) {
                if (drinkNameString.charAt(i) == '%') {
                    drinkCount++;
                }
            }
            tempName = drinkNameString.split("%");
            tempRecipe = drinkRecipeString.split("%");
            for (int k = 0; k < drinkCount; k++) {
                Log.d("MM_UP", "Name:" + tempName[k]);
                Log.d("MM_UP", "In Recipe: " + tempRecipe[k]);
                double price = DrinkOrder.ParseDrinkForPrice(tempRecipe[k]);
                Log.d("MM_UP", "Parse Price:" + price);//tempName[k],String.format("%.2f",price)
                new AttemptPostPrice().execute(tempName[k], String.format("%.2f", price));
            }
        }

    }




/**System level functions**/
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
            MainMenu.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hideSystemUI();
                }
            });
        }
    };




    //Gets the prices
    class GetLibrary extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        boolean failure = false;
        String receivedString;

        // query database method
        @Override
        protected String doInBackground(String... args) {
            // Check for success tag
            int success;
            String placeholder = "";
            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("placeholder", placeholder));

                Log.d("AGL!", "starting");
                // getting product details by making HTTP request
                JSONObject json = jsonParser.makeHttpRequest(ServerAccess.GET_LIB_URL, "POST", params);
                //If in case not connected to internet
                if (json == null) {

                    //Toast.makeText(getApplicationContext(),"Failure to Connect to Server\n check Internet connection",Toast.LENGTH_LONG).show();
                    return null;
                }
                // check your log for json response
                Log.d("AGL", json.toString());


                // json success tag
                success = json.getInt(ServerAccess.TAG_SUCCESS);
                if (success == 1) {
                    Log.d("AGL","Successful!"+ json.toString());
                    receivedString = json.getString(ServerAccess.TAG_MESSAGE);
                    return json.getString(ServerAccess.TAG_MESSAGE);
                } else {
                    Log.d("AGL", "Failure!"+ json.getString(ServerAccess.TAG_MESSAGE));
                    return json.getString(ServerAccess.TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }


        protected void onPostExecute(String file_url) {
            // dismiss the dialog once product deleted
            if (file_url != null){
                Log.d("AGL",file_url);
                DrinkLibString = file_url;
                UpdatePrices();
                CommStream._INTERNETCONN = CommStream.ON;

            }else{
                Toast.makeText(MainMenu.this,"Failure to Access Server. Check Internet Connection"
                        , Toast.LENGTH_SHORT).show();
            }

        }



    }

//Post the prices
    class AttemptPostPrice extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        boolean failure = false;
        String receivedString;

        // query database method
        @Override
        protected String doInBackground(String... args) {
            // Check for success tag
            int success;
            String placeholder = "";
            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("name", args[0]));//recipe name

                params.add(new BasicNameValuePair("price",args[1]));//recipe price
                Log.d("AP$!", "starting");
                // getting product details by making HTTP request

                Log.d("AP$!", "name:"+args[0]+". price:"+args[1]);
                JSONObject json = jsonParser.makeHttpRequest(ServerAccess.PUT_PRICE, "POST", params);
                //If in case not connected to internet
                if (json == null) {
                    return null;
                }
                // check your log for json response
                Log.d("AP$", json.toString());
                // json success tag
                success = json.getInt(ServerAccess.TAG_SUCCESS);
                if (success == 1) {
                    Log.d("AP$","Successful!"+ json.toString());
                    receivedString = json.getString(ServerAccess.TAG_MESSAGE);
                    return json.getString(ServerAccess.TAG_MESSAGE);
                } else {
                    Log.d("AP$", "Failure!"+ json.getString(ServerAccess.TAG_MESSAGE));
                    return json.getString(ServerAccess.TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }


        protected void onPostExecute(String file_url) {
            // dismiss the dialog once product deleted
            if (file_url != null){
                Log.d("AP$", file_url);
            }else{
                Toast.makeText(MainMenu.this,"Failure to Access Server. Check Internet Connection"
                        , Toast.LENGTH_SHORT).show();
            }

        }



    }




}
