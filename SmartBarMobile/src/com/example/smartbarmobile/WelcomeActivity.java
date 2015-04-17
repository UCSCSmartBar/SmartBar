package com.example.smartbarmobile;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

/*
 * This class defines the behavior of the welcome screen which essentially just directs the user to
 * logout of a signed in account
 */
public class WelcomeActivity extends Activity {

    // Initializations
    String pin;                                 // pin for user
    JSONParser jsonParser = new JSONParser();   // JSON parser class
    String myBAC;

    //PHPlogin script location:
    //UCSC Smartbar Server:
    private static final String BAC_URL = "http://www.ucscsmartbar.com/getBAC.php";

    //JSON element ids from response of php script:
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    // generated activity code
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        pin = ((MyApplication)this.getApplication()).myPin;
    }

    // generated activity code
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_welcome, menu);
        return true;
    }

    // sets up action bar for settings and logout
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // Logout chosen from action bar
        if (id == R.id.action_pin) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("My Number");
            builder.setMessage(String.valueOf(pin));
            builder.setPositiveButton("OK", null);
            builder.show();
            return true;
        }

        // Logout chosen from action bar
        if (id == R.id.action_logout) {
            logout();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
    }

    // logout method, returns user to Startup Activity
    private void logout() {
        ((MyApplication)this.getApplication()).setLoggedIn(false);
        Intent intent = new Intent(this, StartupActivity.class);
        startActivity(intent);
    }

    // directs user to Library Browse Activity
    public void welcomeToLibraryBrowse(View view) {
        Intent intent = new Intent(this, LibraryBrowseActivity.class);
        startActivity(intent);
    }

    // query database for latest BAC reading
    public void checkBAC(View view) {
        new GetBAC().execute();
    }


    /*
     * Class to attempt login, call PHP script to query database
     */
    class GetBAC extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        boolean failure = false;
        int success;

        @Override
        protected String doInBackground(String... args) {
            // Check for success tag
            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("pin", pin));

                Log.d("request!", "starting");
                // getting product details by making HTTP request
                JSONObject json = jsonParser.makeHttpRequest(
                        BAC_URL, "POST", params);
                
                if (json == null)
                	return null;
                Log.e("Error: ", json.toString());

                // check your log for json response
                Log.d("Attempting getBAC...", json.toString());

                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("BAC Query Successful!", json.toString());
                    return json.getString(TAG_MESSAGE);
                }else{
                    Log.d("BAC Query Failure!", json.getString(TAG_MESSAGE));
                    return json.getString(TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            if (file_url != null){
                myBAC = file_url;
                AlertDialog.Builder builder = new AlertDialog.Builder(WelcomeActivity.this);
                builder.setTitle("Last BAC Reading");
                builder.setMessage(myBAC);
                builder.setPositiveButton("OK", null);
                builder.show();
            }
        }
    }
}
