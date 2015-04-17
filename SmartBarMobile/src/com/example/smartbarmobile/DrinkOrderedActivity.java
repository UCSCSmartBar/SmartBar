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
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


/*
 * This class defines the behavior of the drink ordered screen: displays pin number for user, allows
 * access to library drink screen.
 */
public class DrinkOrderedActivity extends Activity {

    // Initializations
    TextView pinDisplay;

    String myBAC;
    String pin;

    JSONParser jsonParser = new JSONParser();       // JSON parser class

    //PHP login script:
    //UCSC Smartbar Server:
    private static final String BAC_URL = "http://www.ucscsmartbar.com/getBAC.php";

    //JSON element ids from response of php script:
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drink_ordered);

        pinDisplay = (TextView)findViewById(R.id.displayPin);
        pin =((MyApplication)this.getApplication()).myPin;
        pinDisplay.setText(pin);
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_drink_ordered, menu);
    return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // adds get pin button to action bar
        if (id == R.id.action_pin) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("My Number");
            builder.setMessage(pinDisplay.getText().toString());
            builder.setPositiveButton("OK", null);
            builder.show();
            return true;
        }

        // adds logout button to action bar
        if (id == R.id.action_logout) {
            logout();
        }

        return super.onOptionsItemSelected(item);
    }

    // logs user out of account and returns to Startup Activity
    private void logout() {
        ((MyApplication)this.getApplication()).setLoggedIn(false);
        Intent intent = new Intent(this, StartupActivity.class);
        startActivity(intent);
    }

    // for back navigation
    @Override
    
    public void onBackPressed() {
    }

    // starts Library Browse activity
    public void drinkOrderedToLibraryBrowse(View view) {
        Intent intent = new Intent(this, LibraryBrowseActivity.class);
        startActivity(intent);
    }

    // query database for latest BAC reading
    public void checkBAC(View view) {
        new GetBAC().execute();
    }

    // open Uber app
    public void openUber(View view) {
        PackageManager pm = this.getPackageManager();
        try {
        	// Try for installed app
        	Intent uber = pm.getLaunchIntentForPackage("com.ubercab");
        	if (uber == null) {
        		throw new PackageManager.NameNotFoundException();
        	}
        	uber.addCategory(Intent.CATEGORY_LAUNCHER);
        	this.startActivity(uber);
        } catch (PackageManager.NameNotFoundException e) {
        	// No Uber app! Open Google Play Store.
        	Intent playStore = new Intent(android.content.Intent.ACTION_VIEW);
        	playStore.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.ubercab"));
        	startActivity(playStore);
        }
    }

    // open Lyft app
    public void openLyft(View view) {
        PackageManager pm = this.getPackageManager();
        try {
        	// Try for installed app
        	Intent uber = pm.getLaunchIntentForPackage("me.lyft.android");
        	if (uber == null) {
        		throw new PackageManager.NameNotFoundException();
        	}
        	uber.addCategory(Intent.CATEGORY_LAUNCHER);
        	this.startActivity(uber);
        } catch (PackageManager.NameNotFoundException e) {
        	// No Uber app! Open Google Play Store.
        	Intent playStore = new Intent(android.content.Intent.ACTION_VIEW);
        	playStore.setData(Uri.parse("https://play.google.com/store/apps/details?id=me.lyft.android"));
        	startActivity(playStore);
        }
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
                AlertDialog.Builder builder = new AlertDialog.Builder(DrinkOrderedActivity.this);
                builder.setTitle("Last BAC Reading");
                builder.setMessage(myBAC);
                builder.setPositiveButton("OK", null);
                builder.show();
            }
        }
    }
}
