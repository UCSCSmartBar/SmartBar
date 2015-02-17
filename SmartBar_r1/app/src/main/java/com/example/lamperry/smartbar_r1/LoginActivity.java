package com.example.lamperry.smartbar_r1;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/*
 * This class define the behavior of the login screen. Takes user login/password input and queries
 * DB server to authorize account. Throws invalid credentials toast if account not authorized.
 */
public class LoginActivity extends ActionBarActivity implements View.OnClickListener {

    // Initializations
    EditText user, pass;                        //To store username from login/pass field
    private Button mSubmit;                     //Login button
    private ProgressDialog pDialog;             // Progress Dialog
    JSONParser jsonParser = new JSONParser();   // JSON parser class

    //PHPlogin script location:
    //UCSC Smartbar Server:
    private static final String LOGIN_URL = "http://www.ucscsmartbar.com/login.php";

    //JSON element ids from response of php script:
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    // generated activity method
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //setup input fields
        user = (EditText)findViewById(R.id.type_email);
        pass = (EditText)findViewById(R.id.type_password);

        //setup buttons
        mSubmit = (Button)findViewById(R.id.login_button);

        //register listeners
        mSubmit.setOnClickListener(this);
    }

    // generated button method
    public void onClick(View v) {
        if (v.getId() == R.id.login_button)
            new AttemptLogin().execute();
    }

    // generated activity method
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    // generated activity method
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // Forgot user chosen in action bar
        if (id == R.id.action_forgot_user) {
            // add dialog box to input email address to send information to
            return true;
        }

        // Forgot password chosen in action bar
        if (id == R.id.action_forgot_pass) {
            // add dialog box to input email address to send information to
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /*
     * Class to attempt login, call PHP script to query database
     */
    class AttemptLogin extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        boolean failure = false;

        // set progress dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(LoginActivity.this);
            pDialog.setMessage("Attempting login...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        // query database method
        @Override
        protected String doInBackground(String... args) {
            // Check for success tag
            int success;
            String username = user.getText().toString();
            String password = pass.getText().toString();
            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("username", username));
                params.add(new BasicNameValuePair("password", password));

                Log.d("request!", "starting");
                // getting product details by making HTTP request
                JSONObject json = jsonParser.makeHttpRequest(
                        LOGIN_URL, "POST", params);

                // check your log for json response
                Log.d("Login attempt", json.toString());

                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("Login Successful!", json.toString());
                    ((MyApplication)LoginActivity.this.getApplication()).setMyUsername(username);
                    Intent intent = new Intent(LoginActivity.this, WelcomeActivity.class);
                    finish();
                    startActivity(intent);
                    return json.getString(TAG_MESSAGE);
                }else{
                    Log.d("Login Failure!", json.getString(TAG_MESSAGE));
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
            // dismiss the dialog once product deleted
            pDialog.dismiss();
            if (file_url != null){
                Toast.makeText(LoginActivity.this, file_url, Toast.LENGTH_LONG).show();
            }
        }
    }
}
