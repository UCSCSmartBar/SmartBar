package com.example.trider.smartbarui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;


public class NewUserFinger extends Activity {

    CommStream PiComm;
    String OrderString;
    String userPinNumber;

    static int trans = 0;

//States for FingerPrint Scanner
public enum FingerState {
     IDLE,
     FIRST_FINGER_ON,
     FIRST_FINGER_OFF,
     SECOND_FINGER_ON,
     SECOND_FINGER_OFF,
     THIRD_FINGER_ON,
     THIRD_FINGER_OFF,
     REGISTERED,
     WARNING
}

    FingerState currentState = FingerState.IDLE;
    FingerState nextState = FingerState.IDLE;

    TextView tView;

    /**
     * Changes the transparency of the finger to be shown
     */
    Runnable ChangeFingerPic = new Runnable() {
        @Override
        public void run() {
            ImageView finger = (ImageView) findViewById(R.id.newFingerImg);
            finger.setColorFilter(Color.argb(trans-1, 255, 255, 255));
            Log.d("Color", "Trans is " + trans);
            trans = trans+10;
            if(trans > 230){
                trans = 0;
            }
        }
    };


    TimerTask ChangeFinger = new TimerTask() {
        public void run() {
            NewUserFinger.this.runOnUiThread(ChangeFingerPic);
        }
    };



    //Listens to Pi
    boolean isActive = true;
    class ListenTask extends TimerTask {
        @Override
        public void run(){
            NewUserFinger.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isActive) {
                        /**
                         * @subTimeTask
                         *
                         * 
                         */
                        String t = CommStream.ReadBuffer();
                        if(t!=null) {
                            TextView tV = (TextView) findViewById(R.id.fingerView);
                            tV.append(t);
                            RunPrintStateMachine(t);

                        }
                    }
                }
            });
        }
    }

    @Override
    public void onStop(){
        super.onStop();
        isActive =false;
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user_finger);
        tView = (TextView) findViewById(R.id.nuf_tView);

        PiComm = new CommStream();

        ImageView finger = (ImageView) findViewById(R.id.newFingerImg);
        Intent intent = getIntent();


        try {
            OrderString = intent.getExtras().getString("tString");
        }catch(NullPointerException e){
            e.printStackTrace();
        }



        new Timer().schedule(ChangeFinger,1000,1000);
        new Timer().schedule(new ListenTask(),1000,100);

        if(DrinkOrder.InUserPinString == null){
            userPinNumber = "12345678901";
        }else{
            userPinNumber = DrinkOrder.InUserPinString;
        }
        PiComm.writeString("$FPENROLL,"+ userPinNumber);
        //For actual implementation of state machine start with Finger Print Invisible
        finger.setVisibility(View.INVISIBLE);



        new Timer().scheduleAtFixedRate(HideTask,100,10);
    }


    /**
     *The FingerPrint State machine to guide the user to enroll his fingerprint into the system.
     * The SM progresses based on the fingerprints current enrollment status. The Scanner forwards
     * message to the raspberry Pi over UART, and
     * @param s The Parameter to progress the state machine for the fingerprint
     */
    public void RunPrintStateMachine(String s){

        final String T = s;
        ImageView finger = (ImageView) findViewById(R.id.newFingerImg);
        s= s.trim();
        NewUserFinger.this.runOnUiThread(new Runnable(){
            @Override
            public void run(){
                Toast.makeText(getApplicationContext(),"Incoming Message: "+ T,Toast.LENGTH_SHORT).show();
            }
        });

            switch(currentState){
                case IDLE:
                    switch(s){
                        case "$FPENROLL,EN1":
                            nextState = FingerState.FIRST_FINGER_ON;
                            //tView.setText("Please Remove Finger");
                            break;
                        default:
                            break;
                    }
                    break;
                case FIRST_FINGER_ON:
                    switch(s){
                        case "$FPENROLL,RM1":
                            nextState = FingerState.FIRST_FINGER_OFF;
                            tView.setText("Please Remove Finger");

                            finger.setVisibility(View.VISIBLE);
                            finger.setColorFilter(Color.argb(220, 255, 255, 255));
                            break;
                        case "FPENROLL,ERR,1":
                            nextState = FingerState.WARNING;
                            break;
                        case "FPENROLL,EN1,TIMEOUT":
                            nextState = FingerState.WARNING;
                            break;
                        default:
                            break;
                    }
                    break;
                case FIRST_FINGER_OFF:
                    switch(s){
                        case "$FPENROLL,EN2":
                            nextState = FingerState.SECOND_FINGER_ON;
                            tView.setText("Please Place Finger on Scanner Again");
                            finger.setVisibility(View.VISIBLE);
                            finger.setColorFilter(Color.argb(200, 255, 255, 255));
                            break;
                        case "FPENROLL,ERR,1":
                            nextState = FingerState.WARNING;
                            break;
                        default:
                            break;
                    }
                    break;
                case SECOND_FINGER_ON:
                    switch(s){
                        case "$FPENROLL,RM2":
                            nextState = FingerState.SECOND_FINGER_OFF;
                            tView.setText("Please Remove Finger Again");
                            finger.setColorFilter(Color.argb(180, 255, 255, 255));
                            break;
                        case "FPENROLL,ERR,2":
                            nextState = FingerState.WARNING;
                            break;
                    }
                    break;

                case SECOND_FINGER_OFF:
                    switch(s){
                        case "$FPENROLL,EN3":
                            nextState = FingerState.THIRD_FINGER_ON;
                            tView.setText("Please  Place Finger Once More");
                            finger.setVisibility(View.VISIBLE);
                            finger.setColorFilter(Color.argb(160, 255, 255, 255));
                            break;
                        case "FPENROLL,ERR,2":
                            nextState = FingerState.WARNING;
                            break;
                        default:
                            break;
                    }
                    break;
                case THIRD_FINGER_ON:
                    switch(s){
                        case "$FPENROLL,RM3":
                            nextState = FingerState.THIRD_FINGER_OFF;
                            tView.setText("Please Remove Finger Once More");
                            finger.setColorFilter(Color.argb(140, 255, 255, 255));
                            break;
                        case "FPENROLL,ERR,3":
                            nextState = FingerState.WARNING;
                            break;
                    }
                    break;

                case THIRD_FINGER_OFF:
                    String[] tokens = s.split("[,]");
                    String Iden = "";
                    if(tokens.length > 2){
                        Iden = tokens[1];
                    }
                    switch(Iden){
                        case "SUCC":
                            nextState = FingerState.REGISTERED;
                            tView.setText("ThankYou please wait while we register you in our System");
                            finger.setVisibility(View.VISIBLE);
                            finger.setColorFilter(Color.argb(120, 255, 255, 255));
                            startActivity(new Intent(NewUserFinger.this,ConfirmDrink.class));

                            break;
                        case "FPENROLL,ERR,3":
                            nextState = FingerState.WARNING;
                            break;
                        default:
                            break;
                    }
                    break;
                case REGISTERED:
                    if(s.equals("Finish")){
                        startActivity(new Intent(this,CheckBAC.class));
                    }
                    break;
                case WARNING:
                    tView.setText("Warning Fingerprint scan failure. Please Wait...");
                    PiComm.writeString("$FPENROLL,"+ userPinNumber);
                    nextState = FingerState.IDLE;
                    break;
            }
        currentState = nextState;

        NewUserFinger.this.runOnUiThread(new Runnable(){
            @Override
            public void run(){
                Toast.makeText(getApplicationContext(),"Incoming Message: "+ currentState.toString(),Toast.LENGTH_SHORT).show();
            }
        });

    }


    public void SkipToBAC(View view){startActivity(new Intent(this,ConfirmDrink.class).putExtra("DOrder",OrderString));}




    /***********System Level Functions*******/
    public void startWatch() {
        new Timer().schedule(new TimerTask() {
            public void run() {
                startActivity(new Intent(NewUserFinger.this, IdleMenu.class));
            }

        }, 1000);


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
            NewUserFinger.this.runOnUiThread(new Runnable() {
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
        getMenuInflater().inflate(R.menu.menu_register_finger_print, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
