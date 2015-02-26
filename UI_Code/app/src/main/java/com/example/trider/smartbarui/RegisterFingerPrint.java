package com.example.trider.smartbarui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class RegisterFingerPrint extends Activity {

CommStream PiComm = new CommStream();

static int trans = 0;

//States for FingerPrint Scanner
public enum FingerState {
     IDLE,
     FIRST_FINGER,
     SECOND_FINGER,
     THIRD_FINGER,
     REGISTERED,
     WARNING
}
FingerState currentState = FingerState.IDLE;
FingerState nextState = FingerState.IDLE;

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
            RegisterFingerPrint.this.runOnUiThread(ChangeFingerPic);
        }
    };


    /**
     * @title: mListenerTask
     * @description: The background thread that receives serial communication from the raspberry pi,
     *
     */
    Runnable mListenerTask = new Runnable() {
        @Override
        public void run() {
            byte[] buffer = new byte[128];
            //ret is the size of the size of the incoming buffer
            int ret;
            try {
                if(PiComm.isInitialized()) {
                    ret = PiComm.getIStream().read(buffer);

                    if (ret < 128) {
                        String msg = new String(buffer);
                        RunPrintStateMachine(msg);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Waits for new input communication
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            new Thread(this).start();
        }
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_finger_print);

        ImageView finger = (ImageView) findViewById(R.id.newFingerImg);
        Intent intent = getIntent();
        if(!PiComm.isInitialized()){
            String s = intent.getExtras().getString("tString");
            DrinkOrder a = new DrinkOrder();
            a.DecodeString(s);
        }
        PiComm.writeString("$FP.First");
        new Thread(mListenerTask).start();
        new Timer().schedule(ChangeFinger,1000,1000);

        //For actual implementation of state machine start with Finger Print Invisible
        finger.setVisibility(View.INVISIBLE);
    }



    public void RunPrintStateMachine(String s){

        final String T = s;
        ImageView finger = (ImageView) findViewById(R.id.newFingerImg);
        s= s.trim();
        RegisterFingerPrint.this.runOnUiThread(new Runnable(){
            @Override
            public void run(){
                Toast.makeText(getApplicationContext(),"Incoming Message: "+ T,Toast.LENGTH_SHORT).show();
            }
        });

            switch(currentState){
                case IDLE:
                    switch(s){
                        case "$FP.Ready":
                            nextState = FingerState.FIRST_FINGER;
                            break;
                        default:
                            break;
                    }
                    break;
                case FIRST_FINGER:
                    switch(s){
                        case "$FP.1.Success":
                            nextState = FingerState.SECOND_FINGER;
                            finger.setVisibility(View.VISIBLE);
                            finger.setColorFilter(Color.argb(220, 255, 255, 255));
                            break;
                        case "FP.1.Failure":
                            nextState = FingerState.WARNING;
                            break;
                        default:
                            break;
                    }
                    break;
                case SECOND_FINGER:
                    switch(s){
                        case "$FP.2.Success":
                            nextState = FingerState.THIRD_FINGER;
                            finger.setColorFilter(Color.argb(110, 255, 255, 255));
                            break;
                        case "$FP.2.Failure":
                            nextState = FingerState.WARNING;
                            break;
                    }
                    break;
                case THIRD_FINGER:
                    switch(s){
                        case "$FP.3.Success":
                            nextState = FingerState.REGISTERED;
                            finger.setColorFilter(Color.argb(25, 255, 255, 255));
                            break;
                        case "FP.3.Failure":
                            nextState = FingerState.WARNING;
                            break;
                    }
                    break;
                case REGISTERED:
                    if(s.equals("Finish")){
                        startActivity(new Intent(this,CheckBAC.class));
                    }
                    break;
                case WARNING:
                    break;
            }
        currentState = nextState;

        RegisterFingerPrint.this.runOnUiThread(new Runnable(){
            @Override
            public void run(){
                Toast.makeText(getApplicationContext(),"Incoming Message: "+ currentState.toString(),Toast.LENGTH_SHORT).show();
            }
        });

    }




    public void SkipToBAC(View view){startActivity(new Intent(this,CheckBAC.class));}


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
