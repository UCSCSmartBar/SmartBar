package com.example.trider.smartbarui;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;


public class CheckBAC extends Activity {

    public enum BACState{
        IDLE,
        CHECKING,
        PASSED,
        RETRY,
        FAILED,
        WARNING,
    }

//    public void onResume(){
//        super.onResume();
//        hideSystemUI();
//    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_bac);
        hideSystemUI();
        //startWatch(5000);
        new Timer().scheduleAtFixedRate(HideTask,100,10);
    }


    public void SkipBAC(View view){
        startActivity(new Intent(this,ConfirmDrink.class));
    }







    /***********System Level Functions*******/
    public void startWatch(int watch_t) {
        new Timer().schedule(new TimerTask() {
            public void run() {
                startActivity(new Intent(CheckBAC.this, IdleMenu.class));
            }

        }, watch_t);
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
            CheckBAC.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hideSystemUI();
                }
            });
        }
    };
/*Default Functions*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_check_bac, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
