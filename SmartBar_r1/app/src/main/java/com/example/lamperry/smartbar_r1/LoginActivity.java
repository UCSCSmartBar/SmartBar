package com.example.lamperry.smartbar_r1;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;


public class LoginActivity extends ActionBarActivity {

    EditText usernameText, passwordText;
    String usernameString, passwordString;
    ArrayList<String> usernameDB = new ArrayList<>();
    ArrayList<String> passwordDB = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameDB = ((MyApplication)this.getApplication()).getUsernameDB();
        passwordDB = ((MyApplication)this.getApplication()).getPasswordDB();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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

        return super.onOptionsItemSelected(item);
    }

    public void checkLogin(View view) {
        int index;
        usernameText = (EditText)findViewById(R.id.type_email);
        passwordText = (EditText)findViewById(R.id.type_password);

        usernameString = usernameText.getText().toString();
        passwordString = passwordText.getText().toString();

        // make sure user entered something, display prompt if not
        if ((usernameString.equals("")) || (passwordString.equals(""))) {
            Toast.makeText(this, "You must enter a valid username and password.", Toast.LENGTH_LONG).show();
            return;
        }

        // check if login is in database
        for (index = 0; index < usernameDB.size(); index++) {
            // if login found
            if (usernameString.equals(usernameDB.get(index))) {
                // check password; if passes start welcome activity
                if (passwordString.equals(passwordDB.get(index))) {
                    loginToWelcome(view);
                    return;
                }
            }
        }

        // login not found
        Toast.makeText(this, "Username/password not found. Please try again", Toast.LENGTH_SHORT).show();
    }

    public void loginToWelcome(View view) {
        ((MyApplication)this.getApplication()).setLoggedIn(true);
        Intent intent = new Intent(this, WelcomeActivity.class);
        startActivity(intent);
    }
}
