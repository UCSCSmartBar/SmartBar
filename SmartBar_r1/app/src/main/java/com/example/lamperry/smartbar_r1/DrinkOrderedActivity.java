package com.example.lamperry.smartbar_r1;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;


/*
 * This class defines the behavior of the drink ordered screen: displays pin number for user, allows
 * access to library drink screen.
 */
public class DrinkOrderedActivity extends ActionBarActivity {

    // Initializations
    TextView pinDisplay;
    private ProgressDialog pDialog;             // Progress Dialog
    JSONParser jsonParser = new JSONParser();   // JSON parser class

    //PHPlogin script location:
    //UCSC Smartbar Server:
    private static final String REQUEST_TOKEN_URL = "http://mobile.bactrack.com/oauth/request_token/";
    private static final String AUTHORIZE_URL = "http://mobile.bactrack.com/oauth/authorize/";
        private static final String ACCESS_TOKEN = "http://mobile.bactrack.com/oauth/access_token/";

        //JSON element ids from response of php script:
        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "message";

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_drink_ordered);

            pinDisplay = (TextView)findViewById(R.id.displayPin);
            // format phone number
            pinDisplay.setText(((MyApplication)this.getApplication()).myPin);
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_drink_ordered, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // adds get pin button to action bar
        if (id == R.id.action_pin) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("My Number");
            builder.setMessage(pinDisplay.getText().toString());
            builder.setPositiveButton("OK", null);
            AlertDialog dialog = builder.show();
            return true;
        }

        // adds logout button to action bar
        if (id == R.id.action_logout) {
            logout();
        }

        return super.onOptionsItemSelected(item);
    }

    // logs user out of account and returns to Startup Activity
    private void logout() {
        ((MyApplication)this.getApplication()).setLoggedIn(false);
        Intent intent = new Intent(this, StartupActivity.class);
        startActivity(intent);
    }

    // starts Library Browse activity
    public void drinkOrderedToLibraryBrowse(View view) {
        Intent intent = new Intent(this, LibraryBrowseActivity.class);
        startActivity(intent);
    }


    /*
     * Class to obtain access token for oauth
     */
    class RequestToken extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        boolean failure = false;

        // set progress dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(DrinkOrderedActivity.this);
            pDialog.setMessage("Requesting...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        // query database method
        @Override
        protected String doInBackground(String... args) {
            // Check for success tag
            int success;
            byte[] output = {};
            SecureRandom secureRandom = null;
            try {
                // ???
                secureRandom = SecureRandom.getInstance("SHA1PRNG");
                output = new byte[16];
                secureRandom.nextBytes(output);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            String header = "Content-Type: application/json";
            // get consumer key by signing up for developer program
            String oauth_consumer_key = "";
            String oauth_signature_method = "HMAC-SHA1";
            String oauth_signature = "fa083a866978bdee05dc";
            long oauth_timestamp = System.currentTimeMillis();
            byte[] oauth_nonce = output;
            String oauth_version = "1.0";
            String oauth_callback = "";
            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("Authorization", header));
                params.add(new BasicNameValuePair("oauth_consumer_key", oauth_consumer_key));
                params.add(new BasicNameValuePair("oauth_signature_method", oauth_signature_method));
                params.add(new BasicNameValuePair("oauth_signature", oauth_signature));
                params.add(new BasicNameValuePair("oauth_timestamp", String.valueOf(oauth_timestamp)));
                params.add(new BasicNameValuePair("oauth_nonce", oauth_nonce.toString()));
                params.add(new BasicNameValuePair("oauth_version", oauth_version));
                params.add(new BasicNameValuePair("oauth_callback", oauth_callback));

                Log.d("request!", "starting");
                // getting product details by making HTTP request
                JSONObject json = jsonParser.makeHttpRequest(
                        REQUEST_TOKEN_URL, "POST", params);

                // check your log for json response
                Log.d("Requesting Access Token", json.toString());

                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("Successful!", json.toString());
                    return json.getString(TAG_MESSAGE);
                } else {
                    Log.d("Failure!", json.getString(TAG_MESSAGE));
                    return json.getString(TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * *
         */
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once product deleted
            pDialog.dismiss();
            if (file_url != null) {
                Toast.makeText(DrinkOrderedActivity.this, file_url, Toast.LENGTH_LONG).show();
            }
        }


    }
}
