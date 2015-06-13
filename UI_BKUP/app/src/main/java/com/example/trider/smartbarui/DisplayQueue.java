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
import android.widget.EditText;
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
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    Boolean searchFailure = false;
    JSONParser jsonParser = new JSONParser();

    ServerAccess SA = new ServerAccess();
    String QueueList;


    TextView qView;



    @Override
    public void onBackPressed()
    {
        // code here to show dialog
        super.onBackPressed();  // optional depending on your needs

        startActivity(new Intent(DisplayQueue.this,IdleMenu.class));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_queue);

        qView = (TextView) findViewById(R.id.queue_view);

        Sound.playDrinkQ(getApplicationContext());


        new AttemptGetQ().execute();
        long count= System.currentTimeMillis();
        //String QueueList2 = SA.GetQueue();
        //Log.d("ParseQ","QueueList2:" +QueueList2);


    }

    public void UpdateQueue(View view){
        //QueueList = SA.GetQueue();
        long count= System.currentTimeMillis();
//        while(System.currentTimeMillis() < count +3000 );
//        ParseQ();
        new AttemptGetQ().execute();
    }

    //Parses up the queue from Server
    public void ParseQ(){
        if(QueueList == null){return;}
        String[] sList = QueueList.split(",");
        String Table = "Current Queue\nDrink#         Phone#\n";
        for(int i=0;i < sList.length; i++){
            Table += (i+1) + ":          " +sList[i]+"\n";
        }
        qView.setText(Table);

    }


    public void DeleteQ(View view){
        //if(pin==null){return;}
        new AttemptDeleteQ().execute();



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
                JSONObject json = jsonParser.makeHttpRequest(ServerAccess.LOGIN_URL, "POST", params);

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


    class AttemptDeleteQ extends AsyncTask<String, String, String> {
        int success;
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("DelQ", "Pre-Exec");
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                Log.d("DelQ", "Mid-Execute");
                Log.d("DelQ", "starting");
                // getting product details by making HTTP request

                String PinToDelete = "11100002108";
                EditText e = (EditText) findViewById(R.id.qText);
                PinToDelete = e.getText().toString();
                Log.d("DelQ","Pin getting Posted:" + PinToDelete);
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("pin", PinToDelete));
                Log.d("DelQ","HTTP getting Posted:" + ServerAccess.DELETE_URL);
                JSONObject json = jsonParser.makeHttpRequest(ServerAccess.DELETE_URL, "POST", params);

                // check your log for json response
                Log.d("DelQ", json.toString());
                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("DelQ", "Success String:" + json.toString());
                    searchFailure = false;
                    return json.getString(TAG_MESSAGE);
                } else {
                    Log.d("DelQ","Failure with :"+ json.getString(TAG_MESSAGE));
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
                //Toast.makeText(DisplayQueue.this, file_url, Toast.LENGTH_LONG).show();
                Log.d("DelQ","Returned URL:"+ file_url);

            } else {
                Toast.makeText(DisplayQueue.this, "Failure to Access Server. Check Internet Connection"
                        , Toast.LENGTH_SHORT).show();
            }
        }
    }
//    class AttemptGetDrink extends AsyncTask<String, String, String> {
//        /**
//         * Before starting background thread Show Progress Dialog
//         * */
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            Log.d("AGD","Pre-Exec");
//        }
//
//        @Override
//        protected String doInBackground(String... args) {
//            // TODO Auto-generated method stub
//            // Check for success tag
//            int success;
//            String userpin = eText.getText().toString();
//
//            try {
//                Log.d("AGD","Mid-Execute");
//                // Building Parameters
//                List<NameValuePair> params = new ArrayList<NameValuePair>();
//                params.add(new BasicNameValuePair("pin", userpin));
//
//                Log.d("request!", "starting");
//                // getting product details by making HTTP request
//                JSONObject json = jsonParser.makeHttpRequest(
//                        LOGIN_URL, "POST", params);
//
//                // check your log for json response
//                Log.d("Drink retrieve attempt", json.toString());
//
//                // json success tag
//                success = json.getInt(TAG_SUCCESS);
//                if (success == 1) {
//                    Log.d("Drink found Successful!", json.toString());
//                    searchFailure = false;
//                    //Intent i = new Intent(Login.this, ReadComments.class);
//                    return json.getString(TAG_MESSAGE);
//                }else{
//                    Log.d("Login Failure!", json.getString(TAG_MESSAGE));
//                    searchFailure = true;
//                    return json.getString(TAG_MESSAGE);
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            } catch(NullPointerException npe){
//                npe.printStackTrace();
//            }
//
//            return null;
//
//        }
//        /**
//         * After completing background task Dismiss the progress dialog
//         * **/
//        protected void onPostExecute(String file_url) {
//            // dismiss the dialog once product deleted
//            pDialog.dismiss();
//            if (file_url != null){
//                Toast.makeText(PickUpDrink.this, file_url, Toast.LENGTH_LONG).show();
//                IncomingString = file_url;
//
//            }else{
//                Toast.makeText(PickUpDrink.this,"Failure to Access Server. Check Internet Connection"
//                        , Toast.LENGTH_SHORT).show();
//            }
//
//        }
//
//    }



}
