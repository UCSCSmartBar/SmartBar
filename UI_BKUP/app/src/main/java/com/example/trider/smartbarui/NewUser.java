package com.example.trider.smartbarui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
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
import android.widget.EditText;
import android.widget.ProgressBar;
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


public class NewUser extends Activity {

    public DrinkOrder testDrink;
    private CommStream PiComm = new CommStream();
    JSONParser jsonParser = new JSONParser();

    //Views
    ProgressBar pBar;
    Toast toast;
    EditText eText;
    private ProgressDialog pDialog;

    //Variables
    boolean searching = false;
    boolean searchFailure = true;
    boolean hasFingerPrint = false;
    String pinString="";
    String IncomingString;
    String OutMessage;
    String[] ParsedString;
    MediaPlayer mp;


    //Login tags
    //JSON element ids from response of php script:
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private static final int _NEW_USER_TIMEOUT = 60000;


    //Listens to Pi
    boolean isActive = true;
    class ListenTask extends TimerTask {
        @Override
        public void run(){
            NewUser.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isActive) {
                        /**
                         * @subTimeTask
                         */
                        String t = CommStream.ReadBuffer();
                        if(t!=null) {
                            TextView tV = (TextView) findViewById(R.id.drinkView);
                            if(CommStream.DEBUG_MODE){tV.append(t);}
                            //This Activity is not expecting a message so do nothing.
                            //String rMessage = new SystemCodeParser().DecodeAccessoryMessage(t);
                        }
                    }
                }
            });
        }
    }

    Timer WatchDog = new Timer();
    TimerTask WatchTask = new TimerTask() {
        @Override
        public void run() {
            Log.d("NewUser","Timeout");
            finish();
            startActivity(new Intent(NewUser.this,IdleMenu.class));
        }
    };

    @Override
    public void onStop(){
        super.onStop();
        isActive =false;
        WatchDog.cancel();
        WatchTask.cancel();
        mp.stop();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);
        pBar = (ProgressBar) findViewById(R.id.newUserProgress);
        pBar.setVisibility(View.INVISIBLE);

        hideSystemUI();
        //Schedules the background listening and hiding menu bars tasks
        new Timer().scheduleAtFixedRate(HideTask,100,100);
        new Timer().scheduleAtFixedRate(new ListenTask(),100,100);
        //Sets watchdog timer to return to idle menu after 15s
        WatchDog.schedule(WatchTask,_NEW_USER_TIMEOUT);

        PiComm.PiLog(this.getLocalClassName(),DrinkOrder.OrderStatus());

        TextView tV = (TextView) findViewById(R.id.drinkView);

        Intent i =getIntent();
        String newuser =  i.getStringExtra("New User");
        if(newuser.contains("yes")){
            mp = Sound.playEnterPhone(getApplicationContext());
            TextView tV2 = (TextView) findViewById(R.id.textView4);
                    tV2.setText("Please Enter Phone Number to Register Fingerprint");
        }else{
            mp = Sound.playFingerNotFound(getApplicationContext());
        }




        if(CommStream.DEBUG_MODE){
            tV.setText("CurBuff:"+ CommStream.ReadBuffer()+ "CurQ:"+ DrinkOrder.CurDrinkQueue);
        }else{
            Button b = (Button) findViewById(R.id.new_user_skip);
            b.setVisibility(View.INVISIBLE);

        }



        EnterPin(findViewById(R.id.keyOne));
    }

    /**
     * Checks the user's pin to make sure that first it is a valid format, and then secondly if its on the Q.
     * It will either start a dialog to direct the user if it failed, or start the registration of the fing/
     * TODO Check if the user has registered before, and if so ask them to reset the fingerprint on their phone
     * /erprint.
     * @param view
     */
    public void CheckPin(View view){

        new AttemptGetDrink().execute();
        new HasFP().execute();

        //Displays the progress bar so user knows the drink is being looked up
        pBar = (ProgressBar) findViewById(R.id.newUserProgress);
        pBar.setVisibility(View.VISIBLE);
        searching = true;

        //Creates a singleton task that will run in exactly 2000ms after the button is clicked
        new Timer().schedule(new TimerTask() {
            public void run() {
                NewUser.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pBar.setVisibility(View.INVISIBLE);
                        searching = false;
                        Intent intent = new Intent(NewUser.this, NewUserFinger.class);
                        //If nothing came up from search
                        if (searchFailure || !isInQueue(DrinkOrder.InUserPinString)) {
                            AlertNoUserFound();
                            return;
                        }
                        Log.d("HFP", "Has fingerprint is :" + hasFingerPrint);
                        //If the user has been found, but already has a fingerprint registered.
                        if (hasFingerPrint) {
                            AlertWrongFingerPrint();
                            return;
                        }

                        /********************************END DIALOG POP UP**************************/
                        /***************************************************************************/
                        //If Found on the Server, but not in queue. Should be caught in above statement.
//                        if(!isInQueue(DrinkOrder.InUserPinString)){
//                            Toast.makeText(getApplicationContext(),"You do not have a drink on the Queue",Toast.LENGTH_SHORT).show();
//                            return;
//                        }
                        //Has Drink in Queue but unrecognized fingerprint
                        //intent.putExtra("tString","$FPQ,"+pinString + "$DO,"+IncomingString);
                        //PiComm.writeString("$FPQ,"+pinString);
                        new AttemptGetCust();
                        DrinkOrder t = new DrinkOrder();
                        t.DecodeString(IncomingString);

                        t.storeDrinkOrder(IncomingString);
                        DrinkOrder.InUserPinString = pinString;
                        //PiComm.writeString("$DO,"+IncomingString);
                        startActivity(intent);
                    }
                });
            }
        }, 7000);

    }

    public void AlertNoUserFound(){
        final Dialog dialog = new Dialog(new ContextThemeWrapper(NewUser.this, R.style.SmartUIDialog));
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
        dialog.setContentView(R.layout.dialog_user_not_found);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#44000000")));
        dialog.setTitle("No Drink Order Found For User");
        //dialog.setMessage("Unable to find order with user");
        dialog.findViewById(R.id.dialogBtnBack2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                View lV = (View) findViewById(R.id.new_user_layout);
                lV.setBackground(getResources().getDrawable(R.drawable.cityscape3));
                startActivity(new Intent(NewUser.this, IdleMenu.class));
            }
        });

        dialog.findViewById(R.id.retry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //No more account creation
                View lV = (View) findViewById(R.id.new_user_layout);
                lV.setBackground(getResources().getDrawable(R.drawable.cityscape3));
                dialog.dismiss();

            }
        });



        View lV = (View) findViewById(R.id.new_user_layout);
        lV.setBackground(getResources().getDrawable(R.drawable.cityscape_fullblur));
        dialog.show();
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);




    }

    public void AlertWrongFingerPrint(){
        final Dialog dialog = new Dialog(new ContextThemeWrapper(NewUser.this, R.style.SmartUIDialog));
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
        dialog.setContentView(R.layout.dialog_wrong_fingerprint);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#44000000")));
        dialog.setTitle("Fingerprint Not Recognized");
        TextView tv = (TextView) dialog.findViewById(R.id.wf_tview);
        tv.setText("Sorry, your fingerprint was not recognized. If you want to reset your fingerprint " +
                    "please do so on your phone or retry with a different fingerprint");

        //dialog.setMessage("Unable to find order with user");
        dialog.findViewById(R.id.wf_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                View lV = (View) findViewById(R.id.new_user_layout);
                lV.setBackground(getResources().getDrawable(R.drawable.cityscape3));
                startActivity(new Intent(NewUser.this, IdleMenu.class));
            }
        });

        dialog.findViewById(R.id.wf_retry_finger).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //No more account creation
                View lV = (View) findViewById(R.id.new_user_layout);
                lV.setBackground(getResources().getDrawable(R.drawable.cityscape3));
                dialog.dismiss();
                startActivity(new Intent(NewUser.this, FingerPickUp.class));
            }
        });



        View lV = (View) findViewById(R.id.new_user_layout);
        lV.setBackground(getResources().getDrawable(R.drawable.cityscape_fullblur));
        dialog.show();
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

    }

    /***********Menu Navigators*******/
    public void GoToRegister(View view){
        startActivity(new Intent(this,NewUserFinger.class));
    }

    public void SkipToPickFinger (View view){
        if(CommStream.DEBUG_MODE) {
            startActivity(new Intent(this, NewUserFinger.class));
        }
    }

    public Boolean isInQueue(String userPin){

        if(DrinkOrder.CurDrinkQueue!=null){
            Log.d("inQ","Q is:"+DrinkOrder.CurDrinkQueue);
        }else{return false;}

        String[] tokens = DrinkOrder.CurDrinkQueue.split("[@,]");
        Log.d("inQ", "In:"+userPin);
        Log.d("inQ", "tokens:"+ tokens);
        for(int i = 0; i < tokens.length; i++){
            Log.d("inQ","token "+tokens[i]);
            if(tokens[i].equalsIgnoreCase(userPin)){
                Log.d("inQ"," found it equal");
                return true;
            }
        }

        Log.d("Q"," Did not find equal");
        return false;
    }

    //Server Related Functions
    class AttemptGetDrink extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(NewUser.this);
            pDialog.setMessage("Attempting to get drink...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            //pDialog.show();
            Log.d("AGD", "Pre-Exec");
        }

        @Override
        protected String doInBackground(String... args) {
            // TODO Auto-generated method stub
            // Check for success tag
            int success;
            //String userpin = eText.getText().toString();
            String userpin = pinString;
            try {
                Log.d("AGD","Mid-Execute");
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("pin", userpin));

                Log.d("AGD", "starting");
                // getting product details by making HTTP request
                JSONObject json = jsonParser.makeHttpRequest(
                        ServerAccess.GET_DRINK_URL, "POST", params);

                // check your log for json response
                Log.d("AGD","Drink retrieve attempt"+ json.toString());

                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("AGD","Drink found Successful!"+ json.toString());
                    searchFailure = false;
                    //Intent i = new Intent(Login.this, ReadComments.class);
                    return json.getString(TAG_MESSAGE);
                }else{
                    Log.d("AGD","Login Failure!"+ json.getString(TAG_MESSAGE));
                    searchFailure = true;
                    return json.getString(TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch(NullPointerException npe){
                npe.printStackTrace();
            }

            return null;

        }
        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once product deleted
            pDialog.dismiss();
            if (file_url != null){
                //Toast.makeText(NewUser.this, file_url, Toast.LENGTH_LONG).show();
                IncomingString = file_url;
                Log.d("AGD","Success, file_url:"+file_url);
                Toast.makeText(NewUser.this, file_url, Toast.LENGTH_LONG);
                if(CommStream.DEBUG_MODE){Toast.makeText(NewUser.this,file_url,Toast.LENGTH_LONG);}
            }else{
                Toast.makeText(NewUser.this,"Failure to Access Server. Check Internet Connection"
                        , Toast.LENGTH_SHORT).show();
            }

        }

    }

    class HasFP extends AsyncTask<String, String, String> {
        int success;
        @Override
        protected String doInBackground(String... args) {
            try {
                Log.d("HFP", "Mid-Execute");
                // getting product details by making HTTP request

                String HasFPString = DrinkOrder.InUserPinString;
                Log.d("HFP", "Pin getting Posted:" + HasFPString);
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("pin", HasFPString));
                JSONObject json = jsonParser.makeHttpRequest(ServerAccess.HAS_FP, "POST", params);


                // check your log for json response
                Log.d("HFP", json.toString());
                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("HFP", "Success String:" + json.toString());
                    //searchFailure = false;
                    return json.getString(TAG_MESSAGE);
                } else {
                    Log.d("HFP", "Failure with :" + json.getString(TAG_MESSAGE));
                    //searchFailure = true;
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
                Log.d("HFP", "POST: Returned URL:" + file_url);
                //Successful server access.

              if(file_url.contains("Not Found")){
                  //Toast.makeText(NewUser.this, "NF: "+file_url, Toast.LENGTH_LONG).show();
                  hasFingerPrint = false;
              }else{
                  //They have a fingerprint
                  //Toast.makeText(NewUser.this, "F: "+file_url, Toast.LENGTH_LONG).show();
                  hasFingerPrint = true;
              }

            } else {
                Toast.makeText(NewUser.this, "Failure to Access Server. Check Internet Connection"
                        , Toast.LENGTH_SHORT).show();
            }
        }
    }
    //References the current drink queue with the inputted user string


    /***********System Level Functions*******/
    /**Manuel Keyboard**/
    //@TODO Make into a custom fragment
    public void EnterPin(View view){
        switch(view.getId()){
            case R.id.keyOne:
                pinString+="1";
                break;
            case R.id.keyTwo:
                pinString+="2";
                break;
            case R.id.keyThree:
                pinString+="3";
                break;
            case R.id.keyFour:
                pinString+="4";
                break;
            case R.id.keyFive:
                pinString+="5";
                break;
            case R.id.keySix:
                pinString+="6";
                break;
            case R.id.keySeven:
                pinString+="7";
                break;
            case R.id.keyEight:
                pinString+="8";
                break;
            case R.id.keyNine:
                pinString+="9";
                break;
            case R.id.keyZero:
                pinString+="0";
                break;
            case R.id.keyBack:
                if(pinString.length() == 0){return;}
                pinString = pinString.substring(0,pinString.length()-1);
                break;
            case R.id.keyEnter:
                if(pinString.length() < 11){
                    Toast.makeText(getApplicationContext(),"Please Enter Full 11-Digits",Toast.LENGTH_SHORT).show();
                }else if(pinString.length()> 11){
                    Toast.makeText(getApplicationContext(),"Phone Number Entered is Too Long",Toast.LENGTH_SHORT).show();
                }else{
                    //Toast.makeText(getApplicationContext(),"Hey... Ok",Toast.LENGTH_SHORT).show();
                    DrinkOrder.InUserPinString = pinString;
                    if(pinString.equals(DrinkOrder.ADMIN_PIN_1)){
                        Sound.playGoodMorning(getApplicationContext());

                        finish();
                    }else if(pinString.equals(DrinkOrder.ADMIN_PIN_2)){
                        Sound.playHelloLauren(getApplicationContext());
                        finish();
                    }else {
                        CheckPin(view);
                    }
                }

                break;
        }
        TextView tView = (TextView) findViewById(R.id.enterField);


        /**
         * Creates a formatted string for the inputted phone numbers e.g. 1-(831)-555-5555
         */
        int endOfString = (pinString.length() > 11) ? 11:pinString.length();

        pinString = pinString.substring(0,endOfString);//Makes sure it is not over 11-digits
        String vString = "";
        if(pinString.length() == 1){
            vString = String.format("%s-",pinString.substring(0,1));
        }else if(pinString.length() > 1 && pinString.length() <5){
            vString = String.format("%s-(%s)",pinString.substring(0,1),pinString.substring(1));
        }else if(pinString.length() >= 5 && pinString.length() <8) {
            vString = String.format("%s-(%s)-%s", pinString.substring(0,1), pinString.substring(1, 4), pinString.substring(4));
        }else if(pinString.length() >=8){
            vString = String.format("%s-(%s)-%s-%s", pinString.substring(0,1), pinString.substring(1, 4), pinString.substring(4,7),pinString.substring(7));
        }
        Log.d("StrTest",vString);
        tView.setText(vString);
    }

    class AttemptGetCust extends AsyncTask<String, String, String> {
        int success;
        @Override
        protected String doInBackground(String... args) {
            try {
                Log.d("AGC", "Mid-Execute");
                // getting product details by making HTTP request

                //String token = "token";
                String pinToPost = DrinkOrder.InUserPinString;
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
                    //searchFailure = false;
                    return json.getString(TAG_MESSAGE);
                } else {
                    Log.d("AGC", "Failure with :" + json.getString(TAG_MESSAGE));
                    //searchFailure = true;
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
                PiComm.PiLog("Braintree ID" +file_url);
                DrinkOrder.InBrainID = file_url;
                //new AttemptPostPayment().execute(file_url);

            } else {
                Log.d("AGC","file_url is null");
//                Toast.makeText(MainActivity.this, "Failure to Access Server. Check Internet Connection"
//                        , Toast.LENGTH_SHORT).show();
            }
        }
    }



    /*System Level Functions*/
    public void GoBack(View view){
        startActivity(new Intent(NewUser.this,IdleMenu.class));
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
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }
    TimerTask HideTask = new TimerTask() {
        @Override
        public void run(){
            NewUser.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hideSystemUI();
                }
            });
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_user, menu);
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




