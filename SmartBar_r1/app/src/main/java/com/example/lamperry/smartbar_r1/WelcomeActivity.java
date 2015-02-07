package com.example.lamperry.smartbar_r1;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class WelcomeActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_welcome, menu);
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

        if (id == R.id.action_logout) {
            logout();
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        ((MyApplication)this.getApplication()).setLoggedIn(false);
        Intent intent = new Intent(this, StartupActivity.class);
        startActivity(intent);
    }

    public void welcomeToLibraryBrowse(View view) {
        Intent intent = new Intent(this, LibraryBrowseActivity.class);
        startActivity(intent);
    }
}
