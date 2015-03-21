package com.example.smartbarmobile;

/**
 * Modified from com.ecs.android.sample.oauth2
 * 
 * @author davydewaele
 */
import java.net.URLDecoder;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Execute the OAuthRequestTokenTask to retrieve the request, and authorize the request.
 * After the request is authorized by the user, the callback URL will be intercepted here.
 */
@SuppressLint("SetJavaScriptEnabled")
public class OAuthAccessTokenActivity extends Activity {
	
	// Initializations
	private SharedPreferences prefs;
	private OAuth2Helper oAuth2Helper;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        Log.i(Constants.TAG, "Starting task to retrieve request token.");
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
        oAuth2Helper = new OAuth2Helper(this.prefs);
        webview = new WebView(this);
        webview.getSettings().setJavaScriptEnabled(true);  
        webview.setVisibility(View.VISIBLE);
        setContentView(webview);
        
        String authorizationUrl = oAuth2Helper.getAuthorizationUrl();
        Log.i(Constants.TAG, "Using authorizationUrl = " + authorizationUrl);
        
        handled=false;
        
        webview.setWebViewClient(new WebViewClient() {  

        	@Override  
            public void onPageStarted(WebView view, String url,Bitmap bitmap)  {  
        		Log.d(Constants.TAG, "onPageStarted : " + url + " handled = " + handled);
            }
        	
        	// checks params and processes token if authorization url correct
        	@Override  
            public void onPageFinished(final WebView view, final String url)  {
        		Log.d(Constants.TAG, "onPageFinished : " + url + " handled = " + handled);
        		
        		if (url.startsWith(Constants.OAUTH2PARAMS.getRederictUri())) {
	        		webview.setVisibility(View.INVISIBLE);
	        		
	        		if (!handled) {
	        			new ProcessToken(url,oAuth2Helper).execute();
	        		}
        		} else {
        			webview.setVisibility(View.VISIBLE);
        		}
            }

        });  
        
        webview.loadUrl(authorizationUrl);		
	}
	
	private WebView  webview;
	
	boolean handled=false;
	private boolean hasLoggedIn;
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.i(Constants.TAG, "onResume called with " + hasLoggedIn);
		if (hasLoggedIn) {
			finish();
		}
	}

	
	/**
	 * Background task to extract authorization code from url and retrieve access token associated with the code.
	 *
	 */
	private class ProcessToken extends AsyncTask<Uri, OAuth2Helper, Void> {

		String url;
		boolean startActivity=false;
		String authorizationCode;
		

		public ProcessToken(String url, OAuth2Helper oAuth2Helper) {
			this.url=url;
		}
		
		@Override
		protected Void doInBackground(Uri...params) {

			
			if (url.startsWith(Constants.OAUTH2PARAMS.getRederictUri())) {
				Log.i(Constants.TAG, "Redirect URL found" + url);
				handled=true;
        		try {
        			if (url.indexOf("code=")!=-1) {
            			authorizationCode = extractCodeFromUrl(url);
            			
        				// for db entry
        				String username = authorizationCode.substring(0, 19);
        				((MyApplication)OAuthAccessTokenActivity.this.getApplication()).myUsername = username;
        				((MyApplication)OAuthAccessTokenActivity.this.getApplication()).myPassword = username;
            			
            			Log.i(Constants.TAG, "Found code = " + authorizationCode);
						
            			oAuth2Helper.retrieveAndStoreAccessToken(authorizationCode);
            			startActivity=true;
			  		    hasLoggedIn=true;

        			} else if (url.indexOf("error=")!=-1) {
        				startActivity=true;
        			}
        			
				} catch (Exception e) {
					e.printStackTrace();
				}

        	} else {
        		Log.i(Constants.TAG, "Not doing anything for url " + url);
        	}
            return null;
		}

		private String extractCodeFromUrl(String url) throws Exception {
			String encodedCode = url.substring(Constants.OAUTH2PARAMS.getRederictUri().length()+7,url.length());
			return URLDecoder.decode(encodedCode,"UTF-8");
		}
		
		@Override
		protected void onPreExecute() {
			
		}

		/**
		 * When we're done and we've retrieved either a valid token or an error from the server,
		 * we'll return to our original app flow
		 */
		@Override
		protected void onPostExecute(Void result) {
			if (startActivity) {
				Log.i(Constants.TAG," ++++++++++++ Starting Welcome screen.");
				startActivity(new Intent(OAuthAccessTokenActivity.this, WelcomeActivity.class));
				finish();
			}
		}
	}
}
