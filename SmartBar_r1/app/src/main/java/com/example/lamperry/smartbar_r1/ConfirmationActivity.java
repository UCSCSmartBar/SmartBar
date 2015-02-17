package com.example.lamperry.smartbar_r1;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/*
 * This class defines the behavior of the confirmation screen, namely entering drink and pin
 * information into the database
 */
public class ConfirmationActivity extends ActionBarActivity {

    // Initializations
    String drinkOrder;
    TextView displayDrink;
    int pin;

    JSONParser jsonParser = new JSONParser();       // JSON parser class

    //PHP login script:
    //UCSC Smartbar Server:
    private static final String LOGIN_URL = "http://www.ucscsmartbar.com/addDrink.php";

    //JSON element ids from response of php script:
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    // generated activity method
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        Intent intent = getIntent();
        drinkOrder = intent.getStringExtra("drinkOrder");
        displayDrink = (TextView)findViewById(R.id.drinkOrder);
        displayDrink.setText(drinkOrder);
        pin = ((MyApplication)this.getApplication()).myPin;
    }

    // generated activity method to display action bar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_confirmation, menu);
        return true;
    }

    // generated activity method for behavior of action bar menu items
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // Get pin clicked
        if (id == R.id.action_pin) {
            // TODO: add dialog to display user pin number
            return true;
        }

        // Logout clicked
        if (id == R.id.action_logout) {
            logout();
        }

        return super.onOptionsItemSelected(item);
    }

    // directs user back to Startup Activity
    private void logout() {
        ((MyApplication)this.getApplication()).setLoggedIn(false);
        ((MyApplication)this.getApplication()).myUsername = "";
        ((MyApplication)this.getApplication()).myPin = 0;
        Intent intent = new Intent(this, StartupActivity.class);
        startActivity(intent);
    }

    // directs user to Library Browse Activity
    public void confirmationToLibraryBrowse(View view) {
        Intent intent = new Intent(this, LibraryBrowseActivity.class);
        startActivity(intent);
    }

    // directs user to Drink Ordered Activity
    public void confirmationToDrinkOrdered(View view) {
        // instantiate and execute DrinkOrder to enter pin and drink order to database
        new DrinkOrder().execute();
    }

    // class to query database and add drink order/pin number, extends AsyncTask so that query can
    // be background thread
    class DrinkOrder extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        boolean failure = false;

        // queries database and adds new user information
        @Override
        protected String doInBackground(String... args) {
            // Check for success tag
            int success;
            String username = ((MyApplication)ConfirmationActivity.this.getApplication()).myUsername;
            String myPin = String.valueOf(pin);
            String drink = drinkOrder;
            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("username", username));
                params.add(new BasicNameValuePair("pin", myPin));
                params.add(new BasicNameValuePair("drink", drink));

                Log.d("request!", "starting");

                //Posting user data to script
                JSONObject json = jsonParser.makeHttpRequest(
                        LOGIN_URL, "POST", params);

                // full json response
                Log.d("Sending Drink Order...", json.toString());

                // json success element
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("Drink Sent!", json.toString());
                    Intent intent = new Intent(ConfirmationActivity.this,
                            DrinkOrderedActivity.class);
                    finish();
                    startActivity(intent);
                    return json.getString(TAG_MESSAGE);
                }else{
                    failure = true;
                    Log.d("Drink Send Failure", json.getString(TAG_MESSAGE));
                    return json.getString(TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
