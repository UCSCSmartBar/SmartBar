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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


/**
 * This class defines the behavior of the confirmation screen. It displays the user's current drink
 * choice and requires confirmation before adding the drink to the queue.
 */
public class ConfirmationActivity extends Activity implements ConnectionCallbacks,
        OnConnectionFailedListener, ResultCallback<LoadPeopleResult> {

    String drinkOrder;
    TextView displayDrink;
    String pin, pinDisp, tempCountry, tempArea, tempNum3, tempNum4;
    ArrayList<String> liquorReturnList;
    String finalRecipe = "";
    String sendRecipe = "";
    String username;

    JSONParser jsonParser = new JSONParser();
	
	private static final String TAG = "GoogleAPiClient";
	
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

    /**
     * Used to store the PendingIntent most recently returned by Google Play Services until the user
     * clicks sign in.
     */
    private PendingIntent mSignInIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        /* Grab the drink order from Library Browse. */
        Intent intent = getIntent();
        drinkOrder = intent.getStringExtra("drinkOrder");
        finalRecipe = intent.getStringExtra("drinkRecipe");
        sendRecipe = finalRecipe;

        liquorReturnList = intent.getStringArrayListExtra("liquorReturnList");
        displayDrink = (TextView)findViewById(R.id.drinkOrder);
        displayDrink.setText(drinkOrder);
        pin = MyApplication.myPin;

        new FindUser().execute();
        MyApplication.myUsername = username;
        
        /**
         * When we build the GoogleApiClient we specify where connected and connection failed
         * callbacks should be returned, which Google APIs our app uses and which OAuth 2.0 scopes
         * our app requests.
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Inflate the menu; this adds items to the action bar if it is present. */
        getMenuInflater().inflate(R.menu.menu_confirmation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /**
         * Handle action bar item clicks here. The action bar will automatically handle clicks on
         * the Home/Up button, so long as you specify a parent activity in AndroidManifest.xml.
         */
        int id = item.getItemId();

        /* Action bar display pin sequence */
        if (id == R.id.action_pin) {
            pin = MyApplication.myPin + '1';
            tempCountry = pin.substring(0,1);
            tempArea = pin.substring(1,4);
            tempNum3 = pin.substring(4,7);
            tempNum4 = pin.substring(7,11);
            pinDisp = tempCountry + ' ' + '(' + tempArea + ')' + ' ' + tempNum3 + '-' + tempNum4;

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("My Number");
            builder.setMessage(pinDisp);
            builder.setPositiveButton("OK", null);
            builder.show();
            return true;
        }

        /* Action bar request uber sequence */
        if (id == R.id.action_uber) {
            PackageManager pm = this.getPackageManager();
            try {
        	/* Try for installed app */
                Intent uber = pm.getLaunchIntentForPackage("com.ubercab");
                if (uber == null) {
                    throw new PackageManager.NameNotFoundException();
                }
                uber.addCategory(Intent.CATEGORY_LAUNCHER);
                this.startActivity(uber);
            } catch (PackageManager.NameNotFoundException e) {
            /* No Uber app! Open Google Play Store. */
                Intent playStore = new Intent(android.content.Intent.ACTION_VIEW);
                playStore.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.ubercab"));
                startActivity(playStore);
            }
        }

        /* Action bar request lyft sequence */
        if (id == R.id.action_lyft) {
            PackageManager pm = this.getPackageManager();
            try {
            /* Try for installed app. */
                Intent uber = pm.getLaunchIntentForPackage("me.lyft.android");
                if (uber == null) {
                    throw new PackageManager.NameNotFoundException();
                }
                uber.addCategory(Intent.CATEGORY_LAUNCHER);
                this.startActivity(uber);
            } catch (PackageManager.NameNotFoundException e) {
        	/* No Uber app! Open Google Play Store. */
                Intent playStore = new Intent(android.content.Intent.ACTION_VIEW);
                playStore.setData(Uri.parse("https://play.google.com/store/apps/details?id=me.lyft.android"));
                startActivity(playStore);
            }
        }

        /* Action bar reset fingerprint sequence */
        if (id == R.id.action_resetFP) {
        	AlertDialog.Builder builder = new AlertDialog.Builder(this)
        		.setTitle("Reset Fingerprint?")
        		.setMessage("Are you sure you want to reset your fingerprint information?")
        		.setPositiveButton("Reset FP", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						/* User confirmed reset */
						new ResetFP().execute();
					}
				})
				.setNegativeButton("Cancel", null);
        	builder.show();
        }

        /* Action bar logout sequence */
        if (id == R.id.action_logout) {
			Toast.makeText(this, "Signing out...", Toast.LENGTH_SHORT).show();
			if (((MyApplication)this.getApplication()).gSignIn) {
                /**
                 * Clear the default account on sign out so that Google Play Services will not
                 * return an onConnected callback without user interaction.
                 */
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

    /* Logout method for non-Google+ sign in */
    public void logout() {
    	((MyApplication)this.getApplication()).loggedIn = false;
    	Intent intent = new Intent(this, StartupActivity.class);
    	startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, LibraryBrowseActivity.class);
        startActivity(intent);
    }

    /* Directs user back to drink library screen */
    public void confirmationToLibraryBrowse(View view) {
        Intent intent = new Intent(this, LibraryBrowseActivity.class);
        startActivity(intent);
    }

    /* Directs user to drink ordered screen */
    public void confirmationToDrinkOrdered(View view) {
        /**
         * Check if the user has a drink on the queue already. If not, send new order. If they do,
         * then display a dialog to confirm the drink order change.
         */
        new CheckQueue().execute();
    }

	@Override
	protected void onStart() {
		super.onStart();
		mGoogleApiClient.connect();
        new FindUser().execute();
	}

	@Override
	protected void onStop() {
		super.onStop();
		
		if (mGoogleApiClient.isConnected()) {
			mGoogleApiClient.disconnect();
		}
	}

    @Override
    protected void onResume() {
        super.onResume();
        new FindUser().execute();
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

        /* Retrieve some profile information to personalize our app for the user */
        Person currentUser = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
		Plus.AccountApi.getAccountName(mGoogleApiClient);
		
		Log.v(TAG, "Signed in as " + currentUser.getDisplayName());
		Plus.PeopleApi.loadVisible(mGoogleApiClient, null).setResultCallback(this);

        /* Indicate that the sign in process is complete. */
		mSignInProgress = STATE_DEFAULT;
	}
	
	public void onConnectionSuspended(int cause) {
        /**
         * Connection to Google Play Services was lost. Call connect() to attempt to re-establish
         * the connection or get a ConnectionResult that we can attempt to resolve.
         */
		mGoogleApiClient.connect();
	}

	/**
	 * onConnectionFailed is called when our Activity could not connect to Google Play Services.
     * onConnectionFailed indicates that the user needs to select an account, grant permissions or
     * resolve an error in order for sign in.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult result) {
        /**
         * Refer to the JavaDoc for the ConnectionResult to see what error codes might be returned
         * in onConnectionFailed.
         */
		Log.i(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = " + result.getErrorCode());

		if (result.getErrorCode() == ConnectionResult.API_UNAVAILABLE) {
            /**
             * The device's current configuration might not be supported with the requested API or
             * a required component may not be installed.
             */
			Toast.makeText(this, "The device's current configuration might not be supported.",
                    Toast.LENGTH_SHORT).show();
		} else if (mSignInProgress != STATE_IN_PROGRESS) {
            /**
             * We do not have an intent in progress so we should store the latest error resolution
             * intent for use when the sign in button is clicked.
             */
			mSignInIntent = result.getResolution();
			
			if (mSignInProgress == STATE_SIGN_IN) {
                /**
                 * STATE_SIGN_IN indicates the user already clicked the sign in button so we should
                 * continue processing errors until the user is signed in or they click cancel.
                 */
				resolveSignInError();
			}
		}
	}
	
	/**
	 * Starts an appropriate intent or dialog for user interaction to resolve the current error
     * preventing the user from being signed in. This could be a dialog allowing the user to select
     * an account, an activity allowing the user to consent to the permissions being requested by
     * your app, a setting to enable device networking, etc.
	 */
	private void resolveSignInError() {
		if (mSignInIntent != null) {
            /**
             * We have an intent which will allow our user to sign in or resolve an error. For example,
             * if the user needs to select an account to sign in with, or if they need consent to the
             * permissions your app is requesting.
             */
			try {
                /**
                 * Send the pending intent that we stored on the most recent onConnectionFailed
                 * callback. This will allow the user to resolve the error currently preventing our
                 * connection to Google Play Services.
                 */
				mSignInProgress = STATE_IN_PROGRESS;
				startIntentSenderForResult(mSignInIntent.getIntentSender(), RC_SIGN_IN, null, 0, 0, 0);
			} catch (SendIntentException e) {
				Log.i(TAG, "Sign in intent could not be sent: " + e.getLocalizedMessage());
                /**
                 * The intent was cancelled before it was sent. Return to the default state and
                 * attempt to connect to get an updated ConnectionResult.
                 */
				mSignInProgress = STATE_SIGN_IN;
				mGoogleApiClient.connect();
			}
		} else {
            /**
             * Google Play Services wasn't able to provide an intent for some error types, so we
             * show the default Google Play Services error dialog which may still start an intent on
             * our behalf if the user can resolve the issue.
             */
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
			Log.e(TAG, "Error requesting visible circles: " + peopleData.getStatus());
		}
	}


    /**
     * This class gets called when a user confirms their drink order. It calls a PHP script which
     * checks to see if the user already has a drink on the queue. If they do, display a dialog to
     * confirm the new drink order. If they do not, add the drink.
     */
    class CheckQueue extends AsyncTask<String, String, String> {

        boolean failure = false;
        int success;

        @Override
        protected String doInBackground(String... args) {

            pin = MyApplication.myPin;

            try {
                /* Building Parameters */
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("pin", pin));

                Log.d("request!", "starting");

                /* Getting product details by making an HTTP request */
                JSONObject json = jsonParser.makeHttpRequest(
                        ServerAccess.CHECK_QUEUE_URL, "POST", params);

                if (json == null) {
                    Toast.makeText(ConfirmationActivity.this,
                            "Cannot connect to server. Please check internet connection.",
                            Toast.LENGTH_SHORT).show();
                    return null;
                }

                /* Check the logcat for full JSON response */
                Log.d("Checking queue...", json.toString());

                /* JSON success tag */
                success = json.getInt(ServerAccess.TAG_SUCCESS);

                if (success == 1) {
                    Log.d("Drink already on queue.", json.toString());
                    return json.getString(ServerAccess.TAG_MESSAGE);
                }else{
                    failure = true;
                    Log.d("No current drink.", json.getString(ServerAccess.TAG_MESSAGE));
                    return json.getString(ServerAccess.TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        /* After completing background task display any notifications from system to the user. */
        protected void onPostExecute(final String file_url) {
            if (file_url != null){
                if (success == 1) {
                    if (file_url.equals("")) {
                    /* No drink on queue. Add the drink. */
                        new DrinkOrder().execute();
                    } else {
                        /** Success means a drink was found already on the queue. */
                        AlertDialog.Builder builder = new AlertDialog.Builder(ConfirmationActivity.this);
                        builder.setTitle("But Wait!");
                        builder.setMessage("You seem to have a drink order on the queue already. Would you " +
                                "like to override your order: \n\n" + file_url);
                        builder.setPositiveButton("Override", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                new DrinkOrder().execute();
                            }
                        });
                        builder.setNegativeButton("Keep Current", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int ids) {
                                Toast.makeText(ConfirmationActivity.this, "You got it! Smartbar is ready" +
                                        "whenever you are to retrieve your " + file_url, Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(ConfirmationActivity.this, DrinkOrderedActivity.class);
                                finish();
                                startActivity(intent);
                            }
                        });
                        builder.show();
                    }
                }
            }
        }
    }


    /** 
     * This class gets called when a user confirms their drink order. It calls a PHP script which
     * adds a drink order to the queue under the current users account.
     */
    class DrinkOrder extends AsyncTask<String, String, String> {

        boolean failure = false;
        int success;

        @Override
        protected String doInBackground(String... args) {

            pin = MyApplication.myPin;
            Log.i("Username", username);
            String drink = drinkOrder;

            try {
                /* Building Parameters */
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("username", username));
                params.add(new BasicNameValuePair("pin", pin));
                params.add(new BasicNameValuePair("drink", drink));
                params.add(new BasicNameValuePair("recipe", sendRecipe));

                Log.d("request!", "starting");

                /* Getting product details by making an HTTP request */
                JSONObject json = jsonParser.makeHttpRequest(
                        ServerAccess.ADD_DRINK_URL, "POST", params);
                
                if (json == null) {
                	Toast.makeText(ConfirmationActivity.this,
                            "Cannot connect to server. Please check internet connection.",
                            Toast.LENGTH_SHORT).show();
                	return null;
                }

                /* Check the logcat for full JSON response */
                Log.d("Sending Drink Order...", json.toString());

                /* JSON success tag */
                success = json.getInt(ServerAccess.TAG_SUCCESS);

                if (success == 1) {
                    Log.d("Drink Sent!", json.toString());
                    return json.getString(ServerAccess.TAG_MESSAGE);
                }else{
                    failure = true;
                    Log.d("Drink Send Failure", json.getString(ServerAccess.TAG_MESSAGE));
                    return json.getString(ServerAccess.TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        /* After completing background task display any notifications from system to the user. */
        protected void onPostExecute(String file_url) {
            if (file_url != null){
                if (success == 1) {
                    Toast.makeText(ConfirmationActivity.this, file_url, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ConfirmationActivity.this, DrinkOrderedActivity.class);
                    finish();
                    startActivity(intent);
                }
            }
        }
    }


    /**
     * This class gets called when the users requests to reset their fingerprint template in the
     * database. It calls a PHP script on the Smartbar server which resets the template.
     */
    class ResetFP extends AsyncTask<String, String, String> {
        int success;

        @Override
        protected String doInBackground(String... args) {
        	
            try {
                Log.d("RFP", "Mid-Execute");
                String pinNum = MyApplication.myPin;
                
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("pin", pinNum));

                /* Getting product details by making HTTP request */
                JSONObject json = jsonParser.makeHttpRequest(
                		ServerAccess.RESET_FP_URL, "POST", params);
                
                if (json == null){
                	Toast.makeText(ConfirmationActivity.this, "Failure to Access Server. Check Internet Connection"
                            , Toast.LENGTH_SHORT).show();
                	return null;
                }

                /* Check the logcat for full JSON response */
                Log.d("RFP", json.toString());
                
                /* JSON success tag */
                success = json.getInt(ServerAccess.TAG_SUCCESS);

                if (success == 1) {
                    Log.d("RFP", "Success String:" + json.toString());
                    return json.getString(ServerAccess.TAG_MESSAGE);
                } else {
                    Log.d("RFP", "Failure with :" + json.getString(ServerAccess.TAG_MESSAGE));
                    return json.getString(ServerAccess.TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
        
        /* Let user know if failure or success. */
        protected void onPostExecute(String file_url) {
            if (file_url != null) {
                Log.d("RFP", "Returned URL:" + file_url);
                if (success == 1) {
                	Toast.makeText(ConfirmationActivity.this, file_url, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ConfirmationActivity.this,
                        "Failure to Access Server. Check Internet Connection"
                        , Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * This class gets called before sending user information to the server to ensure data is updated.
     * Queries the database with the user phone number and receives the user's username.
     */
    class FindUser extends AsyncTask<String, String, String> {

        int success;

        @Override
        protected String doInBackground(String... args) {

            Log.d("FindUser", "starting...");
            String phone = MyApplication.myPin;
            try {
                /* Build parameters */
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("phone", phone));

                Log.d("request!", "starting");

                /* Getting product details by making HTTP request */
                JSONObject json = jsonParser.makeHttpRequest(
                        ServerAccess.FIND_USER_URL, "POST", params);

                if (json == null) {
                    Toast.makeText(ConfirmationActivity.this,
                            "Cannot connect to server. Please check internet connection.",
                            Toast.LENGTH_SHORT).show();
                    return null;
                }

                /* Check the logcat for full JSON response */
                Log.d("FindUser", "returned");

                /* JSON success tag */
                success = json.getInt(ServerAccess.TAG_SUCCESS);

                if (success == 1) {
                    Log.d("FindUser", json.toString());
                    ((MyApplication)ConfirmationActivity.this.getApplication()).setLoggedIn(true);
                    return json.getString(ServerAccess.TAG_MESSAGE);
                }else{
                    Log.d("FindUser", json.getString(ServerAccess.TAG_MESSAGE));
                    return json.getString(ServerAccess.TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        /* Save the username. */
        protected void onPostExecute(String file_url) {
            if (file_url != null){
                if (success == 1) {
                    username = file_url;
                }
            }
        }
    }
}
