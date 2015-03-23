package com.example.trider.smartbarui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.trider.smartbarui.util.SystemUiHider;


public class SystemStatus extends Activity {

    private static final String Pi ="PiStatus";
    private static final String Fp ="FingerPrintStatus";
    private static final String Bc ="BACStatus";
    private static final String Vs ="ValveState";
    private static final String Ll ="LiquidLevels";

    public Inventory INV = new Inventory();

    private Spinner cList;
    private TextView mText;
    CommStream PiComm;
    SystemCodeParser SCP;
    String InMessage;


    //Append Text onto textView after Decoding and Parsing Message;
    //TODO: Use the parsed up input to display on text view.
    Runnable mUpdateUI = new Runnable() {
        @Override
        public void run() {
            SCP.DecodeAccessoryMessage(InMessage);
            mText.append("->"+InMessage+ "\n");
        }
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_status);

        cList = (Spinner) findViewById(R.id.command_spinner);
        //mText = (TextView) findViewById(R.id.status_log);
        PiComm = new CommStream();
        //SCP = new SystemCodeParser();
        if(PiComm.isInitialized()) {
            new Thread(mListenerTask).start();
        }


    }

    public void SendCommand(View view){
        //if(!PiComm.isInitialized()){return;}

        String s = String.valueOf(cList.getSelectedItem());
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();

        switch(s){
            case Pi:
                PiComm.writeString(CommandStrings.RequestSys_State);
                break;
            case Fp:
                PiComm.writeString(CommandStrings.RequestFP_State);
                break;
            case Bc:
                PiComm.writeString(CommandStrings.RequestBAC_State);
                break;
            case Vs:
                PiComm.writeString(CommandStrings.RequestValve_States);
                break;
            case Ll:
                PiComm.writeString(CommandStrings.RequestLiquid_Levels);
                mText.setText(INV.PrintInventory());

                break;
        }

    }

    public void CheckLiquidLevels(View v){
        startActivity(new Intent(this,Container_Screen.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_system_status2, menu);
        return true;
    }



    /**
     * @title: mListenerTask
     * @description: The background thread that receives serial communication from the raspberry pi,
     *
     */
    Runnable mListenerTask = new Runnable() {
        @Override
        public void run() {
            try {
                InMessage = PiComm.readString();
                if (InMessage != null) {
                    //mText.post(mUpdateUI);
                }
                //Waits for new input communication
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //Restarts this thread.
                new Thread(this).start();
            }catch (NullPointerException npe) {
                npe.printStackTrace();
            }
        }
    };










}
