package com.example.smartbarmobile;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
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

/**
 * This class defines all the behavior on the create account screen.
 * Checks for correct age, username already in database, sends new user info to database.
 */
public class NewUserActivity extends Activity implements View.OnClickListener {

    EditText user, pass, repass, email, agesb;
    Spinner sexsb;
    private ProgressDialog pDialog;
    JSONParser jsonParser = new JSONParser();
    Button mRegister;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_user);

        setupUI(findViewById(R.id.new_user_activity));

        String[] genders = { "Male", "Female", "Other"};

        user = (EditText)findViewById(R.id.type_username);
        pass = (EditText)findViewById(R.id.type_password);
        repass = (EditText)findViewById(R.id.retype_password);
        email = (EditText)findViewById(R.id.type_email);
        agesb = (EditText)findViewById(R.id.type_age);

        /* Setup drop down gender menu */
        sexsb = (Spinner)findViewById(R.id.type_sex);
        ArrayAdapter<String> gender = new ArrayAdapter<>(this,
                R.layout.spinner_text_white, genders);
        gender.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sexsb.setAdapter(gender);

        mRegister = (Button) findViewById(R.id.login_button);
        mRegister.setOnClickListener(this);
	}

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, StartupActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        mRegister.setEnabled(false);

    	/**
         * Instantiate and execute CreateUser class to query database and create account provide
         * all inputs valid
         */
        String age = agesb.getText().toString();
        if ((user.getText().toString().equals("")) || (pass.getText().toString().equals("")) || 
        		(agesb.getText().toString().equals("")) || (email.getText().toString().equals(""))) {
            Toast.makeText(this, "All fields required", Toast.LENGTH_LONG).show();
            return;
        } else if (Integer.valueOf(age) < 21) {
            Toast.makeText(this, "Sorry, you must be 21 to use the Smart Bar.", Toast.LENGTH_LONG).show();
            return;
        } else if (!pass.getText().toString().equals(repass.getText().toString())) {
        	Toast.makeText(this, "Passwords do not match. Please try again.", Toast.LENGTH_SHORT).show();
        	return;
        }

        /* EVERYTHING IS AWESOME so create new user */
        new CreateUser().execute();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		/* Inflate the menu; this adds items to the action bar if it is present. */
		getMenuInflater().inflate(R.menu.menu_new_user, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		/** 
		 * Handle action bar item clicks here. The action bar will automatically handle clicks on
         * the Home/Up button, so long as you specify a parent activity in AndroidManifest.xml
		 */
        int id = item.getItemId();

        /* Action bar about Smartbar sequence */
        if (id == R.id.action_aboutSB) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("About Smartbar");
            builder.setMessage("UCSC Senior Design Project 2015: Version 0.1");
            builder.setPositiveButton("OK", null);
            builder.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
	}

    /* http://stackoverflow.com/questions/4165414/how-to-hide-soft-keyboard-on-android-after-clicking-outside-edittext */
    public void setupUI(View view) {
    	/* Set up touch listener for non text box views to hide keyboard */
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                	switch (event.getAction()) {
	                	case MotionEvent.ACTION_DOWN: break;
	                	case MotionEvent.ACTION_UP:
	                		v.performClick();
	                    	MyApplication.hideSoftKeyboard(NewUserActivity.this);
	                    	break;
	                    default: break;
                	}
                    return false;
                }
            });
        }
        /* If a layout container, iterate over children and seed recursion */
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup)view).getChildCount(); i++) {
                View innerView = ((ViewGroup)view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }


    /**
     * This class gets called when a user has entered all parameters and is ready to register a new
     * account. This will fail if the current username/email/phone number is already in use in the
     * Smartbar database.
     */
    class CreateUser extends AsyncTask<String, String, String> {

        boolean failure = false;
        int success;

        /* Before starting background thread Show Progress Dialog */
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

            String username = user.getText().toString();
            String password = pass.getText().toString();
            String emailAddr = email.getText().toString();
            String phone = MyApplication.myPin;
            String age = agesb.getText().toString();
            String sex = sexsb.toString();

            try {
                /* Building Parameters */
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("username", username));
                params.add(new BasicNameValuePair("password", password));
                params.add(new BasicNameValuePair("email", emailAddr));
                params.add(new BasicNameValuePair("phone", phone));
                params.add(new BasicNameValuePair("age", age));
                params.add(new BasicNameValuePair("sex", sex));

                Log.d("request!", "starting");

                /* Getting product details by making an HTTP request */
                JSONObject json = jsonParser.makeHttpRequest(
                        ServerAccess.REGISTER_URL, "POST", params);
                
                if (json == null) {
                	Toast.makeText(NewUserActivity.this,
                            "Cannot connect to server. Please check internet connection.",
                            Toast.LENGTH_SHORT).show();
                	return null;
                }

                /* Check Logcat for full JSON response */
                Log.d("Login attempt", json.toString());

                /* JSON success tag */
                success = json.getInt(ServerAccess.TAG_SUCCESS);

                if (success == 1) {
                    Log.d("User Created!", json.toString());
                    MyApplication.myUsername = username;
                    ((MyApplication)NewUserActivity.this.getApplication()).myPassword = password;
                    ((MyApplication)NewUserActivity.this.getApplication()).myEmail= emailAddr;
                    ((MyApplication)NewUserActivity.this.getApplication()).myAge = age;
                    ((MyApplication)NewUserActivity.this.getApplication()).myGender = sex;
                    ((MyApplication)NewUserActivity.this.getApplication()).setLoggedIn(true);
                    return json.getString(ServerAccess.TAG_MESSAGE);
                } else {
                    failure = true;
                    Log.d("Create Account Failure!", json.getString(ServerAccess.TAG_MESSAGE));
                    return json.getString(ServerAccess.TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
            	Log.e("NPE: ", e.toString());
            	e.printStackTrace();
            }
            return null;
        }

        /**
         * After completing background task dismiss the progress dialog and continue with application
         * flow. Since this is a new user, they must enter payment information before proceeding
         * to order drinks.
         */
        protected void onPostExecute(String file_url) {
            pDialog.dismiss();
            mRegister.setEnabled(true);
            if (file_url != null) {
                Toast.makeText(NewUserActivity.this, file_url, Toast.LENGTH_SHORT).show();
                if (success == 1) {
                    ((MyApplication)NewUserActivity.this.getApplication()).gSignIn = false;
                    Intent intent = new Intent(NewUserActivity.this, PaymentActivity.class);
                    finish();
                    startActivity(intent);
                }
            }
        }
    }
}
