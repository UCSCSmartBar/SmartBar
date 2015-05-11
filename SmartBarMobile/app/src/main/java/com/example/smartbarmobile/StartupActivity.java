package com.example.smartbarmobile;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.plus.People.LoadPeopleResult;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.PersonBuffer;
import com.google.android.gms.plus.model.people.Person;

/**
 * This class defines the behavior for the startup screen of the mobile app.
 * Directs user to create account, login via SmartBar, login via Google.
 */
public class StartupActivity extends Activity implements ConnectionCallbacks, OnConnectionFailedListener,
		OnClickListener, ResultCallback<LoadPeopleResult> {

	boolean noAge;
	boolean noGender;

    JSONParser jsonParser = new JSONParser();
    private ProgressDialog pDialog;
	
	private static final String TAG = "GoogleAPiClient";
	
	private static final int STATE_DEFAULT = 0;
	private static final int STATE_SIGN_IN = 1;
	private static final int STATE_IN_PROGRESS = 2;
	
	private static final String SAVED_PROGRESS = "Sign in progress";

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
     * Used to store the PendingIntent most recently returned by Google Play Services until the user
     * clicks sign in.
     */
    private PendingIntent mSignInIntent;

    private SignInButton mSignInButton;
    
    private Person currentUser;
    private String mEmail;
    
    boolean prevIntent, tooYoung;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        // initializes app wide global boolean to false
		new MyApplication();
        ((MyApplication)this.getApplication()).setLoggedIn(false);	

        mSignInButton = (SignInButton) findViewById(R.id.sign_in_button);

        mSignInButton.setOnClickListener(this);

        if (savedInstanceState != null) {
        	mSignInProgress = savedInstanceState.getInt(SAVED_PROGRESS, STATE_DEFAULT);
        }
        
        /**
         * When we build the GoogleApiClient we specify where connected and connection failed callbacks
         * should be returned, which Google APIs our app uses and which OAuth 2.0 scopes our app requests.
         */
        mGoogleApiClient = new GoogleApiClient.Builder(this)
        		.addConnectionCallbacks(this)
        		.addOnConnectionFailedListener(this)
        		.addApi(Plus.API, Plus.PlusOptions.builder().build())
        		.addScope(Plus.SCOPE_PLUS_LOGIN)
        		.build();
        
        Intent intent = getIntent();
        tooYoung = intent.getBooleanExtra("tooYoung", true);
        prevIntent = intent.getBooleanExtra("prevIntent", false);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
    @Override
    public void onBackPressed() {
    }

    /** Start Smartbar Login screen **/
    public void startupToLogin(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
		finish();
        startActivity(intent);
    }

    /** Start Create Smartbar account screen **/
    public void startupToNewUser(View view) {
        Intent intent = new Intent(this, NewUserActivity.class);
		finish();
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

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(SAVED_PROGRESS, mSignInProgress);
	}

	@Override
	public void onClick(View view) {
		if (!mGoogleApiClient.isConnecting()) {
			// We only process button clicks when GoogleApiClient is not transition-ing between
			// connected and not connected.
			switch(view.getId()) {
				case R.id.sign_in_button:
					Toast.makeText(this, "Signing in...", Toast.LENGTH_SHORT).show();
				    resolveSignInError();
				    break;
		        default:
		        	break;
			}
		}
	}
	
	/**
	 * onConnected is called when our Activity successfully connects to Google Play services.
	 * onConnected indicates that an account was selected on the device, that the selected account
	 * has granted any requested permissions to our app and that we were able to establish a service
	 * connection to Google Play Services.
	 */
	@Override
	public void onConnected(Bundle connectionHint) {
		Log.v(TAG, "onConnected reached");
		
		if (prevIntent && tooYoung) {
			if (mGoogleApiClient.isConnected()) {
				Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
				mGoogleApiClient.disconnect();
				mGoogleApiClient.connect();

//				if (Plus.AccountApi.getAccountName(mGoogleApiClient) == ((MyApplication)this.getApplication()).myEmail) {
//					Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
//					mGoogleApiClient.disconnect();
//					mGoogleApiClient.connect();
//				} else {
//					tooYoung = false;
//				}
			}
			return;
		}

	    // Update the user interface to reflect that the user is signed in.
	    mSignInButton.setEnabled(false);
		
		// Retrieve some profile information to personalize our app for the user
		currentUser = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
		mEmail = Plus.AccountApi.getAccountName(mGoogleApiClient);
		
		Log.v(TAG, "Signed in as " + currentUser.getDisplayName());
		Plus.PeopleApi.loadVisible(mGoogleApiClient, null).setResultCallback(this);
		
		// Indicate that the sign in process is complete.
		mSignInProgress = STATE_DEFAULT;
		
		// Proceed to register user with smart bar
		configureParams();
		
		new AttemptLogin().execute();
	}
	
	private void configureParams() {
		
		MyApplication.myUsername = mEmail;
		((MyApplication)this.getApplication()).myPassword = mEmail;
		((MyApplication)this.getApplication()).myEmail = mEmail;

		// if user hasAgeRange(), verify over 21
		if (currentUser.hasAgeRange()) {
			int minAge = currentUser.getAgeRange().getMin();
			if (minAge < 21) {
				Toast.makeText(this, "Sorry, you must be 21 to use the SmartBar!", Toast.LENGTH_LONG).show();
				return;
			}
			((MyApplication)this.getApplication()).myAge = String.valueOf(minAge);
			noAge = false;
		} else {
			noAge = true;
		}
		
		// If currentUser hasGender(), assign correct gender
		if (currentUser.hasGender()) {
			switch (currentUser.getGender()) {
				case 0: ((MyApplication)this.getApplication()).myGender = "Male"; break;
				case 1: ((MyApplication)this.getApplication()).myGender = "Female"; break;
				case 2: ((MyApplication)this.getApplication()).myGender = "Other"; break;
				default: break;
			}
			noGender = false;
		} else {
			noGender = true;
		}
	}

	/**
	 * onConnectionFailed is called when our Activity could not connect to Google Play Services.
	 * onConnectionFailed indicates that the user needs to select an account, grant permissions or
	 * resolve an error in order for sign in.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// Refer to the JavaDoc for ConnectionResult to see what error codes might be returned in onConnectionFailed.
		Log.i(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = " + result.getErrorCode());

		if (result.getErrorCode() == ConnectionResult.API_UNAVAILABLE) {
			// The device's current configuration might not be supported with the requested API or a
			// requested API or a required component may not be installed.
			
		} else if (mSignInProgress != STATE_IN_PROGRESS) {
			// We do not have an intent in progress so we should store the latest error resolution
			// intent for use when the sign in button is clicked.
			mSignInIntent = result.getResolution();
			
			if (mSignInProgress == STATE_SIGN_IN) {
				// STATE_SIGN_IN indicates the user already clicked the sign in button so we should
				// continue processing errors until the user is signed in or they click cancel.
				resolveSignInError();
			}
		}
		onSignedOut();
	}
	
	/**
	 * Starts an appropriate intent or dialog for user interaction to resolve the current error preventing
	 * the user from being signed in. This could be a dialog allowing the user to select an account,
	 * an activity allowing the user to consent to the permissions being requested by your app, a
	 * setting to enable device networking, etc.
	 */
	private void resolveSignInError() {
		if (mSignInIntent != null) {
			// We have an intent which will allow our user to sign in or resolve an error. For example,
			// if the user needs to select an account to sign in with, or if they need consent to
			// the permissions your app is requesting.
			try {
				// Send the pending intent that we stored on the most recent OnConnectionFailed callback.
				// This will allow the user to resolve the error currently preventing our connection
				// to Google Play Services.
				mSignInProgress = STATE_IN_PROGRESS;
				startIntentSenderForResult(mSignInIntent.getIntentSender(), RC_SIGN_IN, null, 0, 0, 0);
			} catch (SendIntentException e) {
				Log.i(TAG, "Sign in intent could not be sent: " + e.getLocalizedMessage());
				// The intent was cancelled before it was sent. Return to the default state and attempt
				// to connect to get an updated ConnectionResult.
				mSignInProgress = STATE_SIGN_IN;
				mGoogleApiClient.connect();
			}
		} else {
			// Google Play Services wasn't able to provide an intent for some error types, so we show
			// the default Google Play Services error dialog which may still start an intent on our
			// behalf if the user can resolve the issue.
			Log.v(TAG, "Unable to provide intent");
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case RC_SIGN_IN:
				if (resultCode == RESULT_OK) {
					// If the error resolution was successful we should continue processing errors.
					mSignInProgress = STATE_SIGN_IN;
				} else {
					// If the error resolution was not successful or the user cancelled, we should
					// stop processing errors.
					mSignInProgress = STATE_DEFAULT;
				}
				if (!mGoogleApiClient.isConnecting()) {
					// If Google Play Services resolved the issue with a dialog then onStart is not
					// called so we need to re-attempt
					// connection here.
					mGoogleApiClient.connect();
				}
				break;
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
	
	private void onSignedOut() {
		// Update the UI to reflect that the user signed out
	    mSignInButton.setEnabled(true);
	}
	
	public void onConnectionSuspended(int cause) {
		// Connection to Google Play Services was lost. Call connect() to attempt to re-establish
		// the connection or get a ConnectionResult that we can attempt to resolve.
		mGoogleApiClient.connect();
	}


	/*
     * Class to attempt login, call PHP script to query database
     */
	class AttemptLogin extends AsyncTask<String, String, String> {

		int success;

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(StartupActivity.this);
			pDialog.setMessage("Attempting login...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}

		@Override
		protected String doInBackground(String... args) {

			String username = MyApplication.myUsername;
			String password = ((MyApplication)StartupActivity.this.getApplication()).myPassword;
			try {
				/** Building Parameters **/
				List<NameValuePair> params = new ArrayList<>();
				params.add(new BasicNameValuePair("user_name", username));
				params.add(new BasicNameValuePair("user_password", password));

				Log.d("request!", "starting");
				/** Getting product details by making HTTP request **/
				JSONObject json = jsonParser.makeHttpRequest(
						ServerAccess.LOGIN_URL, "POST", params);

				if (json == null) {
					Toast.makeText(StartupActivity.this, "Cannot connect to server. Please check internet connection.",
							Toast.LENGTH_SHORT).show();
					return null;
				}

				// check your log for json response
				Log.d("Login attempt", json.toString());

				// json success tag
				success = json.getInt(ServerAccess.TAG_SUCCESS);
				if (success == 1) {
					Log.d("Login Successful!", json.toString());
					((MyApplication)StartupActivity.this.getApplication()).setLoggedIn(true);
					return json.getString(ServerAccess.TAG_MESSAGE);
				}else{
					Log.d("Login Failure!", json.getString(ServerAccess.TAG_MESSAGE));
					return json.getString(ServerAccess.TAG_MESSAGE);
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
				pDialog.dismiss();
				if (success == 1) {
					Toast.makeText(StartupActivity.this, file_url, Toast.LENGTH_SHORT).show();
					((MyApplication)StartupActivity.this.getApplication()).gSignIn = true;

					/** Check if user has Braintree customer account **/
					new FindBraintreeCust().execute();

				} else {
					Intent missingParams = new Intent(StartupActivity.this, MissingParamsActivity.class);
					missingParams.putExtra("noAge", noAge);
					missingParams.putExtra("noGender", noGender);
					finish();
					startActivity(missingParams);
				}
			}
		}
	}


	/*
     * Check if user has Braintree customer account already, if not, make one.
     */
	class FindBraintreeCust extends AsyncTask<String, String, String> {

		int success;

		@Override
		protected String doInBackground(String... args) {

			Log.d("FindBraintreeCust", "starting...");
			String phone = MyApplication.myPin;
			try {
				//** Build parameters **/
				List<NameValuePair> params = new ArrayList<>();
				params.add(new BasicNameValuePair("phone", phone));

				Log.d("request!", "starting");

				/** Getting product details by making HTTP request **/
				JSONObject json = jsonParser.makeHttpRequest(
						ServerAccess.FIND_CUST_URL, "POST", params);

				if (json == null) {
					Toast.makeText(StartupActivity.this, "Cannot connect to server. Please check internet connection.",
							Toast.LENGTH_SHORT).show();
					return null;
				}

				/** JSON response returned **/
				Log.d("FindBraintreeCust", "returned");

				/** JSON success tag **/
				success = json.getInt(ServerAccess.TAG_SUCCESS);

				if (success == 1) {
					Log.d("FindBraintreeCust", json.toString());
					((MyApplication)StartupActivity.this.getApplication()).setLoggedIn(true);
					return json.getString(ServerAccess.TAG_MESSAGE);
				}else{
					Log.d("FindBraintreeCust", json.getString(ServerAccess.TAG_MESSAGE));
					return json.getString(ServerAccess.TAG_MESSAGE);
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
				if (success == 1) {
					Intent readyToOrder = new Intent(StartupActivity.this, ReadyToOrderActivity.class);
					finish();
					startActivity(readyToOrder);
				} else {
					/** Braintree Customer ID not found; user must enter payment info **/
					Intent welcome = new Intent(StartupActivity.this, PaymentActivity.class);
					finish();
					startActivity(welcome);
				}
			}
		}
	}
}
