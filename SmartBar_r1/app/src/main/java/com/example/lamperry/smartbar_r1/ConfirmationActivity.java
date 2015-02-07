package com.example.lamperry.smartbar_r1;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


public class ConfirmationActivity extends ActionBarActivity {

    String drinkOrder;
    TextView displayDrink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        Intent intent = getIntent();
        drinkOrder = intent.getStringExtra("drinkOrder");
        displayDrink = (TextView)findViewById(R.id.drinkOrder);
        displayDrink.setText(drinkOrder);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_confirmation, menu);
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

    public void confirmationToDrinkOrdered(View view) {
        Intent intent = new Intent(this, DrinkOrderedActivity.class);
        startActivity(intent);
    }

    public void confirmationToLibraryBrowse(View view) {
        Intent intent = new Intent(this, LibraryBrowseActivity.class);
        startActivity(intent);
    }
}
