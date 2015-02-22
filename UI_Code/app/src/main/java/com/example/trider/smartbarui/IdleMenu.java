package com.example.trider.smartbarui;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextClock;

import java.util.Timer;
import java.util.TimerTask;



public class IdleMenu extends Activity {


    private TextClock textClock;
    static boolean toggle = true;

    Timer timer;
    static long count = 0;

    CommStream PiComm;


    class BackGTask extends TimerTask {
        @Override
        public void run(){
            IdleMenu.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if((count++)%10 == 0) {
                        if(toggle){
                            textClock.setFormat12Hour("hh:mm");
                        }else{
                            textClock.setFormat12Hour("hh mm");
                        }
                        toggle = !toggle;
                    }
                }
            });
        }
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idle_menu);


       textClock = (TextClock) findViewById(R.id.textClock);
       textClock.setFormat12Hour("hh:mm");
       new Timer().scheduleAtFixedRate(new BackGTask(),1000,100);


        ImageView usbConn = (ImageView) findViewById(R.id.usbCon);
        PiComm = new CommStream();
        if(!PiComm.isInitialized()){
            usbConn.setVisibility(View.INVISIBLE);
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_idle_menu, menu);
        return true;
    }

    public void onPickUpClick(View view){

        Intent intent = new Intent(this,PickUpDrink.class);
        startActivity(intent);
    }

    public void GoToNewUser(View view){

        startActivity(new Intent(this,NewUser.class));
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
