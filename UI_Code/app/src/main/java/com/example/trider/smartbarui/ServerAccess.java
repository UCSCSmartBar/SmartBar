package com.example.trider.smartbarui;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.example.trider.smartbarui.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by trider on 3/5/2015.
 */
public class ServerAccess {

    private static final String Q_URL = "http://www.ucscsmartbar.com/getQ.php";
    private static final String LOGIN_URL = "http://www.ucscsmartbar.com/getDrink.php";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";


    Boolean searchFailure = false;
    JSONParser jsonParser = new JSONParser();
    String QueueString = null;
    String DrinkString = null;
    String UserPin;

    class AttemptGetQ extends AsyncTask<String, String, String> {
        int success;
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("AGD", "Pre-Exec");
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
                //Toast.makeText(IdleMenu.this, file_url, Toast.LENGTH_SHORT).show();
                QueueString = file_url;
                Log.d("ServAcc","file_url:" +file_url);
            }else{
                //Toast.makeText(IdleMenu.this,"Failure to Access Server. Check Internet Connection"
                        //, Toast.LENGTH_SHORT).show();
                Log.d("ServAcc","Failure to get into server");
                QueueString = "Failure to Access Server";
            }

        }
    }

    class AttemptGetDrink extends AsyncTask<String, String, String> {
        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            Log.d("AGD","Pre-Exec");
        }

        @Override
        protected String doInBackground(String... args) {
            // TODO Auto-generated method stub
            // Check for success tag
            int success;
            String userpin = UserPin;

            try {
                Log.d("AGD","Mid-Execute");
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("pin", userpin));

                Log.d("request!", "starting");
                // getting product details by making HTTP request
                JSONObject json = jsonParser.makeHttpRequest(
                        LOGIN_URL, "POST", params);

                // check your log for json response
                Log.d("Drink retrieve attempt", json.toString());

                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("Drink found Successful!", json.toString());
                    searchFailure = false;
                    //Intent i = new Intent(Login.this, ReadComments.class);
                    return json.getString(TAG_MESSAGE);
                }else{
                    Log.d("Login Failure!", json.getString(TAG_MESSAGE));
                    searchFailure = true;
                    return json.getString(TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch(NullPointerException npe){
                npe.printStackTrace();
            }

            return null;

        }
        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once product deleted
//            pDialog.dismiss();
            if (file_url != null){
//                Toast.makeText(PickUpDrink.this, file_url, Toast.LENGTH_LONG).show();
               DrinkString = file_url;
                Log.d("ServACC","Got file_url:" +file_url);
            }else{
//                Toast.makeText(PickUpDrink.this,"Failure to Access Server. Check Internet Connection"
//                        , Toast.LENGTH_SHORT).show();
                Log.d("ServAcc","Failure to get into server");
                DrinkString = "Failure to Access Server.";
            }

        }

    }



    public String GetQueue(){
        new AttemptGetQ().execute();
        long current = System.currentTimeMillis();
        while(System.currentTimeMillis() < current + 3000);
        Log.d("ServAcc","GetQueue @return:"+QueueString);
        return QueueString;
    }

    public String GetDrinkOrder(String pinNumber){
        UserPin = pinNumber;
        new AttemptGetDrink();
        return DrinkString;
    }

}
