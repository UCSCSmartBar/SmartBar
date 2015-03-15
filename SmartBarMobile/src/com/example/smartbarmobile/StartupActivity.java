package com.example.smartbarmobile;

import java.util.Timer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

public class StartupActivity extends Activity {
	
	private Timer timer = new Timer();
	private SharedPreferences prefs;
	protected int elapsedTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        // initializes app wide global boolean to false
        ((MyApplication)this.getApplication()).setLoggedIn(false);
        
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
	}

    @Override
    public void onBackPressed() {
    }

    // method to direct user to login screen
    public void startupToLogin(View view) {
        //Intent intent = new Intent(this, LibraryBrowseActivity.class);
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    // method to direct user to create account screen
    public void startupToNewUser(View view) {
        Intent intent = new Intent(this, NewUserActivity.class);
        startActivity(intent);
    }
    
    public void oauthLogin(View view) {
    	Intent intent = new Intent(this, IntroScreen.class);
    	startActivity(intent);
    }
}
