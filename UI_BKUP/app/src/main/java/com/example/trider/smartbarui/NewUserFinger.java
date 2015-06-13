package com.example.trider.smartbarui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
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

    MediaPlayer mp;

    FingerState currentState = FingerState.IDLE;
    FingerState nextState = FingerState.IDLE;

    Timer tim;
    TextView tView;



    //Index for testing purposes
    static int index = 0;
    /**
     * Sends a message after a given amount of time
     */




    int _NEW_USER_FINGER_TIMEOUT = 80000;
    Timer WatchDog = new Timer();
    TimerTask WatchTask = new TimerTask() {
        @Override
        public void run() {
            Log.d("NewUser","Timeout");
            finish();
            startActivity(new Intent(NewUserFinger.this,IdleMenu.class));
        }
    };

    /**
     * FingerPrint Enrollment Test Harness. Only active when DEBUG_MODE is enabled. Simulates the
     * enrollment process
     *
     */
    TimerTask FingerPrintTestHarness = new TimerTask() {
        public void run(){
            NewUserFinger.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("NUF","FPTest index:"+index);
                    if(!isActive){FingerPrintTestHarness.cancel();}
                    switch (index) {
                        case 0:
                            //RunPrintStateMachine("$FPENROLL,RM3");
                            RunPrintStateMachine("$FPENROLL,EN1");
                            break;
                        case 1:
                            RunPrintStateMachine("$FPENROLL,RM1");
                            break;
                        case 2:
                            RunPrintStateMachine("$FPENROLL,EN2");
                            break;
                        case 3:
                            RunPrintStateMachine("$FPENROLL,RM2");
                            break;
                        case 4:
                            RunPrintStateMachine("$FPENROLL,EN3");
                            break;
                        case 5:
                            RunPrintStateMachine("$FPENROLL,EN3,TIMEOUT");
                            break;
                        case 6:
                            RunPrintStateMachine("$FPENROLL,EN1");
                            break;
                        case 7:
                            RunPrintStateMachine("$FPENROLL,RM1");
                            break;
                        case 8:
                            RunPrintStateMachine("$FPENROLL,EN2");
                            break;
                        case 9:
                            RunPrintStateMachine("$FPENROLL,RM2");
                            break;
                        case 10:
                            RunPrintStateMachine("$FPENROLL,EN3");
                            break;
                        case 11:
                            RunPrintStateMachine("$FPENROLL,RM3");
                            break;
                        case 12:
                            RunPrintStateMachine("$FPENROLL,SUCC,18316013559");
                        case 13:
                            FingerPrintTestHarness.cancel();
                        default:
                            //Stops counting
                            return;
                    }
                    index++;
                    //RunPrintStateMachine("$FPENROLL,EN1");
                }
            });
        }//end of run
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
                         */
                        String t = CommStream.ReadBuffer();
                        if(t!=null) {
                            TextView tV = (TextView) findViewById(R.id.fingerView);
                            if(CommStream.DEBUG_MODE){tV.append(t);}
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
        WatchDog.cancel();
        WatchTask.cancel();
        Log.d("LIFE","onStop");
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.d("LIFE", "onDestroy");
    }

    @Override
    public void onStart(){
        super.onStart();

        }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_new_user_finger);
        tView = (TextView) findViewById(R.id.nuf_tView);

        PiComm = new CommStream();
        PiComm.PiLog(this.getLocalClassName(),DrinkOrder.OrderStatus());
        //Sets fingerprint image
        ImageView finger = (ImageView) findViewById(R.id.newFingerImg);
        finger.setColorFilter(Color.argb(200, 0,0,255));


        WatchDog.schedule(WatchTask,_NEW_USER_FINGER_TIMEOUT);

//        try {
//            OrderString = intent.getExtras().getString("tString");
//        }catch(NullPointerException e){
//            e.printStackTrace();
//        }


        userPinNumber = DrinkOrder.InUserPinString;


        //TODO Changed to a timed activity
        new Timer().scheduleAtFixedRate(HideTask, 1, 10);
        new Timer().schedule(new ListenTask(), 1000, 100);



        if(CommStream.DEBUG_MODE){

        }else{
            Button b = (Button) findViewById(R.id.nuf_skip);
            b.setVisibility(View.INVISIBLE);
        }





        final Dialog dialog = new Dialog(new ContextThemeWrapper(NewUserFinger.this, R.style.SmartUIDialog));
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

        dialog.setContentView(R.layout.dialog_enrollment);
        //dialog.setTitle("Do You Have an Account with Smart Bar?");
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#44000000")));
        dialog.findViewById(R.id.enroll_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                CommStream.writeString("$FPENROLL," + userPinNumber);
                Sound.playRegister(getApplicationContext());

                if(CommStream.TEST_HARNESS) {
                    //Begins the test harness
                    index = 0;
                    new Timer().schedule(FingerPrintTestHarness, 3000, 3000);
                }


                dialog.dismiss();

            }
        });
        dialog.show();
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);




    }


    /**
     *The FingerPrint State machine to guide the user to enroll his fingerprint into the system.
     * The SM progresses based on the fingerprints current enrollment status. The Scanner forwards
     * message to the raspberry Pi over UART, and the Raspberry Pi sends custom commands to the
     * Tablet. Since enrollments take three finger presses, the State machine must quickly and
     * efficiently prompt the user(possibly inebriated) to place fingers on the scanner.
     * @param s The Parameter to progress the state machine for the fingerprint
     */
    public void RunPrintStateMachine(String s){

        final String T = s;
        ImageView finger = (ImageView) findViewById(R.id.newFingerImg);
        s= s.trim();
        //Debugging shows messages to/fro raspberry pi.
        if(CommStream.DEBUG_MODE) {
            NewUserFinger.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Incoming Message: " + T, Toast.LENGTH_SHORT).show();
                }
            });
        }

        boolean warning = false;
            //Overarching switch statement for the state machine
        Log.d("FPSM","CurrentState: "+currentState.name());
        Log.d("FPSM","IncomingMessage: "+s);
            switch(currentState){
                case IDLE:
                    switch(s){
                        case "$FPENROLL,EN1":
                            tView.setText("Hello please hold finger on the scanner");
                            nextState = FingerState.FIRST_FINGER_ON;
                            tView.setTextColor(Color.parseColor("#ff0000ff"));
                            finger.setVisibility(View.VISIBLE);
                            finger.setColorFilter(Color.argb(200, 0, 0, 255));
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
                            //Changes the text of the message and color of the fingerprint.
                            tView.setText("Please Remove Finger");
                            tView.setTextColor(Color.parseColor("#ffff0000"));
                            finger.setVisibility(View.VISIBLE);
                            finger.setColorFilter(Color.argb(220, 255,0,0));
                            break;
                        default:
                            String Message[] = s.split(",");
                            if(s.contains("$FPENROLL")){
                                if(s.contains("ERR")|| s.contains("TIMEOUT")){
                                    tView.setText("Warning Fingerprint scan failure. Please Wait...");
                                    nextState = FingerState.WARNING;
                                    warning = true;

//                                    new Timer().schedule(new TimerTask() {
//                                        @Override
//                                        public void run() {
//                                            CommStream.writeString("$FPENROLL,"+userPinNumber);
//                                        }
//                                    },2000);

                                }
                            }
                            break;
                    }
                    break;
                case FIRST_FINGER_OFF:
                    switch(s){
                        case "$FPENROLL,EN2":
                            nextState = FingerState.SECOND_FINGER_ON;
                            tView.setText("Please Place Finger on Scanner Again");
                            tView.setTextColor(Color.parseColor("#ff0000ff"));
                            finger.setVisibility(View.VISIBLE);
                            finger.setColorFilter(Color.argb(200, 0, 0, 255));
                            break;
                        default:
                            String Message[] = s.split(",");
                            if(s.contains("$FPENROLL")){
                                if(s.contains("ERR")|| s.contains("TIMEOUT")){
                                    tView.setText("Warning Fingerprint scan failure. Please Wait...");
                                    nextState = FingerState.WARNING;
                                    warning = true;
//                                    new Timer().schedule(new TimerTask() {
//                                        @Override
//                                        public void run() {
//                                            CommStream.writeString("$FPENROLL,"+userPinNumber);
//                                        }
//                                    },2000);
                                }
                            }
                            break;
                    }
                    break;
                case SECOND_FINGER_ON:
                    switch(s){
                        case "$FPENROLL,RM2":
                            nextState = FingerState.SECOND_FINGER_OFF;
                            tView.setText("Please Remove Finger Again");
                            tView.setTextColor(Color.parseColor("#ffff0000"));
                            finger.setVisibility(View.VISIBLE);
                            finger.setColorFilter(Color.argb(200, 255,0,0));
                            break;
                        default:
                            String Message[] = s.split(",");
                            if(s.contains("$FPENROLL")){
                                if(s.contains("ERR")|| s.contains("TIMEOUT")){
                                    tView.setText("Warning Fingerprint scan failure. Please Wait...");
                                    nextState = FingerState.WARNING;
                                    warning = true;
//                                    new Timer().schedule(new TimerTask() {
//                                        @Override
//                                        public void run() {
//                                            CommStream.writeString("$FPENROLL,"+userPinNumber);
//                                        }
//                                    },2000);
                                }
                            }
                            break;
                    }
                    break;

                case SECOND_FINGER_OFF:
                    switch(s){
                        case "$FPENROLL,EN3":
                            nextState = FingerState.THIRD_FINGER_ON;
                            tView.setText("Please Place Finger Once More");
                            tView.setTextColor(Color.parseColor("#ff0000ff"));
                            finger.setVisibility(View.VISIBLE);
                            finger.setColorFilter(Color.argb(200, 0, 0, 255));

                            break;
                        default:
                            String Message[] = s.split(",");
                            if(s.contains("$FPENROLL")){
                                if(s.contains("ERR")|| s.contains("TIMEOUT")){
                                    tView.setText("Warning Fingerprint scan failure. Please Wait...");
                                    nextState = FingerState.WARNING;
                                    warning = true;
//                                    new Timer().schedule(new TimerTask() {
//                                        @Override
//                                        public void run() {
//                                            CommStream.writeString("$FPENROLL," + userPinNumber);
//                                        }
//                                    },2000);
                                }
                            }
                            break;
                    }
                    break;
                case THIRD_FINGER_ON:
                    switch(s){
                        case "$FPENROLL,RM3":
                            nextState = FingerState.THIRD_FINGER_OFF;
                            tView.setText("Please Remove Finger Once More");
                            tView.setTextColor(Color.parseColor("#ffff0000"));
                            finger.setVisibility(View.VISIBLE);
                            finger.setColorFilter(Color.argb(200, 255, 0, 0));
//                            finger.setColorFilter(Color.argb(200, 255,0,0));
                            //Since its a Success
//                            tView.setText("Thank you please wait while we register you in our System");
//                            tView.setTextColor(Color.parseColor("#ff00ff00"));
//                            finger.setVisibility(View.VISIBLE);
//                            finger.setColorFilter(Color.argb(200, 0,255,0));
                            //Wait for enrollment
                            //TODO
//                            new Timer().schedule(new TimerTask() {
//                                    public void run() {
//                                        //startActivity(new Intent(NewUserFinger.this,ConfirmDrink.class));
//                                         startActivity(new Intent(NewUserFinger.this,CheckBAC.class));
//                                    }
//                                },2000);

                            break;
                        default:
                            String Message[] = s.split(",");
                            if(s.contains("$FPENROLL")){
                                if(s.contains("ERR")|| s.contains("TIMEOUT")){
                                    tView.setText("Warning Fingerprint scan failure. Please Wait...");
                                    nextState = FingerState.WARNING;
                                    warning = true;
//                                    new Timer().schedule(new TimerTask() {
//                                        @Override
//                                        public void run() {
//                                            CommStream.writeString("$FPENROLL,"+userPinNumber);
//                                        }
//                                    },2000);
                                }
                            }
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
                            if(tokens[2].equals("-1")){
                                tView.setText("Warning Fingerprint scan failure. Please Wait...");
                                nextState = FingerState.WARNING;
                                new Timer().schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        CommStream.writeString("$FPENROLL,"+userPinNumber);
                                    }
                                },2000);

                            }else {
                                nextState = FingerState.REGISTERED;
                                tView.setText("Thank you please wait while we register you in our system");
                                tView.setTextColor(Color.parseColor("#ff00ff00"));
                                finger.setVisibility(View.VISIBLE);
                                finger.setColorFilter(Color.argb(200, 0, 255, 0));
                                new Timer().schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        startActivity(new Intent(NewUserFinger.this, CheckBAC.class));
                                    }
                                }, 2000);

                            }
                            break;

                        default:
                            if(s.contains("$FPENROLL")){
                                if(s.contains("ERR")|| s.contains("TIMEOUT")){
                                    tView.setText("Warning Fingerprint scan failure. Please Wait...");
                                    nextState = FingerState.WARNING;
                                    warning = true;
//                                    new Timer().schedule(new TimerTask() {
//                                        @Override
//                                        public void run() {
//                                            CommStream.writeString("$FPENROLL,"+userPinNumber);
//                                        }
//                                    },2000);
                                }
                            }
                            break;
                    }
                    break;
                case REGISTERED:
                    if(s.equals("Finish")){
                        startActivity(new Intent(this, CheckBAC.class));
                    }
                    break;
                case WARNING:

                    switch(s) {
                        case "$FPENROLL,EN1":
                            tView.setText("Please Place Finger on Scanner");
                            nextState = FingerState.FIRST_FINGER_ON;
                            //tView.setText("Please Remove Finger");
                            break;
                    }
                    break;
            }
        currentState = nextState;
        Log.d("FPSM","NextState: "+nextState.name());
    if(CommStream.DEBUG_MODE) {
        NewUserFinger.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Incoming Message: " + currentState.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

        if(warning){
            warning = false;
            GiveInstruction();
        }


    }


    /*****************************Menu Navigators**********************************************/
    public void SkipToBAC(View view){
        //DrinkOrder.InDrinkString = "$DO,1,1@WH,1,1.5@LJ,1,1.5";
        startActivity(new Intent(this, CheckBAC.class));
    }

    public void NewFingerGoBack(View view){startActivity(new Intent(this,IdleMenu.class));}


public void GiveInstruction(){


    final Dialog dialog2 = new Dialog(new ContextThemeWrapper(NewUserFinger.this, R.style.SmartUIDialog));
    dialog2.setCanceledOnTouchOutside(false);
    dialog2.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

    dialog2.getWindow().addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    dialog2.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

    dialog2.getWindow().getDecorView() .setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

    dialog2.setContentView(R.layout.dialog_fp_instruct);
    //dialog.setTitle("Do You Have an Account with Smart Bar?");
    dialog2.getWindow().
            setBackgroundDrawable(new ColorDrawable(Color.parseColor("#44000000")
            ));
    dialog2.findViewById(R.id.dialogBtnYes_ifp).setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    dialog2.dismiss();


                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            CommStream.writeString("$FPENROLL,"+userPinNumber);
                        }
                    },2000);
//                    if(CommStream.TEST_HARNESS){
//                        index = 0;
//                    }

                }
            }
    );


    dialog2.show();
    dialog2.getWindow().
            clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);


}

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
