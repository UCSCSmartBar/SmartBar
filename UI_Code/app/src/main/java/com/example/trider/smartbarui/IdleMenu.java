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



    Timer timer;
    static long count = 0;

    CommStream PiComm;


    class BackGTask extends TimerTask {
        @Override
        public void run(){
            IdleMenu.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if((count++)%10 == 0) {
                        if(toggle){
                            textClock.setFormat12Hour("hh:mm");
                        }else{
                            textClock.setFormat12Hour("hh mm");
                        }
                        toggle = !toggle;
                    }
                }
            });
        }
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idle_menu);


       textClock = (TextClock) findViewById(R.id.textClock);
       textClock.setFormat12Hour("hh:mm");
       new Timer().scheduleAtFixedRate(new BackGTask(),1000,100);


        ImageView usbConn = (ImageView) findViewById(R.id.usbCon);
        PiComm = new CommStream();
        if(!PiComm.isInitialized()){
            usbConn.setVisibility(View.INVISIBLE);
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_idle_menu, menu);
        return true;
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
                    Toast.makeText(IdleMenu.this, file_url, Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(IdleMenu.this,"Failure to Access Server. Check Internet Connection"
                            , Toast.LENGTH_SHORT).show();
                }

            }


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
