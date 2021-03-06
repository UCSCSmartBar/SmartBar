package com.example.trider.smartbarui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
public class ConfirmationActivity extends Activity {

    // Initializations
    String drinkOrder;
    TextView displayDrink;
    String pin = null;
    ArrayList<String> liquorReturnList;
    String finalRecipe = "";
    String sendRecipe = "";

    JSONParser jsonParser = new JSONParser();       // JSON parser class

    //PHP login script:
    //UCSC Smartbar Server:

    @Override
    public void onBackPressed() {
        Log.d("CDA", "onBackPressed Called");
        Intent setIntent = new Intent(ConfirmationActivity.this,LibraryBrowseActivity.class);
        startActivity(setIntent);
    }

    //JSON element ids from response of php script:
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    // generated activity method
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        // grab the drink order from either Library Browse or Customize Drink
        // if previous intent was Customize Drink grab the specified liquors
        // display data
        Intent intent = getIntent();
        drinkOrder = intent.getStringExtra("drinkOrder");
        finalRecipe = intent.getStringExtra("drinkRecipe");
        sendRecipe = finalRecipe;
        DrinkOrder.InDrinkString = finalRecipe;
        DrinkOrder.InDrinkNameString = drinkOrder;

        liquorReturnList = intent.getStringArrayListExtra("liquorReturnList");
        displayDrink = (TextView)findViewById(R.id.drinkOrder);
        displayDrink.setText(drinkOrder);
        //TODO Found a straggler
        pin = MyApplication.myPin;

        CommStream PiComm = new CommStream();
        PiComm.PiLog(this.getLocalClassName(),DrinkOrder.OrderStatus());



        //TODO Might have fixed it

        // if previous screen was customize drink screen, display the custom brands along with
        // drink order
        // parse through each string and grab only liquor brand, not "Default: "
        //TODO liquor ReturnList was throwing at a NPE
        if (liquorReturnList != null && liquorReturnList.size() > 0) {
            String recipe;
            if (liquorReturnList.get(0).startsWith("Default: ")) {
                String[] liquorOnly = liquorReturnList.get(0).split(":");
                liquorOnly[1] = liquorOnly[1].trim();
                recipe = "with " + liquorOnly[1];
            } else {
                recipe = "with " + liquorReturnList.get(0);
            }
            // skip this if only one liquor in drink
            if (liquorReturnList.size() != 1) {
                for (int i = 1; i < liquorReturnList.size() - 1; i++) {
                    if (liquorReturnList.get(i).startsWith("Default: ")) {
                        String[] liquorOnly = liquorReturnList.get(i).split(":");
                        liquorOnly[1] = liquorOnly[1].trim();
                        recipe = recipe + ", " + liquorOnly[1];
                    } else {
                        recipe = recipe + ", " + liquorReturnList.get(i);
                    }
                }
                if (liquorReturnList.get(liquorReturnList.size() - 1).startsWith("Default: ")) {
                    String[] liquorOnly = liquorReturnList.get(liquorReturnList.size() - 1).split(":");
                    liquorOnly[1] = liquorOnly[1].trim();
                    recipe = recipe + ", " + liquorOnly[1];
                } else {
                    recipe = recipe + " and " + liquorReturnList.get(liquorReturnList.size() - 1);
                }
            }
            // display in text view
            TextView customRecipe = (TextView) findViewById(R.id.customRecipe);
            customRecipe.setText(recipe);
            customRecipe.setVisibility(View.VISIBLE);



        }
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
//        if (id == R.id.action_pin) {
//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            builder.setTitle("My Number");
//            builder.setMessage(String.valueOf(pin));
//            builder.setPositiveButton("OK", null);
//            AlertDialog dialog = builder.show();
//            return true;
//        }

//        // Logout clicked
//        if (id == R.id.action_logout) {
//            logout();
//        }

        return super.onOptionsItemSelected(item);
    }

    // directs user back to Startup Activity
    private void logout() {
        ((MyApplication)this.getApplication()).setLoggedIn(false);
//        Intent intent = new Intent(this, StartupActivity.class);
//        startActivity(intent);
    }

    // directs user to Library Browse Activity
    public void confirmationToLibraryBrowse(View view) {
        Intent intent = new Intent(this, LibraryBrowseActivity.class);
        startActivity(intent);
    }

    // directs user to Drink Ordered Activity
    public void confirmationToDrinkOrdered(View view) {
        // instantiate and execute DrinkOrder to enter pin and drink order to database


        //TODO Since this is the UI version, there is no reason to put a drink on the queue.
        //new PlaceOrder().execute();
        startActivity(new Intent(ConfirmationActivity.this,CheckBAC.class));
    }

    /*
     * method gets called when customize drink button clicked
     * allows for lowercase spelling as well as leading and trailing whitespace on user input
     * builds array of liquors present in the chosen drink and sends to customize drink activity
     */
    public void customizeDrink(View view) {
        if (finalRecipe == null) {
            Log.e("Customize Error", "Recipe null.");
            return;
        } else if (drinkOrder.equals("Carbonated Orange Gatorade")) {
            Toast.makeText(this, "Sorry, there is no liquor in this drink to customize!", Toast.LENGTH_SHORT).show();
            return;
        }
        ArrayList<String> liquorList;
        liquorList = buildLiquorList(finalRecipe);

        Intent intent = new Intent(this, CustomizeDrinkActivity.class);
        intent.putExtra("drinkOrder", drinkOrder);
        intent.putExtra("drinkRecipe", sendRecipe);
        intent.putStringArrayListExtra("liquorList", liquorList);
        startActivity(intent);
    }

    // method for sending recipe to database, prototype for now
    private ArrayList<String> buildLiquorList(String recipe) {
        ArrayList<String> liquors = new ArrayList<>();
        String[] temp = recipe.split(",");
        int numLiquors = Integer.valueOf(temp[0]);
        recipe = recipe.replace(recipe.split("@")[0] + "@", "");
        for (int i = 0; i < numLiquors; i++) {
            switch (recipe.split(",")[0]) {
                case "B":
                    liquors.add("Bitters");
                    break;
                case "G":
                    liquors.add("Gin");
                    break;
                case "R":
                    liquors.add("Rum");
                    break;
                case "T":
                    liquors.add("Tequila");
                    break;
                case "V":
                    liquors.add("Vodka");
                    break;
                case "W":
                    liquors.add("Whiskey");
                    break;
                default:
                    Log.e("Build Recipe", "default");
                    break;
            }
            recipe = recipe.replace(recipe.split("@")[0] + "@", "");
        }
        return liquors;
    }

///TODO NO need to put on queue, instead send to check BAC/Confirm Drink
    // class to query database and add drink order/pin number, extends AsyncTask so that query can
    // be background thread
    class PlaceOrder extends AsyncTask<String, String, String> {

        boolean failure = false;

        // queries database and adds new user information
        @Override
        protected String doInBackground(String... args) {
            // Check for success tag
            int success;
            String username = MyApplication.myUsername;
            if(username== null || username.equalsIgnoreCase("lpeezy")){
                username = "Anon";
            }
            if(pin == null){
                pin = "00000000000";//If Anonymous user.
            }
            String drink = drinkOrder;
            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("username", username));
                params.add(new BasicNameValuePair("pin", pin));
                params.add(new BasicNameValuePair("drink", drink));
                params.add(new BasicNameValuePair("recipe", sendRecipe));

                Log.d("ConfAct", "Username:" + username);
                Log.d("ConfAct", "Pin" + pin);
                Log.d("ConfAct", "Drink:" + drink);
                Log.d("ConfAct", "Recipe:" + sendRecipe);

                //Posting user data to script
                JSONObject json = jsonParser.makeHttpRequest(
                        ServerAccess.ADD_DRINK_URL, "POST", params);

                // full json response
                Log.d("ConfAct","Sending Drink Order..." + json.toString());

                // json success element
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("ConfAct", "Drink Sent!" + json.toString());
                    //Intent intent = new Intent(ConfirmationActivity.this,DrinkOrderedActivity.class);
                    finish();
                    //TODO here is where it outs to the other system.

                    com.example.trider.smartbarui.DrinkOrder.InDrinkString = sendRecipe;
                    startActivity(new Intent(ConfirmationActivity.this,CheckBAC.class));

                    return json.getString(TAG_MESSAGE);
                }else{
                    failure = true;
                    Log.d("ConfAct","Drink Send Failure"+ json.getString(TAG_MESSAGE));
                    return json.getString(TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
