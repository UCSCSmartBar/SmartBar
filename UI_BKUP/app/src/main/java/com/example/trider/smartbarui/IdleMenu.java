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
import android.text.Layout;
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
import android.widget.ProgressBar;
import android.widget.TextClock;
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


public class IdleMenu extends Activity {


    TextClock textClock;
    TextView tView;


    static boolean toggle = true;
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    Boolean searchFailure = false;
    JSONParser jsonParser = new JSONParser();


    MediaPlayer mediaPlayer;
    ProgressBar pBar;
    ProgressDialog pDialog;

    boolean allowOrdering;

    static String QueueString = null;
    static String OldQueueString = null;
    long subTimer = 10;
    long subTimer2 = 0;

    long testCount = 0;
    //PI communications
    static CommStream PiComm = new CommStream();


    static Boolean IdleMenuActive = true;

    boolean updatingQueue = true;

    /**
     * Main background task Idle Menu
     * @BackGtask: Is a timer triggered __ISR to perform tasks
     */
    class BackGTask extends TimerTask {
        @Override
        public void run(){
            IdleMenu.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (IdleMenuActive) {
                        try {
                            /**
                             * @subTimeTask1 Executes AttemptgetQ() every 10s, and sends it to Pi if it is
                             *          different from the previous Queue.
                             */

                            if (subTimer > 2) {
                                new AttemptGetQ().execute();
                                subTimer = 0;
                                //makes sure queue is non-null
                                if (QueueString != null){
                                    if(!QueueString.equals("00000000000")) {
                                        if (!QueueString.equalsIgnoreCase(OldQueueString)) {
                                            CommStream.writeString("$FPQ," + QueueString);
                                            OldQueueString = QueueString;
                                            updatingQueue = true;
                                            Log.d("IDLE", "There's a new Queue:" + QueueString);
                                        }
                                    }
                                }
                            } else {
                                subTimer++;
                            }
                            DrinkOrder.CurDrinkQueue = QueueString;

                            /**
                             * @subTimeTask2 Checks the status of the communication link between the two systems
                             */
                            if (subTimer2 > 5) {
                                subTimer2 = 0;
                                if (PiComm.isDisconnected()) {
                                    Toast.makeText(getApplicationContext(), "Detected Disconnect", Toast.LENGTH_LONG).show();
                                    AlertSystemAdmin();

                                    BackGTask.this.cancel();
                                } else {
//                                    if (CommStream.Beagle != null) {
//                                        //Toast.makeText(getApplicationContext(), PiComm.ReadStatus() + CommStream.Beagle.TIME_TILL_WALK, Toast.LENGTH_SHORT).show();
//                                    }else {
//                                        //Toast.makeText(getApplicationContext(), PiComm.ReadStatus() + "Beagle is null", Toast.LENGTH_SHORT).show();
//                                    }
                                    }
                                }else {
                                    subTimer2++;
                                    }


                            /**
                             * @subTimeTask3
                             */
                            String t = CommStream.ReadBuffer();
                            if (t != null) {
                                if (t.contains("FATAL")) {
                                    CommStream.FATAL_ERROR = true;
                                    finish();
                                } else if (t.contains("EXIT") || t.contains("exit")) {
                                    CommStream.SYSTEM_STATUS = CommStream.OFF;
                                    finish();
                                } else if (t.contains("admin")) {
                                    AlertSystemAdmin();
                                    //BackGTask.this.cancel();
                                }else if (t.contains("$FPQ,ACK")){
                                    updatingQueue = false;
                                }
                            }
                        }catch(NullPointerException npe){
                            npe.printStackTrace();
                        }
                    }else{
                        cancel();
                    }
                }
            });
        }
    }

    /************************************Entry Exit Function***************************************/
    /**********************************************************************************************/
    @Override
    public void onStart() {
        super.onStart();
        IdleMenuActive = true;
        updatingQueue = true;

        Log.d("LIFE", "\tIdleMenu onStart()");
    }
    @Override
    public void onStop(){
        super.onStop();
        try {
            IdleMenuActive = false;
            mediaPlayer.stop();
        }catch(NullPointerException npe){
            npe.printStackTrace();
        }
            Log.d("LIFE", "\tIdleMenu onStop()");
    }
    @Override
    public void onResume() {
        super.onResume();
         Log.d("LIFE", "\t\tIdleMenu onResume()");
    }
    @Override
    public void onPause() {
        super.onPause();
        Log.d("LIFE", "\t\tIdleMenu onPause()");
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        //startActivity(new Intent(IdleMenu.this,IdleMenu.class));
        Log.d("LIFE", "IdleMenu onDestroy()");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idle_menu);
        Log.d("LIFE", "IdleMenu onCreate()");
        //Logs to the raspberry pi


        IdleMenuActive = true;
        // Hide the status bar.
        hideSystemUI();
        //Grab the current Drink Queue;
        ClearUserData();

        new AttemptGetQ().execute();
        //Set the clock


        //Begin the background listening/hiding menu tasks
       new Timer().scheduleAtFixedRate(new BackGTask(),500,500);
       new Timer().scheduleAtFixedRate(HideTask, 100, 100);




        PiComm.PiLog(this.getLocalClassName(), DrinkOrder.OrderStatus());
        /**DEBUG OPTIONS**/
        if(CommStream.DEBUG_MODE){
            //tView.setText("Created Menu");
//        if(PiComm.isInitialized()){
//        }
        }else{
            //mediaPlayer = Sound.playWelcome(getApplicationContext());
        }
        PiComm.PiLog("Exiting On Create");
    }

    /**Menu Navigators**/

    //public void onPickUpClick(View view){startActivity(new Intent(this,PickUpDrink.class));}

    public void GoToNewUser(View view){
        startActivity(new Intent(this,NewUser.class));
    }


    public void FingerPickUp(View view){
//        ProgressDialog pDialog;
//        pDialog = new ProgressDialog(IdleMenu.this);
//        pDialog.setMessage("Please Wait...");
//        pDialog.setIndeterminate(false);
//        pDialog.setCancelable(true);
//        pDialog.show();
//        pDialog.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
//                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
//                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
//        pDialog.show();
//        while(updatingQueue);
//        pDialog.dismiss();
        pDialog = new ProgressDialog(IdleMenu.this);
        pDialog.setMessage("Loading Users...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                pDialog.dismiss();
                finish();
                startActivity(new Intent(IdleMenu.this, FingerPickUp.class));
            }
        },1000);

    }

    public void AlertSystemAdmin(){

        View lV = (View) findViewById(R.id.topView);
        lV.setBackground(getResources().getDrawable(R.drawable.cityscape_fullblur));
        TextView tView = (TextView) findViewById(R.id.smart_bar_title);
        String color = "#44ffffff";
        tView.setTextColor(Color.parseColor(color));
        Button b1 = (Button) findViewById(R.id.pick_up_drink_button);
        Button b2 = (Button) findViewById(R.id.order_drink_button);
        b1.setTextColor(Color.parseColor(color));
        b2.setTextColor(Color.parseColor(color));


//
        final Dialog dialog2 = new Dialog(new ContextThemeWrapper(IdleMenu.this, R.style.SmartUIDialog));
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

        dialog2.setContentView(R.layout.dialog_alert_admin);
        dialog2.setTitle("Please Wait for System Administrator");
        dialog2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#44000000")));
        dialog2.show();
        dialog2.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);



    }

    /**
     * Allows the owner/admin to exit from the program
     * @param view
     */
    public void GoToSystemMain(View view){

        final Dialog dialog2 = new Dialog(new ContextThemeWrapper(IdleMenu.this, R.style.SmartUIDialog));
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

        dialog2.setContentView(R.layout.dialog_back_to_main);
        //dialog.setTitle("Do You Have an Account with Smart Bar?");
        dialog2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#44000000")));
        dialog2.findViewById(R.id.dialog_btm_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog2.dismiss();
                finish();
            }

        });
        dialog2.findViewById(R.id.dialog_btm_exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog2.dismiss();

            }

        });
        dialog2.show();
        dialog2.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);


    }

    /**
     * Opens up the the appropriate Dialogs, while hiding the menu/action bars accordingly.
     * @param view
     */
    public void GoToOrderDrink(View view){
        LayoutInflater inflater = LayoutInflater.from(this);
        //The view being uploaded is formatted in the fragment_change_liquor_inv.xml.xml file is in the text
        //box area
        //View v = inflater.inflate(R.layout.fragment_have_account, null);

        //New Alert Pop-Up

        View lV = (View) findViewById(R.id.topView);
        lV.setBackground(getResources().getDrawable(R.drawable.cityscape_fullblur));
        TextView tView = (TextView) findViewById(R.id.smart_bar_title);
        String color = "#44ffffff";
        tView.setTextColor(Color.parseColor(color));
        Button b1 = (Button) findViewById(R.id.pick_up_drink_button);
        Button b2 = (Button) findViewById(R.id.order_drink_button);
        b1.setTextColor(Color.parseColor(color));
        b2.setTextColor(Color.parseColor(color));




        final Dialog dialog = new Dialog(new ContextThemeWrapper(this, R.style.SmartUIDialog));
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        dialog.setContentView(R.layout.dialog_idle_menu_1);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#44000000")));
        //dialog.setTitle("Do You Have an Account with Smart Bar?");
        dialog.findViewById(R.id.dialogBtnYes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                startActivity(new Intent(IdleMenu.this, LoginActivity.class));
            }
        });

        dialog.findViewById(R.id.dialogBtnNo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //No more account creation
                dialog.dismiss();
                final Dialog dialog2 = new Dialog(new ContextThemeWrapper(IdleMenu.this, R.style.SmartUIDialog));
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

                dialog2.setContentView(R.layout.dialog_noorder);
                //dialog.setTitle("Do You Have an Account with Smart Bar?");
                dialog2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#44000000")));
                dialog2.findViewById(R.id.dialogBtnBack).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        dialog2.dismiss();

                        View lV = (View) findViewById(R.id.topView);
                        lV.setBackground(getResources().getDrawable(R.drawable.cityscape));

                        TextView tView = (TextView) findViewById(R.id.smart_bar_title);
                        String color = "#ffffffff";
                        tView.setTextColor(Color.parseColor(color));
                        Button b1 = (Button) findViewById(R.id.pick_up_drink_button);
                        Button b2 = (Button) findViewById(R.id.order_drink_button);
                        b1.setTextColor(Color.parseColor(color));
                        b2.setTextColor(Color.parseColor(color));


                        //startActivity(new Intent(IdleMenu.this, LibraryBrowseActivity.class));

                    }
                });
                dialog2.show();
                dialog2.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

            }
        });
        dialog.show();
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);




    }

    /**
     * Starts an Asynchronous thread to obtain the current drink queue, and precedes to a Queue Debug Screen.
     * @param v
     */
    public void GetQueue(View v){
        if(CommStream.DEBUG_MODE) {
            new AttemptGetQ().execute();
            long time = System.currentTimeMillis();
            while (System.currentTimeMillis() < time + 1000) ;
            startActivity(new Intent(this, DisplayQueue.class));
        }
    }

    /**
     * Clears any Data Local to the system that is used for server access
     */
    public void ClearUserData(){
        MyApplication.myPin = null;
        MyApplication.myUsername = null;
        DrinkOrder.InDrinkString = null;
        DrinkOrder.InDrinkPrice = 0.0;
        DrinkOrder.CupReady = false;
        DrinkOrder.InUserPinString = null;
        DrinkOrder.InDrinkNameString = null;


    }
    /**
     * Server Access
     */
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
                        Log.d("Q", "Mid-Execute");
                        Log.d("Q", "starting");
                        // getting product details by making HTTP request

                        List<NameValuePair> params = new ArrayList<NameValuePair>();
                        params.add(new BasicNameValuePair("pin", "1"));
                        JSONObject json = jsonParser.makeHttpRequest(ServerAccess.GET_QUEUE_URL, "POST", params);
                        if(json == null){return null;}
                        // check your log for json response
                        Log.d("Q", json.toString());
                        // json success tag
                        //PiComm.PiLog("Getting Q");
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
                        Log.d("Q","JSON is null");
                        npe.printStackTrace();
                    }

            return null;
        }

            protected void onPostExecute(String file_url) {
                // dismiss the dialog once product deleted
                if (file_url != null){
                    Log.d("Q",file_url);
                    QueueString = file_url;
                    //PiComm.PiLog("GotQ:"+QueueString);

                }else{
                    Toast.makeText(IdleMenu.this,"Failure to Access Server. Check Internet Connection"
                            , Toast.LENGTH_SHORT).show();
                }

            }
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        return false;
    }
    //Overrides the home/back button thing
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d("LIFE",keyCode+":"+event);
        if ((keyCode == KeyEvent.KEYCODE_HOME)) {
            if(CommStream.DEBUG_MODE){Toast.makeText(this, "You pressed the home button!", Toast.LENGTH_LONG).show();}
            showDialog("'HOME'");
            Log.d("LIFE","Home pressed");
            return true;
            //return true;
        }else if(keyCode == KeyEvent.KEYCODE_BACK){
            if(CommStream.DEBUG_MODE){Toast.makeText(this, "You pressed the back button!", Toast.LENGTH_LONG).show();}
            return true;
        }else{
            //if(CommStream.DEBUG_MODE){Toast.makeText(this,"You Pressed the:"+keyCode,Toast.LENGTH_LONG).show();}
        }

        return super.onKeyDown(keyCode, event);
        //return true;
    }

    /*System Functions*/
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
        TimerTask HideTask = new TimerTask() {
            @Override
            public void run(){
                IdleMenu.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideSystemUI();
                    }
                });
            }
        };


    void showDialog(String the_key){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("You have pressed the " + the_key + " button. Would you like to exit the app?")
                .setCancelable(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        finish();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.setTitle("CoderzHeaven.");
        alert.show();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        //this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
    }

    public void onUserLeaveHint() { // this only executes when Home is selected.
        // do stuff
        super.onUserLeaveHint();
        System.out.println("HOMEEEEEEEEE");
    }



}

