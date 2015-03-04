package com.example.lamperry.smartbar_r1;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


/*
 * this class defines the behavior of the custom drink activity in which a user can select a
 * particular brand of liquor for a chosen drink
 */
public class CustomizeDrinkActivity extends ActionBarActivity {

    // Initializations
    int numLiquors;
    String drinkOrder;
    String[] liquorString;
    ArrayList<String> liquorList = new ArrayList<>();
    ArrayList<String> liquorReturnList = new ArrayList<>();
    String pin;
    String recipe = "";

    ArrayList<Spinner> spinnerArrayList = new ArrayList<>();
    Spinner liquor1, liquor2, liquor3, liquor4, liquor5;

    String[] gin = { "Choose Gin", "Default: Bombay Sapphire", "Beefeater" };
    String[] rum = { "Choose Rum", "Default: Bacardi", "Captain Morgan" };
    String[] tequila = { "Choose Tequila", "Default: Jose Cuervo", "Milagro", "Patron" };
    String[] vodka = { "Choose Vodka", "Default: Smirnoff", "Svedka", "Absolut" };
    String[] whiskey = { "Choose Whiskey", "Default: Jack Daniels", "Jameson", "Johnny Walker" };
    String[] bitters = { "Choose Bitters", "Default: Angostura", "Peychaud's Bitters", "The Bitter Truth" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customize_drink);

        pin = String.valueOf(((MyApplication)this.getApplication()).myPin);

        // grab variables from previous intent
        Intent intent = getIntent();
        drinkOrder = intent.getStringExtra("drinkOrder");
        recipe = intent.getStringExtra("drinkRecipe");
        liquorList = intent.getStringArrayListExtra("liquorList");

        // display which chosen drink user is customizing
        TextView drinkOrderView = (TextView)findViewById(R.id.customize_drink);
        drinkOrderView.setText(drinkOrder);
        numLiquors = liquorList.size();
        liquorString = new String[numLiquors];

        buildSpinners();
    }

    // inflates the action bar items (settings bar)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_customize_drink, menu);
        return true;
    }

    // handles behavior of  each item in action bar when clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // Get pin clicked
        if (id == R.id.action_pin) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("My Number");
            builder.setMessage(String.valueOf(pin));
            builder.setPositiveButton("OK", null);
            AlertDialog dialog = builder.show();
            return true;
        }

        // Logout clicked
        if (id == R.id.action_logout) {
            logout();
        }

        return super.onOptionsItemSelected(item);
    }

    // directs user back to Startup Activity/resets all globals
    private void logout() {
        ((MyApplication)this.getApplication()).setLoggedIn(false);
        Intent intent = new Intent(this, StartupActivity.class);
        startActivity(intent);
    }

    /*
     * Associates java spinners with (xml spinners) resource id in layout and builds each specific
     * one for each type of liquor
     */
    private void buildSpinners() {
        // assign spinner with resource id
        liquor1 = (Spinner)findViewById(R.id.liquor1);
        liquor2 = (Spinner)findViewById(R.id.liquor2);
        liquor3 = (Spinner)findViewById(R.id.liquor3);
        liquor4 = (Spinner)findViewById(R.id.liquor4);
        liquor5 = (Spinner)findViewById(R.id.liquor5);

        // create array of spinners to facilitate associations
        spinnerArrayList.add(liquor1);
        spinnerArrayList.add(liquor2);
        spinnerArrayList.add(liquor3);
        spinnerArrayList.add(liquor4);
        spinnerArrayList.add(liquor5);

        // build specific spinners for each type of liquor
        for (int i = 0; i < numLiquors; i++) {
            liquorString[i] = liquorList.get(i);
            assignSpinner(i + 1, liquorString[i]);
        }
    }

    // this method populates the spinner with the strings of the different options of each liquor
    private void assignSpinner(int i, String s) {
        switch (s) {
            case "Bitters":
                ArrayAdapter<String> bittersAdapter;
                bittersAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, bitters);
                bittersAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                spinnerArrayList.get(i-1).setAdapter(bittersAdapter);
                spinnerArrayList.get(i-1).setVisibility(View.VISIBLE);
                break;
            case "Gin":
                ArrayAdapter<String> ginAdapter;
                ginAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, gin);
                ginAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                spinnerArrayList.get(i-1).setAdapter(ginAdapter);
                spinnerArrayList.get(i-1).setVisibility(View.VISIBLE);
                break;
            case "Rum":
                ArrayAdapter<String> rumAdapter;
                rumAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, rum);
                rumAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                spinnerArrayList.get(i-1).setAdapter(rumAdapter);
                spinnerArrayList.get(i-1).setVisibility(View.VISIBLE);
                break;
            case "Tequila":
                ArrayAdapter<String> tequilaAdapter;
                tequilaAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, tequila);
                tequilaAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                spinnerArrayList.get(i-1).setAdapter(tequilaAdapter);
                spinnerArrayList.get(i-1).setVisibility(View.VISIBLE);
                break;
            case "Vodka":
                ArrayAdapter<String> vodkaAdapter;
                vodkaAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, vodka);
                vodkaAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                spinnerArrayList.get(i-1).setAdapter(vodkaAdapter);
                spinnerArrayList.get(i-1).setVisibility(View.VISIBLE);
                break;
            case "Vermouth":
                ArrayAdapter<String> vermouthAdapter;
                vermouthAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, vodka);
                vermouthAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                spinnerArrayList.get(i-1).setAdapter(vermouthAdapter);
                spinnerArrayList.get(i-1).setVisibility(View.VISIBLE);
                break;
            case "Whiskey":
                ArrayAdapter<String> whiskeyAdapter;
                whiskeyAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, whiskey);
                whiskeyAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                spinnerArrayList.get(i-1).setAdapter(whiskeyAdapter);
                spinnerArrayList.get(i-1).setVisibility(View.VISIBLE);
                break;
        }
    }

    /*
     * method gets called when user clicks the customize button on this screen
     * grabs the chosen items from each spinner and adds the appropriate value to the liquor return
     * list which gets parsed through in Confirmation Activity
     */
    public void customizeToConfirmation(View view) {
        int i = 0;
        boolean choose = false;
        while (i < numLiquors) {
            switch (i) {
                // if starts with 'choose' then automatically grab item at position 1 which holds
                // the default liquor brand in all liquor arrays
                case 0:
                    if (liquor1.getSelectedItem().toString().startsWith("Choose")) {
                        choose = true;
                        liquorReturnList.add(liquor1.getItemAtPosition(1).toString());
                    } else {
                        liquorReturnList.add(liquor1.getSelectedItem().toString());
                    }
                    break;
                case 1:
                    if (liquor2.getSelectedItem().toString().startsWith("Choose")) {
                        choose = true;
                        liquorReturnList.add(liquor2.getItemAtPosition(1).toString());
                    } else {
                        liquorReturnList.add(liquor2.getSelectedItem().toString());
                    }
                    break;
                case 2:
                    if (liquor3.getSelectedItem().toString().startsWith("Choose")) {
                        choose = true;
                        liquorReturnList.add(liquor3.getItemAtPosition(1).toString());
                    } else {
                        liquorReturnList.add(liquor3.getSelectedItem().toString());
                    }
                    break;
                case 3:
                    if (liquor4.getSelectedItem().toString().startsWith("Choose")) {
                        choose = true;
                        liquorReturnList.add(liquor4.getItemAtPosition(1).toString());
                    } else {
                        liquorReturnList.add(liquor4.getSelectedItem().toString());
                    }
                    break;
                case 4:
                    if (liquor5.getSelectedItem().toString().startsWith("Choose")) {
                        choose = true;
                        liquorReturnList.add(liquor5.getItemAtPosition(1).toString());
                    } else {
                        liquorReturnList.add(liquor5.getSelectedItem().toString());
                    }
                    break;
                default:
            }
            i++;
        }

        if (choose) {
            Toast.makeText(this, "You've failed to specify the liquor brand for one or more " +
                    "liquors in your drink. The defaults will be applied.", Toast.LENGTH_SHORT).show();
        }

        // send user back to confirmation activity with the appropriate drink order and liquor
        // return list
        Intent intent = new Intent(this, ConfirmationActivity.class);
        intent.putExtra("drinkOrder", drinkOrder);
        intent.putExtra("drinkRecipe", recipe);
        intent.putStringArrayListExtra("liquorReturnList", liquorReturnList);
        startActivity(intent);
    }
}
