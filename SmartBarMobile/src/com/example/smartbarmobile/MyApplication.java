package com.example.smartbarmobile;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.view.inputmethod.InputMethodManager;

/*
 * This class holds the global app-wide variables (number, username, logged in)
 */
public class MyApplication extends Application {

    boolean loggedIn = false;
    String myUsername = "lp";
    String myPin = "12345678901";
    String myPassword = "lp";

    public void setLoggedIn(boolean setLogIn) {
        loggedIn = setLogIn;
        myPin = getMy10DigitPhoneNumber();
        while (myPin.length() < 11) {
            myPin = "1" + myPin;
        }
    }

    // method to hide keyboard when user clicks outside of edit text area
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    // grab phone number from users phone
    private String getMyPhoneNumber() {
        TelephonyManager mTelephonyMgr;
        mTelephonyMgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        return mTelephonyMgr.getLine1Number();
    }

    private String getMy10DigitPhoneNumber() {
        return getMyPhoneNumber();
    }
}
