package com.example.smartbarmobile;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class NewUserActivity extends Activity implements View.OnClickListener {

    // Initializations
    EditText user, pass, agesb, weightsb;           // User Inputs
    Spinner sexsb;
    private Button mRegister;                       // Register Button
    private ProgressDialog pDialog;                 // Progress Dialog
    JSONParser jsonParser = new JSONParser();       // JSON parser class
    String pin;                                     // pin for user

    //PHP login script:
    //UCSC Smartbar Server:
    private static final String LOGIN_URL = "http://www.ucscsmartbar.com/register.php";

    //JSON element ids from response of php script:
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_user);

        String[] genders = { "Male", "Female", "Other"};

        user = (EditText)findViewById(R.id.type_email);
        pass = (EditText)findViewById(R.id.type_password);
        agesb = (EditText)findViewById(R.id.type_age);
        weightsb = (EditText)findViewById(R.id.type_weight);

        // set up drop down gender menu
        sexsb = (Spinner)findViewById(R.id.type_sex);
        ArrayAdapter<String> gender = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, genders);
        gender.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sexsb.setAdapter(gender);

        mRegister = (Button)findViewById(R.id.login_button);
        mRegister.setOnClickListener(this);
	}

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, StartupActivity.class);
        startActivity(intent);
    }

    // generated activity method
    @Override
    public void onClick(View v) {
        // instantiate and execute CreateUser class to query database and create account
        String age = agesb.getText().toString();
        if ((agesb.getText().toString().equals("")) || (weightsb.getText().toString().equals(""))) {
            Toast.makeText(this, "All fields required", Toast.LENGTH_LONG).show();
        } else {
            if (Integer.valueOf(age) < 21) {
                Toast.makeText(this, "Sorry, you must be 21 to use the Smart Bar.", Toast.LENGTH_LONG).show();
                return;
            }
            new CreateUser().execute();
        }
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

    // http://stackoverflow.com/questions/4165414/how-to-hide-soft-keyboard-on-android-after-clicking-outside-edittext
    public void setupUI(View view) {
        // set up touch listener for non-text box views to hide keyboard
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    ((MyApplication)NewUserActivity.this.getApplication()).hideSoftKeyboard(NewUserActivity.this);
                    return false;
                }
            });
        }
        // if a layout container, iterate over children and seed recursion
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup)view).getChildCount(); i++) {
                View innerView = ((ViewGroup)view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }


    // class to query database and add new user information, extends AsyncTask so that query can be
    // background thread
    class CreateUser extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        boolean failure = false;

        // initializes the progress dialog
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
            // Check for success tag
            int success;
            String username = user.getText().toString();
            String password = pass.getText().toString();
            String age = agesb.getText().toString();
            String weight = weightsb.getText().toString();
            String sex = sexsb.toString();
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
                    ((MyApplication)NewUserActivity.this.getApplication()).myUsername = username;
                    ((MyApplication)NewUserActivity.this.getApplication()).setLoggedIn(true);
                    Intent intent = new Intent(NewUserActivity.this, WelcomeActivity.class);
                    finish();
                    startActivity(intent);
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
}
