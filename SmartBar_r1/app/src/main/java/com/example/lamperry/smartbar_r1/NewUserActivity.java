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


// this class defines the behavior of the new user screen
// namely takes user input to create account, parses through login/password DB to authorize new
// account, and logs user in
// throws user prompt if login already created
public class NewUserActivity extends ActionBarActivity implements View.OnClickListener {

    EditText newUsernameText, newPasswordText, user, pass, agesb, weightsb, sexsb;
    String usernameString, passwordString;
    private Button mRegister;
    ArrayList<String> usernameDB = new ArrayList<>();
    ArrayList<String> passwordDB = new ArrayList<>();

    // Progress Dialog
    private ProgressDialog pDialog;

    // JSON parser class
    JSONParser jsonParser = new JSONParser();

    //PHP login script

    //UCSC Smartbar Server:
    private static final String LOGIN_URL = "http://www.ucscsmartbar.com/register.php";

    //JSON element ids from repsonse of php script:
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);

        usernameDB = ((MyApplication)this.getApplication()).getUsernameDB();
        passwordDB = ((MyApplication)this.getApplication()).getPasswordDB();

        newUsernameText = (EditText)findViewById(R.id.type_email);
        newPasswordText = (EditText)findViewById(R.id.type_password);

        user = (EditText)findViewById(R.id.type_email);
        pass = (EditText)findViewById(R.id.type_password);
        agesb = (EditText)findViewById(R.id.type_age);
        weightsb = (EditText)findViewById(R.id.type_weight);
        sexsb = (EditText)findViewById(R.id.type_sex);

        mRegister = (Button)findViewById(R.id.login_button);
        mRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub

        new CreateUser().execute();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_user, menu);
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

    // makes sure user input is valid; has entered something for username and password
    // and throws user prompt if login already taken
    public void checkUserLogin(View view) {
        usernameString = newUsernameText.getText().toString();
        passwordString = newPasswordText.getText().toString();

        if ((usernameString.equals("")) || (passwordString.equals(""))) {
            Toast.makeText(this, "You must enter a valid login and password", Toast.LENGTH_LONG).show();
            return;
        }

        if (!checkExists(usernameString, passwordString)) {
            newUserToWelcome(view, usernameString, passwordString);
        } else {
            Toast.makeText(this, "Username taken. Please try again.", Toast.LENGTH_LONG).show();
        }
    }

    // parses through DB to check if login account already exists
    private boolean checkExists(String username, String password) {
        int index = 0;
        for ( ; index < usernameDB.size(); index++) {
            if (username.equals(usernameDB.get(index))) {
                return true;
            }
        }
        return false;
    }


    class CreateUser extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        boolean failure = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(NewUserActivity.this);
            pDialog.setMessage("Creating User...");
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
            String age = agesb.getText().toString();
            String weight = weightsb.getText().toString();
            String sex =sexsb.getText().toString();
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
                    finish();
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
                Toast.makeText(NewUserActivity.this, file_url, Toast.LENGTH_LONG).show();
            }

        }

    }

    // starts welcome activity and sets all relevant global variables
    public void newUserToWelcome(View view, String username, String password) {
        usernameDB.add(username);
        passwordDB.add((password));
        ((MyApplication)this.getApplication()).setLoggedIn(true);
        ((MyApplication)this.getApplication()).setMyUsername(usernameString);
        ((MyApplication)this.getApplication()).setMyPassword(passwordString);

        Intent intent = new Intent(this, WelcomeActivity.class);
        startActivity(intent);
    }
}
