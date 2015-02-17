package com.example.lamperry.smartbar_r1;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


/*
 * This class defines the behavior of the drink ordered screen: displays pin number for user, allows
 * access to library drink screen.
 */
public class DrinkOrderedActivity extends ActionBarActivity {

    // Initializations
    TextView pinDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drink_ordered);

        pinDisplay = (TextView) findViewById(R.id.displayPin);
        pinDisplay.setText(String.format("%04d", ((MyApplication)this.getApplication()).myPin));
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        // adds logout button to action bar
        if (id == R.id.action_logout) {
            logout();
        }
        
        // adds get pin button to action bar
        if (id == R.id.action_pin) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // logs user out of account and returns to Startup Activity
    private void logout() {
        ((MyApplication)this.getApplication()).setLoggedIn(false);
        ((MyApplication)this.getApplication()).myUsername = "";
        ((MyApplication)this.getApplication()).myPin = 0;
        Intent intent = new Intent(this, StartupActivity.class);
        startActivity(intent);
    }

    // starts Library Browse activity
    public void drinkOrderedToLibraryBrowse(View view) {
        Intent intent = new Intent(this, LibraryBrowseActivity.class);
        startActivity(intent);
    }
}
