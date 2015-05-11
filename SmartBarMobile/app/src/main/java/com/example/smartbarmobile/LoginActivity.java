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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/*
 * This 
 */
public class LoginActivity extends Activity implements View.OnClickListener {

    EditText user, pass;
    private ProgressDialog pDialog;
    JSONParser jsonParser = new JSONParser();

    /** JSON element IDs from response of php script **/
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

        setupUI(findViewById(R.id.login_activity));

        //setup input fields
        user = (EditText)findViewById(R.id.type_email);
        pass = (EditText)findViewById(R.id.type_password);

        //setup buttons
        Button mSubmit = (Button)findViewById(R.id.login_button);

        //register listeners
        mSubmit.setOnClickListener(this);
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

    // generated button method
    public void onClick(View v) {
        String username = user.getText().toString();
        String password = pass.getText().toString();
        if ((username.equals("")) || (password.equals(""))) {
            Toast.makeText(this, "Username and password required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (v.getId() == R.id.login_button) {
            new AttemptLogin().execute();
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

    // http://stackoverflow.com/questions/4165414/how-to-hide-soft-keyboard-on-android-after-clicking-outside-editText
    public void setupUI(View view) {
        // set up touch listener for non-text box views to hide keyboard
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                	switch (event.getAction()) {
	                	case MotionEvent.ACTION_DOWN: break;
	                	case MotionEvent.ACTION_UP:
	                		v.performClick();
	                    	MyApplication.hideSoftKeyboard(LoginActivity.this);
	                    	break;
	                    default: break;
                	}
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


    /*
     * Class to attempt login, call PHP script to query database
     */
    class AttemptLogin extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         **/
        int success;

        /** Start progress dialog **/
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

            String username = user.getText().toString();
            String password = pass.getText().toString();
            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("user_name", username));
                params.add(new BasicNameValuePair("user_password", password));

                Log.d("request!", "starting");
                // getting product details by making HTTP request
                JSONObject json = jsonParser.makeHttpRequest(
                        ServerAccess.LOGIN_URL, "POST", params);
                
                if (json == null) {
                	Toast.makeText(LoginActivity.this, "Cannot connect to server. Please check internet connection.",
                            Toast.LENGTH_SHORT).show();
                	return null;
                }

                // check your log for json response
                Log.d("Login attempt", json.toString());

                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("Login Successful!", json.toString());
                    MyApplication.myUsername = username;
                    ((MyApplication)LoginActivity.this.getApplication()).setLoggedIn(true);
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
                Toast.makeText(LoginActivity.this, file_url, Toast.LENGTH_SHORT).show();
                if (success == 1) {
                    ((MyApplication)LoginActivity.this.getApplication()).gSignIn = false;
                    /** Assert user has Braintree customer ID, if not, setup payment **/
                    new FindBraintreeCust().execute();
                }
            }
        }
    }


    /*
     * Check if user has Braintree customer account already, if not, make one.
     */
    class FindBraintreeCust extends AsyncTask<String, String, String> {

        int success;

        @Override
        protected String doInBackground(String... args) {

            Log.d("FindBraintreeCust", "starting...");
            String phone = MyApplication.myPin;
            try {
                //** Build parameters **/
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("phone", phone));

                Log.d("request!", "starting");

                /** Getting product details by making HTTP request **/
                JSONObject json = jsonParser.makeHttpRequest(
                        ServerAccess.FIND_CUST_URL, "POST", params);

                Log.d("URL", ServerAccess.FIND_CUST_URL);

                if (json == null) {
                    Toast.makeText(LoginActivity.this, "Cannot connect to server. Please check internet connection.",
                            Toast.LENGTH_SHORT).show();
                    return null;
                }

                /** JSON response returned **/
                Log.d("FindBraintreeCust", "returned");

                /** JSON success tag **/
                success = json.getInt(ServerAccess.TAG_SUCCESS);

                if (success == 1) {
                    Log.d("FindBraintreeCust", json.toString());
                    ((MyApplication)LoginActivity.this.getApplication()).setLoggedIn(true);
                    return json.getString(ServerAccess.TAG_MESSAGE);
                }else{
                    Log.d("FindBraintreeCust", json.getString(ServerAccess.TAG_MESSAGE));
                    return json.getString(ServerAccess.TAG_MESSAGE);
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
                if (success == 1) {
                    Intent readyToOrder = new Intent(LoginActivity.this, ReadyToOrderActivity.class);
                    finish();
                    startActivity(readyToOrder);
                } else {
                    /** Braintree Customer ID not found; user must enter payment info **/
                    Intent missingParams = new Intent(LoginActivity.this, PaymentActivity.class);
                    finish();
                    startActivity(missingParams);
                }
            }
        }
    }
}
