package com.example.smartbarmobile;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MissingParamsActivity extends Activity implements OnClickListener {
	
	boolean noAge, noGender;
	TextView missingWeight, missingAge, missingGender;
	String myWeight, myAge, myGender;
	EditText weight, age;
	Spinner gender;
	Button done;

    JSONParser jsonParser = new JSONParser();

    //PHP login script:
    //UCSC Smartbar Server:
    private static final String REGISTER_URL = "http://www.ucscsmartbar.com/register.php";

    //JSON element ids from response of php script:
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    String[] genders = { "Male", "Female", "Other"};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_missing_params);
		
		Button done = (Button)findViewById(R.id.done_button);
		done.setOnClickListener(this);

		missingWeight = (TextView)findViewById(R.id.missing_weight);
		missingAge = (TextView)findViewById(R.id.missing_age);
		missingGender = (TextView)findViewById(R.id.missing_gender);
		
		weight = (EditText)findViewById(R.id.enter_missing_weight);
		age = (EditText)findViewById(R.id.enter_missing_age);
		gender = (Spinner)findViewById(R.id.enter_missing_gender);
		
        ArrayAdapter<String> genderList = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, genders);
        genderList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gender.setAdapter(genderList);
		
		Intent intent = getIntent();
		noAge = intent.getBooleanExtra("noAge", true);
		noGender = intent.getBooleanExtra("noGender", true);
		
		configureParams();
	}
	
    @Override
    public void onBackPressed() {
		Intent intent = new Intent(this, StartupActivity.class);
		startActivity(intent);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.missing_params, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void configureParams() {
		if (noGender) {
			missingGender.setText("Your Gender");
			missingGender.setVisibility(View.VISIBLE);
			gender.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.done_button:
				/** Grab weight input */
				((MyApplication)this.getApplication()).myWeight = weight.getText().toString();
				myWeight = weight.getText().toString();
				
				/** Grab age input */
				myAge = age.getText().toString();
				if (Integer.valueOf(myAge) < 21) {
					Toast.makeText(this, "Sorry you must be 21 to use the SmartBar.", Toast.LENGTH_LONG).show();
					Intent intent = new Intent(this, StartupActivity.class);
					intent.putExtra("tooYoung",	true);
					intent.putExtra("prevIntent", true);
					finish();
					startActivity(intent);
					return;
				}
				((MyApplication)this.getApplication()).myAge = age.getText().toString();
				if (noGender)
					((MyApplication)this.getApplication()).myGender = gender.toString();
				new CreateUser().execute();
				break;
			default: break;
		}
	}


	/**
	 * Class to query database and add new user information.
	 * @author eloys
	 */
    class CreateUser extends AsyncTask<String, String, String> {

        boolean failure = false;
        int success;

        @Override
        protected String doInBackground(String... args) {
            // Check for success tag
            String username = ((MyApplication)MissingParamsActivity.this.getApplication()).myUsername;
            String password = ((MyApplication)MissingParamsActivity.this.getApplication()).myPassword;
            String email = ((MyApplication)MissingParamsActivity.this.getApplication()).myEmail;
            String phone = ((MyApplication)MissingParamsActivity.this.getApplication()).myPin;
            String age = ((MyApplication)MissingParamsActivity.this.getApplication()).myAge;
            String weight = ((MyApplication)MissingParamsActivity.this.getApplication()).myWeight;
            String sex = ((MyApplication)MissingParamsActivity.this.getApplication()).myGender;
            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("username", username));
                params.add(new BasicNameValuePair("password", password));
                params.add(new BasicNameValuePair("email", email));
                params.add(new BasicNameValuePair("phone", phone));
                params.add(new BasicNameValuePair("age", age));
                params.add(new BasicNameValuePair("weight", weight));
                params.add(new BasicNameValuePair("sex", sex));

                Log.d("request!", "starting");

                //Posting user data to script
                JSONObject json = jsonParser.makeHttpRequest(
                        REGISTER_URL, "POST", params);

                // full json response
                Log.d("Login attempt", json.toString());

                // json success element
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("User Created!", json.toString());
                    ((MyApplication)MissingParamsActivity.this.getApplication()).setLoggedIn(true);
                    return json.getString(TAG_MESSAGE);
                }else{
                    failure = true;
                    Log.d("Login Failure!", json.getString(TAG_MESSAGE));
                    return json.getString(TAG_MESSAGE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String file_url) {
            if (file_url != null){
                Toast.makeText(MissingParamsActivity.this, file_url, Toast.LENGTH_LONG).show();
                if (success == 1) {
		            Intent intent = new Intent(MissingParamsActivity.this, WelcomeActivity.class);
		            finish();
		            startActivity(intent);
                }
            }
        }
    }
}
