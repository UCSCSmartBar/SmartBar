package com.example.trider.smartbarui;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;




public class PickUpFinger extends Activity {

    CommStream PiComm;
    ImageView fingImg;

    boolean toggle = false;
    static int failureCount;
    long Timeout = 0;

    String OrderString;

    public enum FingerState {
        IDLE,
        COMPARING,
        PASSED,
        FAILED,
        WARNING
    }


    FingerState currentState = FingerState.IDLE;
    FingerState nextState = FingerState.IDLE;


    TimerTask FlashFinger =  new TimerTask() {
        public void run() {
            PickUpFinger.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(toggle){
                        fingImg.setVisibility(View.INVISIBLE);
                    }else{
                        fingImg.setVisibility(View.VISIBLE);
                    }
                    toggle = !toggle;
                }
            });
        };
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
                        CompareFingerSM(msg);
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
        setContentView(R.layout.activity_pick_up_finger);


        ImageView usbConn = (ImageView) findViewById(R.id.usbCon1);
        fingImg= (ImageView) findViewById(R.id.fingerImg);


        PiComm = new CommStream();

        Intent i = getIntent();
        try {
            OrderString = "$DO," + i.getExtras().getString("tString");
            Toast toast = Toast.makeText(getApplicationContext(), OrderString, Toast.LENGTH_LONG);
            toast.show();
            //PiComm.writeString(s);
            new Thread(mListenerTask).start();
        }catch(NullPointerException e){
            Toast.makeText(getApplicationContext(), "No Drink Added", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
         }

        if(!PiComm.isInitialized()){
                usbConn.setVisibility(View.INVISIBLE);
            //Already Decoded in last activity
//                DrinkOrder a = new DrinkOrder();
//                a.DecodeString(OrderString);

        }

        new Timer().schedule(FlashFinger,1000,5000);


    }

    public void SkipFingerPrint(View view){
        Intent i = new Intent(this,CheckBAC.class).putExtra("DString",OrderString);
        startActivity(i);
    }

    public void CompareFingerSM(String msg) {

        switch (currentState) {
            case IDLE:
                if (msg.equals("$FP.Start")) {
                    nextState = FingerState.COMPARING;
                } else if (msg.equals("$FP.Error")) {
                    nextState = FingerState.WARNING;
                }
                break;
            case COMPARING:
                if (msg.equals("$FP.SUCCESS")) {
                    nextState = FingerState.PASSED;
                } else if (msg.equals("$FP.FAILED")) {
                    failureCount++;
                    if(failureCount > 3) {
                        nextState = FingerState.WARNING;
                    }else{
                        nextState = FingerState.FAILED;
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
                Toast.makeText(getApplicationContext(), "Unknown state", Toast.LENGTH_SHORT).show();
                Log.d("SM", "Unknown State");
        }
        currentState = nextState;

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
