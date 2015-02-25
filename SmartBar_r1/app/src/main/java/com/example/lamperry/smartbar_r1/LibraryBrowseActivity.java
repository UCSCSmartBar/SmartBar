package com.example.lamperry.smartbar_r1;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;


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
    ArrayList<String> filteredLibrary = new ArrayList<>();
    ArrayAdapter<String> drinkAdapter, filteredAdapter;
    String drinkOrder;
    EditText drinkOrderTyped;
    String pin;
    String lastChange = "";

    // generated activity method
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library_browse);
        setupUI(findViewById(R.id.library_browse_activity));

        populateLibrary(drinkLibrary);
        populateLibrary(filteredLibrary);

        // setup lists and adapters for displaying the library
        drinkList = (ListView)findViewById(R.id.drinkList);
        drinkOrderTyped = (EditText)findViewById(R.id.typeDrink);
        drinkOrderTyped.addTextChangedListener(filterTextWatcher);
        drinkAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, drinkLibrary);
        filteredAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, drinkLibrary);
        drinkList.setAdapter(drinkAdapter);

        drinkList.setOnItemClickListener(this);

        pin = ((MyApplication)this.getApplication()).myPin;
    }

    // to filter library as user enters input
    // all methods necessary to implement TextWatcher
    private TextWatcher filterTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        // still need logic to update library when user removes characters from constraint
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            Log.d("LAMPERRY", "onTextChanged filtering...");

            if (lastChange.length() < s.toString().length()) {
                // user has added characters to constraint
                for (int i = 0; i < filteredLibrary.size(); i++) {
                    if (!filteredLibrary.get(i).toUpperCase().startsWith(s.toString().toUpperCase())) {
                        filteredAdapter.remove(filteredLibrary.get(i));
                    }
                }
                filteredAdapter.notifyDataSetChanged();
                drinkList.setFilterText(s.toString());
                drinkList.setAdapter(filteredAdapter);
            }
            if (lastChange.length() > s.toString().length()) {
                drinkAdapter.clear();
                // user has removed characters from constraint
                for (int i = 0; i < filteredLibrary.size(); i++) {
                    if (filteredLibrary.get(i).toUpperCase().startsWith(s.toString().toUpperCase())) {
                        drinkAdapter.add(filteredLibrary.get(i));
                    }
                }
                drinkAdapter.notifyDataSetChanged();
                drinkList.setFilterText(s.toString());
                drinkList.setAdapter(drinkAdapter);
            }
            lastChange = s.toString();
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    // necessary method when invoking text change listener
    @Override
    protected void onDestroy() {
        super.onDestroy();
        drinkOrderTyped.removeTextChangedListener(filterTextWatcher);
    }

    // inflate options action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_library_browse, menu);
        return true;
    }

    // define options actions bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_pin) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("My Number");
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
        Intent intent = new Intent(this, StartupActivity.class);
        startActivity(intent);
    }

    // populates the (prototype) library of drinks
    private void populateLibrary(ArrayList<String> drinkLibraryList) {
        drinkLibraryList.clear();
        drinkLibraryList.add("Adios Motherfucker");
        drinkLibraryList.add("Bloody Mary");
        drinkLibraryList.add("Cosmopolitan");
        drinkLibraryList.add("Gin and Tonic");
        drinkLibraryList.add("Incredible Hulk");
        drinkLibraryList.add("Lemon Drop");
        drinkLibraryList.add("Long Island Iced Tea");
        drinkLibraryList.add("Mai Tai");
        drinkLibraryList.add("Manhattan");
        drinkLibraryList.add("Margarita");
        drinkLibraryList.add("Martini");
        drinkLibraryList.add("Mojito");
        drinkLibraryList.add("Old Fashioned");
        drinkLibraryList.add("Rum and Coke");
        drinkLibraryList.add("Screwdriver");
        drinkLibraryList.add("Sex on the Beach");
        drinkLibraryList.add("Vodka Cranberry");
        drinkLibraryList.add("Vodka Soda");
        drinkLibraryList.add("Whiskey Ginger");
        drinkLibraryList.add("Whiskey Sour");
        drinkLibraryList.add("White Russian");
    }

    // directs user to Confirmation Screen
    public void libraryBrowseToConfirmation(View view) {
        drinkOrderTyped = (EditText)findViewById(R.id.typeDrink);
        drinkOrder = drinkOrderTyped.getText().toString();
        if (drinkOrder.equals("")) {
            Toast.makeText(this, "You must choose a drink!", Toast.LENGTH_SHORT).show();
            return;
        }
        drinkOrderTyped.setText(drinkOrder);

        // make sure user enters in valid drink order, case insensitive and leading/trailing
        // whitespace ok
        boolean isInLibrary = false;
        for (int i = 0; i < drinkLibrary.size(); i++) {
            if (drinkOrder.toUpperCase().trim().equals(drinkLibrary.get(i).toUpperCase())) {
                isInLibrary = true;
                drinkOrder = drinkLibrary.get(i);
                break;
            }
        }
        if (!isInLibrary) {
            Toast.makeText(this, "Sorry, SmartBar does not have that drink in its inventory. Please try again.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        // send drink order to next activity
        Intent intent = new Intent(this, ConfirmationActivity.class);
        intent.putExtra("drinkOrder", drinkOrder);
        startActivity(intent);
    }

    // necessary method to implement AdapterView and View classes
    @Override
    public void onClick(View v) {
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // grab the drink selected from the library and display in edit text
        drinkOrderTyped = (EditText)findViewById(R.id.typeDrink);
        drinkOrder = drinkLibrary.get(position);
        drinkOrderTyped.setText(drinkOrder);
    }

    // http://stackoverflow.com/questions/4165414/how-to-hide-soft-keyboard-on-android-after-clicking-outside-edittext
    public void setupUI(View view) {
        // set up touch listener for non-text box views to hide keyboard
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    ((MyApplication)LibraryBrowseActivity.this.getApplication()).hideSoftKeyboard(LibraryBrowseActivity.this);
                    return false;
                }
            });
        }
        // if a layout container, iterate over children and seed recursion
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup)view).getChildCount(); i++) {
                View innerView = ((ViewGroup)view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }
}
