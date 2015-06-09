package com.example.trider.smartbarui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;




public class PickUpFinger extends Activity {

    CommStream PiComm;
    ImageView fingImg;
    TextView tView;
    String userPinNumber;

    boolean toggle = false;
    static int failureCount;
    long Timeout = 0;

    String OrderString;

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
            PickUpFinger.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isActive) {
                        /**
                         * @subTimeTask
                         */
                        String t = CommStream.ReadBuffer();
                        if(t!=null) {
                            TextView tV = (TextView) findViewById(R.id.puf_text);
                            tV.append(t);
                            CompareFingerSM(t);
                            String rMessage = new SystemCodeParser().DecodeAccessoryMessage(t);
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
        setContentView(R.layout.activity_pick_up_finger);

        //Hiding menu
        hideSystemUI();

        //Loading view references
        //ImageView usbConn = (ImageView) findViewById(R.id.usbCon1);
        fingImg= (ImageView) findViewById(R.id.fingerImg);
        tView = (TextView) findViewById(R.id.puf_tView);
        tView.setText("Please Place Your Finger on the Scanner");
        tView.setTextColor(Color.parseColor("#ff0000ff"));


        //Declaring new communiccation reference
        PiComm = new CommStream();



        //Scheduling hiding menu, and recevie communications
        new Timer().scheduleAtFixedRate(new ListenTask(), 100,100);
        new Timer().scheduleAtFixedRate(HideTask,100,100);


        //Deciphering string for drink order and double checking.
        Intent i = getIntent();
        try {
            OrderString = "$DO," + i.getExtras().getString("tString");
            Toast toast = Toast.makeText(getApplicationContext(), OrderString, Toast.LENGTH_LONG);
            toast.show();

        }catch(NullPointerException e){
            Toast.makeText(getApplicationContext(), "No Drink Added", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
         }



        //User is Null only for testing
        if(DrinkOrder.InUserPinString == null){
            userPinNumber = "12345678901";
            PiComm.writeString("$FPQ,"+ userPinNumber);
            long time = System.currentTimeMillis();
            while(System.currentTimeMillis() < time + 7000);

        }else{
            userPinNumber = DrinkOrder.InUserPinString;
        }

        PiComm.writeString("$FPIDEN,START");

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
                    startActivity(new Intent(PickUpFinger.this,ConfirmDrink.class));
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
                        startActivity(new Intent(PickUpFinger.this,IdleMenu.class));
                    }
                }
                break;
            case PASSED:
                if(msg.equalsIgnoreCase("Finish")){
                    startActivity(new Intent(this,CheckBAC.class));
                }
                break;
            case FAILED:
                if(msg.equals("$FP.Start")){
                    nextState = FingerState.COMPARING;
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
            PickUpFinger.this.runOnUiThread(new Runnable() {
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
