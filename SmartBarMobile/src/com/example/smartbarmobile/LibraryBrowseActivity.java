package com.example.smartbarmobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/*
 * This class defines the behavior of the order drink screen, namely allows a user to manually
 * enter a drink to order or choose from the library of drinks displayed and passes the drink order
 * to the next screen.
 */
public class LibraryBrowseActivity extends Activity implements View.OnClickListener,
                AdapterView.OnItemClickListener {

    // Initializations
    ListView drinkList;
    ArrayList<String> drinks = new ArrayList<>();
    ArrayList<String> recipes = new ArrayList<>();
    ArrayList<String> drinkLibrary = new ArrayList<>();
    ArrayList<String> filteredLibrary = new ArrayList<>();
    ArrayAdapter<String> drinkAdapter, filteredAdapter;

    String drinkOrder;
    EditText drinkOrderTyped;
    String pin;
    String lastChange = "";
    String drinkNameString;
    String drinkRecipeString;

    String receivedString = "";
    private ProgressDialog pDialog;             // Progress Dialog
    JSONParser jsonParser = new JSONParser();   // JSON parser class

    //PHPlogin script location:
    //UCSC Smartbar Server:
    private static final String GET_LIB_URL = "http://www.ucscsmartbar.com/getLib.php";

    //JSON element ids from response of php script:
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    // generated activity method
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library_browse);
        setupUI(findViewById(R.id.library_browse_activity));

        // setup lists and adapters for displaying the library
        drinkList = (ListView) findViewById(R.id.drinkList);
        drinkOrderTyped = (EditText) findViewById(R.id.typeDrink);
        drinkOrderTyped.addTextChangedListener(filterTextWatcher);

        // DB CODE
        new GetLibrary().execute();

        pin = ((MyApplication)this.getApplication()).myPin;
    }

    // necessary method when invoking text change listener
    @Override
    protected void onDestroy() {
        super.onDestroy();
        drinkOrderTyped.removeTextChangedListener(filterTextWatcher);
    }

    // inflate options action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_library_browse, menu);
        return true;
    }

    // define options actions bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_pin) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("My Number");
            builder.setMessage(String.valueOf(pin));
            builder.setPositiveButton("OK", null);
            AlertDialog dialog = builder.show();
            return true;
        }

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

    // to filter library as user enters input
    // all methods necessary to implement TextWatcher
    private TextWatcher filterTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        // still need logic to update library when user removes characters from constraint
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            Log.d("LAMPERRY", "onTextChanged filtering...");

            if (lastChange.length() < s.toString().length()) {
                // user has added characters to constraint
                for (int i = 0; i < filteredLibrary.size(); i++) {
                    if (!filteredLibrary.get(i).toUpperCase().startsWith(s.toString().toUpperCase())) {
                        filteredAdapter.remove(filteredLibrary.get(i));
                    }
                }
                filteredAdapter.notifyDataSetChanged();
                drinkList.setFilterText(s.toString());
                drinkList.setAdapter(filteredAdapter);
            }
            if (lastChange.length() > s.toString().length()) {
                drinkAdapter.clear();
                // user has removed characters from constraint
                for (int i = 0; i < filteredLibrary.size(); i++) {
                    if (filteredLibrary.get(i).toUpperCase().startsWith(s.toString().toUpperCase())) {
                        drinkAdapter.add(filteredLibrary.get(i));
                    }
                }
                drinkAdapter.notifyDataSetChanged();
                drinkList.setFilterText(s.toString());
                drinkList.setAdapter(drinkAdapter);
            }
            lastChange = s.toString();
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    // directs user to Confirmation Screen
    public void libraryBrowseToConfirmation(View view) {
        String myRecipe = "";

        drinkOrderTyped = (EditText) findViewById(R.id.typeDrink);
        drinkOrder = drinkOrderTyped.getText().toString();
        if (drinkOrder.equals("")) {
            Toast.makeText(this, "You must choose a drink!", Toast.LENGTH_SHORT).show();
            return;
        }
        drinkOrderTyped.setText(drinkOrder);

        // make sure user enters in valid drink order, case insensitive and leading/trailing
        // whitespace ok
        boolean isInLibrary = false;
        for (int i = 0; i < drinkLibrary.size(); i++) {
            if (drinkOrder.toUpperCase().trim().equals(drinkLibrary.get(i).toUpperCase())) {
                isInLibrary = true;
                drinkOrder = drinkLibrary.get(i);
                break;
            }
        }
        if (!isInLibrary) {
            Toast.makeText(this, "Sorry, SmartBar does not have that drink in its inventory. Please try again!",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        // grab recipe string
        for (int i = 0; i < drinks.size(); i++) {
            if (drinks.get(i).equals(drinkOrder))
                myRecipe = recipes.get(i);
        }

        // send drink order to next activity
        Intent intent = new Intent(this, ConfirmationActivity.class);
        intent.putExtra("drinkOrder", drinkOrder);
        intent.putExtra("drinkRecipe", myRecipe);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, WelcomeActivity.class);
        startActivity(intent);
    }

    // necessary method to implement AdapterView and View classes
    @Override
    public void onClick(View v) {
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // grab the drink selected from the library and display in edit text
        drinkOrderTyped = (EditText)findViewById(R.id.typeDrink);
        drinkOrder = drinkLibrary.get(position);
        drinkOrderTyped.setText(drinkOrder);
    }

    // http://stackoverflow.com/questions/4165414/how-to-hide-soft-keyboard-on-android-after-clicking-outside-EditText
    public void setupUI(View view) {
        // set up touch listener for non-text box views to hide keyboard
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    ((MyApplication)LibraryBrowseActivity.this.getApplication()).hideSoftKeyboard(LibraryBrowseActivity.this);
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

    // parse library string from database and create name/recipe lists
    private void parseStrings() {
        Log.d("RECEIVED", receivedString);

        String[] parsedArray;
        parsedArray = receivedString.split("#");
        drinkNameString = parsedArray[0];
        drinkRecipeString = parsedArray[1];
        String[] tempName, tempRecipe;

        // count how many drinks
        int drinkCount = 0;

        // parse names and recipes into separate string lists
//        for (int i = 0; i < drinkNameString.length(); i++) {
//            if (drinkNameString.length() > 0) {
//                if (drinkNameString.charAt(i) == '%') {
//                    drinkCount++;
//                    tempName = drinkNameString.split("%");
//                    drinks.add(tempName[0]);
//                    drinkLibrary.add(tempName[0]);
//                    filteredLibrary.add(tempName[0]);
//                    drinkNameString = drinkNameString.replace(tempName[0] + "%", "");
//                }
//            }
//        }
        for (int i = 0; i < drinkNameString.length(); i++) {
            if (drinkNameString.charAt(i) == '%') {
                drinkCount++;
            }
        }
        tempName = drinkNameString.split("%");
        tempRecipe = drinkRecipeString.split("%");
        for (int k = 0; k < drinkCount; k++) {
            drinks.add(tempName[k]);
            drinkLibrary.add(tempName[k]);
            filteredLibrary.add(tempName[k]);
            recipes.add(tempRecipe[k]);
        }

        drinkAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, drinkLibrary);
        filteredAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, drinkLibrary);

        drinkList.setAdapter(drinkAdapter);
        drinkList.setOnItemClickListener(this);

    }


    /*
     * Class to attempt login, call PHP script to query database
     */
    class GetLibrary extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        boolean failure = false;

        // set progress dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(LibraryBrowseActivity.this);
            pDialog.setMessage("Checking Inventory...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        // query database method
        @Override
        protected String doInBackground(String... args) {
            // Check for success tag
            int success;
            String placeholder = "";
            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("placeholder", placeholder));

                Log.d("request!", "starting");
                // getting product details by making HTTP request
                JSONObject json = jsonParser.makeHttpRequest(
                        GET_LIB_URL, "POST", params);

                // check your log for json response
                Log.d("Attempting Inventory Request", json.toString());

                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("Successful!", json.toString());
                    receivedString = json.getString(TAG_MESSAGE);
                    return json.getString(TAG_MESSAGE);
                }else{
                    Log.d("Failure!", json.getString(TAG_MESSAGE));
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
                receivedString = file_url;
            }
            parseStrings();
        }
    }
}
