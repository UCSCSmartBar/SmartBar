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
    JSONParser jsonParser = new JSONParser();       // JSON parser class
    String pin;                                     // pin for user

    //PHP login script:
    //UCSC Smartbar Server:
    private static final String LOGIN_URL = "http://www.ucscsmartbar.com/register.php";

    //JSON element ids from response of php script:
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    // generated activity code
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        pin = ((MyApplication)this.getApplication()).myPin;
        new CreateUser().execute();
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


    // class to query database and add new user information, extends AsyncTask so that query can be
    // background thread
    class CreateUser extends AsyncTask<String, String, String> {

        boolean failure = false;

        @Override
        protected String doInBackground(String... args) {
            // Check for success tag
            int success;
            String username = ((MyApplication)WelcomeActivity.this.getApplication()).myUsername;
            String password = ((MyApplication)WelcomeActivity.this.getApplication()).myPassword;
            String age = "21";
            String weight = "100";
            String sex = "Male";
            
            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("username", username));
                params.add(new BasicNameValuePair("password", password));
                params.add(new BasicNameValuePair("age", age));
                params.add(new BasicNameValuePair("weight", weight));
                params.add(new BasicNameValuePair("sex", sex));

                Log.d("request!", "starting");

                //Posting user data to script
                JSONObject json = jsonParser.makeHttpRequest(
                        LOGIN_URL, "POST", params);

                // full json response
                Log.d("Login attempt", json.toString());

                // json success element
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("User Created!", json.toString());
                    ((MyApplication)WelcomeActivity.this.getApplication()).setLoggedIn(true);
                    //finish();
                    return json.getString(TAG_MESSAGE);
                }else{
                    failure = true;
                    Log.d("Login Failure!", json.getString(TAG_MESSAGE));
                    return json.getString(TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
