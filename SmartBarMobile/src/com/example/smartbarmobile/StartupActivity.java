package com.example.smartbarmobile;

/*
 * Modified from com.ecs.android.sample.oauth2
 * 
 * @author davydewaele
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/*
 * This class defines the behavior for the startup screen of the mobile app.
 * Directs user to create account, login via SmartBar, login via Google authentication.
 */
public class StartupActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        // initializes app wide global boolean to false
        ((MyApplication)this.getApplication()).setLoggedIn(false);
	}

	// for back navigation
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
    
    // when 'sign in with gmail' clicked
    public void oauthLogin(View view) {
    	startOauthFlow(Oauth2Params.GOOGLE_PLUS);
    }

    // starts google oauth authentication
	private void startOauthFlow(Oauth2Params oauth2Params) {
		Constants.OAUTH2PARAMS = oauth2Params;
		Intent intent = new Intent(this, OAuthAccessTokenActivity.class);
		startActivity(intent);
	}
}
