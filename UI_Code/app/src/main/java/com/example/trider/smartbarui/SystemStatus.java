package com.example.trider.smartbarui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Handler;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import com.example.trider.smartbarui.util.SystemUiHider;


public class SystemStatus extends Activity {


    private SystemUiHider mSystemUiHider;

    //public View contentView;
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_status);



        //Hides all action bars and other uneccesary things to view
        try {
            final View contentView = findViewById(R.id.system_view);
            mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
            mSystemUiHider.setup();
            mSystemUiHider.hide();
            mSystemUiHider
                    .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                        // Cached values.
                        @Override
                        @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                        public void onVisibilityChange(boolean visible) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                                // If the ViewPropertyAnimator API is available
                                // (Honeycomb MR2 and later), use it to animate the
                                // in-layout UI controls at the bottom of the
                                // screen.
                                if (visible) {
                                    // Schedule a hide().
                                    delayedHide(10);
                                }
                            }
                        }
                    });

        }catch(NullPointerException e){
            }




    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_system_status2, menu);
        return true;
    }
    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
