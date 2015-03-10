package com.example.trider.smartbarui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class Container_Screen extends Activity {

    Inventory INV = new Inventory();
    String ContainerString;
    Button curButt;
    Button preButt;

    int SelectedContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container__screen);


        EditText myEditText = (EditText) findViewById(R.id.typeView);
        try {
            InputMethodManager inputManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }catch(NullPointerException n){}
        myEditText.clearFocus();

    }




    public void onContainerClick(View view) {

        preButt = curButt;
        curButt = (Button) view;
        if(preButt!=null){
            preButt.setBackground(getResources().getDrawable(R.drawable.cont_butt));
            preButt.setTextColor(getResources().getColor(R.color.white));
        }
        curButt.setBackground(getResources().getDrawable(R.drawable.cont_butt_high));
        curButt.setTextColor(getResources().getColor(R.color.black));
        //button.setStyler
        //Opens up corresponding container with information
        switch (view.getId()) {
            case R.id.con1_button:
                ContainerString = INV.getContainer(1).PrintContainer();
                //button.setBackground();
                SelectedContainer = 1;
                break;
            case R.id.con2_button:
                ContainerString = INV.getContainer(2).PrintContainer();
                SelectedContainer = 2;
                break;
            case R.id.con3_button:
                ContainerString = INV.getContainer(3).PrintContainer();
                SelectedContainer = 3;
                break;
            case R.id.con4_button:
                ContainerString = INV.getContainer(4).PrintContainer();
                SelectedContainer = 4;
                break;
            case R.id.con5_button:
                ContainerString = INV.getContainer(5).PrintContainer();
                SelectedContainer = 5;
                break;
            case R.id.con6_button:
                ContainerString = INV.getContainer(6).PrintContainer();
                SelectedContainer = 6;
                break;
            case R.id.con7_button:
                ContainerString = INV.getContainer(7).PrintContainer();
                SelectedContainer = 7;
                break;
            case R.id.con8_button:
                ContainerString = INV.getContainer(8).PrintContainer();
                SelectedContainer = 8;
                break;
            case R.id.con9_button:
                ContainerString = INV.getContainer(9).PrintContainer();
                SelectedContainer = 9;
                break;
            case R.id.con10_button:
                ContainerString = INV.getContainer(10).PrintContainer();
                SelectedContainer = 10;
                break;
            case R.id.con11_button:
                ContainerString = INV.getContainer(11).PrintContainer();
                SelectedContainer = 11;
                break;
            case R.id.con12_button:
                ContainerString = INV.getContainer(12).PrintContainer();
                SelectedContainer = 12;
                break;
            case R.id.con13_button:
                ContainerString = INV.getContainer(13).PrintContainer();
                SelectedContainer = 13;
                break;
            case R.id.con14_button:
                ContainerString = INV.getContainer(14).PrintContainer();
                SelectedContainer = 14;
                break;
            case R.id.con15_button:
                ContainerString = INV.getContainer(15).PrintContainer();
                SelectedContainer = 15;
                break;
            case R.id.con16_button:
                ContainerString = INV.getContainer(16).PrintContainer();
                SelectedContainer = 16;
                break;
            case R.id.con17_button:
                ContainerString = INV.getContainer(17).PrintContainer();
                SelectedContainer = 17;
                break;
            case R.id.con18_button:
                ContainerString = INV.getContainer(18).PrintContainer();
                SelectedContainer = 18;
                break;
        }
        //Toast.makeText(getApplicationContext(), ContainerString, Toast.LENGTH_LONG).show();


        TextView tView = (TextView) findViewById(R.id.cont_tview);
        tView.setText(ContainerString);
    }
//
    public void ReplaceContainer(View view){

        r.run();
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

Runnable r = new Runnable(){
    @Override
    public void run() {
        EditText tText = (EditText) findViewById(R.id.typeView);
        EditText bText = (EditText) findViewById(R.id.brandView);
        EditText mText = (EditText) findViewById(R.id.maxView);
        String Type   = tText.getText().toString();
        String Brand  = bText.getText().toString();
        String maxVol = mText.getText().toString();
        float vol;
        try {
            vol = Float.parseFloat(maxVol);
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
            return;
        }
        //Adds/Updates new LCO to inventory
        Log.d("CS", "Prev Con:" + INV.getContainer(SelectedContainer).PrintContainer());
        INV.AddToInventory(SelectedContainer, Type, Brand, vol, vol);
        Log.d("CS", "New Con:" + INV.getContainer(SelectedContainer).PrintContainer());


        ContainerString = INV.getContainer(SelectedContainer).PrintContainer();

        TextView tView = (TextView) findViewById(R.id.cont_tview);
        tView.setText(ContainerString);
    }
};


    public void SendInventory(View view){
        TextView tView = (TextView) findViewById(R.id.cont_tview);
        tView.setText(INV.SerialInventory());

    }

    //Default System functions

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_container__screen, menu);
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
