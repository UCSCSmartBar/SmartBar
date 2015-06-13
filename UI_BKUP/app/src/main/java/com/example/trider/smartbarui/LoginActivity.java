package com.example.trider.smartbarui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class LoginActivity extends Activity implements View.OnClickListener {

    // Initializations
    EditText user, pass;                        //To store username from login/pass field
    private ProgressDialog pDialog;             // Progress Dialog
    JSONParser jsonParser = new JSONParser();   // JSON parser class
    String pin;                                 // pin for user

    //PHPlogin script location:
    //UCSC Smartbar Server:


    //JSON element ids from response of php script:
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    // generated activity method
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
        //TODO Captains Log: Saw another crash on the way over.
        //pin = MyApplication.myPin;
    }

    // generated button method
    public void onClick(View v) {
        String username = user.getText().toString();
        String password = pass.getText().toString();
        if ((username.equals("")) || (password.equals(""))) {
            Toast.makeText(this, "Username and Password required", Toast.LENGTH_SHORT).show();
            return;
        }
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



    // http://stackoverflow.com/questions/4165414/how-to-hide-soft-keyboard-on-android-after-clicking-outside-editText
    public void setupUI(View view) {
        // set up touch listener for non-text box views to hide keyboard
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    //TODO Captains Log Day3... I stumbled upon another crash...
                    //((MyApplication)LoginActivity.this.getApplication()).hideSoftKeyboard(LoginActivity.this);
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

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d("KEYBOARD", "KEY:" + keyCode + "\tEvent" + event.toString());
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (CommStream.DEBUG_MODE) {
                Toast.makeText(this, "You pressed the back button!", Toast.LENGTH_LONG).show();
            }
            startActivity(new Intent(this, IdleMenu.class));

        }
        return super.onKeyDown(keyCode, event);
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
            /*TODO Captains Log Day 4, The crashes have been consistent. We have been rooting them from their home, I yearn for the day
            //TODO when my kids do not have to endure the crashes I have had to.

            */
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
                params.add(new BasicNameValuePair("user_name", username));
                params.add(new BasicNameValuePair("user_password", password));

                Log.d("ALOG", "Starting Login");
                Log.d("ALOG","Username: "+username+"\tpassword:"+password);
                // getting product details by making HTTP request
                JSONObject json = jsonParser.makeHttpRequest(
                        ServerAccess.LOGIN_URL, "POST", params);

                if(json==null){
                    if(CommStream.DEBUG_MODE){Toast.makeText(getApplicationContext(),"Failure to Access Server",Toast.LENGTH_LONG).show();}
                        return null;
                }
                // check your log for json response
                Log.d("ALOG", json.toString());
                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("ALOG","Login Successful!"+ json.toString());
                    //TODO Captains Log Day5 : THE CRASHES ARE EVERYWHERE THEY'RE SURROUNDING US
                    MyApplication.myUsername = username;
                    //TODO Should have solved issue
                    Intent intent = new Intent(LoginActivity.this, LibraryBrowseActivity.class);
                    new AttemptGetPhone().execute();
                    finish();
                    startActivity(intent);

                    return json.getString(TAG_MESSAGE);
                }else{
                    Log.d("ALOG","Login Failure!"+ json.getString(TAG_MESSAGE));

                    return json.getString(TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         *
         *
         **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once product deleted
            //TODO Skippers Log: It seems as though the captain missed a crash.
            pDialog.dismiss();
            if (file_url != null){
                if(CommStream.DEBUG_MODE){Toast.makeText(LoginActivity.this, file_url, Toast.LENGTH_LONG).show();}
                if(file_url.contains("Failed")){
                    Toast.makeText(LoginActivity.this, "Username/Login Not Found", Toast.LENGTH_LONG).show();
                }

            }else{
                Toast.makeText(LoginActivity.this, "Username/Login Not Found", Toast.LENGTH_LONG).show();
            }
            Log.d("ALOG","file_url:"+file_url);
        }
    }


    class AttemptGetPhone extends AsyncTask<String, String, String> {
        int success;

        @Override
        protected String doInBackground(String... args) {
            try {
                String username = MyApplication.myUsername;
                Log.d("AGP", "Pin getting Posted:" + username);
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("username", username));
                Log.d("AGP", "HTTP getting Posted:" + ServerAccess.FIND_PHONE);
                JSONObject json = jsonParser.makeHttpRequest(ServerAccess.FIND_PHONE, "POST", params);
                // check your log for json response
                Log.d("AGP", json.toString());
                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("AGP", "Success String:" + json.toString());
                    return json.getString(TAG_MESSAGE);
                } else {
                    Log.d("AGP", "Failure with :" + json.getString(TAG_MESSAGE));
                    return json.getString(TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException npe) {
                npe.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String file_url) {
            // dismiss the dialog once product deleted
            if (file_url != null) {
                Log.d("AGP", "Returned URL:" + file_url);
                DrinkOrder.InUserPinString = file_url;
                CommStream PiComm = new CommStream();
                PiComm.PiLog("Got phone:"+DrinkOrder.InUserPinString);
                //Toast.makeText(LoginActivity.this, "Found Phone"+file_url, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(LoginActivity.this, "Failure to Access Server. Check Internet Connection"
                        , Toast.LENGTH_SHORT).show();
            }
        }
    }

}

