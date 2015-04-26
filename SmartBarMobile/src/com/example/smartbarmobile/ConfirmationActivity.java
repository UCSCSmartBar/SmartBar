package com.example.smartbarmobile;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.People.LoadPeopleResult;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.PersonBuffer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


/*
 * This class defines the behavior of the confirmation screen, namely entering drink and pin
 * information into the database
 */
public class ConfirmationActivity extends Activity implements ConnectionCallbacks, OnConnectionFailedListener, ResultCallback<LoadPeopleResult> {

    // Initializations
    String drinkOrder;
    TextView displayDrink;
    String pin;
    ArrayList<String> liquorReturnList;
    String finalRecipe = "";
    String sendRecipe = "";
    String myBAC;

    JSONParser jsonParser = new JSONParser();       // JSON parser class

    //PHP login script:
    //UCSC Smartbar Server:
    private static final String ADD_DRINK_URL = "http://www.smartbarproject.com/addDrink.php";

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
        setContentView(R.layout.activity_confirmation);

        // grab the drink order from either Library Browse or Customize Drink
        // if previous intent was Customize Drink grab the specified liquors
        // display data
        Intent intent = getIntent();
        drinkOrder = intent.getStringExtra("drinkOrder");
        finalRecipe = intent.getStringExtra("drinkRecipe");
        sendRecipe = finalRecipe;

        liquorReturnList = intent.getStringArrayListExtra("liquorReturnList");
        displayDrink = (TextView)findViewById(R.id.drinkOrder);
        displayDrink.setText(drinkOrder);
        pin = ((MyApplication)this.getApplication()).getNumber();

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
            builder.show();
            return true;
        }

        // Logout chosen from action bar
        if (id == R.id.action_logout) {
			Toast.makeText(this, "Signing out...", Toast.LENGTH_SHORT).show();
			if (((MyApplication)this.getApplication()).gSignIn) {
				// Clear the default account on sign out so that Google Play services will not return an onConnected
				// callback without user interaction.
				Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
				mGoogleApiClient.disconnect();
				mGoogleApiClient.connect();
	        	
				Intent intent = new Intent(ConfirmationActivity.this, StartupActivity.class);
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

    // for back navigation
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, LibraryBrowseActivity.class);
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

    /*
     * method gets called when customize drink button clicked
     * allows for lowercase spelling as well as leading and trailing whitespace on user input
     * builds array of liquors present in the chosen drink and sends to customize drink activity
     */
    public void customizeDrink(View view) {
        if (finalRecipe == null) {
            Log.e("Customize Error", "Recipe null.");
            return;
        } else if (drinkOrder.equals("Carbonated Orange Gatorade") || drinkOrder.equals("Orange Gatorade")) {
            Toast.makeText(this, "Sorry, there is no liquor in this drink to customize!", Toast.LENGTH_SHORT).show();
            return;
        }
        ArrayList<String> liquorList;
        liquorList = buildLiquorList(finalRecipe);

        
        // start customize drink screen and pass activity proper drink order data
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
                case "GN":
                    liquors.add("Gin");
                    break;
                case "RM":
                    liquors.add("Rum");
                    break;
                case "TQ":
                    liquors.add("Tequila");
                    break;
                case "VO":
                    liquors.add("Vodka");
                    break;
                case "WH":
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


    // class to query database and add drink order/pin number, extends AsyncTask so that query can
    // be background thread
    class DrinkOrder extends AsyncTask<String, String, String> {

        boolean failure = false;

        @Override
        protected String doInBackground(String... args) {
            // Check for success tag
            int success;
            pin = ((MyApplication)ConfirmationActivity.this.getApplication()).getNumber();
            String username = ((MyApplication)ConfirmationActivity.this.getApplication()).myUsername;
            Log.i("Username", username);
            String drink = drinkOrder;
            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("username", username));
                params.add(new BasicNameValuePair("pin", pin));
                params.add(new BasicNameValuePair("drink", drink));
                params.add(new BasicNameValuePair("recipe", sendRecipe));

                Log.d("request!", "starting");

                //Posting user data to script
                JSONObject json = jsonParser.makeHttpRequest(
                        ADD_DRINK_URL, "POST", params);
                
                if (json == null) {
                	Toast.makeText(ConfirmationActivity.this, "Cannot connect to server. Please check internet connection.", Toast.LENGTH_SHORT).show();
                	return null;
                }

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

        /**
         * After completing background task display any notifications from system.
         * **/
        protected void onPostExecute(String file_url) {
            if (file_url != null){
                Toast.makeText(ConfirmationActivity.this, file_url, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
