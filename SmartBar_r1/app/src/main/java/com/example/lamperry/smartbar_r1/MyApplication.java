package com.example.lamperry.smartbar_r1;

import android.app.Application;

import java.util.ArrayList;
import java.util.Random;

/*
 * This class holds all the global variables for the entire application. Namely the
 * username/pin for logged in user
 */
public class MyApplication extends Application {
    Random random = new Random();
    boolean loggedIn = false;
    String myUsername;
    int myPin;

    public void setLoggedIn(boolean setLogIn) {
        loggedIn = setLogIn;
    }
    public int addPin() {
        myPin = random.nextInt(random.nextInt(100000) % 10000);
        return myPin;
    }
}
