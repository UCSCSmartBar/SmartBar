package com.example.trider.smartbarui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class PickUpDrink extends Activity {


    //php login script location:


    //Objects
    public DrinkOrder testDrink;
    private CommStream PiComm;

    JSONParser jsonParser = new JSONParser();

    //Views
    ProgressBar pBar;
    Toast toast;
    EditText eText;
    private ProgressDialog pDialog;

    //Variables
    boolean searching = false;
    boolean searchFailure = true;
    String pinString;
    String IncomingString;
    String OutMessage;
    String[] ParsedString;


    //Login tags
    private static final String LOGIN_URL = "http://www.ucscsmartbar.com/getDrink.php";
    //JSON element ids from response of php script:
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";






    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_up_drink);

        //Creates a "virtual drink order"
        testDrink = new DrinkOrder();
        //Checks and maintains connection with R-Pi
        PiComm = new CommStream();

//        //Testing serial link by hard coding the addition of elements to drink order
//        DrinkOrder.LiquorObj newThing = new DrinkOrder.LiquorObj(1,2,3);
//        testDrink.AddToDrinkOrder(newThing);
//
//        DrinkOrder.MixerObj newThing2 = new DrinkOrder.MixerObj(3,4,true,3);
//        testDrink.AddToDrinkOrder(newThing2);
        //Displays the comm status, and virtual drink order, along with its serial equivalent on
        //the actual UI
//        TextView tView = (TextView) findViewById(R.id.drinkView);
//        //tView.setText("Pi Comm is:" + PiComm.Status()+ "\n" + PiComm.readString());
//        tView.append(testDrink.toString());
//        tView.append(testDrink.serialDrink());

        //Hides a progress bar that will be used to indicate there order is being searched for
        pBar = (ProgressBar) findViewById(R.id.findUserProgress);
        pBar.setVisibility(View.INVISIBLE);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_user, menu);
        return true;
    }

    /**
     * Checks for valid pin. Displays warnings if the pin entered is too Long/Short or not numeric.
     * Upon entering a valid pin, the program checks the server via a PHP function, and awaits the return
     * of the drink order, (and fingerprint data).
     * @param view The Edit Text view. Required by Android, not being used.
     */
    public void CheckPin(View view){
        Context context = getApplicationContext();
        //Hides Message
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);


        //If the pin has already been entered and is currently being searched for, alert user
        /*
        if(searching){
            toast = Toast.makeText(context, "Please Wait While Your Order Is Being Found", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
*/
        //Grabs the entered pin number
        eText = (EditText) findViewById(R.id.txtPin);
        int Pin;
        pinString = eText.getText().toString();

        //Checking for pin Length
        if(pinString.length() != 11){
            toast = Toast.makeText(context, "Pin is too short: " + pinString, Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        //Checking Numeric-based String for errors
        /*
        try {
            Pin = Integer.decode(pinString);
        }catch(NumberFormatException e){
            toast = Toast.makeText(context, "Warn: Pin decoded incorrectly. ", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }*/

        new AttemptGetDrink().execute();

        //Displays the progress bar so user knows the drink is being looked up
        pBar = (ProgressBar) findViewById(R.id.findUserProgress);
        pBar.setVisibility(View.VISIBLE);
        searching = true;

        //Creates a singleton task that will run in exactly 2000ms after the button is clicked
        new Timer().schedule(new TimerTask() {
            public void run() {
                PickUpDrink.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pBar.setVisibility(View.INVISIBLE);
                        searching = false;
                        Intent intent = new Intent(PickUpDrink.this,PickUpFinger.class);

                        //If nothing came up
                        if(searchFailure){
                            return;
                        }
                        //Otherwise write the drink order to the Pi
                        PiComm.writeString("$DO,"+ IncomingString );
                        DrinkOrder t = new DrinkOrder();
                        t.DecodeString(IncomingString);
                        t.storeDrinkOrder(IncomingString);
                        PiComm.writeString("$FPQ," +eText.getText().toString());
                        intent.putExtra("tString",IncomingString);
                        startActivity(intent);
                    }
                });
            }
        },5000);


    }

    class AttemptGetDrink extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(PickUpDrink.this);
            pDialog.setMessage("Attempting to get drink...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
            Log.d("AGD","Pre-Exec");
        }

        @Override
        protected String doInBackground(String... args) {
            // TODO Auto-generated method stub
            // Check for success tag
            int success;
            String userpin = eText.getText().toString();

            try {
                Log.d("AGD","Mid-Execute");
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("pin", userpin));

                Log.d("request!", "starting");
                // getting product details by making HTTP request
                JSONObject json = jsonParser.makeHttpRequest(
                        LOGIN_URL, "POST", params);

                // check your log for json response
                Log.d("Drink retrieve attempt", json.toString());

                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("Drink found Successful!", json.toString());
                    searchFailure = false;
                    //Intent i = new Intent(Login.this, ReadComments.class);
                    return json.getString(TAG_MESSAGE);
                }else{
                    Log.d("Login Failure!", json.getString(TAG_MESSAGE));
                    searchFailure = true;
                    return json.getString(TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch(NullPointerException npe){
                npe.printStackTrace();
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
               Toast.makeText(PickUpDrink.this, file_url, Toast.LENGTH_LONG).show();
               IncomingString = file_url;

            }else{
                Toast.makeText(PickUpDrink.this,"Failure to Access Server. Check Internet Connection"
                        , Toast.LENGTH_SHORT).show();
            }

        }

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
    /**
     * Skips screen
     * @param view
     */
    public void SkipToPickFinger (View view){
        startActivity(new Intent(this,PickUpFinger.class));

    }






}
