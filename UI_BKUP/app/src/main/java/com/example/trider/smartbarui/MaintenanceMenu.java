package com.example.trider.smartbarui;

import android.support.v7.app.ActionBarActivity;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.text.style.UpdateAppearance;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.security.KeyStore;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MaintenanceMenu extends ActionBarActivity {
    Inventory INV = new Inventory();
    String ContainerString;
    int SelectedContainer;
    Button curButt;
    Button preButt;
    String KeypadValue = "";
    CommStream PiComm = new CommStream();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance_menu);
        new Timer().schedule(HideTask, 1, 100);
        try {
            InputMethodManager inputManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }catch(NullPointerException n){
            n.printStackTrace();
        }
    }

    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        View mDecorView;
        mDecorView = getWindow().getDecorView();
        mDecorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }
    TimerTask HideTask = new TimerTask() {
        @Override
        public void run(){
            MaintenanceMenu.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hideSystemUI();
                }
            });
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_maintenance_menu, menu);
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


//    public void GetInventory(View view) {
//
//        CommStream PiComm = new CommStream();
//        PiComm.writeString("$DS,$GI");
//        String InventoryCode = CommStream.ReadBuffer();
//
//    }

    public void onContainerClick(View view) {


        preButt = curButt;
        curButt = (Button) view;
        if(preButt!=null){
            preButt.setBackground(getResources().getDrawable(R.drawable.cont_butt));
            preButt.setTextColor(getResources().getColor(R.color.white));
        }
        curButt.setBackground(getResources().getDrawable(R.drawable.cont_butt_high));
        curButt.setTextColor(getResources().getColor(R.color.black));

        //Opens up corresponding container with information
        SelectedContainer = findContainerByID(view);
    }


    public void CalibrateClick(View view) {
        CalibrateValve();

    }

    public void OpenValveClick(View view) {
        OpenValve(Double.parseDouble(KeypadValue));

    }

    public void EnterPin(View view){
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        switch(view.getId()){
            case R.id.keyOne:
                KeypadValue+="1";
                break;
            case R.id.keyTwo:
                KeypadValue+="2";
                break;
            case R.id.keyThree:
                KeypadValue+="3";
                break;
            case R.id.keyFour:
                KeypadValue+="4";
                break;
            case R.id.keyFive:
                KeypadValue+="5";
                break;
            case R.id.keySix:
                KeypadValue+="6";
                break;
            case R.id.keySeven:
                KeypadValue+="7";
                break;
            case R.id.keyEight:
                KeypadValue+="8";
                break;
            case R.id.keyNine:
                KeypadValue+="9";
                break;
            case R.id.keyZero:
                KeypadValue+="0";
                break;
            case R.id.keyDelete:
                if(KeypadValue.length() == 0){return;}
                KeypadValue = KeypadValue.substring(0,KeypadValue.length()-1);
                break;
            case R.id.keyClear:

                KeypadValue = "";
                break;
        }
        if(KeypadValue.length() > 2) {
            KeypadValue = KeypadValue.substring(0, KeypadValue.length() - 1);
        }
        TextView tView = (TextView) findViewById(R.id.KeypadValueText);
        tView.setText(KeypadValue+" Seconds");
    }
    public void PurgeClick(View view) {
        int PurgeDuration = 15;
        OpenValve(PurgeDuration);
    }
    public void CalibrateValve() {
        double CalibrationVolume = 1.5;
        PiComm.writeString("$DS,$CV,"+SelectedContainer+","+CalibrationVolume);
    }

    public void OpenValve(double open_duration) {
        PiComm.writeString("$DS,$OV,"+ContainerString+","+open_duration);
    }
    //Returns container number, and replaces the text view text
    public int findContainerByID(View view) {
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
            case R.id.con19_button:
                ContainerString = INV.getContainer(18).PrintContainer();
                SelectedContainer = 18;
                break;
            case R.id.con20_button:
                ContainerString = INV.getContainer(18).PrintContainer();
                SelectedContainer = 18;
                break;
        }
//        TextView tView = (TextView) findViewById(R.id.cont_tview);
//        tView.setText(ContainerString);
        return SelectedContainer;
    }
}