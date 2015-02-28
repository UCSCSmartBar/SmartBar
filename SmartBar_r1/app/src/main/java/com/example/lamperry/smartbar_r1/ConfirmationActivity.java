package com.example.lamperry.smartbar_r1;

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
    String pin;
    ArrayList<String> liquorReturnList;
    String finalRecipe = "";

    JSONParser jsonParser = new JSONParser();       // JSON parser class

    //PHP login script:
    //UCSC Smartbar Server:
    private static final String ADD_DRINK_URL = "http://www.ucscsmartbar.com/addDrink.php";

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
        liquorReturnList = intent.getStringArrayListExtra("liquorReturnList");
        displayDrink = (TextView)findViewById(R.id.drinkOrder);
        displayDrink.setText(drinkOrder);
        pin = ((MyApplication)this.getApplication()).myPin;

        // if previous screen was customize drink screen, display the custom brands along with
        // drink order
        // parse through each string and grab only liquor brand, not "Default: "
        if (liquorReturnList != null) {
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
        if (id == R.id.action_pin) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("My Number");
            builder.setMessage(String.valueOf(pin));
            builder.setPositiveButton("OK", null);
            AlertDialog dialog = builder.show();
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
        buildRecipe(drinkOrder);
        new DrinkOrder().execute();
    }

    /*
     * method gets called when customize drink button clicked
     * allows for lowercase spelling as well as leading and trailing whitespace on user input
     * builds array of liquors present in the chosen drink and sends to customize drink activity
     */
    public void customizeDrink(View view) {
        ArrayList<String> liquorList = new ArrayList<>();
        switch (drinkOrder.toUpperCase()) {
            case "AMF":
                drinkOrder = "Adios Motherfucker";
                liquorList.add("Bitters");
                liquorList.add("Gin");
                liquorList.add("Rum");
                liquorList.add("Tequila");
                liquorList.add("Vodka");
            case "GIN AND TONIC":
                drinkOrder = "Gin and Tonic";
                liquorList.add("Gin");
                break;
            case "LONG ISLAND ICED TEA":
                drinkOrder = "Long Island Iced Tea";
                liquorList.add("Gin");
                liquorList.add("Rum");
                liquorList.add("Tequila");
                liquorList.add("Vodka");
                break;
            case "MANHATTAN":
                drinkOrder = "Manhattan";
                liquorList.add("Bitters");
                liquorList.add("Vermouth");
                liquorList.add("Whiskey");
                break;
            case "WHISKEY SOUR":
                drinkOrder = "Whiskey Sour";
                liquorList.add("Whiskey");
                break;
            default:
        }

        Intent intent = new Intent(this, CustomizeDrinkActivity.class);
        intent.putExtra("drinkOrder", drinkOrder);
        intent.putStringArrayListExtra("liquorList", liquorList);
        startActivity(intent);
    }

    // method for sending recipe to database, prototype for now
    private void buildRecipe(String drink) {
        switch (drink) {
            case "Gin and Tonic":
                finalRecipe = "1,2@G,0,1.0@T,0,1,3.0*";
                break;
            case "Long Island Iced Tea":
                finalRecipe = "";
                break;
            case "Manhattan":
                finalRecipe = "1,2@W,0,4.0@V,0,0,2.0@A,0,0,0.5*";
                break;
            case "Whiskey Sour":
                finalRecipe = "";
                break;
        }
    }


    // class to query database and add drink order/pin number, extends AsyncTask so that query can
    // be background thread
    class DrinkOrder extends AsyncTask<String, String, String> {

        boolean failure = false;

        // queries database and adds new user information
        @Override
        protected String doInBackground(String... args) {
            // Check for success tag
            int success;
            String username = ((MyApplication)ConfirmationActivity.this.getApplication()).myUsername;
            String drink = drinkOrder;
            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("username", username));
                params.add(new BasicNameValuePair("pin", pin));
                params.add(new BasicNameValuePair("drink", drink));
                //params.add(new BasicNameValuePair("recipe", finalRecipe));

                Log.d("request!", "starting");

                //Posting user data to script
                JSONObject json = jsonParser.makeHttpRequest(
                        ADD_DRINK_URL, "POST", params);

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
