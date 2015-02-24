package com.example.lamperry.smartbar_r1;

import android.app.Activity;
import android.app.Application;
import android.view.inputmethod.InputMethodManager;

/*
 * This class holds all the global variables for the entire application. Namely the
 * username/pin for logged in user
 */
public class MyApplication extends Application {
    boolean loggedIn = false;
    String myUsername = "lpeezy";
    String myPin = "16505559898";

    public void setLoggedIn(boolean setLogIn) {
        loggedIn = setLogIn;
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }
}
