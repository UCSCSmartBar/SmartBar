package com.example.trider.smartbarui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;


public class MainMenu extends Activity {
    Context context;
    CommStream PiComm;
    boolean isActive = true;
    String InMessage;

/*Background Communications*/
    Runnable nListenerTask = new Runnable() {
        @Override
        public void run() {
            InMessage = PiComm.ReadBuffer();
            if(InMessage != null){

                //Toast.makeText(getApplicationContext(),InMessage,Toast.LENGTH_SHORT).show();
            }
            //Waits for new input communication
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //Restarts this thread only if active
            if(isActive) {
                new Thread(this).start();
            }
        }
    };
//    public void onStop(){
//        super.onStop();
//        PiComm.writeString("STOP");
//        isActive = false;
//
//    }
//    public void onResume(){
//        super.onResume();
//        if(PiComm.isInitialized()){
//            PiComm.writeString("Resume");
//        }
//        hideSystemUI();
//        isActive = true;
//    }



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        hideSystemUI();

        TextView textView = (TextView) findViewById(R.id.testView);
        PiComm = new CommStream();

        if (PiComm.isInitialized()) {
            //textView.setText(PiComm.readString());
            context = getApplicationContext();
            Toast toast = Toast.makeText(context,"PiComm is Initialized",Toast.LENGTH_LONG);
            toast.show();
            PiComm.writeString("$SYS,MainStart");
            //new Thread(nListenerTask).start();
        }else{
            context = getApplicationContext();
            Toast toast = Toast.makeText(context,"No PiComm is Initialized",Toast.LENGTH_LONG);
            toast.show();
            textView.setText(PiComm.ReadStatus());
        }
        //Beings the hiding of the tasks.
        new Timer().scheduleAtFixedRate(HideTask,100,100);
    }



//Starts other menus
    public void StartUIClicked(View view){
        //isActive = false;
        startActivity(new Intent(this, IdleMenu.class));
    }
    public void SystemStatusClicked(View view){
        //isActive = false;
        startActivity(new Intent(this,SystemStatus.class));
    }




/**System level functions**/
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
            MainMenu.this.runOnUiThread(new Runnable() {
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
        getMenuInflater().inflate(R.menu.menu_test, menu);
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
