package com.example.smartbarmobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.PersonBuffer;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class ReadyToOrderActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ResultCallback<People.LoadPeopleResult> {

    JSONParser jsonParser = new JSONParser();
    String pin, pinDisp, tempCountry, tempArea, tempNum3, tempNum4;
    String username;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ready_to_order);

        pin = MyApplication.myPin;

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

        new FindUser().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /** Inflate the menu; this adds items to the action bar if it is present. **/
        getMenuInflater().inflate(R.menu.menu_ready_to_order, menu);
        return true;
    }

    /**
     * Handle action bar item clicks here. The action bar will automatically handle clicks on
     * the Home/Up button, so long as you specify a parent activity in AndroidManifest
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        /** ActionBar display pin sequence **/
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

        /** ActionBar reset fingerprint sequence **/
        if (id == R.id.action_resetFP) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle("Reset Fingerprint?")
                    .setMessage("Are you sure you want to reset your fingerprint information?")
                    .setPositiveButton("Reset FP", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            /** User confirmed **/
                            new ResetFP().execute();
                        }
                    })
                    .setNegativeButton("Cancel", null);
            builder.show();
        }

        /** ActionBar logout sequence **/
        if (id == R.id.action_logout) {
            Toast.makeText(this, "Signing out...", Toast.LENGTH_SHORT).show();
            if (((MyApplication)this.getApplication()).gSignIn) {
                /**
                 * Clear the default account on sign out so that Google Play Services will not return
                 * an onConnected callback without user interaction.
                 */
                Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                mGoogleApiClient.disconnect();
                mGoogleApiClient.connect();

                Intent intent = new Intent(ReadyToOrderActivity.this, StartupActivity.class);
                startActivity(intent);
            } else {
                logout();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
    }

    /** Logout (not G+) **/
    public void logout() {
        ((MyApplication)this.getApplication()).loggedIn = false;
        Intent intent = new Intent(this, StartupActivity.class);
        startActivity(intent);
    }

    /** Go to library browse screen **/
    public void goToLibraryBrowse(View view) {
        Intent libraryBrowse = new Intent(this, LibraryBrowseActivity.class);
        startActivity(libraryBrowse);
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
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVED_PROGRESS, mSignInProgress);
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

        // Retrieve some profile information to personalize our app for the user
        Person currentUser = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
        Plus.AccountApi.getAccountName(mGoogleApiClient);

        Log.v(TAG, "Signed in as " + currentUser.getDisplayName());
        Plus.PeopleApi.loadVisible(mGoogleApiClient, null).setResultCallback(this);

        // Indicate that the sign in process is complete.
        mSignInProgress = STATE_DEFAULT;
    }

    public void onConnectionSuspended(int cause) {
        /** Connection to Google Play Services was lost. Call connect() to attempt to re-establish the
         * connection or get a ConnectionResult that we can attempt to resolve. **/
        mGoogleApiClient.connect();
    }

    /**
     * onConnectionFailed is called when our Activity could not connect to Google Play Services.
     * onConnectionFailed indicates that the user needs to select an account, grant permissions or
     * resolve an error in order for sign in.
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        /** Refer to the JavaDoc for ConnectionResult to see what error codes might be returned in
         * onConnectionFailed **/
        Log.i(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = " + result.getErrorCode());

        if (result.getErrorCode() == ConnectionResult.API_UNAVAILABLE) {
            /**
             * The device's current configuration might not be supported with the requested API or
             * a required component may not be installed.
             */
            Toast.makeText(this, "The device's current configuration might not be supported.",
                    Toast.LENGTH_SHORT).show();
        } else if (mSignInProgress != STATE_IN_PROGRESS) {
            /** We do not have an intent in progress so we should store the latest error resolution
             * intent for use when the sign in button is clicked. **/
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
            /** We have an intent which will allow our user to sign in or resolve an error. For
             * example, if the user needs to select an account to sign in with, or if they need
             * consent to the permissions your app is requesting. **/
            try {
                /** Send the pending intent that we stored on the most recent OnConnectionFailed callback.
                 * This will allow the user to resolve the error currently preventing our connection
                 * to Google Play Services **/
                mSignInProgress = STATE_IN_PROGRESS;
                startIntentSenderForResult(mSignInIntent.getIntentSender(), RC_SIGN_IN, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                Log.i(TAG, "Sign in intent could not be sent: " + e.getLocalizedMessage());
                /** The intent was cancelled before it was sent. Return to the default state and attempt
                 * to connect to get an updated Connection Result. **/
                mSignInProgress = STATE_SIGN_IN;
                mGoogleApiClient.connect();
            }
        } else {
            /** Google Play Services wasn't able to provide an intent for some error types, so we
             * show the default Google Play Services error dialog which may still start an intent
             * on our behalf if the user can resolve the issue. **/
            Log.v(TAG, "Unable to provide intent");
        }
    }

    @Override
    public void onResult(People.LoadPeopleResult peopleData) {
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
     * Class to reset fingerprint in database.
     * @author lamperry
     */
    class ResetFP extends AsyncTask<String, String, String> {
        int success;

        @Override
        protected String doInBackground(String... args) {

            try {
                Log.d("RFP", "Mid-Execute");
                String pinNum = ((MyApplication)ReadyToOrderActivity.this.getApplication()).getNumber();

                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("pin", pinNum));

                /* Getting product details by making an HTTP request */
                JSONObject json = jsonParser.makeHttpRequest(
                        ServerAccess.RESET_FP_URL, "POST", params);

                if (json == null){
                    Toast.makeText(ReadyToOrderActivity.this,
                            "Failure to Access Server. Check Internet Connection", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(ReadyToOrderActivity.this, file_url, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ReadyToOrderActivity.this,
                        "Failure to Access Server. Check Internet Connection", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(ReadyToOrderActivity.this, "Cannot connect to server. Please check internet connection.",
                            Toast.LENGTH_SHORT).show();
                    return null;
                }

                /* JSON response returned */
                Log.d("FindUser", "returned");

                /* JSON success tag */
                success = json.getInt(ServerAccess.TAG_SUCCESS);

                if (success == 1) {
                    Log.d("FindUser", json.toString());
                    ((MyApplication)ReadyToOrderActivity.this.getApplication()).setLoggedIn(true);
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
