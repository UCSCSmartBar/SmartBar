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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;


// this class defines all activity within the login screen
// takes user login/password input and parses through DB to authorize account
// throws user prompt to try again if account not authorized
public class LoginActivity extends ActionBarActivity implements View.OnClickListener {

    EditText user, pass, usernameText, passwordText; //To store username from login/pass field
    private Button mSubmit; //Login button
    String usernameString, passwordString;
    ArrayList<String> usernameDB = new ArrayList<>();
    ArrayList<String> passwordDB = new ArrayList<>();
    // Progress Dialog
    private ProgressDialog pDialog;
    // JSON parser class
    JSONParser jsonParser = new JSONParser();

    //PHPlogin script location:
    //UCSC Smartbar Server:
    private static final String LOGIN_URL = "http://www.ucscsmartbar.com/login.php";

    //JSON element ids from repsonse of php script:
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

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

        // grabs databases from global class
        usernameDB = ((MyApplication)this.getApplication()).getUsernameDB();
        passwordDB = ((MyApplication)this.getApplication()).getPasswordDB();


    }


    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.login_button:
                new AttemptLogin().execute();
                break;

            default:
                break;
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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


    class AttemptLogin extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        boolean failure = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(LoginActivity.this);
            pDialog.setMessage("Attempting login...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            // TODO Auto-generated method stub
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
                    //Intent i = new Intent(Login.this, ReadComments.class);
                    finish();
                    //startActivity(i);
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


    // parses through database to verify account information
    public void checkLogin(View view) {
        int index;
        usernameText = (EditText)findViewById(R.id.type_email);
        passwordText = (EditText)findViewById(R.id.type_password);

        // grab username and password from EditText
        usernameString = usernameText.getText().toString();
        passwordString = passwordText.getText().toString();

        // make sure user entered something, display prompt if not
        if ((usernameString.equals("")) || (passwordString.equals(""))) {
            Toast.makeText(this, "You must enter a valid username and password.", Toast.LENGTH_LONG).show();
            return;
        }

        // check if login is in database
        for (index = 0; index < usernameDB.size(); index++) {
            // if login found
            if (usernameString.equals(usernameDB.get(index))) {
                // check password; if passes start welcome activity
                if (passwordString.equals(passwordDB.get(index))) {
                    loginToWelcome(view);
                    return;
                }
            }
        }

        // login not found
        Toast.makeText(this, "Username/password not found. Please try again", Toast.LENGTH_SHORT).show();
    }

    // starts welcome activity
    public void loginToWelcome(View view) {
        ((MyApplication)this.getApplication()).setLoggedIn(true);
        ((MyApplication)this.getApplication()).setMyUsername(usernameString);
        ((MyApplication)this.getApplication()).setMyPassword(passwordString);
        Intent intent = new Intent(this, WelcomeActivity.class);
        startActivity(intent);
    }
}
