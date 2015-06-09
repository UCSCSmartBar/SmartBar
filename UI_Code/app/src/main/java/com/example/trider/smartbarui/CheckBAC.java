package com.example.trider.smartbarui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
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
import com.example.trider.smartbarui.CommStream;



public class CheckBAC extends Activity {

    Boolean isActive;
    CommStream PiComm;

    public enum BACState {
        IDLE,
        CHECKING,
        PASSED,
        RETRY,
        FAILED,
        WARNING,
    }

    int failureCount = 0;
    BACState currentState = BACState.IDLE;
    BACState nextState = BACState.IDLE;

    TextView bacTview;
    TextView bacMarks;
    TextView bacTitle;
    TextView prevBacView;
    ProgressBar pBar;
    ProgressBar pBar2;

    private static final int _BAC_TIMEOUT = 100000;
    //BAC variables
    String BACstring = null;

    float curBAC = 0;
    float prevBAC = 0;
    Boolean passedBAC = false;
    int index = 0;

    MediaPlayer mp;

    /**
     * *******************************************
     */
    //Server variables
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    Boolean searchFailure = false;
    JSONParser jsonParser = new JSONParser();
    Boolean searching = false;



    /**
     * Background listen task
     */
    class ListenTask extends TimerTask {
        @Override
        public void run() {
            if (isActive) {
                CheckBAC.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        String t = CommStream.ReadBuffer();

                        if (t != null) {
                            Log.d("CBAC", "Reading Buffer with +" + t);
                            RunBACStateMachine(t);
                        }
                    }

                });
            } else {
                Log.d("CBAC", "Canceling CheckBAC timertask");
                this.cancel();
            }
        }
    }
    Timer BACTestTimer = new Timer();
    TimerTask BACTestHarness = new TimerTask() {
        @Override
        public void run() {
            CheckBAC.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("CBAC", "Testing with: " + index);
                    switch (index++) {
                        case 0:
                            CommStream.StoreBuffer("$BAC,READY");
                            //RunBACStateMachine("$BAC,WORKING");
                            break;
                        case 2:
                            CommStream.StoreBuffer("$BAC,TIMEOUT");
                            break;
                        case 5:
                            CommStream.StoreBuffer("$BAC,TIMEOUT");
                            break;
                        case 6:
                            //CommStream.StoreBuffer("$BAC,TIMEOUT");
                            break;
                        case 7:
                            //CommStream.StoreBuffer("$BAC,TIMEOUT");
                            break;
                        case 8:
                            CommStream.StoreBuffer("$BAC,PASSED,360");
                            break;
                        default:
                            //Stops counting
                            return;
                    }
                    if (index < 10) {
                        long time = System.currentTimeMillis();
                        //while(time+4000 > System.currentTimeMillis());
                        //new Thread(BACTestComms).start();
                    } else {

                    }
                    //RunPrintStateMachine("$FPENROLL,EN1");
                }
            });
        }
    };


    Timer WatchDog = new Timer();
    TimerTask WatchTask = new TimerTask() {
        @Override
        public void run() {
            Log.d("BAC", "Timeout");
            if (CommStream.DEBUG_MODE) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "WATCHTIMEOUT", Toast.LENGTH_SHORT).show();
                    }
                });

            }
            startActivity(new Intent(CheckBAC.this, IdleMenu.class));
        }
    };

    int COUNT = 3;
    Timer Count = new Timer();
    TimerTask CountTask = new TimerTask() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView tv = (TextView) findViewById(R.id.countdown_tview);
                    if (COUNT == 0) {
                        tv.setVisibility(View.INVISIBLE);

                    } else {

                        tv.setText(String.valueOf(COUNT));
                        //tv.setVisibility(View.VISIBLE);
                        COUNT--;
                    }
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("CBAC", "CheckBAC onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_bac);
        hideSystemUI();

        //Reinitializes state machine
        currentState = BACState.IDLE;
        nextState = BACState.IDLE;
        isActive = true;
        failureCount = 0;
        PiComm = new CommStream();


        //Initializing Views
        bacTview = (TextView) findViewById(R.id.bac_tview);
        prevBacView = (TextView) findViewById(R.id.prev_bacview);

        prevBacView.setVisibility(View.INVISIBLE);
        prevBAC = -1;


        //test.append("\nTest");

        //Sets initial UI screen
        bacMarks = (TextView) findViewById(R.id.bac_marks);
        pBar = (ProgressBar) findViewById(R.id.bac_pbar);
        pBar2 = (ProgressBar) findViewById(R.id.bac_pbar2);
        bacTitle = (TextView) findViewById(R.id.bac_test_stuff);

        bacTitle.setVisibility(View.INVISIBLE);
        pBar2.setVisibility(View.INVISIBLE);
        pBar.setVisibility((View.INVISIBLE));
        bacMarks.setVisibility(View.INVISIBLE);




        bacTview.setText("Please Breathe Into The Sensor On Your Right");
        //Timer before breathing into sensor
        CommStream.writeString("$BAC,START");
        /**************************BACKGROUND TIMERS***************/
//        new Timer().schedule(new TimerTask() {
//            @Override
//            public void run() {
//                CommStream.writeString("$BAC,START");
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        //bacTview.setText("Please Breathe Into The Sensor On Your Right");
//                    }
//                });
//            }
//        }, 1000);
        //Timer before displaying BAC
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        String s = String.format("%.2f", prevBAC);
//                        Log.d("Text", s);
//                        TextView test = (TextView) findViewById(R.id.bac_test_stuff);
//                        test.append("\n" + s);
                    }
                });
            }
        }, 2000);
        //Starting the Background Tasks
        new Timer().scheduleAtFixedRate(HideTask, 100, 10);
        new Timer().scheduleAtFixedRate(new ListenTask(), 10, 10);

        //Resests the countdown variable.
        COUNT = 4;
        //Count.scheduleAtFixedRate(CountTask,1,1000);

        findViewById(R.id.countdown_tview).setVisibility(View.INVISIBLE);
        WatchDog.schedule(WatchTask, _BAC_TIMEOUT);

        /********************************************************************/

        /******************Test Harness/ DEBUGGING METHODS********************/
        PiComm.PiLog(this.getLocalClassName(), DrinkOrder.OrderStatus());

        index = 0;
        if (CommStream.TEST_HARNESS) {
            Log.d("CBAC", "Starting new Thread");
            //new Timer().scheduleAtFixedRate(BACTestHarness,100,100);
            BACTestTimer.schedule(BACTestHarness, 5000, 1000);
            //new Thread(BACTestComms).start();\

            //Can run test harness with InUserPinString
            if (DrinkOrder.InUserPinString == null) {
                DrinkOrder.InUserPinString = "18316013559";
            }
        }

        //Debugging options
        if (CommStream.DEBUG_MODE) {
        } else {
            mp = Sound.playBreathe(getApplicationContext());
        }

        //Attempts to obtain previous BAC.
        new AttemptGetBAC().execute();
    }

    @Override
    public void onStop() {
        Log.d("LIFE", "CheckBAC onStop()");
        super.onStop();
        isActive = false;
        Log.d("CBAC", "Canceling BAC Task");
        WatchDog.cancel();
        WatchTask.cancel();

        BACTestHarness.cancel();
        BACTestTimer.cancel();

        //CountTask.cancel();
        //Count.cancel();

        if(mp!=null){mp.stop();}
    }

    public void SkipBAC(View view) {
        startActivity(new Intent(this, ConfirmDrink.class));
    }

    /**
     * Runs the BAC state machine to prompt the user to breathe into the breathalyzer.
     *
     * @param msg A message coming from the raspberry pi.
     * @return True if a message can be used within the state machine, false otherwise.
     */
    public Boolean RunBACStateMachine(String msg) {
        //Should never be null, but just in case checking for null
        if (msg == null) {
            return false;
        }

        Log.d("CBAC", "Current state:" + currentState);
        Log.d("CBAC", "Running with " + msg);
        Log.d("CBAC", "Failure Count: " + failureCount);
        if (CommStream.DEBUG_MODE) {
            Toast.makeText(getApplicationContext(), "CurState:" + currentState.name() + "\nBACSM:" + msg +
                    "\nNextState" + nextState.name(), Toast.LENGTH_SHORT).show();
        }
        msg = msg.trim();
        String Tokens[] = msg.split("[,]");
        if (!Tokens[0].equals("$BAC")) {
            return false;
        }

        switch (currentState) {
            case IDLE:
                if (CommStream.DEBUG_MODE) {
                    Toast.makeText(getApplicationContext(), "Tokens:" + msg, Toast.LENGTH_SHORT).show();
                }
                switch (msg) {
                    //Waiting only occurs if BAC sensor detects a visible change
                    case "$BAC,READY":
                        if (CommStream.DEBUG_MODE) {
                            Toast.makeText(getApplicationContext(), "IDLE--->CHECKING", Toast.LENGTH_SHORT).show();
                        }
                        nextState = BACState.CHECKING;
                        //bacTview.setText("Please Breathe into the Sensor");
                        pBar.setVisibility(View.VISIBLE);
                        break;
                    default:
                        break;
                }
                break;
            case CHECKING:
                String tokens[] = msg.split("[,]");
                try {
                    msg = tokens[0] + "," + tokens[1];
                } catch (ArrayIndexOutOfBoundsException AIB) {
                    if (CommStream.DEBUG_MODE) {
                        Toast.makeText(getApplicationContext(), "Array Out of Bounds error", Toast.LENGTH_LONG).show();
                    }
                }
                curBAC = -1;
                switch (msg) {
                    case "$BAC,PASSED":
                        if (CommStream.DEBUG_MODE) {
                            Toast.makeText(getApplicationContext(), "CHECKING--->PASSED", Toast.LENGTH_SHORT).show();
                        }
                        nextState = BACState.PASSED;
                        //Change Visibility of items
                        pBar.setVisibility(View.INVISIBLE);
                        pBar2.setVisibility(View.VISIBLE);
                        Sound.playSuccess(getApplicationContext());
                        //bacMarks.setVisibility(View.VISIBLE);//TODO removed tick marks

                        //Filling in BAC information
                        BACstring = tokens[2];
                        passedBAC = true;
                        try {
                            curBAC = Float.parseFloat(tokens[2]);
                        } catch (NullPointerException npe) {
                            npe.printStackTrace();
                        }

                        if (curBAC > 1) {
                            Log.d("CBAC", "Getting Analog Value");
                            curBAC = curBAC / 10000;
                            Log.d("CBAC", "New Val is:" + curBAC);
                        }
                        //
                        //bacTview.setText("Passed BAC\nYour current estimated BAC:" + curBAC);//TODO changed
                        bacTview.setText("We detect safe levels of alcohol in your breath.");
                        //Graphically displays BAC
                        Thread t = new Thread(new Runnable() {
                            public void run() {
                                DisplaySlider(curBAC);
                            }
                        });
                        t.start();
                        new AttemptPutBAC().execute();
                        break;
                    case "$BAC,FAILED":
                        Sound.playSuccess(getApplicationContext());
                        nextState = BACState.FAILED;
                        //Setting view visibility
                        pBar.setVisibility(View.INVISIBLE);
                        pBar2.setVisibility(View.VISIBLE);
                        //bacMarks.setVisibility(View.VISIBLE);

                        BACstring = tokens[2];

                        passedBAC = false;
                        try {
                            curBAC = Float.parseFloat(tokens[2]);
                        } catch (NullPointerException npe) {
                            npe.printStackTrace();
                        }
                        if (curBAC > 1) {
                            Log.d("CBAC", "Getting Analog Value");
                            curBAC = curBAC / 10000;
                            Log.d("CBAC", "New Val is:" + curBAC);
                        }


                        // bacTview.setText("Failed BAC\nYour current estimated BAC:" + curBAC);
                        bacTview.setText("We detect unsafe levels of alcohol in your breath.");
                        AlertFailedBAC();
                        new AttemptPutBAC().execute();

                        //Returns the to the idle menu

                        break;
                    case "$BAC,ERROR":
                        nextState = BACState.RETRY;
                        pBar.setVisibility(View.INVISIBLE);
                        bacTview.setText("Error Measuring BAC.. Please Wait");

                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                CommStream.writeString("$BAC,START");
                            }
                        }, 1000);
                        break;
                    case "$BAC,TIMEOUT":
                        if (failureCount == 2) {
                            nextState = BACState.WARNING;
                            //bacTview.setText("Unable to detect BAC, please come back another time");
                            if(mp != null)mp.stop();
                            mp = Sound.playBACNotDetected(getApplicationContext());
                            AskForResponse();
//                            new Timer().schedule(new TimerTask() {
//                                @Override
//                                public void run() {
//                                    startActivity(new Intent(CheckBAC.this, IdleMenu.class));
//                                }
//                            }, 3000);
                        } else {
                            bacTview.setText("Could not detect breath, please breathe into the sensor");
                            nextState = BACState.CHECKING;
                            failureCount++;
                            if(mp!=null)mp.stop();
                            mp = Sound.playKeepBreathing(getApplicationContext());
                            //pBar.setVisibility(View.INVISIBLE);

                            COUNT = 4;
                            //Count.schedule(CountTask,1,1000);
                            new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    CommStream.writeString("$BAC,START");
                                }
                            }, 1);
                        }
                        break;
                    default:
                        break;
                }
                break;
            case PASSED:
                break;
            case RETRY:
                if (CommStream.DEBUG_MODE) {
                    Toast.makeText(getApplicationContext(), "Tokens:" + Tokens[1], Toast.LENGTH_SHORT).show();
                }
                switch (msg) {
                    case "$BAC,WORKING":
                        if (CommStream.DEBUG_MODE) {
                            Toast.makeText(getApplicationContext(), "IDLE--->CHECKING", Toast.LENGTH_SHORT).show();
                        }
                        nextState = BACState.CHECKING;
                        bacTview.setText("Please Breathe into Sensor");
                        pBar.setVisibility(View.VISIBLE);
                        break;
                    default:
                        break;
                }
                break;
            case FAILED:
                break;
            case WARNING:
                break;
            default:
                return false;
        }
        Log.d("CBAC", "NextState:" + nextState);
        currentState = nextState;
        return true;
    }


    public void AskForResponse() {

        pBar.setVisibility(View.INVISIBLE);

        final Dialog dialog2 = new Dialog(new ContextThemeWrapper(CheckBAC.this, R.style.SmartUIDialog));
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

        dialog2.setContentView(R.layout.dialog_reset_bac);
        //dialog.setTitle("Do You Have an Account with Smart Bar?");
        dialog2.getWindow().
        setBackgroundDrawable(new ColorDrawable(Color.parseColor("#44000000")
        ));
        dialog2.findViewById(R.id.dialogBtnYes_bac).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        dialog2.dismiss();
                        currentState = BACState.IDLE;
                        nextState = BACState.IDLE;

                         new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    CommStream.writeString("$BAC,START");
                                }
                            }, 1);
                        failureCount = 0;
                        if(CommStream.TEST_HARNESS){
                            index = 0;
                        }


                        //startActivity(new Intent(IdleMenu.this, LibraryBrowseActivity.class));
                    }
                }
        );
        dialog2.findViewById(R.id.dialogBtnNo_bac).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        dialog2.dismiss();
                        finish();
                        startActivity(new Intent(CheckBAC.this, IdleMenu.class));
                    }
                }
        );
        dialog2.show();
        dialog2.getWindow().
        clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

}

    public void AlertFailedBAC(){
        final Dialog dialog2 = new Dialog(new ContextThemeWrapper(CheckBAC.this, R.style.SmartUIDialog));
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

        dialog2.setContentView(R.layout.dialog_failed_bac);
        //dialog.setTitle("Do You Have an Account with Smart Bar?");
        dialog2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#44000000")));
        dialog2.findViewById(R.id.diag_bac_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog2.dismiss();
                startActivity(new Intent(CheckBAC.this, IdleMenu.class));

            }
        });
        dialog2.show();
        dialog2.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

        mp = Sound.playYouAreDrunk(getApplicationContext());

    }





    /**
     * Visually displays to the user what their BAC is.
     *
     * @param val
     */
    public void DisplaySlider(float val) {
        Log.d("BAC", "Val is:" + Float.toString(val));
        if (val < 0) {
            return;
        }
        Log.d("BAC", "Index is" + index);


        int sliderSpeed = 10;//Less is faster
        //Progress bar of 0 - 0.20%
        int i = 0;
        int adjBac = (int) (1000 * val);

        for (; i < 1.4 * adjBac && i < 1023; i++) {
            Log.d("BAC", "i = " + i);
            pBar2.setProgress(i);
            long time = System.currentTimeMillis();
            while (time + sliderSpeed > (System.currentTimeMillis())) ;
        }

        for (; i > adjBac; i--) {
            Log.d("BAC", "i = " + i);
            pBar2.setProgress(i);
            long time = System.currentTimeMillis();
            while (time + sliderSpeed > (System.currentTimeMillis())) ;
        }

        Intent intent;
        if (passedBAC) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    startActivity(new Intent(CheckBAC.this, ConfirmDrink.class));
                }
            }, 2000);
        } else {



//            new Timer().schedule(new TimerTask() {
//                @Override
//                public void run() {
//                    startActivity(new Intent(CheckBAC.this, IdleMenu.class));
//                }
//            }, 2000);





        }


    }

    /*************************************************************/
    /**
     * *******************Server Access Functions************************
     */
    class AttemptPutBAC extends AsyncTask<String, String, String> {
        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("PutBAC", "Pre-Exec");
        }

        @Override
        protected String doInBackground(String... args) {
            // TODO Auto-generated method stub
            // Check for success tag
            int success;
//            String userpin = eText.getText().toString();

            String userpin = DrinkOrder.InUserPinString;
            try {
                Log.d("PutBAC", "Mid-Execute");
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();

                //TODO testing parameters
                params.add(new BasicNameValuePair("pin", userpin));
//            params.add(new BasicNameValuePair("username","TylerJames"));
//            params.add(new BasicNameValuePair("email","tylerjrider@gmail.com"));
                params.add(new BasicNameValuePair("bac", Float.toString(curBAC)));


                Log.d("PutBAC", "pin" + userpin + ". bac:" + curBAC);
                // getting product details by making HTTP request
                JSONObject json = jsonParser.makeHttpRequest(
                        ServerAccess.PUTBAC_URL, "POST", params);
                if (json == null) {
                    return null;
                }
                // check your log for json response
                Log.d("PutBAC", "PostBAC attempt:" + json.toString());

                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("PutBAC", "Put BAC Successful! " + json.toString());
                    searchFailure = false;
                    //Intent i = new Intent(Login.this, ReadComments.class);
                    return json.getString(TAG_MESSAGE);
                } else {
                    Log.d("PutBAC", "Login Failure! " + json.getString(TAG_MESSAGE));
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
                    Toast.makeText(CheckBAC.this, file_url, Toast.LENGTH_LONG).show();
                }
                //IncomingString = file_url;
                Log.d("PutBAC", "Success url is ! " + file_url);
                //CompareFingerSM("$GOTDRINK");
                //startActivity(new Intent(CheckBAC.this,ConfirmDrink.class));

            } else {
                Toast.makeText(CheckBAC.this, "Failure to Access Server. Check Internet Connection"
                        , Toast.LENGTH_SHORT).show();
            }

        }

    }

    class AttemptGetBAC extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... args) {
            // TODO Auto-generated method stub
            // Check for success tag
            int success;
//            String userpin = eText.getText().toString();

            String userpin = DrinkOrder.InUserPinString;
            try {
                Log.d("GetBAC", "Mid-Execute");
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();

                //TODO testing parameters
                params.add(new BasicNameValuePair("pin", userpin));

                Log.d("GetBAC", "starting");
                // getting product details by making HTTP request
                JSONObject json = jsonParser.makeHttpRequest(
                        ServerAccess.GETBAC_URL, "POST", params);
                if (json == null) {
                    return null;
                }
                // check your log for json response
                Log.d("GetBAC", "GetBAC attempt:" + json.toString());

                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("GetBAC", "Get BAC Successful! " + json.toString());
                    searchFailure = false;
                    //Intent i = new Intent(Login.this, ReadComments.class);
                    return json.getString(TAG_MESSAGE);
                } else {
                    Log.d("GetBAC", "GetBAC Failure! " + json.getString(TAG_MESSAGE));
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
                    Toast.makeText(CheckBAC.this, file_url, Toast.LENGTH_LONG).show();
                }
                Log.d("GetBAC", "File url is " + file_url);
                try {
                    prevBAC = Float.valueOf(file_url);
                    CheckBAC.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("CBAC", "prevBACView:" + prevBacView.toString());
                            //prevBacView.append(file_url);
                        }
                    });
                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Failed to Get BAC", Toast.LENGTH_SHORT).show();
                } catch (IndexOutOfBoundsException ioe) {
                    ioe.printStackTrace();
                }
            } else {
                Toast.makeText(CheckBAC.this, "Failure to Access Server. Check Internet Connection"
                        , Toast.LENGTH_SHORT).show();
                //startActivity(new Intent(CheckBAC.this, IdleMenu.class));
            }

        }

    }
    /**************System Level Functions********************/
    /**
     * *********see Other classes for Descriptions********
     */
    public void startWatch(int watch_t) {
        new Timer().schedule(new TimerTask() {
            public void run() {
                startActivity(new Intent(CheckBAC.this, IdleMenu.class));
            }

        }, watch_t);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_HOME)) {
            if (CommStream.DEBUG_MODE) {
                Toast.makeText(this, "You pressed the home button!", Toast.LENGTH_LONG).show();
            }
            //return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            startActivity(new Intent(CheckBAC.this, IdleMenu.class));
            return true;
        } else {
            if (CommStream.DEBUG_MODE) {
                Toast.makeText(this, "You Pressed the:" + keyCode, Toast.LENGTH_LONG).show();
            }
        }
        return super.onKeyDown(keyCode, event);
        //return true;
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
        public void run() {
            CheckBAC.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hideSystemUI();
                }
            });
        }
    };
/*Default Functions*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_check_bac, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}