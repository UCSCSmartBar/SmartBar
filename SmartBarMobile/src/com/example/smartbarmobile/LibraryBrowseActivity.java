package com.example.smartbarmobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.People.LoadPeopleResult;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.PersonBuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/*
 * This class defines the behavior of the order drink screen, namely allows a user to manually
 * enter a drink to order or choose from the library of drinks displayed and passes the drink order
 * to the next screen.
 */
public class LibraryBrowseActivity extends Activity implements View.OnClickListener,
                AdapterView.OnItemClickListener, ConnectionCallbacks, OnConnectionFailedListener, ResultCallback<LoadPeopleResult> {

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

    //JSON element ids from response of php script:
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
	
	private static final String TAG = "smartbar GoogleAPiClient";
	
	private static final int STATE_DEFAULT = 0;
	private static final int STATE_SIGN_IN = 1;
	private static final int STATE_IN_PROGRESS = 2;

    /* Request code used to invoke sign in user interactions. */
    private static final int RC_SIGN_IN = 0;

    /* Client used to interact with Google APIs. */
    private GoogleApiClient mGoogleApiClient;
    
    // We use mSignInProgress to track whether user has clicked sign in.
    // mSignInProgress can be one of three values:
    //
    //       STATE_DEFAULT: The default state of the application before the user
    //                      has clicked 'sign in', or after they have clicked
    //                      'sign out'.  In this state we will not attempt to
    //                      resolve sign in errors and so will display our
    //                      Activity in a signed out state.
    //       STATE_SIGN_IN: This state indicates that the user has clicked 'sign
    //                      in', so resolve successive errors preventing sign in
    //                      until the user has successfully authorized an account
    //                      for our app.
    //   STATE_IN_PROGRESS: This state indicates that we have started an intent to
    //                      resolve an error, and so we should not start further
    //                      intents until the current intent completes.
    private int mSignInProgress;

    /* 
     * Used to store the PendingIntent most recently returned by Google Play Services until the user clicks sign in.
     */
    private PendingIntent mSignInIntent;
    
    private Person currentUser;

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

        pin = ((MyApplication)this.getApplication()).getNumber();
        
        /**
         * When we build the GoogleApiClient we specify where connected and connection failed callbacks should be returned,
         * which Google APIs our app uses and which OAuth 2.0 scopes our app requests.
         */
        mGoogleApiClient = new GoogleApiClient.Builder(this)
        		.addConnectionCallbacks(this)
        		.addOnConnectionFailedListener(this)
        		.addApi(Plus.API, Plus.PlusOptions.builder().build())
        		.addScope(Plus.SCOPE_PLUS_LOGIN)
        		.build();
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

        // user selected get pin
        if (id == R.id.action_pin) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("My Number");
            builder.setMessage(String.valueOf(pin));
            builder.setPositiveButton("OK", null);
            builder.show();
            return true;
        }

        // Reset Fingerprint
        if (id == R.id.action_resetFP) {
        	AlertDialog.Builder builder = new AlertDialog.Builder(this)
        		.setTitle("Reset Fingerprint?")
        		.setMessage("Are you sure you want to reset your fingerprint information?")
        		.setPositiveButton("Reset FP", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// User wants to reset fingerprint
						new ResetFP().execute();
					}
				})
				.setNegativeButton("Cancel", null);
        	builder.show();
        }

        // logout
        if (id == R.id.action_logout) {
			Toast.makeText(this, "Signing out...", Toast.LENGTH_SHORT).show();
			if (((MyApplication)this.getApplication()).gSignIn) {
				// Clear the default account on sign out so that Google Play services will not return an onConnected
				// callback without user interaction.
				Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
				mGoogleApiClient.disconnect();
				mGoogleApiClient.connect();
	        	
				Intent intent = new Intent(LibraryBrowseActivity.this, StartupActivity.class);
				startActivity(intent);
			} else {
				logout();
			}
        }

        return super.onOptionsItemSelected(item);
    }
    
    public void logout() {
    	((MyApplication)this.getApplication()).loggedIn = false;
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
                    if (!filteredLibrary.get(i).toUpperCase(Locale.US).startsWith(s.toString().toUpperCase())) {
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
            if (drinkOrder.toUpperCase(Locale.getDefault()).trim().equals(drinkLibrary.get(i).toUpperCase())) {
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

    // for back navigation
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
                	switch (event.getAction()) {
	                	case MotionEvent.ACTION_DOWN: break;
	                	case MotionEvent.ACTION_UP:
	                		v.performClick();
	                    	MyApplication.hideSoftKeyboard(LibraryBrowseActivity.this);
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

	
	@Override
	protected void onStart() {
		super.onStart();
		mGoogleApiClient.connect();
	}

	@Override
	protected void onStop() {
		super.onStop();
		
		if (mGoogleApiClient.isConnected()) {
			mGoogleApiClient.disconnect();
		}
	}
	
	/**
	 * onConnected is called when our Activity successfully connects to Google Play services. onConnected indicates that an account
	 * was selected on the device, that the selected account has granted any requested permissions to our app and that we were able
	 * to establish a service connection to Google Play Services.
	 */
	@Override
	public void onConnected(Bundle connectionHint) {
		Log.v(TAG, "onConnected reached");
		
		// Retrieve some profile information to personalize our app for the user
		currentUser = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
		Plus.AccountApi.getAccountName(mGoogleApiClient);
		
		Log.v(TAG, "Signed in as " + currentUser.getDisplayName());
		Plus.PeopleApi.loadVisible(mGoogleApiClient, null).setResultCallback(this);
		
		// Indicate that the sign in process is complete.
		mSignInProgress = STATE_DEFAULT;
	}
	
	public void onConnectionSuspended(int cause) {
		// Connection to Google Play Services was lost. Call connect() to attempt to re-establish the connection or get a
		// ConnectionResult that we can attempt to resolve.
		mGoogleApiClient.connect();
	}

	/**
	 * onConnectionFailed is called when our Activity could not connect to Google Play Services. onConnectionFailed indicates that
	 * the user needs to select an account, grant permissions or resolve an error in order for sign in.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// Refer to the JavaDoc for ConnectionResult to see what error codes might be returned in onConnectionFailed.
		Log.i(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = " + result.getErrorCode());

		if (result.getErrorCode() == ConnectionResult.API_UNAVAILABLE) {
			// The device's current configuration might not be supported with the requested API or a requested API or a required
			// component may not be installed.
			
		} else if (mSignInProgress != STATE_IN_PROGRESS) {
			// We do not have an intent in progress so we should store the latest error resolution intent for use when the sign
			// in button is clicked.
			mSignInIntent = result.getResolution();
			
			if (mSignInProgress == STATE_SIGN_IN) {
				// STATE_SIGN_IN indicates the user already clicked the sign in button so we should continue processing errors until
				// the user is signed in or they click cancel.
				resolveSignInError();
			}
		}
	}
	
	/**
	 * Starts an appropriate intent or dialog for user interaction to resolve the current error preventing the user from being
	 * signed in. This could be a dialog allowing the user to select an account, an activity allowing the user to consent to the
	 * permissions being requested by your app, a setting to enable device networking, etc.
	 */
	private void resolveSignInError() {
		if (mSignInIntent != null) {
			// We have an intent which will allow our user to sign in or resolve an error. For example, if the user needs to
			// select an account to sign in with, or if they need consent to the permissions your app is requesting.
			try {
				// Send the pending intent that we stored on the most recent OnConnectionFailed callback. This will allow the
				// user to resolve the error currently preventing our connection to Google Play Services.
				mSignInProgress = STATE_IN_PROGRESS;
				startIntentSenderForResult(mSignInIntent.getIntentSender(), RC_SIGN_IN, null, 0, 0, 0);
			} catch (SendIntentException e) {
				Log.i(TAG, "Sign in intent could not be sent: " + e.getLocalizedMessage());
				// The intent was cancelled before it was sent. Return to the default state and attempt to connect to get an updated ConnectionResult.
				mSignInProgress = STATE_SIGN_IN;
				mGoogleApiClient.connect();
			}
		} else {
			// Google Play Services wasn't able to provide an intent for some error types, so we show the default Google Play
			// Services error dialog which may still start an intent on our behalf if the user can resolve the issue.
			Log.v(TAG, "Unable to provide intent");
		}
	}
	
	@Override
	public void onResult(LoadPeopleResult peopleData) {
		if (peopleData.getStatus().getStatusCode() == CommonStatusCodes.SUCCESS) {
			PersonBuffer personBuffer = peopleData.getPersonBuffer();
			try {
				int count = personBuffer.getCount();
				Log.d(TAG, "mCirclesList starting");
				for (int i = 0; i < count; i++) {
					Log.d(TAG, "Display name: " + personBuffer.get(i).getDisplayName());
				}
			} finally {
				personBuffer.close();
			}
		} else {
			Log.e(TAG, "Error requesting visible circiles: " + peopleData.getStatus());
		}
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
                        ServerAccess.GET_LIB_URL, "POST", params);
                
                if (json == null) {
                	Toast.makeText(LibraryBrowseActivity.this, "Cannot connect to server. Please check internet connection.", Toast.LENGTH_SHORT).show();
                	return null;
                }

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
    
    
    /**
     * Class to reset fingerprint in database.
     * @author lamperry
     *
     */
    class ResetFP extends AsyncTask<String, String, String> {
        int success;

        @Override
        protected String doInBackground(String... args) {
        	
            try {
                Log.d("RFP", "Mid-Execute");
                String pinNum = ((MyApplication)LibraryBrowseActivity.this.getApplication()).getNumber();
                
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("pin", pinNum));
                JSONObject json = jsonParser.makeHttpRequest(
                		ServerAccess.RESET_FP_URL, "POST", params);
                
                if (json == null){
                	Toast.makeText(LibraryBrowseActivity.this, "Failure to Access Server. Check Internet Connection"
                            , Toast.LENGTH_SHORT).show();
                	return null;
                }

                // check your log for json response
                Log.d("RFP", json.toString());
                
                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("RFP", "Success String:" + json.toString());
                    return json.getString(TAG_MESSAGE);
                } else {
                    Log.d("RFP", "Failure with :" + json.getString(TAG_MESSAGE));
                    return json.getString(TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException npe) {
                npe.printStackTrace();
            }
            return null;
        }
        
        /**
         * Let user know if failure or success.
         */
        protected void onPostExecute(String file_url) {
            if (file_url != null) {
                Log.d("RFP", "Returned URL:" + file_url);
                if (success == 1) {
                	Toast.makeText(LibraryBrowseActivity.this, "Fingerprint has been reset for " + 
                			((MyApplication)LibraryBrowseActivity.this.getApplication()).myUsername, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(LibraryBrowseActivity.this, "Failure to Access Server. Check Internet Connection"
                        , Toast.LENGTH_SHORT).show();
            }
        }
    }
}
