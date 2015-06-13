package com.example.trider.smartbarui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


/*
 * This class defines the behavior of the welcome screen which essentially just directs the user to
 * logout of a signed in account
 */
public class WelcomeActivity extends Activity {

    String pin;




    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_HOME)) {

            return true;
        }else if(keyCode == KeyEvent.KEYCODE_BACK){
            finish();
            startActivity(new Intent(this,IdleMenu.class));
            return true;
        }else if(keyCode == KeyEvent.KEYCODE_MENU){

            return true;
        }else if(keyCode == KeyEvent.KEYCODE_WINDOW){;
            return true;
        }
        return super.onKeyDown(keyCode, event);
        //return true;
    }



    // generated activity code
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
    //TODO Captains Log Day 2: After continuing with our mission, we stumbled upon another crash.
        //pin = ((MyApplication)this.getApplication()).myPin;

        TextView tView = (TextView) findViewById(R.id.welcome_greeting);
        tView.append(" "+MyApplication.myUsername);
    }

    // generated activity code
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_welcome, menu);
        return true;
    }

    // sets up action bar for settings and logout
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        return super.onOptionsItemSelected(item);
    }

    // logout method, returns user to Startup Activity
//    private void logout() {
//        ((MyApplication)this.getApplication()).setLoggedIn(false);
//        Intent intent = new Intent(this, StartupActivity.class);
//        startActivity(intent);
//    }

    // directs user to Library Browse Activity
    public void welcomeToLibraryBrowse(View view) {
        Intent intent = new Intent(this, LibraryBrowseActivity.class);
        startActivity(intent);
    }
}

