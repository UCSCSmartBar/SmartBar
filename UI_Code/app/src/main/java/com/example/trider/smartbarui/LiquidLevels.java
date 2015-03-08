package com.example.trider.smartbarui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


public class LiquidLevels extends Activity {


    Inventory INV = new Inventory();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liquid_levels);
    }


    /**
     * Displays a Dialog Box showing all the different Mixers in the inventory.
     * @param view
     */
    public void CheckMixers(View view){
        //The inflater is the actual box that pops up
        LayoutInflater inflater= LayoutInflater.from(this);

        //The view being uploaded is formatted in the fragment_change_liquor_inv.xml.xml file is in the text
        //box area
        View v = inflater.inflate(R.layout.fragment_change_liquor_inv, null);

        //Builds the actual pop up with custom style
        new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.SmartUIDialog))
                .setTitle("Current Mixer Inventory")
                .setMessage("Scroll for current Mixer Inventory")
                .setView(v)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                //.setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /**
     * Displays a Dialog Box showing all the different Mixers in the inventory.
     * @param view
     */
    public void CheckLiquors(View view){
        //The inflater is the actual box that pops up
        LayoutInflater inflater= LayoutInflater.from(this);

        //The view being uploaded is formatted in the fragment_change_liquor_inv_inv.xml file is in the text
        //box area
        View v = inflater.inflate(R.layout.fragment_change_liquor_inv, null);


        //Builds the actual pop up with custom style
        new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.SmartUIDialog))
                .setTitle("Current Liquor Inventory")
                .setMessage("Scroll for current Liquor Inventory")
                .setView(v)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                        //.setIcon(android.R.drawable.ic_dialog_alert)
                .show();
        TextView lView = (TextView) findViewById(R.id.textView5);
        try {
            lView.setText(INV.PrintInventory());
        }catch(NullPointerException npe){
            npe.printStackTrace();
        }

        }








    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_liquid_levels, menu);
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
}
