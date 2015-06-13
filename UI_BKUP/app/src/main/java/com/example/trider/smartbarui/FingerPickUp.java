package com.example.trider.smartbarui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class FingerPickUp extends Activity {

    CommStream PiComm;
    ImageView fingImg;
    TextView tView;
    String IncomingString;
    String userPinNumber;

    boolean toggle = false;
    static int failureCount;
    long Timeout = 0;

    /**
     * JSON/ PHP Function Information
     */
    //Login tags
    //JSON element ids from response of php script:
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    JSONParser jsonParser = new JSONParser();
    String OrderString = "";
    boolean searchFailure = true;

    MediaPlayer mp;

    public enum FingerState {
        IDLE,
        WAITING,
        COMPARING,
        PASSED,
        FAILED,
        WARNING
    }


    FingerState currentState = FingerState.IDLE;
    FingerState nextState = FingerState.IDLE;


    /**
     * @title: mListenerTask
     * @description: The background thread that receives serial communication from the raspberry pi,
     */
    //Listens to Pi
    boolean isActive = true;

    class ListenTask extends TimerTask {
        @Override
        public void run() {
            FingerPickUp.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isActive) {
                        /**
                         * @subTimeTask
                         */
                        String t = CommStream.ReadBuffer();

                        if (t != null) {
                            t = t.trim();
                            //TextView tV = (TextView) findViewById(R.id.fup_text);
                            //tV.append(t);
                            CompareFingerSM(t);
                            if (t.equals("$CUP,READY")) {
                                SystemCodeParser.CupReady = true;
                            }


                            //String rMessage = new SystemCodeParser().DecodeAccessoryMessage(t);
                        }
                    }
                }
            });
        }
    }


    int _FINGER_PICKUP_TIMEOUT = 25000;
    Timer WatchDog = new Timer();
    TimerTask WatchTask = new TimerTask() {
        @Override
        public void run() {
            Log.d("NewUser","Timeout");
            finish();
            startActivity(new Intent(FingerPickUp.this,IdleMenu.class));
        }
    };

    public void onResume() {
        super.onResume();
        hideSystemUI();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("LIFE", "FPU onCreate()");
        isActive = false;
        if(mp != null)mp.stop();
        WatchDog.cancel();
        WatchTask.cancel();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finger_pick_up);
        Log.d("LIFE", "FPU onCreate()");
        //Hiding menu
        hideSystemUI();


        //Loading view references
        //ImageView usbConn = (ImageView) findViewById(R.id.usbCon10);
        fingImg = (ImageView) findViewById(R.id.fingerImg);
        fingImg.setColorFilter(Color.parseColor("#ff0000ff"));

        tView = (TextView) findViewById(R.id.fup_tView);
        tView.setText("Please hold your finger on the scanner.");
        tView.setTextColor(Color.parseColor("#ff0000ff"));

        //Declaring new communication reference
        PiComm = new CommStream();
        PiComm.PiLog(this.getLocalClassName(),DrinkOrder.OrderStatus());

        CommStream.writeString("$FPIDEN,START");
        failureCount = 0;
        ProgressBar pBar = (ProgressBar) findViewById(R.id.progressBarfpu);
        pBar.setVisibility(View.INVISIBLE);
        //Scheduling hiding menu, and recevie communications
        new Timer().scheduleAtFixedRate(new ListenTask(), 100, 100);
        new Timer().scheduleAtFixedRate(HideTask, 100, 100);
        WatchDog.schedule(WatchTask,_FINGER_PICKUP_TIMEOUT);

        if (CommStream.DEBUG_MODE) {

        } else {
            Button bButt = (Button) findViewById(R.id.fpu_skip);
            bButt.setVisibility(View.INVISIBLE);
            mp = Sound.playPlaceFinger(getApplicationContext());
        }


    }


    //Menu Navigators
    public void SkipToNewUser(View view) {
        //Intent i = new Intent(this,CheckBAC.class).putExtra("DString",OrderString);
        //if(CommStream.DEBUG_MODE) {
        startActivity(new Intent(this, NewUser.class).putExtra("New User","yes"));
        //}
    }


    public void SkipToWater(View view){
        Intent i = new Intent(this, ConfirmDrink.class);
        i.putExtra("Water",true);
        startActivity(i);
    }


    public void Skip(View view) {
        startActivity(new Intent(this, NewUser.class));
    }

    public void FPUGoBack(View view) {
        startActivity(new Intent(this, IdleMenu.class));
    }

    /**
     * FingerPrint State Machine to determine if the fingerprint not only has a drink on the queue,
     * but also what that drink is.
     */
    public void CompareFingerSM(String msg) {
        if (CommStream.DEBUG_MODE) {
            Toast.makeText(getApplicationContext(), "MSG:" + msg, Toast.LENGTH_SHORT).show();
        }
        msg = msg.trim();
        switch (currentState) {
            case IDLE:
                switch (msg) {
                    case ("$FPIDEN,WORKING"):
                        nextState = FingerState.COMPARING;
                        break;
                    case "$FPIDEN,ERR,NOTFOUND":
                        nextState = FingerState.WARNING;
                        break;
                    case "$FPIDEN,ENDED":
                        CommStream.writeString("$FPIDEN,START");
                        break;
                    default:
                        break;
                }
                break;
            case COMPARING:
                String[] tokens = msg.split("[,]");
                String msg2 = msg;
                if (tokens.length > 2) {
                    msg2 = tokens[0] + "," + tokens[1];
                }
                if (msg2.equals("$FPIDEN,SUCC") && !msg.contains("-1")) {
                    nextState = FingerState.PASSED;
                    tView = (TextView) findViewById(R.id.fup_tView);
                    tView.setText("Hello Please Place your index finger on the Scanner.");
                    if (CommStream.DEBUG_MODE) {
                        try {
                            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                            Toast.makeText(getApplicationContext(), tokens[tokens.length - 1], Toast.LENGTH_LONG).show();
                        } catch (NullPointerException npe) {
                            npe.printStackTrace();
                        }
                    }

                    userPinNumber = tokens[tokens.length - 1];
                    DrinkOrder.InUserPinString = userPinNumber;
                    if(userPinNumber.contains(DrinkOrder.ADMIN_PIN_1) || userPinNumber.contains(DrinkOrder.ADMIN_PIN_2)){
                        Toast.makeText(getApplicationContext(),"ADMIN PIN detected",Toast.LENGTH_SHORT);
                        finish();
                        return;
                    }else {
                        Toast.makeText(getApplicationContext(),"NO Admin Pin"+userPinNumber,Toast.LENGTH_SHORT);

                        new FingerPickUp.AttemptGetDrink().execute();
                    }
                } else if (msg2.equals("$FPIDEN,ERR") || msg.contains("-1")) {
                    failureCount++;
                    if (failureCount < 3) {
                        nextState = FingerState.IDLE;
                        tView.setText("Please Hold Finger");
                        CommStream.writeString("$FPIDEN,START");
                    } else {
                        //tView.setText("Please Come Back Another Time");
                        nextState = FingerState.WARNING;
                        tView.setText("Please Wait..");
                        startActivity(new Intent(FingerPickUp.this, NewUser.class).putExtra("New User","no"));
                    }
                }
                break;
//            case PASSED:
//                if(msg.equals("$GOTDRINK")){
//                    startActivity(new Intent(FingerPickUp.this,ConfirmDrink.class));
//                }
//                break;
            case WARNING:
                tView.setText("Your Fingerprint was not detected on our System.");
                break;
            default:
                if (CommStream.DEBUG_MODE) {
                    Toast.makeText(getApplicationContext(), "Unknown state.", Toast.LENGTH_SHORT).show();
                }
                Log.d("SM", "Unknown State");
        }
        currentState = nextState;

    }


    class AttemptGetDrink extends AsyncTask<String, String, String> {
        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("AGD", "Pre-Exec");
        }

        @Override
        protected String doInBackground(String... args) {
            // TODO Auto-generated method stub
            // Check for success tag
            int success;
//            String userpin = eText.getText().toString();
            String userpin = userPinNumber;
            if (userPinNumber.contains("-1")) {
                return null;
            }
            DrinkOrder.InUserPinString = userpin;
            try {
                Log.d("GetDQ", "Mid-Execute");
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("pin", userpin));

                Log.d("GetDQ", "starting");
                // getting product details by making HTTP request
                JSONObject json = jsonParser.makeHttpRequest(
                        ServerAccess.GET_DRINK_URL, "POST", params);

                // check your log for json response
                Log.d("GetDQ", "Drink retrieve attempt" + json.toString());

                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("GetDQ", "Drink found Successful!" + json.toString());
                    searchFailure = false;
                    //Intent i = new Intent(Login.this, ReadComments.class);
                    return json.getString(TAG_MESSAGE);
                } else {
                    Log.d("GetDQ", "Login Failure!" + json.getString(TAG_MESSAGE));
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

        /**
         * After completing background task Dismiss the progress dialog
         * *
         */
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once product deleted
            if (file_url != null) {
                if (CommStream.DEBUG_MODE) {
                    Toast.makeText(FingerPickUp.this, file_url, Toast.LENGTH_LONG).show();
                }
                if(!file_url.contains("Failed") && !file_url.contains("failed")) {
                    IncomingString = file_url;
                    DrinkOrder.InDrinkString = file_url;
                    Log.d("GetDQ", "Success url is !" + file_url);
                    new AttemptGetCust().execute();
                    //CompareFingerSM("$GOTDRINK");
                    startActivity(new Intent(FingerPickUp.this, CheckBAC.class));
                }else{
                    Toast.makeText(FingerPickUp.this,"Login Failed. Try Again",Toast.LENGTH_LONG).show();
                }

            } else {
                Toast.makeText(FingerPickUp.this, "Failure to Access Server. Check Internet Connection"
                        , Toast.LENGTH_SHORT).show();
            }

        }

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
                Log.d("AGC", "file_url is null");
//                Toast.makeText(MainActivity.this, "Failure to Access Server. Check Internet Connection"
//                        , Toast.LENGTH_SHORT).show();
            }
        }
    }



    /**
     * System functions****
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
        public void run() {
            FingerPickUp.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hideSystemUI();
                }
            });
        }
    };

    public void GoBack(View view) {
        startActivity(new Intent(FingerPickUp.this, IdleMenu.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pick_up_finger, menu);
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
