package com.example.lamperry.smartbar_r1;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;


/*
 * This class represents the behavior of the first screen a user sees when opening the app.
 * Displays logo, buttons to direct to either login or new user
 */
public class StartupActivity extends ActionBarActivity {

    // generated activity method
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        // initializes app wide global boolean to false
        ((MyApplication)this.getApplication()).setLoggedIn(false);
    }

    // method to direct user to login screen
    public void startupToLogin(View view) {
        Intent intent = new Intent(this, LibraryBrowseActivity.class);
        startActivity(intent);
    }

    // method to direct user to create account screen
    public void startupToNewUser(View view) {
        Intent intent = new Intent(this, NewUserActivity.class);
        startActivity(intent);
    }
}
