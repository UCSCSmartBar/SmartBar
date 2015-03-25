package com.example.trider.smartbarui;

import android.app.ActionBar;
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
import android.widget.TextClock;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Delayed;


public class IdleMenu extends Activity {


    private TextClock textClock;
    static boolean toggle = true;

    private static final String Q_URL = "http://www.ucscsmartbar.com/getQ.php";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    Boolean searchFailure = false;
    JSONParser jsonParser = new JSONParser();

    String QueueString =null;
    String OldQueueString = null;
      int counter = 0;


    long testCount = 0;
    //PI comunications
    CommStream PiComm;
    boolean isActive = true;
    String InMessage = null;
    static Boolean IdleMenuActive = true;

    Runnable mListenerTask = new Runnable() {
        @Override
        public void run() {
            testCount++;
            Log.d("TCNT","Current Count is at:"+testCount);
            InMessage = PiComm.readString();
            if(InMessage != null){
                Toast.makeText(getApplicationContext(),InMessage,Toast.LENGTH_SHORT).show();
            }
            //Waits for new input communication
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //Restarts this thread only if active
            if(isActive) {
                new Thread(this).start();
            }
        }
    };



    class BackGTask extends TimerTask {
        @Override
        public void run(){
            IdleMenu.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (IdleMenuActive) {
                        //Periodically gets the queue and forwards it to the Pi
                        if(counter > 10) {
                            new AttemptGetQ().execute();
                            counter = 0;
                            //makes sure queue is non-null
                            if((QueueString!=null) && !QueueString.equalsIgnoreCase(OldQueueString)){
                                PiComm.writeString("$FPQ," + QueueString);
                                OldQueueString = QueueString;
                                Log.d("IDLE","Theres a new Queue:"+QueueString);
                            }
                        }else{
                            counter++;
                        }

                        if (toggle) {
                            textClock.setFormat12Hour("hh:mm");
                            toggle = false;
                        } else {
                            textClock.setFormat12Hour("hh mm");
                            toggle = true;
                        }
                    }
                    //hideSystemUI();
                }
            });
        }
    }


    public void onResume(){
        super.onResume();
        if(PiComm.isInitialized()){
            PiComm.writeString("Resume");
        }
        hideSystemUI();
        isActive = true;
        IdleMenuActive = true;
    }

    public void onStop(){
        super.onStop();
        if(PiComm.isInitialized()){
            PiComm.writeString("Stop");
        }
        isActive = false;
        IdleMenuActive = false;
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idle_menu);

        // Hide the status bar.
        hideSystemUI();
       IdleMenuActive = true;

       textClock = (TextClock) findViewById(R.id.textClock);
       textClock.setFormat12Hour("hh:mm");

       new Timer().scheduleAtFixedRate(new BackGTask(),1000,1000);
       new Timer().scheduleAtFixedRate(HideTask,100,100);
       ImageView usbConn = (ImageView) findViewById(R.id.usbCon);
       PiComm = new CommStream();
       if(!PiComm.isInitialized()){
            usbConn.setVisibility(View.INVISIBLE);
       }
    }




    public void onPickUpClick(View view){
        Intent intent = new Intent(this,PickUpDrink.class);
        startActivity(intent);
    }

    public void GoToNewUser(View view){

        startActivity(new Intent(this,NewUser.class));
    }

    public void GetQueue(View v){
            new AttemptGetQ().execute();
            long time = System.currentTimeMillis();
            while(System.currentTimeMillis() < time + 1000);
            startActivity(new Intent(this,DisplayQueue.class));
    }


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
                        Log.d("AGD", "Mid-Execute");
                        Log.d("request!", "starting");
                        // getting product details by making HTTP request

                        List<NameValuePair> params = new ArrayList<NameValuePair>();
                        params.add(new BasicNameValuePair("pin", "1"));
                        JSONObject json = jsonParser.makeHttpRequest(Q_URL, "POST", params);

                        // check your log for json response
                        Log.d("Q", json.toString());
                        // json success tag
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
                        npe.printStackTrace();
                    }

            return null;
        }

            protected void onPostExecute(String file_url) {
                // dismiss the dialog once product deleted
                if (file_url != null){
                    Log.d("IDLE",file_url);
                    QueueString = file_url;
                }else{
                    Toast.makeText(IdleMenu.this,"Failure to Access Server. Check Internet Connection"
                            , Toast.LENGTH_SHORT).show();
                }

            }
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
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_idle_menu, menu);
        return true;
    }



}
