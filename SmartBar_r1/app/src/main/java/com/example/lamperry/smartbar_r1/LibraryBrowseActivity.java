package com.example.lamperry.smartbar_r1;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/*
 * This class defines the behavior of the order drink screen, namely allows a user to manually
 * enter a drink to order or choose from the library of drinks displayed and passes the drink order
 * to the next screen.
 */
public class LibraryBrowseActivity extends ActionBarActivity implements View.OnClickListener,
AdapterView.OnItemClickListener {

    // Initializations
    ListView drinkList;
    ArrayList<String> drinkLibrary = new ArrayList<>();
    ArrayAdapter drinkAdapter;
    String drinkOrder;
    EditText drinkOrderTyped;
    String pin;

    // generated activity method
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library_browse);
        populateLibrary();

        drinkList = (ListView)findViewById(R.id.drinkList);
        drinkOrderTyped = (EditText)findViewById(R.id.typeDrink);
        drinkAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, drinkLibrary);
        drinkList.setAdapter(drinkAdapter);

        drinkList.setOnItemClickListener(this);

        pin = ((MyApplication)this.getApplication()).myPin;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_library_browse, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_pin) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("My Pin");
            builder.setMessage(String.valueOf(pin));
            builder.setPositiveButton("OK", null);
            AlertDialog dialog = builder.show();
            return true;
        }

        if (id == R.id.action_logout) {
            logout();
        }

        return super.onOptionsItemSelected(item);
    }

    // directs user back to Startup Activity
    private void logout() {
        ((MyApplication)this.getApplication()).setLoggedIn(false);
        ((MyApplication)this.getApplication()).myUsername = "";
        ((MyApplication)this.getApplication()).myPin = "";
        Intent intent = new Intent(this, StartupActivity.class);
        startActivity(intent);
    }

    // populates the (prototype) library of drinks
    private void populateLibrary() {
        drinkLibrary.add("Adios Motherfucker");
        drinkLibrary.add("Bloody Mary");
        drinkLibrary.add("Cosmopolitan");
        drinkLibrary.add("Gin and Tonic");
        drinkLibrary.add("Incredible Hulk");
        drinkLibrary.add("Lemon Drop");
        drinkLibrary.add("Long Island Iced Tea");
        drinkLibrary.add("Mai Tai");
        drinkLibrary.add("Manhattan");
        drinkLibrary.add("Margarita");
        drinkLibrary.add("Martini");
        drinkLibrary.add("Mojito");
        drinkLibrary.add("Old Fashioned");
        drinkLibrary.add("Rum and Coke");
        drinkLibrary.add("Screaming Orgasm");
        drinkLibrary.add("Screwdriver");
        drinkLibrary.add("Sex on the Beach");
        drinkLibrary.add("Vodka Cranberry");
        drinkLibrary.add("Vodka Soda");
        drinkLibrary.add("Whiskey Ginger");
        drinkLibrary.add("Whiskey Sour");
        drinkLibrary.add("White Russian");
    }

    // directs user to Confirmation Screen
    public void libraryBrowseToConfirmation(View view) {
        if (drinkOrderTyped == null) {
            Toast.makeText(this, "You must choose a drink!", Toast.LENGTH_SHORT).show();
            return;
        }
        drinkOrder = drinkOrderTyped.getText().toString();

        Intent intent = new Intent(this, ConfirmationActivity.class);
        intent.putExtra("drinkOrder", drinkOrder);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        drinkOrderTyped = (EditText)findViewById(R.id.typeDrink);
        drinkOrder = drinkLibrary.get(position);
        drinkOrderTyped.setText(drinkOrder);
    }
}
