package com.example.trider.smartbarui;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class DisplayQueue extends Activity {


    //Server request data
    private static final String LOGIN_URL = "http://www.ucscsmartbar.com/getQ.php";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    Boolean searchFailure = false;
    JSONParser jsonParser = new JSONParser();

    String QueueList;


    TextView qView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_queue);

        qView = (TextView) findViewById(R.id.queue_view);

        new AttemptGetQ().execute();




    }








    public void ParseQ(){

        String[] sList = QueueList.split(",");
        String Table = "Current Queue\n";
        for(int i=0;i < sList.length; i++){
            Table += "Drink Number: " +(i+1) + " | " +(sList[i]+"\n");
        }
        qView.setText(Table);

    }






    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_display_queue, menu);
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

    class AttemptGetQ extends AsyncTask<String, String, String> {


        int success;

        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("AGD", "Pre-Exec");
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                Log.d("AGD", "Mid-Execute");
                Log.d("request!", "starting");
                // getting product details by making HTTP request

                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("pin", "1"));
                JSONObject json = jsonParser.makeHttpRequest(LOGIN_URL, "POST", params);

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
                Toast.makeText(DisplayQueue.this, file_url, Toast.LENGTH_LONG).show();
                QueueList = file_url;
                ParseQ();
            } else {
                Toast.makeText(DisplayQueue.this, "Failure to Access Server. Check Internet Connection"
                        , Toast.LENGTH_SHORT).show();
            }

        }


    }




}
