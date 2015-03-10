package com.example.lamperry.smartbar_r1;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


/*
 * This class defines the behavior of the drink ordered screen: displays pin number for user, allows
 * access to library drink screen.
 */
public class DrinkOrderedActivity extends ActionBarActivity {

    // Initializations
    TextView pinDisplay;

    ProgressDialog pDialog;
    JSONParser jsonParser;

    private static final String REQUEST_TOKEN_URL = "https://github.com/login/oauth/authorize";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drink_ordered);

        pinDisplay = (TextView)findViewById(R.id.displayPin);
        // format phone number
        pinDisplay.setText(((MyApplication)this.getApplication()).myPin);

        // BACtrack
        float measuredBac = 0;
        boolean bacDone = false;
        Intent intent = getIntent();
        bacDone = intent.getBooleanExtra("bac?", false);
        measuredBac = intent.getFloatExtra("measuredBac", 0);
        if (bacDone) {
            pinDisplay.setText(String.valueOf(measuredBac));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_drink_ordered, menu);
    return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // adds get pin button to action bar
        if (id == R.id.action_pin) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("My Number");
            builder.setMessage(pinDisplay.getText().toString());
            builder.setPositiveButton("OK", null);
            AlertDialog dialog = builder.show();
            return true;
        }

        // adds logout button to action bar
        if (id == R.id.action_logout) {
            logout();
        }

        return super.onOptionsItemSelected(item);
    }

    // logs user out of account and returns to Startup Activity
    private void logout() {
        ((MyApplication)this.getApplication()).setLoggedIn(false);
        Intent intent = new Intent(this, StartupActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
    }

    // starts Library Browse activity
    public void drinkOrderedToLibraryBrowse(View view) {
        Intent intent = new Intent(this, LibraryBrowseActivity.class);
        startActivity(intent);
    }

    public void openBACtrack(View view) {
//        final String playStoreBACtrack = "com.bactrack.bactrack_mobile";
//        Intent intent;
//        PackageManager manager = getPackageManager();
//        try {
//            intent = manager.getLaunchIntentForPackage(playStoreBACtrack);
//            if (intent == null)
//                throw new PackageManager.NameNotFoundException();
//            intent.addCategory(Intent.CATEGORY_LAUNCHER);
//            startActivity(intent);
//        } catch (PackageManager.NameNotFoundException e) {
//            Log.e("BACtrack Error", "App not found.");
//            Toast.makeText(this, "You must download the BACtrack app!", Toast.LENGTH_SHORT).show();
//            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + playStoreBACtrack)));
//        } catch (ActivityNotFoundException e) {
//            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + playStoreBACtrack)));
//        }

        Intent intent = new Intent(this, BACtrackActivity.class);
        startActivity(intent);
    }
}
