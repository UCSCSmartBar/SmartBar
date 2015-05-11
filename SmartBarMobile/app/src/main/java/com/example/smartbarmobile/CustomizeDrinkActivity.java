package com.example.smartbarmobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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


/*
 * this class defines the behavior of the custom drink activity in which a user can select a
 * particular brand of liquor for a chosen drink
 */
public class CustomizeDrinkActivity extends Activity implements ConnectionCallbacks, OnConnectionFailedListener, ResultCallback<LoadPeopleResult> {

    // Initializations
    int numLiquors;
    String drinkOrder;
    String[] liquorString;
    ArrayList<String> liquorList = new ArrayList<>();
    ArrayList<String> liquorReturnList = new ArrayList<>();
    String pin;
    String recipe = "";

    ArrayList<Spinner> spinnerArrayList = new ArrayList<>();
    Spinner liquor1, liquor2, liquor3, liquor4, liquor5;

    String[] gin = { "Choose Gin", "Default: Bombay Sapphire", "Beefeater" };
    String[] rum = { "Choose Rum", "Default: Bacardi", "Captain Morgan" };
    String[] tequila = { "Choose Tequila", "Default: Jose Cuervo", "Milagro", "Patron" };
    String[] vodka = { "Choose Vodka", "Default: Smirnoff", "Svedka", "Absolut" };
    String[] whiskey = { "Choose Whiskey", "Default: Jack Daniels", "Jameson", "Johnny Walker" };
    String[] bitters = { "Choose Bitters", "Default: Angostura", "Peychaud's Bitters", "The Bitter Truth" };
    String[] bourbon = { "Choose Bourbon", "Default: Jim Beam", "Baker's", "Evan Williams" };

    JSONParser jsonParser = new JSONParser();       // JSON parser class

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customize_drink);

        pin = String.valueOf(((MyApplication)this.getApplication()).getNumber());

        // grab variables from previous intent
        Intent intent = getIntent();
        drinkOrder = intent.getStringExtra("drinkOrder");
        recipe = intent.getStringExtra("drinkRecipe");
        liquorList = intent.getStringArrayListExtra("liquorList");

        // display which chosen drink user is customizing
        TextView drinkOrderView = (TextView)findViewById(R.id.customize_drink);
        drinkOrderView.setText(drinkOrder);
        numLiquors = liquorList.size();
        liquorString = new String[numLiquors];

        buildSpinners();
        
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

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    // inflates the action bar items (settings bar)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_customize_drink, menu);
        return true;
    }

    // handles behavior of  each item in action bar when clicked
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

        // Logout chosen from action bar
        if (id == R.id.action_logout) {
			Toast.makeText(this, "Signing out...", Toast.LENGTH_SHORT).show();
			if (((MyApplication)this.getApplication()).gSignIn) {
				// Clear the default account on sign out so that Google Play services will not return an onConnected
				// callback without user interaction.
				Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
				mGoogleApiClient.disconnect();
				mGoogleApiClient.connect();
	        	
				Intent intent = new Intent(CustomizeDrinkActivity.this, StartupActivity.class);
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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, ConfirmationActivity.class);
        intent.putExtra("drinkOrder", drinkOrder);
        if (liquorReturnList.size() == 0) {
            intent.putStringArrayListExtra("liquorReturnList", null);
            intent.putExtra("drinkRecipe", recipe);
            Toast.makeText(this, "You didn't specify liquors!", Toast.LENGTH_SHORT).show();
        } else {
            intent.putStringArrayListExtra("liquorReturnList", liquorReturnList);
        }
        startActivity(intent);
    }

    /*
     * Associates java spinners with (xml spinners) resource id in layout and builds each specific
     * one for each type of liquor
     */
    private void buildSpinners() {
        // assign spinner with resource id
        liquor1 = (Spinner)findViewById(R.id.liquor1);
        liquor2 = (Spinner)findViewById(R.id.liquor2);
        liquor3 = (Spinner)findViewById(R.id.liquor3);
        liquor4 = (Spinner)findViewById(R.id.liquor4);
        liquor5 = (Spinner)findViewById(R.id.liquor5);

        // create array of spinners to facilitate associations
        spinnerArrayList.add(liquor1);
        spinnerArrayList.add(liquor2);
        spinnerArrayList.add(liquor3);
        spinnerArrayList.add(liquor4);
        spinnerArrayList.add(liquor5);

        // build specific spinners for each type of liquor
        for (int i = 0; i < numLiquors; i++) {
            liquorString[i] = liquorList.get(i);
            assignSpinner(i + 1, liquorString[i]);
        }
    }

    // this method populates the spinner with the strings of the different options of each liquor
    private void assignSpinner(int i, String s) {
        switch (s) {
            case "Bitters":
                ArrayAdapter<String> bittersAdapter;
                bittersAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, bitters);
                bittersAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerArrayList.get(i-1).setAdapter(bittersAdapter);
                spinnerArrayList.get(i-1).setVisibility(View.VISIBLE);
                break;
            case "Bourbon":
                ArrayAdapter<String> bourbonAdapter;
                bourbonAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, bourbon);
                bourbonAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerArrayList.get(i-1).setAdapter(bourbonAdapter);
                spinnerArrayList.get(i-1).setVisibility(View.VISIBLE);
                break;
            case "Gin":
                ArrayAdapter<String> ginAdapter;
                ginAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, gin);
                ginAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerArrayList.get(i-1).setAdapter(ginAdapter);
                spinnerArrayList.get(i-1).setVisibility(View.VISIBLE);
                break;
            case "Rum":
                ArrayAdapter<String> rumAdapter;
                rumAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, rum);
                rumAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerArrayList.get(i-1).setAdapter(rumAdapter);
                spinnerArrayList.get(i-1).setVisibility(View.VISIBLE);
                break;
            case "Tequila":
                ArrayAdapter<String> tequilaAdapter;
                tequilaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, tequila);
                tequilaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerArrayList.get(i-1).setAdapter(tequilaAdapter);
                spinnerArrayList.get(i-1).setVisibility(View.VISIBLE);
                break;
            case "Vodka":
                ArrayAdapter<String> vodkaAdapter;
                vodkaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, vodka);
                vodkaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerArrayList.get(i-1).setAdapter(vodkaAdapter);
                spinnerArrayList.get(i-1).setVisibility(View.VISIBLE);
                break;
            case "Vermouth":
                ArrayAdapter<String> vermouthAdapter;
                vermouthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, vodka);
                vermouthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerArrayList.get(i-1).setAdapter(vermouthAdapter);
                spinnerArrayList.get(i-1).setVisibility(View.VISIBLE);
                break;
            case "Whiskey":
                ArrayAdapter<String> whiskeyAdapter;
                whiskeyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, whiskey);
                whiskeyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerArrayList.get(i-1).setAdapter(whiskeyAdapter);
                spinnerArrayList.get(i-1).setVisibility(View.VISIBLE);
                break;
        }
    }

    /*
     * method gets called when user clicks the customize button on this screen
     * grabs the chosen items from each spinner and adds the appropriate value to the liquor return
     * list which gets parsed through in Confirmation Activity
     */
    public void customizeToConfirmation(View view) {
        int i = 0;
        boolean choose = false;
        while (i < numLiquors) {
            switch (i) {
                // if starts with 'choose' then automatically grab item at position 1 which holds
                // the default liquor brand in all liquor arrays
                case 0:
                    if (liquor1.getSelectedItem().toString().startsWith("Choose")) {
                        choose = true;
                        liquorReturnList.add(liquor1.getItemAtPosition(1).toString());
                    } else {
                        liquorReturnList.add(liquor1.getSelectedItem().toString());
                    }
                    break;
                case 1:
                    if (liquor2.getSelectedItem().toString().startsWith("Choose")) {
                        choose = true;
                        liquorReturnList.add(liquor2.getItemAtPosition(1).toString());
                    } else {
                        liquorReturnList.add(liquor2.getSelectedItem().toString());
                    }
                    break;
                case 2:
                    if (liquor3.getSelectedItem().toString().startsWith("Choose")) {
                        choose = true;
                        liquorReturnList.add(liquor3.getItemAtPosition(1).toString());
                    } else {
                        liquorReturnList.add(liquor3.getSelectedItem().toString());
                    }
                    break;
                case 3:
                    if (liquor4.getSelectedItem().toString().startsWith("Choose")) {
                        choose = true;
                        liquorReturnList.add(liquor4.getItemAtPosition(1).toString());
                    } else {
                        liquorReturnList.add(liquor4.getSelectedItem().toString());
                    }
                    break;
                case 4:
                    if (liquor5.getSelectedItem().toString().startsWith("Choose")) {
                        choose = true;
                        liquorReturnList.add(liquor5.getItemAtPosition(1).toString());
                    } else {
                        liquorReturnList.add(liquor5.getSelectedItem().toString());
                    }
                    break;
                default:
            }
            i++;
        }

        if (choose) {
            Toast.makeText(this, "You've failed to specify the liquor brand for one or more " +
                    "liquors in your drink. The defaults will be applied.", Toast.LENGTH_SHORT).show();
        }

        // send user back to confirmation activity with the appropriate drink order and liquor
        // return list
        Intent intent = new Intent(this, ConfirmationActivity.class);
        intent.putExtra("drinkOrder", drinkOrder);
        intent.putExtra("drinkRecipe", recipe);
        intent.putStringArrayListExtra("liquorReturnList", liquorReturnList);
        startActivity(intent);
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
                String pinNum = ((MyApplication)CustomizeDrinkActivity.this.getApplication()).getNumber();
                
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("pin", pinNum));
                JSONObject json = jsonParser.makeHttpRequest(
                		ServerAccess.RESET_FP_URL, "POST", params);
                
                if (json == null){
                	Toast.makeText(CustomizeDrinkActivity.this, "Failure to Access Server. Check Internet Connection"
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
                	Toast.makeText(CustomizeDrinkActivity.this, "Fingerprint has been reset for " + 
                			((MyApplication)CustomizeDrinkActivity.this.getApplication()).myUsername, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(CustomizeDrinkActivity.this, "Failure to Access Server. Check Internet Connection"
                        , Toast.LENGTH_SHORT).show();
            }
        }
    }
}
