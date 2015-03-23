package com.example.trider.smartbarui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
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
    private static final String LOGIN_URL = "http://www.ucscsmartbar.com/getDrink.php";
    //JSON element ids from response of php script:
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    JSONParser jsonParser = new JSONParser();
    String OrderString;
    boolean searchFailure = true;

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
     *
     */
    //Listens to Pi
    boolean isActive = true;
    class ListenTask extends TimerTask {
        @Override
        public void run(){
            FingerPickUp.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isActive) {
                        /**
                         * @subTimeTask
                         */
                        String t = CommStream.ReadBuffer();
                        if(t!=null) {
                            TextView tV = (TextView) findViewById(R.id.fup_text);
                            tV.append(t);
                            CompareFingerSM(t);
                            String rMessage = SystemCodeParser.DecodeAccessoryMessage(t);
                        }


                    }
                }
            });
        }
    }



    public void onResume(){
        super.onResume();
        hideSystemUI();
    }

    @Override
    public void onStop(){
        super.onStop();
        isActive = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finger_pick_up);

        //Hiding menu
        hideSystemUI();


        //Loading view references
        ImageView usbConn = (ImageView) findViewById(R.id.usbCon10);
        fingImg= (ImageView) findViewById(R.id.fingerImg);
        tView = (TextView) findViewById(R.id.fup_tView);
        tView.setText("Please Place Your Finger on the Scanner");


        //Declaring new communiccation reference
        PiComm = new CommStream();
        if(!PiComm.isInitialized()){
            usbConn.setVisibility(View.INVISIBLE);
        }

        PiComm.writeString("$FPIDEN,START");


        //Scheduling hiding menu, and recevie communications
        new Timer().scheduleAtFixedRate(new ListenTask(), 100,100);
        new Timer().scheduleAtFixedRate(HideTask,100,100);


        //Deciphering string for drink order and double checking.
        Intent i = getIntent();




//        //User is Null only for testing
//        if(DrinkOrder.InUserPinString == null){
//            userPinNumber = "12345678901";
//            PiComm.writeString("$FPQ,"+ userPinNumber);
//            long time = System.currentTimeMillis();
//            while(System.currentTimeMillis() < time + 7000);
//
//        }else{
//            userPinNumber = DrinkOrder.InUserPinString;
//        }



        /**
         * Starts Communication with the raspberry Pi for identifying the fingerprint
         */




    }

    public void SkipFingerPrint(View view){
        //Intent i = new Intent(this,CheckBAC.class).putExtra("DString",OrderString);

        startActivity(new Intent(this,ConfirmDrink.class).putExtra("DString",OrderString));
    }

    public void CompareFingerSM(String msg) {

        Toast.makeText(getApplicationContext(), "MSG:"+msg, Toast.LENGTH_SHORT).show();

        msg = msg.trim();
        switch (currentState) {
            case IDLE:
                if (msg.equals("$FPIDEN,WORKING")) {
                    nextState = FingerState.COMPARING;
                } else if (msg.equals("$FPIDEN,ERR,NOTFOUND")) {
                    nextState = FingerState.WARNING;
                } else if(msg.equals("$FPIDEN,ENDED")){
                    PiComm.writeString("$FPIDEN,START");
                }
                break;
            case COMPARING:
                String[] tokens = msg.split("[,]");
                if(tokens.length > 2){
                    msg = tokens[0]+","+tokens[1];
                }

                if (msg.equals("$FPIDEN,SUCC")) {
                    nextState = FingerState.PASSED;
                    tView = (TextView) findViewById(R.id.fup_tView);
                    tView.setText("Please Place Your Finger on the Scanner");

                    Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_SHORT).show();

                    Toast.makeText(getApplicationContext(),tokens[tokens.length-1],Toast.LENGTH_LONG).show();

                    userPinNumber = tokens[tokens.length-1];
                    new FingerPickUp.AttemptGetDrink().execute();



                } else if (msg.equals("$FPIDEN,ERR")) {
                    tView.setText("ERROR");
                    failureCount++;
                    if(failureCount > 5) {
                        nextState = FingerState.IDLE;
                        tView.setText("Please Try Again");
                        PiComm.writeString("$FPIDEN,START");
                    }else{
                        tView.setText("Please Come Back Another Time");
                        nextState = FingerState.WARNING;

                        //startActivity(new Intent(FingerPickUp.this,IdleMenu.class));
                    }
                }
                break;
            case PASSED:

                if(msg.equals("$GOTDRINK")){
                    startActivity(new Intent(FingerPickUp.this,ConfirmDrink.class));
                }
                break;
            case WARNING:
                break;
            default:
                Toast.makeText(getApplicationContext(), "Unknown state.", Toast.LENGTH_SHORT).show();
                Log.d("SM", "Unknown State");
        }


        currentState = nextState;

    }



    class AttemptGetDrink extends AsyncTask<String, String, String> {
        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            Log.d("AGD","Pre-Exec");
        }

        @Override
        protected String doInBackground(String... args) {
            // TODO Auto-generated method stub
            // Check for success tag
            int success;
//            String userpin = eText.getText().toString();
            String userpin = userPinNumber;
            new DrinkOrder().InUserPinString = userpin;
            try {
                Log.d("GetDQ","Mid-Execute");
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("pin", userpin));

                Log.d("GetDQ", "starting");
                // getting product details by making HTTP request
                JSONObject json = jsonParser.makeHttpRequest(
                        LOGIN_URL, "POST", params);

                // check your log for json response
                Log.d("GetDQ","Drink retrieve attempt"+ json.toString());

                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("GetDQ", "Drink found Successful!"+json.toString());
                    searchFailure = false;
                    //Intent i = new Intent(Login.this, ReadComments.class);
                    return json.getString(TAG_MESSAGE);
                }else{
                    Log.d("GetDQ","Login Failure!"+ json.getString(TAG_MESSAGE));
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
            if (file_url != null){
                Toast.makeText(FingerPickUp.this, file_url, Toast.LENGTH_LONG).show();
                IncomingString = file_url;
                DrinkOrder.InDrinkString = file_url;
                Log.d("GetDQ","Success url is !"+ file_url);
                CompareFingerSM("$GOTDRINK");

            }else{
                Toast.makeText(FingerPickUp.this,"Failure to Access Server. Check Internet Connection"
                        , Toast.LENGTH_SHORT).show();
            }

        }

    }
    /**System functions*****/

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
            FingerPickUp.this.runOnUiThread(new Runnable() {
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
