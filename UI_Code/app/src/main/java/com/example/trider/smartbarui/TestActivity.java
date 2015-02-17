package com.example.trider.smartbarui;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.trider.smartbarui.CommStream;

import java.io.IOException;
import java.io.OutputStream;


public class TestActivity extends ActionBarActivity {
    Context context;
    CommStream PiComm;
    OutputStream mOutputStream;
    String OutMessage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        TextView textView = (TextView) findViewById(R.id.testView);
        PiComm = new CommStream();
        if (PiComm.isInitialized()) {
            //textView.setText(PiComm.readString());
            context = getApplicationContext();
            Toast toast = Toast.makeText(context,"PiComm is Initialized",Toast.LENGTH_LONG);
            toast.show();


            OutMessage = "testing\n";
            byte[] buffer = new byte[256];
            buffer = OutMessage.getBytes();
            mOutputStream = PiComm.getOStream();
            try {
                mOutputStream.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
                context = getApplicationContext();
                toast = Toast.makeText(context,"Test Failed",Toast.LENGTH_LONG);
                toast.show();
                //textView.append("Test Failed");
            }
        }else{
            context = getApplicationContext();
            Toast toast = Toast.makeText(context,"No PiComm is Initialized",Toast.LENGTH_LONG);
            toast.show();
            textView.setText(PiComm.Status());
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_test, menu);
        return true;
    }

    public void StartUIClicked(View view){
        Intent intent = new Intent(this, IdleMenu.class);
        startActivity(intent);


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
