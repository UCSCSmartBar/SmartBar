package com.example.smartbarmobile;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import com.google.api.client.auth.oauth2.Credential;

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
//    	Intent intent = new Intent(this, IntroScreen.class);
//    	startActivity(intent);
    	startOauthFlow(Oauth2Params.GOOGLE_PLUS);
    }

	private void startOauthFlow(Oauth2Params oauth2Params) {
		Constants.OAUTH2PARAMS = oauth2Params;
		startActivity(new Intent().setClass(this, OAuthAccessTokenActivity.class));
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		startTimer();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		stopTimer();
	}

	private void stopTimer() {
		timer.cancel();
	}
	
	protected  void startTimer() {
		Log.i(Constants.TAG," +++++ Started timer");
		timer = new Timer();
	    timer.scheduleAtFixedRate(new TimerTask() {
	        public void run() {
	        	Log.i(Constants.TAG," +++++ Refreshing data");
	        	try {
		            Message msg = new Message();
		            Bundle bundle = new Bundle();
		            bundle.putString("plus", getTokenStatusText(Oauth2Params.GOOGLE_PLUS));
		            msg.setData(bundle);
		            
	        	} catch (Exception ex) {
	        		ex.printStackTrace();
	        		timer.cancel();
		            Message msg = new Message();
		            Bundle bundle = new Bundle();
		            bundle.putString("plus", ex.getMessage());
		            msg.setData(bundle);
	        	}

	        }
	    }, 0, 1000);
	}

	private String getTokenStatusText(Oauth2Params oauth2Params) throws IOException {
		Credential credential = new OAuth2Helper(this.prefs,oauth2Params).loadCredential();
		String output = null;
		if (credential==null || credential.getAccessToken()==null) {
			output = "No access token found.";
		} else if (credential.getExpirationTimeMilliseconds()!=null){
			output = credential.getAccessToken() + "[ " + credential.getExpiresInSeconds() + " seconds remaining]";
		} else {
			output = credential.getAccessToken() + "[does not expire]";
		}
		return output;
	}
}
